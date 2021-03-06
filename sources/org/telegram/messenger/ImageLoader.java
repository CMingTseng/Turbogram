package org.telegram.messenger;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.SparseArray;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.util.MimeTypes;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.messenger.FileLoader.FileLoaderDelegate;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$Document;
import org.telegram.tgnet.TLRPC$FileLocation;
import org.telegram.tgnet.TLRPC$InputEncryptedFile;
import org.telegram.tgnet.TLRPC$InputFile;
import org.telegram.tgnet.TLRPC$Message;
import org.telegram.tgnet.TLRPC$PhotoSize;
import org.telegram.tgnet.TLRPC$TL_documentEncrypted;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_fileEncryptedLocation;
import org.telegram.tgnet.TLRPC$TL_fileLocation;
import org.telegram.tgnet.TLRPC$TL_fileLocationUnavailable;
import org.telegram.tgnet.TLRPC$TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC$TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC$TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC$TL_photoCachedSize;
import org.telegram.tgnet.TLRPC$TL_photoSize;
import org.telegram.tgnet.TLRPC$TL_upload_getWebFile;
import org.telegram.ui.Components.AnimatedFileDrawable;
import turbogram.Utilities.TurboConfig$Storage;
import turbogram.Utilities.TurboUtils;

public class ImageLoader {
    private static volatile ImageLoader Instance = null;
    private static byte[] bytes;
    private static byte[] bytesThumb;
    private static byte[] header = new byte[12];
    private static byte[] headerThumb = new byte[12];
    private HashMap<String, Integer> bitmapUseCounts = new HashMap();
    private DispatchQueue cacheOutQueue = new DispatchQueue("cacheOutQueue");
    private DispatchQueue cacheThumbOutQueue = new DispatchQueue("cacheThumbOutQueue");
    private int currentHttpFileLoadTasksCount = 0;
    private int currentHttpTasksCount = 0;
    private ConcurrentHashMap<String, Float> fileProgresses = new ConcurrentHashMap();
    private HashMap<String, Integer> forceLoadingImages = new HashMap();
    private LinkedList<HttpFileTask> httpFileLoadTasks = new LinkedList();
    private HashMap<String, HttpFileTask> httpFileLoadTasksByKeys = new HashMap();
    private LinkedList<HttpImageTask> httpTasks = new LinkedList();
    private String ignoreRemoval = null;
    private DispatchQueue imageLoadQueue = new DispatchQueue("imageLoadQueue");
    private HashMap<String, CacheImage> imageLoadingByKeys = new HashMap();
    private SparseArray<CacheImage> imageLoadingByTag = new SparseArray();
    private HashMap<String, CacheImage> imageLoadingByUrl = new HashMap();
    private volatile long lastCacheOutTime = 0;
    private int lastImageNum = 0;
    private long lastProgressUpdateTime = 0;
    private LruCache memCache;
    private HashMap<String, String> replacedBitmaps = new HashMap();
    private HashMap<String, Runnable> retryHttpsTasks = new HashMap();
    private File telegramPath = null;
    private ConcurrentHashMap<String, WebFile> testWebFile = new ConcurrentHashMap();
    private HashMap<String, ThumbGenerateTask> thumbGenerateTasks = new HashMap();
    private DispatchQueue thumbGeneratingQueue = new DispatchQueue("thumbGeneratingQueue");
    private HashMap<String, ThumbGenerateInfo> waitingForQualityThumb = new HashMap();
    private SparseArray<String> waitingForQualityThumbByTag = new SparseArray();

    /* renamed from: org.telegram.messenger.ImageLoader$3 */
    class C08063 extends BroadcastReceiver {
        C08063() {
        }

        public void onReceive(Context arg0, Intent intent) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("file system changed");
            }
            Runnable r = new ImageLoader$3$$Lambda$0(this);
            if ("android.intent.action.MEDIA_UNMOUNTED".equals(intent.getAction())) {
                AndroidUtilities.runOnUIThread(r, 1000);
            } else {
                r.run();
            }
        }

        final /* synthetic */ void lambda$onReceive$0$ImageLoader$3() {
            ImageLoader.this.checkMediaPaths();
        }
    }

    private class CacheImage {
        protected boolean animatedFile;
        protected CacheOutTask cacheTask;
        protected int currentAccount;
        protected File encryptionKeyPath;
        protected String ext;
        protected String filter;
        protected ArrayList<String> filters;
        protected File finalFilePath;
        protected HttpImageTask httpTask;
        protected String httpUrl;
        protected ArrayList<ImageReceiver> imageReceiverArray;
        protected String key;
        protected ArrayList<String> keys;
        protected TLObject location;
        protected SecureDocument secureDocument;
        protected boolean selfThumb;
        protected File tempFilePath;
        protected ArrayList<Boolean> thumbs;
        protected String url;

        private CacheImage() {
            this.imageReceiverArray = new ArrayList();
            this.keys = new ArrayList();
            this.filters = new ArrayList();
            this.thumbs = new ArrayList();
        }

        public void addImageReceiver(ImageReceiver imageReceiver, String key, String filter, boolean thumb) {
            if (!this.imageReceiverArray.contains(imageReceiver)) {
                this.imageReceiverArray.add(imageReceiver);
                this.keys.add(key);
                this.filters.add(filter);
                this.thumbs.add(Boolean.valueOf(thumb));
                ImageLoader.this.imageLoadingByTag.put(imageReceiver.getTag(thumb), this);
            }
        }

        public void replaceImageReceiver(ImageReceiver imageReceiver, String key, String filter, boolean thumb) {
            int index = this.imageReceiverArray.indexOf(imageReceiver);
            if (index != -1) {
                if (((Boolean) this.thumbs.get(index)).booleanValue() != thumb) {
                    index = this.imageReceiverArray.subList(index + 1, this.imageReceiverArray.size()).indexOf(imageReceiver);
                    if (index == -1) {
                        return;
                    }
                }
                this.keys.set(index, key);
                this.filters.set(index, filter);
            }
        }

        public void removeImageReceiver(ImageReceiver imageReceiver) {
            Boolean thumb = Boolean.valueOf(this.selfThumb);
            int a = 0;
            while (a < this.imageReceiverArray.size()) {
                ImageReceiver obj = (ImageReceiver) this.imageReceiverArray.get(a);
                if (obj == null || obj == imageReceiver) {
                    this.imageReceiverArray.remove(a);
                    this.keys.remove(a);
                    this.filters.remove(a);
                    thumb = (Boolean) this.thumbs.remove(a);
                    if (obj != null) {
                        ImageLoader.this.imageLoadingByTag.remove(obj.getTag(thumb.booleanValue()));
                    }
                    a--;
                }
                a++;
            }
            if (this.imageReceiverArray.size() == 0) {
                for (a = 0; a < this.imageReceiverArray.size(); a++) {
                    ImageLoader.this.imageLoadingByTag.remove(((ImageReceiver) this.imageReceiverArray.get(a)).getTag(thumb.booleanValue()));
                }
                this.imageReceiverArray.clear();
                if (!(this.location == null || ImageLoader.this.forceLoadingImages.containsKey(this.key))) {
                    if (this.location instanceof TLRPC$FileLocation) {
                        FileLoader.getInstance(this.currentAccount).cancelLoadFile((TLRPC$FileLocation) this.location, this.ext);
                    } else if (this.location instanceof TLRPC$Document) {
                        FileLoader.getInstance(this.currentAccount).cancelLoadFile((TLRPC$Document) this.location);
                    } else if (this.location instanceof SecureDocument) {
                        FileLoader.getInstance(this.currentAccount).cancelLoadFile((SecureDocument) this.location);
                    } else if (this.location instanceof WebFile) {
                        FileLoader.getInstance(this.currentAccount).cancelLoadFile((WebFile) this.location);
                    }
                }
                if (this.cacheTask != null) {
                    if (this.selfThumb) {
                        ImageLoader.this.cacheThumbOutQueue.cancelRunnable(this.cacheTask);
                    } else {
                        ImageLoader.this.cacheOutQueue.cancelRunnable(this.cacheTask);
                    }
                    this.cacheTask.cancel();
                    this.cacheTask = null;
                }
                if (this.httpTask != null) {
                    ImageLoader.this.httpTasks.remove(this.httpTask);
                    this.httpTask.cancel(true);
                    this.httpTask = null;
                }
                if (this.url != null) {
                    ImageLoader.this.imageLoadingByUrl.remove(this.url);
                }
                if (this.key != null) {
                    ImageLoader.this.imageLoadingByKeys.remove(this.key);
                }
            }
        }

        public void setImageAndClear(BitmapDrawable image) {
            if (image != null) {
                AndroidUtilities.runOnUIThread(new ImageLoader$CacheImage$$Lambda$0(this, image, new ArrayList(this.imageReceiverArray)));
            }
            for (int a = 0; a < this.imageReceiverArray.size(); a++) {
                ImageLoader.this.imageLoadingByTag.remove(((ImageReceiver) this.imageReceiverArray.get(a)).getTag(this.selfThumb));
            }
            this.imageReceiverArray.clear();
            if (this.url != null) {
                ImageLoader.this.imageLoadingByUrl.remove(this.url);
            }
            if (this.key != null) {
                ImageLoader.this.imageLoadingByKeys.remove(this.key);
            }
        }

        final /* synthetic */ void lambda$setImageAndClear$0$ImageLoader$CacheImage(BitmapDrawable image, ArrayList finalImageReceiverArray) {
            int a;
            if (image instanceof AnimatedFileDrawable) {
                boolean imageSet = false;
                BitmapDrawable fileDrawable = (AnimatedFileDrawable) image;
                a = 0;
                while (a < finalImageReceiverArray.size()) {
                    if (((ImageReceiver) finalImageReceiverArray.get(a)).setImageBitmapByKey(a == 0 ? fileDrawable : fileDrawable.makeCopy(), this.key, this.selfThumb, false)) {
                        imageSet = true;
                    }
                    a++;
                }
                if (!imageSet) {
                    ((AnimatedFileDrawable) image).recycle();
                    return;
                }
                return;
            }
            for (a = 0; a < finalImageReceiverArray.size(); a++) {
                ((ImageReceiver) finalImageReceiverArray.get(a)).setImageBitmapByKey(image, this.key, this.selfThumb, false);
            }
        }
    }

    private class CacheOutTask implements Runnable {
        private CacheImage cacheImage;
        private boolean isCancelled;
        private Thread runningThread;
        private final Object sync = new Object();

        public CacheOutTask(CacheImage image) {
            this.cacheImage = image;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
            r56 = this;
            r0 = r56;
            r6 = r0.sync;
            monitor-enter(r6);
            r5 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x0031 }
            r0 = r56;
            r0.runningThread = r5;	 Catch:{ all -> 0x0031 }
            java.lang.Thread.interrupted();	 Catch:{ all -> 0x0031 }
            r0 = r56;
            r5 = r0.isCancelled;	 Catch:{ all -> 0x0031 }
            if (r5 == 0) goto L_0x0018;
        L_0x0016:
            monitor-exit(r6);	 Catch:{ all -> 0x0031 }
        L_0x0017:
            return;
        L_0x0018:
            monitor-exit(r6);	 Catch:{ all -> 0x0031 }
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.animatedFile;
            if (r5 == 0) goto L_0x0067;
        L_0x0021:
            r0 = r56;
            r6 = r0.sync;
            monitor-enter(r6);
            r0 = r56;
            r5 = r0.isCancelled;	 Catch:{ all -> 0x002e }
            if (r5 == 0) goto L_0x0034;
        L_0x002c:
            monitor-exit(r6);	 Catch:{ all -> 0x002e }
            goto L_0x0017;
        L_0x002e:
            r5 = move-exception;
            monitor-exit(r6);	 Catch:{ all -> 0x002e }
            throw r5;
        L_0x0031:
            r5 = move-exception;
            monitor-exit(r6);	 Catch:{ all -> 0x0031 }
            throw r5;
        L_0x0034:
            monitor-exit(r6);	 Catch:{ all -> 0x002e }
            r29 = new org.telegram.ui.Components.AnimatedFileDrawable;
            r0 = r56;
            r5 = r0.cacheImage;
            r6 = r5.finalFilePath;
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.filter;
            if (r5 == 0) goto L_0x0065;
        L_0x0045:
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.filter;
            r7 = "d";
            r5 = r5.equals(r7);
            if (r5 == 0) goto L_0x0065;
        L_0x0054:
            r5 = 1;
        L_0x0055:
            r0 = r29;
            r0.<init>(r6, r5);
            java.lang.Thread.interrupted();
            r0 = r56;
            r1 = r29;
            r0.onPostExecute(r1);
            goto L_0x0017;
        L_0x0065:
            r5 = 0;
            goto L_0x0055;
        L_0x0067:
            r37 = 0;
            r38 = 0;
            r33 = 0;
            r0 = r56;
            r5 = r0.cacheImage;
            r0 = r5.finalFilePath;
            r21 = r0;
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.secureDocument;
            if (r5 != 0) goto L_0x0094;
        L_0x007d:
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.encryptionKeyPath;
            if (r5 == 0) goto L_0x017e;
        L_0x0085:
            if (r21 == 0) goto L_0x017e;
        L_0x0087:
            r5 = r21.getAbsolutePath();
            r6 = ".enc";
            r5 = r5.endsWith(r6);
            if (r5 == 0) goto L_0x017e;
        L_0x0094:
            r34 = 1;
        L_0x0096:
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.secureDocument;
            if (r5 == 0) goto L_0x018e;
        L_0x009e:
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.secureDocument;
            r0 = r5.secureDocumentKey;
            r52 = r0;
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.secureDocument;
            r5 = r5.secureFile;
            if (r5 == 0) goto L_0x0182;
        L_0x00b2:
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.secureDocument;
            r5 = r5.secureFile;
            r5 = r5.file_hash;
            if (r5 == 0) goto L_0x0182;
        L_0x00be:
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.secureDocument;
            r5 = r5.secureFile;
            r0 = r5.file_hash;
            r51 = r0;
        L_0x00ca:
            r22 = 1;
            r54 = 0;
            r5 = android.os.Build.VERSION.SDK_INT;
            r6 = 19;
            if (r5 >= r6) goto L_0x0128;
        L_0x00d4:
            r46 = 0;
            r47 = new java.io.RandomAccessFile;	 Catch:{ Exception -> 0x019f }
            r5 = "r";
            r0 = r47;
            r1 = r21;
            r0.<init>(r1, r5);	 Catch:{ Exception -> 0x019f }
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Exception -> 0x08c5, all -> 0x08c0 }
            r5 = r5.selfThumb;	 Catch:{ Exception -> 0x08c5, all -> 0x08c0 }
            if (r5 == 0) goto L_0x0194;
        L_0x00ea:
            r20 = org.telegram.messenger.ImageLoader.headerThumb;	 Catch:{ Exception -> 0x08c5, all -> 0x08c0 }
        L_0x00ee:
            r5 = 0;
            r0 = r20;
            r6 = r0.length;	 Catch:{ Exception -> 0x08c5, all -> 0x08c0 }
            r0 = r47;
            r1 = r20;
            r0.readFully(r1, r5, r6);	 Catch:{ Exception -> 0x08c5, all -> 0x08c0 }
            r5 = new java.lang.String;	 Catch:{ Exception -> 0x08c5, all -> 0x08c0 }
            r0 = r20;
            r5.<init>(r0);	 Catch:{ Exception -> 0x08c5, all -> 0x08c0 }
            r53 = r5.toLowerCase();	 Catch:{ Exception -> 0x08c5, all -> 0x08c0 }
            r53 = r53.toLowerCase();	 Catch:{ Exception -> 0x08c5, all -> 0x08c0 }
            r5 = "riff";
            r0 = r53;
            r5 = r0.startsWith(r5);	 Catch:{ Exception -> 0x08c5, all -> 0x08c0 }
            if (r5 == 0) goto L_0x0120;
        L_0x0113:
            r5 = "webp";
            r0 = r53;
            r5 = r0.endsWith(r5);	 Catch:{ Exception -> 0x08c5, all -> 0x08c0 }
            if (r5 == 0) goto L_0x0120;
        L_0x011e:
            r54 = 1;
        L_0x0120:
            r47.close();	 Catch:{ Exception -> 0x08c5, all -> 0x08c0 }
            if (r47 == 0) goto L_0x0128;
        L_0x0125:
            r47.close();	 Catch:{ Exception -> 0x019a }
        L_0x0128:
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.selfThumb;
            if (r5 == 0) goto L_0x03e6;
        L_0x0130:
            r16 = 0;
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.filter;
            if (r5 == 0) goto L_0x014b;
        L_0x013a:
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.filter;
            r6 = "b2";
            r5 = r5.contains(r6);
            if (r5 == 0) goto L_0x01bb;
        L_0x0149:
            r16 = 3;
        L_0x014b:
            r0 = r56;
            r5 = org.telegram.messenger.ImageLoader.this;	 Catch:{ Throwable -> 0x0167 }
            r6 = java.lang.System.currentTimeMillis();	 Catch:{ Throwable -> 0x0167 }
            r5.lastCacheOutTime = r6;	 Catch:{ Throwable -> 0x0167 }
            r0 = r56;
            r6 = r0.sync;	 Catch:{ Throwable -> 0x0167 }
            monitor-enter(r6);	 Catch:{ Throwable -> 0x0167 }
            r0 = r56;
            r5 = r0.isCancelled;	 Catch:{ all -> 0x0164 }
            if (r5 == 0) goto L_0x01e1;
        L_0x0161:
            monitor-exit(r6);	 Catch:{ all -> 0x0164 }
            goto L_0x0017;
        L_0x0164:
            r5 = move-exception;
            monitor-exit(r6);	 Catch:{ all -> 0x0164 }
            throw r5;	 Catch:{ Throwable -> 0x0167 }
        L_0x0167:
            r25 = move-exception;
            r4 = r33;
        L_0x016a:
            org.telegram.messenger.FileLog.e(r25);
        L_0x016d:
            java.lang.Thread.interrupted();
            if (r4 == 0) goto L_0x08bd;
        L_0x0172:
            r5 = new android.graphics.drawable.BitmapDrawable;
            r5.<init>(r4);
        L_0x0177:
            r0 = r56;
            r0.onPostExecute(r5);
            goto L_0x0017;
        L_0x017e:
            r34 = 0;
            goto L_0x0096;
        L_0x0182:
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.secureDocument;
            r0 = r5.fileHash;
            r51 = r0;
            goto L_0x00ca;
        L_0x018e:
            r52 = 0;
            r51 = 0;
            goto L_0x00ca;
        L_0x0194:
            r20 = org.telegram.messenger.ImageLoader.header;	 Catch:{ Exception -> 0x08c5, all -> 0x08c0 }
            goto L_0x00ee;
        L_0x019a:
            r25 = move-exception;
            org.telegram.messenger.FileLog.e(r25);
            goto L_0x0128;
        L_0x019f:
            r25 = move-exception;
        L_0x01a0:
            org.telegram.messenger.FileLog.e(r25);	 Catch:{ all -> 0x01af }
            if (r46 == 0) goto L_0x0128;
        L_0x01a5:
            r46.close();	 Catch:{ Exception -> 0x01a9 }
            goto L_0x0128;
        L_0x01a9:
            r25 = move-exception;
            org.telegram.messenger.FileLog.e(r25);
            goto L_0x0128;
        L_0x01af:
            r5 = move-exception;
        L_0x01b0:
            if (r46 == 0) goto L_0x01b5;
        L_0x01b2:
            r46.close();	 Catch:{ Exception -> 0x01b6 }
        L_0x01b5:
            throw r5;
        L_0x01b6:
            r25 = move-exception;
            org.telegram.messenger.FileLog.e(r25);
            goto L_0x01b5;
        L_0x01bb:
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.filter;
            r6 = "b1";
            r5 = r5.contains(r6);
            if (r5 == 0) goto L_0x01ce;
        L_0x01ca:
            r16 = 2;
            goto L_0x014b;
        L_0x01ce:
            r0 = r56;
            r5 = r0.cacheImage;
            r5 = r5.filter;
            r6 = "b";
            r5 = r5.contains(r6);
            if (r5 == 0) goto L_0x014b;
        L_0x01dd:
            r16 = 1;
            goto L_0x014b;
        L_0x01e1:
            monitor-exit(r6);	 Catch:{ all -> 0x0164 }
            r41 = new android.graphics.BitmapFactory$Options;	 Catch:{ Throwable -> 0x0167 }
            r41.<init>();	 Catch:{ Throwable -> 0x0167 }
            r5 = 1;
            r0 = r41;
            r0.inSampleSize = r5;	 Catch:{ Throwable -> 0x0167 }
            r5 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Throwable -> 0x0167 }
            r6 = 21;
            if (r5 >= r6) goto L_0x01f7;
        L_0x01f2:
            r5 = 1;
            r0 = r41;
            r0.inPurgeable = r5;	 Catch:{ Throwable -> 0x0167 }
        L_0x01f7:
            if (r54 == 0) goto L_0x026c;
        L_0x01f9:
            r28 = new java.io.RandomAccessFile;	 Catch:{ Throwable -> 0x0167 }
            r5 = "r";
            r0 = r28;
            r1 = r21;
            r0.<init>(r1, r5);	 Catch:{ Throwable -> 0x0167 }
            r4 = r28.getChannel();	 Catch:{ Throwable -> 0x0167 }
            r5 = java.nio.channels.FileChannel.MapMode.READ_ONLY;	 Catch:{ Throwable -> 0x0167 }
            r6 = 0;
            r8 = r21.length();	 Catch:{ Throwable -> 0x0167 }
            r19 = r4.map(r5, r6, r8);	 Catch:{ Throwable -> 0x0167 }
            r18 = new android.graphics.BitmapFactory$Options;	 Catch:{ Throwable -> 0x0167 }
            r18.<init>();	 Catch:{ Throwable -> 0x0167 }
            r5 = 1;
            r0 = r18;
            r0.inJustDecodeBounds = r5;	 Catch:{ Throwable -> 0x0167 }
            r5 = 0;
            r6 = r19.limit();	 Catch:{ Throwable -> 0x0167 }
            r7 = 1;
            r0 = r19;
            r1 = r18;
            org.telegram.messenger.Utilities.loadWebpImage(r5, r0, r6, r1, r7);	 Catch:{ Throwable -> 0x0167 }
            r0 = r18;
            r5 = r0.outWidth;	 Catch:{ Throwable -> 0x0167 }
            r0 = r18;
            r6 = r0.outHeight;	 Catch:{ Throwable -> 0x0167 }
            r7 = android.graphics.Bitmap.Config.ARGB_8888;	 Catch:{ Throwable -> 0x0167 }
            r4 = org.telegram.messenger.Bitmaps.createBitmap(r5, r6, r7);	 Catch:{ Throwable -> 0x0167 }
            r6 = r19.limit();	 Catch:{ Throwable -> 0x0267 }
            r7 = 0;
            r0 = r41;
            r5 = r0.inPurgeable;	 Catch:{ Throwable -> 0x0267 }
            if (r5 != 0) goto L_0x026a;
        L_0x0245:
            r5 = 1;
        L_0x0246:
            r0 = r19;
            org.telegram.messenger.Utilities.loadWebpImage(r4, r0, r6, r7, r5);	 Catch:{ Throwable -> 0x0267 }
            r28.close();	 Catch:{ Throwable -> 0x0267 }
        L_0x024e:
            if (r4 != 0) goto L_0x032f;
        L_0x0250:
            r6 = r21.length();	 Catch:{ Throwable -> 0x0267 }
            r8 = 0;
            r5 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
            if (r5 == 0) goto L_0x0262;
        L_0x025a:
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0267 }
            r5 = r5.filter;	 Catch:{ Throwable -> 0x0267 }
            if (r5 != 0) goto L_0x016d;
        L_0x0262:
            r21.delete();	 Catch:{ Throwable -> 0x0267 }
            goto L_0x016d;
        L_0x0267:
            r25 = move-exception;
            goto L_0x016a;
        L_0x026a:
            r5 = 0;
            goto L_0x0246;
        L_0x026c:
            r0 = r41;
            r5 = r0.inPurgeable;	 Catch:{ Throwable -> 0x0167 }
            if (r5 != 0) goto L_0x0274;
        L_0x0272:
            if (r52 == 0) goto L_0x0306;
        L_0x0274:
            r27 = new java.io.RandomAccessFile;	 Catch:{ Throwable -> 0x0167 }
            r5 = "r";
            r0 = r27;
            r1 = r21;
            r0.<init>(r1, r5);	 Catch:{ Throwable -> 0x0167 }
            r6 = r27.length();	 Catch:{ Throwable -> 0x0167 }
            r0 = (int) r6;	 Catch:{ Throwable -> 0x0167 }
            r36 = r0;
            r40 = 0;
            r5 = org.telegram.messenger.ImageLoader.bytesThumb;	 Catch:{ Throwable -> 0x0167 }
            if (r5 == 0) goto L_0x02f2;
        L_0x028f:
            r5 = org.telegram.messenger.ImageLoader.bytesThumb;	 Catch:{ Throwable -> 0x0167 }
            r5 = r5.length;	 Catch:{ Throwable -> 0x0167 }
            r0 = r36;
            if (r5 < r0) goto L_0x02f2;
        L_0x0298:
            r23 = org.telegram.messenger.ImageLoader.bytesThumb;	 Catch:{ Throwable -> 0x0167 }
        L_0x029c:
            if (r23 != 0) goto L_0x02a7;
        L_0x029e:
            r0 = r36;
            r0 = new byte[r0];	 Catch:{ Throwable -> 0x0167 }
            r23 = r0;
            org.telegram.messenger.ImageLoader.bytesThumb = r23;	 Catch:{ Throwable -> 0x0167 }
        L_0x02a7:
            r5 = 0;
            r0 = r27;
            r1 = r23;
            r2 = r36;
            r0.readFully(r1, r5, r2);	 Catch:{ Throwable -> 0x0167 }
            r27.close();	 Catch:{ Throwable -> 0x0167 }
            r26 = 0;
            if (r52 == 0) goto L_0x02f5;
        L_0x02b8:
            r5 = 0;
            r0 = r23;
            r1 = r36;
            r2 = r52;
            org.telegram.messenger.secretmedia.EncryptedFileInputStream.decryptBytesWithKeyFile(r0, r5, r1, r2);	 Catch:{ Throwable -> 0x0167 }
            r5 = 0;
            r0 = r23;
            r1 = r36;
            r31 = org.telegram.messenger.Utilities.computeSHA256(r0, r5, r1);	 Catch:{ Throwable -> 0x0167 }
            if (r51 == 0) goto L_0x02d7;
        L_0x02cd:
            r0 = r31;
            r1 = r51;
            r5 = java.util.Arrays.equals(r0, r1);	 Catch:{ Throwable -> 0x0167 }
            if (r5 != 0) goto L_0x02d9;
        L_0x02d7:
            r26 = 1;
        L_0x02d9:
            r5 = 0;
            r5 = r23[r5];	 Catch:{ Throwable -> 0x0167 }
            r0 = r5 & 255;
            r40 = r0;
            r36 = r36 - r40;
        L_0x02e2:
            if (r26 != 0) goto L_0x08d2;
        L_0x02e4:
            r0 = r23;
            r1 = r40;
            r2 = r36;
            r3 = r41;
            r4 = android.graphics.BitmapFactory.decodeByteArray(r0, r1, r2, r3);	 Catch:{ Throwable -> 0x0167 }
            goto L_0x024e;
        L_0x02f2:
            r23 = 0;
            goto L_0x029c;
        L_0x02f5:
            if (r34 == 0) goto L_0x02e2;
        L_0x02f7:
            r5 = 0;
            r0 = r56;
            r6 = r0.cacheImage;	 Catch:{ Throwable -> 0x0167 }
            r6 = r6.encryptionKeyPath;	 Catch:{ Throwable -> 0x0167 }
            r0 = r23;
            r1 = r36;
            org.telegram.messenger.secretmedia.EncryptedFileInputStream.decryptBytesWithKeyFile(r0, r5, r1, r6);	 Catch:{ Throwable -> 0x0167 }
            goto L_0x02e2;
        L_0x0306:
            if (r34 == 0) goto L_0x0325;
        L_0x0308:
            r35 = new org.telegram.messenger.secretmedia.EncryptedFileInputStream;	 Catch:{ Throwable -> 0x0167 }
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0167 }
            r5 = r5.encryptionKeyPath;	 Catch:{ Throwable -> 0x0167 }
            r0 = r35;
            r1 = r21;
            r0.<init>(r1, r5);	 Catch:{ Throwable -> 0x0167 }
        L_0x0317:
            r5 = 0;
            r0 = r35;
            r1 = r41;
            r4 = android.graphics.BitmapFactory.decodeStream(r0, r5, r1);	 Catch:{ Throwable -> 0x0167 }
            r35.close();	 Catch:{ Throwable -> 0x0267 }
            goto L_0x024e;
        L_0x0325:
            r35 = new java.io.FileInputStream;	 Catch:{ Throwable -> 0x0167 }
            r0 = r35;
            r1 = r21;
            r0.<init>(r1);	 Catch:{ Throwable -> 0x0167 }
            goto L_0x0317;
        L_0x032f:
            r5 = 1;
            r0 = r16;
            if (r0 != r5) goto L_0x0357;
        L_0x0334:
            r5 = r4.getConfig();	 Catch:{ Throwable -> 0x0267 }
            r6 = android.graphics.Bitmap.Config.ARGB_8888;	 Catch:{ Throwable -> 0x0267 }
            if (r5 != r6) goto L_0x016d;
        L_0x033c:
            r5 = 3;
            r0 = r41;
            r6 = r0.inPurgeable;	 Catch:{ Throwable -> 0x0267 }
            if (r6 == 0) goto L_0x0355;
        L_0x0343:
            r6 = 0;
        L_0x0344:
            r7 = r4.getWidth();	 Catch:{ Throwable -> 0x0267 }
            r8 = r4.getHeight();	 Catch:{ Throwable -> 0x0267 }
            r9 = r4.getRowBytes();	 Catch:{ Throwable -> 0x0267 }
            org.telegram.messenger.Utilities.blurBitmap(r4, r5, r6, r7, r8, r9);	 Catch:{ Throwable -> 0x0267 }
            goto L_0x016d;
        L_0x0355:
            r6 = 1;
            goto L_0x0344;
        L_0x0357:
            r5 = 2;
            r0 = r16;
            if (r0 != r5) goto L_0x037f;
        L_0x035c:
            r5 = r4.getConfig();	 Catch:{ Throwable -> 0x0267 }
            r6 = android.graphics.Bitmap.Config.ARGB_8888;	 Catch:{ Throwable -> 0x0267 }
            if (r5 != r6) goto L_0x016d;
        L_0x0364:
            r5 = 1;
            r0 = r41;
            r6 = r0.inPurgeable;	 Catch:{ Throwable -> 0x0267 }
            if (r6 == 0) goto L_0x037d;
        L_0x036b:
            r6 = 0;
        L_0x036c:
            r7 = r4.getWidth();	 Catch:{ Throwable -> 0x0267 }
            r8 = r4.getHeight();	 Catch:{ Throwable -> 0x0267 }
            r9 = r4.getRowBytes();	 Catch:{ Throwable -> 0x0267 }
            org.telegram.messenger.Utilities.blurBitmap(r4, r5, r6, r7, r8, r9);	 Catch:{ Throwable -> 0x0267 }
            goto L_0x016d;
        L_0x037d:
            r6 = 1;
            goto L_0x036c;
        L_0x037f:
            r5 = 3;
            r0 = r16;
            if (r0 != r5) goto L_0x03d9;
        L_0x0384:
            r5 = r4.getConfig();	 Catch:{ Throwable -> 0x0267 }
            r6 = android.graphics.Bitmap.Config.ARGB_8888;	 Catch:{ Throwable -> 0x0267 }
            if (r5 != r6) goto L_0x016d;
        L_0x038c:
            r5 = 7;
            r0 = r41;
            r6 = r0.inPurgeable;	 Catch:{ Throwable -> 0x0267 }
            if (r6 == 0) goto L_0x03d3;
        L_0x0393:
            r6 = 0;
        L_0x0394:
            r7 = r4.getWidth();	 Catch:{ Throwable -> 0x0267 }
            r8 = r4.getHeight();	 Catch:{ Throwable -> 0x0267 }
            r9 = r4.getRowBytes();	 Catch:{ Throwable -> 0x0267 }
            org.telegram.messenger.Utilities.blurBitmap(r4, r5, r6, r7, r8, r9);	 Catch:{ Throwable -> 0x0267 }
            r5 = 7;
            r0 = r41;
            r6 = r0.inPurgeable;	 Catch:{ Throwable -> 0x0267 }
            if (r6 == 0) goto L_0x03d5;
        L_0x03aa:
            r6 = 0;
        L_0x03ab:
            r7 = r4.getWidth();	 Catch:{ Throwable -> 0x0267 }
            r8 = r4.getHeight();	 Catch:{ Throwable -> 0x0267 }
            r9 = r4.getRowBytes();	 Catch:{ Throwable -> 0x0267 }
            org.telegram.messenger.Utilities.blurBitmap(r4, r5, r6, r7, r8, r9);	 Catch:{ Throwable -> 0x0267 }
            r5 = 7;
            r0 = r41;
            r6 = r0.inPurgeable;	 Catch:{ Throwable -> 0x0267 }
            if (r6 == 0) goto L_0x03d7;
        L_0x03c1:
            r6 = 0;
        L_0x03c2:
            r7 = r4.getWidth();	 Catch:{ Throwable -> 0x0267 }
            r8 = r4.getHeight();	 Catch:{ Throwable -> 0x0267 }
            r9 = r4.getRowBytes();	 Catch:{ Throwable -> 0x0267 }
            org.telegram.messenger.Utilities.blurBitmap(r4, r5, r6, r7, r8, r9);	 Catch:{ Throwable -> 0x0267 }
            goto L_0x016d;
        L_0x03d3:
            r6 = 1;
            goto L_0x0394;
        L_0x03d5:
            r6 = 1;
            goto L_0x03ab;
        L_0x03d7:
            r6 = 1;
            goto L_0x03c2;
        L_0x03d9:
            if (r16 != 0) goto L_0x016d;
        L_0x03db:
            r0 = r41;
            r5 = r0.inPurgeable;	 Catch:{ Throwable -> 0x0267 }
            if (r5 == 0) goto L_0x016d;
        L_0x03e1:
            org.telegram.messenger.Utilities.pinBitmap(r4);	 Catch:{ Throwable -> 0x0267 }
            goto L_0x016d;
        L_0x03e6:
            r39 = 0;
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.httpUrl;	 Catch:{ Throwable -> 0x0488 }
            if (r5 == 0) goto L_0x0436;
        L_0x03f0:
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.httpUrl;	 Catch:{ Throwable -> 0x0488 }
            r6 = "thumb://";
            r5 = r5.startsWith(r6);	 Catch:{ Throwable -> 0x0488 }
            if (r5 == 0) goto L_0x048d;
        L_0x03ff:
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.httpUrl;	 Catch:{ Throwable -> 0x0488 }
            r6 = ":";
            r7 = 8;
            r32 = r5.indexOf(r6, r7);	 Catch:{ Throwable -> 0x0488 }
            if (r32 < 0) goto L_0x0434;
        L_0x0410:
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.httpUrl;	 Catch:{ Throwable -> 0x0488 }
            r6 = 8;
            r0 = r32;
            r5 = r5.substring(r6, r0);	 Catch:{ Throwable -> 0x0488 }
            r6 = java.lang.Long.parseLong(r5);	 Catch:{ Throwable -> 0x0488 }
            r37 = java.lang.Long.valueOf(r6);	 Catch:{ Throwable -> 0x0488 }
            r38 = 0;
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.httpUrl;	 Catch:{ Throwable -> 0x0488 }
            r6 = r32 + 1;
            r39 = r5.substring(r6);	 Catch:{ Throwable -> 0x0488 }
        L_0x0434:
            r22 = 0;
        L_0x0436:
            r24 = 20;
            if (r37 == 0) goto L_0x043c;
        L_0x043a:
            r24 = 0;
        L_0x043c:
            if (r24 == 0) goto L_0x046c;
        L_0x043e:
            r0 = r56;
            r5 = org.telegram.messenger.ImageLoader.this;	 Catch:{ Throwable -> 0x0488 }
            r6 = r5.lastCacheOutTime;	 Catch:{ Throwable -> 0x0488 }
            r8 = 0;
            r5 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
            if (r5 == 0) goto L_0x046c;
        L_0x044c:
            r0 = r56;
            r5 = org.telegram.messenger.ImageLoader.this;	 Catch:{ Throwable -> 0x0488 }
            r6 = r5.lastCacheOutTime;	 Catch:{ Throwable -> 0x0488 }
            r8 = java.lang.System.currentTimeMillis();	 Catch:{ Throwable -> 0x0488 }
            r0 = r24;
            r10 = (long) r0;	 Catch:{ Throwable -> 0x0488 }
            r8 = r8 - r10;
            r5 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
            if (r5 <= 0) goto L_0x046c;
        L_0x0460:
            r5 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Throwable -> 0x0488 }
            r6 = 21;
            if (r5 >= r6) goto L_0x046c;
        L_0x0466:
            r0 = r24;
            r6 = (long) r0;	 Catch:{ Throwable -> 0x0488 }
            java.lang.Thread.sleep(r6);	 Catch:{ Throwable -> 0x0488 }
        L_0x046c:
            r0 = r56;
            r5 = org.telegram.messenger.ImageLoader.this;	 Catch:{ Throwable -> 0x0488 }
            r6 = java.lang.System.currentTimeMillis();	 Catch:{ Throwable -> 0x0488 }
            r5.lastCacheOutTime = r6;	 Catch:{ Throwable -> 0x0488 }
            r0 = r56;
            r6 = r0.sync;	 Catch:{ Throwable -> 0x0488 }
            monitor-enter(r6);	 Catch:{ Throwable -> 0x0488 }
            r0 = r56;
            r5 = r0.isCancelled;	 Catch:{ all -> 0x0485 }
            if (r5 == 0) goto L_0x04dc;
        L_0x0482:
            monitor-exit(r6);	 Catch:{ all -> 0x0485 }
            goto L_0x0017;
        L_0x0485:
            r5 = move-exception;
            monitor-exit(r6);	 Catch:{ all -> 0x0485 }
            throw r5;	 Catch:{ Throwable -> 0x0488 }
        L_0x0488:
            r5 = move-exception;
            r4 = r33;
            goto L_0x016d;
        L_0x048d:
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.httpUrl;	 Catch:{ Throwable -> 0x0488 }
            r6 = "vthumb://";
            r5 = r5.startsWith(r6);	 Catch:{ Throwable -> 0x0488 }
            if (r5 == 0) goto L_0x04c9;
        L_0x049c:
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.httpUrl;	 Catch:{ Throwable -> 0x0488 }
            r6 = ":";
            r7 = 9;
            r32 = r5.indexOf(r6, r7);	 Catch:{ Throwable -> 0x0488 }
            if (r32 < 0) goto L_0x04c5;
        L_0x04ad:
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.httpUrl;	 Catch:{ Throwable -> 0x0488 }
            r6 = 9;
            r0 = r32;
            r5 = r5.substring(r6, r0);	 Catch:{ Throwable -> 0x0488 }
            r6 = java.lang.Long.parseLong(r5);	 Catch:{ Throwable -> 0x0488 }
            r37 = java.lang.Long.valueOf(r6);	 Catch:{ Throwable -> 0x0488 }
            r38 = 1;
        L_0x04c5:
            r22 = 0;
            goto L_0x0436;
        L_0x04c9:
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.httpUrl;	 Catch:{ Throwable -> 0x0488 }
            r6 = "http";
            r5 = r5.startsWith(r6);	 Catch:{ Throwable -> 0x0488 }
            if (r5 != 0) goto L_0x0436;
        L_0x04d8:
            r22 = 0;
            goto L_0x0436;
        L_0x04dc:
            monitor-exit(r6);	 Catch:{ all -> 0x0485 }
            r41 = new android.graphics.BitmapFactory$Options;	 Catch:{ Throwable -> 0x0488 }
            r41.<init>();	 Catch:{ Throwable -> 0x0488 }
            r5 = 1;
            r0 = r41;
            r0.inSampleSize = r5;	 Catch:{ Throwable -> 0x0488 }
            r55 = 0;
            r30 = 0;
            r15 = 0;
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.filter;	 Catch:{ Throwable -> 0x0488 }
            if (r5 == 0) goto L_0x064b;
        L_0x04f4:
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.filter;	 Catch:{ Throwable -> 0x0488 }
            r6 = "_";
            r12 = r5.split(r6);	 Catch:{ Throwable -> 0x0488 }
            r5 = r12.length;	 Catch:{ Throwable -> 0x0488 }
            r6 = 2;
            if (r5 < r6) goto L_0x051b;
        L_0x0505:
            r5 = 0;
            r5 = r12[r5];	 Catch:{ Throwable -> 0x0488 }
            r5 = java.lang.Float.parseFloat(r5);	 Catch:{ Throwable -> 0x0488 }
            r6 = org.telegram.messenger.AndroidUtilities.density;	 Catch:{ Throwable -> 0x0488 }
            r55 = r5 * r6;
            r5 = 1;
            r5 = r12[r5];	 Catch:{ Throwable -> 0x0488 }
            r5 = java.lang.Float.parseFloat(r5);	 Catch:{ Throwable -> 0x0488 }
            r6 = org.telegram.messenger.AndroidUtilities.density;	 Catch:{ Throwable -> 0x0488 }
            r30 = r5 * r6;
        L_0x051b:
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.filter;	 Catch:{ Throwable -> 0x0488 }
            r6 = "b";
            r5 = r5.contains(r6);	 Catch:{ Throwable -> 0x0488 }
            if (r5 == 0) goto L_0x052b;
        L_0x052a:
            r15 = 1;
        L_0x052b:
            r5 = 0;
            r5 = (r55 > r5 ? 1 : (r55 == r5 ? 0 : -1));
            if (r5 == 0) goto L_0x08ca;
        L_0x0530:
            r5 = 0;
            r5 = (r30 > r5 ? 1 : (r30 == r5 ? 0 : -1));
            if (r5 == 0) goto L_0x08ca;
        L_0x0535:
            r5 = 1;
            r0 = r41;
            r0.inJustDecodeBounds = r5;	 Catch:{ Throwable -> 0x0488 }
            if (r37 == 0) goto L_0x05a3;
        L_0x053c:
            if (r39 != 0) goto L_0x05a3;
        L_0x053e:
            if (r38 == 0) goto L_0x0590;
        L_0x0540:
            r5 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.getContentResolver();	 Catch:{ Throwable -> 0x0488 }
            r6 = r37.longValue();	 Catch:{ Throwable -> 0x0488 }
            r8 = 1;
            r0 = r41;
            android.provider.MediaStore.Video.Thumbnails.getThumbnail(r5, r6, r8, r0);	 Catch:{ Throwable -> 0x0488 }
            r4 = r33;
        L_0x0552:
            r0 = r41;
            r5 = r0.outWidth;	 Catch:{ Throwable -> 0x058d }
            r0 = (float) r5;	 Catch:{ Throwable -> 0x058d }
            r44 = r0;
            r0 = r41;
            r5 = r0.outHeight;	 Catch:{ Throwable -> 0x058d }
            r0 = (float) r5;	 Catch:{ Throwable -> 0x058d }
            r42 = r0;
            r5 = r44 / r55;
            r6 = r42 / r30;
            r49 = java.lang.Math.max(r5, r6);	 Catch:{ Throwable -> 0x058d }
            r5 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
            r5 = (r49 > r5 ? 1 : (r49 == r5 ? 0 : -1));
            if (r5 >= 0) goto L_0x0570;
        L_0x056e:
            r49 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        L_0x0570:
            r5 = 0;
            r0 = r41;
            r0.inJustDecodeBounds = r5;	 Catch:{ Throwable -> 0x058d }
            r0 = r49;
            r5 = (int) r0;	 Catch:{ Throwable -> 0x058d }
            r0 = r41;
            r0.inSampleSize = r5;	 Catch:{ Throwable -> 0x058d }
        L_0x057c:
            r0 = r56;
            r6 = r0.sync;	 Catch:{ Throwable -> 0x058d }
            monitor-enter(r6);	 Catch:{ Throwable -> 0x058d }
            r0 = r56;
            r5 = r0.isCancelled;	 Catch:{ all -> 0x058a }
            if (r5 == 0) goto L_0x06a8;
        L_0x0587:
            monitor-exit(r6);	 Catch:{ all -> 0x058a }
            goto L_0x0017;
        L_0x058a:
            r5 = move-exception;
            monitor-exit(r6);	 Catch:{ all -> 0x058a }
            throw r5;	 Catch:{ Throwable -> 0x058d }
        L_0x058d:
            r5 = move-exception;
            goto L_0x016d;
        L_0x0590:
            r5 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.getContentResolver();	 Catch:{ Throwable -> 0x0488 }
            r6 = r37.longValue();	 Catch:{ Throwable -> 0x0488 }
            r8 = 1;
            r0 = r41;
            android.provider.MediaStore.Images.Thumbnails.getThumbnail(r5, r6, r8, r0);	 Catch:{ Throwable -> 0x0488 }
            r4 = r33;
            goto L_0x0552;
        L_0x05a3:
            if (r52 == 0) goto L_0x0622;
        L_0x05a5:
            r27 = new java.io.RandomAccessFile;	 Catch:{ Throwable -> 0x0488 }
            r5 = "r";
            r0 = r27;
            r1 = r21;
            r0.<init>(r1, r5);	 Catch:{ Throwable -> 0x0488 }
            r6 = r27.length();	 Catch:{ Throwable -> 0x0488 }
            r0 = (int) r6;	 Catch:{ Throwable -> 0x0488 }
            r36 = r0;
            r5 = org.telegram.messenger.ImageLoader.bytes;	 Catch:{ Throwable -> 0x0488 }
            if (r5 == 0) goto L_0x061f;
        L_0x05be:
            r5 = org.telegram.messenger.ImageLoader.bytes;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.length;	 Catch:{ Throwable -> 0x0488 }
            r0 = r36;
            if (r5 < r0) goto L_0x061f;
        L_0x05c7:
            r23 = org.telegram.messenger.ImageLoader.bytes;	 Catch:{ Throwable -> 0x0488 }
        L_0x05cb:
            if (r23 != 0) goto L_0x05d6;
        L_0x05cd:
            r0 = r36;
            r0 = new byte[r0];	 Catch:{ Throwable -> 0x0488 }
            r23 = r0;
            org.telegram.messenger.ImageLoader.bytes = r23;	 Catch:{ Throwable -> 0x0488 }
        L_0x05d6:
            r5 = 0;
            r0 = r27;
            r1 = r23;
            r2 = r36;
            r0.readFully(r1, r5, r2);	 Catch:{ Throwable -> 0x0488 }
            r27.close();	 Catch:{ Throwable -> 0x0488 }
            r5 = 0;
            r0 = r23;
            r1 = r36;
            r2 = r52;
            org.telegram.messenger.secretmedia.EncryptedFileInputStream.decryptBytesWithKeyFile(r0, r5, r1, r2);	 Catch:{ Throwable -> 0x0488 }
            r5 = 0;
            r0 = r23;
            r1 = r36;
            r31 = org.telegram.messenger.Utilities.computeSHA256(r0, r5, r1);	 Catch:{ Throwable -> 0x0488 }
            r26 = 0;
            if (r51 == 0) goto L_0x0604;
        L_0x05fa:
            r0 = r31;
            r1 = r51;
            r5 = java.util.Arrays.equals(r0, r1);	 Catch:{ Throwable -> 0x0488 }
            if (r5 != 0) goto L_0x0606;
        L_0x0604:
            r26 = 1;
        L_0x0606:
            r5 = 0;
            r5 = r23[r5];	 Catch:{ Throwable -> 0x0488 }
            r0 = r5 & 255;
            r40 = r0;
            r36 = r36 - r40;
            if (r26 != 0) goto L_0x08ce;
        L_0x0611:
            r0 = r23;
            r1 = r40;
            r2 = r36;
            r3 = r41;
            r4 = android.graphics.BitmapFactory.decodeByteArray(r0, r1, r2, r3);	 Catch:{ Throwable -> 0x0488 }
            goto L_0x0552;
        L_0x061f:
            r23 = 0;
            goto L_0x05cb;
        L_0x0622:
            if (r34 == 0) goto L_0x0641;
        L_0x0624:
            r35 = new org.telegram.messenger.secretmedia.EncryptedFileInputStream;	 Catch:{ Throwable -> 0x0488 }
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x0488 }
            r5 = r5.encryptionKeyPath;	 Catch:{ Throwable -> 0x0488 }
            r0 = r35;
            r1 = r21;
            r0.<init>(r1, r5);	 Catch:{ Throwable -> 0x0488 }
        L_0x0633:
            r5 = 0;
            r0 = r35;
            r1 = r41;
            r4 = android.graphics.BitmapFactory.decodeStream(r0, r5, r1);	 Catch:{ Throwable -> 0x0488 }
            r35.close();	 Catch:{ Throwable -> 0x058d }
            goto L_0x0552;
        L_0x0641:
            r35 = new java.io.FileInputStream;	 Catch:{ Throwable -> 0x0488 }
            r0 = r35;
            r1 = r21;
            r0.<init>(r1);	 Catch:{ Throwable -> 0x0488 }
            goto L_0x0633;
        L_0x064b:
            if (r39 == 0) goto L_0x08ca;
        L_0x064d:
            r5 = 1;
            r0 = r41;
            r0.inJustDecodeBounds = r5;	 Catch:{ Throwable -> 0x0488 }
            r5 = android.graphics.Bitmap.Config.RGB_565;	 Catch:{ Throwable -> 0x0488 }
            r0 = r41;
            r0.inPreferredConfig = r5;	 Catch:{ Throwable -> 0x0488 }
            r35 = new java.io.FileInputStream;	 Catch:{ Throwable -> 0x0488 }
            r0 = r35;
            r1 = r21;
            r0.<init>(r1);	 Catch:{ Throwable -> 0x0488 }
            r5 = 0;
            r0 = r35;
            r1 = r41;
            r4 = android.graphics.BitmapFactory.decodeStream(r0, r5, r1);	 Catch:{ Throwable -> 0x0488 }
            r35.close();	 Catch:{ Throwable -> 0x058d }
            r0 = r41;
            r0 = r0.outWidth;	 Catch:{ Throwable -> 0x058d }
            r45 = r0;
            r0 = r41;
            r0 = r0.outHeight;	 Catch:{ Throwable -> 0x058d }
            r43 = r0;
            r5 = 0;
            r0 = r41;
            r0.inJustDecodeBounds = r5;	 Catch:{ Throwable -> 0x058d }
            r0 = r45;
            r5 = r0 / 200;
            r0 = r43;
            r6 = r0 / 200;
            r5 = java.lang.Math.max(r5, r6);	 Catch:{ Throwable -> 0x058d }
            r0 = (float) r5;	 Catch:{ Throwable -> 0x058d }
            r49 = r0;
            r5 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
            r5 = (r49 > r5 ? 1 : (r49 == r5 ? 0 : -1));
            if (r5 >= 0) goto L_0x0695;
        L_0x0693:
            r49 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        L_0x0695:
            r48 = 1;
        L_0x0697:
            r48 = r48 * 2;
            r5 = r48 * 2;
            r5 = (float) r5;	 Catch:{ Throwable -> 0x058d }
            r5 = (r5 > r49 ? 1 : (r5 == r49 ? 0 : -1));
            if (r5 < 0) goto L_0x0697;
        L_0x06a0:
            r0 = r48;
            r1 = r41;
            r1.inSampleSize = r0;	 Catch:{ Throwable -> 0x058d }
            goto L_0x057c;
        L_0x06a8:
            monitor-exit(r6);	 Catch:{ all -> 0x058a }
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x058d }
            r5 = r5.filter;	 Catch:{ Throwable -> 0x058d }
            if (r5 == 0) goto L_0x06bb;
        L_0x06b1:
            if (r15 != 0) goto L_0x06bb;
        L_0x06b3:
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x058d }
            r5 = r5.httpUrl;	 Catch:{ Throwable -> 0x058d }
            if (r5 == 0) goto L_0x075c;
        L_0x06bb:
            r5 = android.graphics.Bitmap.Config.ARGB_8888;	 Catch:{ Throwable -> 0x058d }
            r0 = r41;
            r0.inPreferredConfig = r5;	 Catch:{ Throwable -> 0x058d }
        L_0x06c1:
            r5 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Throwable -> 0x058d }
            r6 = 21;
            if (r5 >= r6) goto L_0x06cc;
        L_0x06c7:
            r5 = 1;
            r0 = r41;
            r0.inPurgeable = r5;	 Catch:{ Throwable -> 0x058d }
        L_0x06cc:
            r5 = 0;
            r0 = r41;
            r0.inDither = r5;	 Catch:{ Throwable -> 0x058d }
            if (r37 == 0) goto L_0x06e8;
        L_0x06d3:
            if (r39 != 0) goto L_0x06e8;
        L_0x06d5:
            if (r38 == 0) goto L_0x0764;
        L_0x06d7:
            r5 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Throwable -> 0x058d }
            r5 = r5.getContentResolver();	 Catch:{ Throwable -> 0x058d }
            r6 = r37.longValue();	 Catch:{ Throwable -> 0x058d }
            r8 = 1;
            r0 = r41;
            r4 = android.provider.MediaStore.Video.Thumbnails.getThumbnail(r5, r6, r8, r0);	 Catch:{ Throwable -> 0x058d }
        L_0x06e8:
            if (r4 != 0) goto L_0x0741;
        L_0x06ea:
            if (r54 == 0) goto L_0x0779;
        L_0x06ec:
            r28 = new java.io.RandomAccessFile;	 Catch:{ Throwable -> 0x058d }
            r5 = "r";
            r0 = r28;
            r1 = r21;
            r0.<init>(r1, r5);	 Catch:{ Throwable -> 0x058d }
            r6 = r28.getChannel();	 Catch:{ Throwable -> 0x058d }
            r7 = java.nio.channels.FileChannel.MapMode.READ_ONLY;	 Catch:{ Throwable -> 0x058d }
            r8 = 0;
            r10 = r21.length();	 Catch:{ Throwable -> 0x058d }
            r19 = r6.map(r7, r8, r10);	 Catch:{ Throwable -> 0x058d }
            r18 = new android.graphics.BitmapFactory$Options;	 Catch:{ Throwable -> 0x058d }
            r18.<init>();	 Catch:{ Throwable -> 0x058d }
            r5 = 1;
            r0 = r18;
            r0.inJustDecodeBounds = r5;	 Catch:{ Throwable -> 0x058d }
            r5 = 0;
            r6 = r19.limit();	 Catch:{ Throwable -> 0x058d }
            r7 = 1;
            r0 = r19;
            r1 = r18;
            org.telegram.messenger.Utilities.loadWebpImage(r5, r0, r6, r1, r7);	 Catch:{ Throwable -> 0x058d }
            r0 = r18;
            r5 = r0.outWidth;	 Catch:{ Throwable -> 0x058d }
            r0 = r18;
            r6 = r0.outHeight;	 Catch:{ Throwable -> 0x058d }
            r7 = android.graphics.Bitmap.Config.ARGB_8888;	 Catch:{ Throwable -> 0x058d }
            r4 = org.telegram.messenger.Bitmaps.createBitmap(r5, r6, r7);	 Catch:{ Throwable -> 0x058d }
            r6 = r19.limit();	 Catch:{ Throwable -> 0x058d }
            r7 = 0;
            r0 = r41;
            r5 = r0.inPurgeable;	 Catch:{ Throwable -> 0x058d }
            if (r5 != 0) goto L_0x0777;
        L_0x0738:
            r5 = 1;
        L_0x0739:
            r0 = r19;
            org.telegram.messenger.Utilities.loadWebpImage(r4, r0, r6, r7, r5);	 Catch:{ Throwable -> 0x058d }
            r28.close();	 Catch:{ Throwable -> 0x058d }
        L_0x0741:
            if (r4 != 0) goto L_0x083c;
        L_0x0743:
            if (r22 == 0) goto L_0x016d;
        L_0x0745:
            r6 = r21.length();	 Catch:{ Throwable -> 0x058d }
            r8 = 0;
            r5 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
            if (r5 == 0) goto L_0x0757;
        L_0x074f:
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x058d }
            r5 = r5.filter;	 Catch:{ Throwable -> 0x058d }
            if (r5 != 0) goto L_0x016d;
        L_0x0757:
            r21.delete();	 Catch:{ Throwable -> 0x058d }
            goto L_0x016d;
        L_0x075c:
            r5 = android.graphics.Bitmap.Config.RGB_565;	 Catch:{ Throwable -> 0x058d }
            r0 = r41;
            r0.inPreferredConfig = r5;	 Catch:{ Throwable -> 0x058d }
            goto L_0x06c1;
        L_0x0764:
            r5 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Throwable -> 0x058d }
            r5 = r5.getContentResolver();	 Catch:{ Throwable -> 0x058d }
            r6 = r37.longValue();	 Catch:{ Throwable -> 0x058d }
            r8 = 1;
            r0 = r41;
            r4 = android.provider.MediaStore.Images.Thumbnails.getThumbnail(r5, r6, r8, r0);	 Catch:{ Throwable -> 0x058d }
            goto L_0x06e8;
        L_0x0777:
            r5 = 0;
            goto L_0x0739;
        L_0x0779:
            r0 = r41;
            r5 = r0.inPurgeable;	 Catch:{ Throwable -> 0x058d }
            if (r5 != 0) goto L_0x0781;
        L_0x077f:
            if (r52 == 0) goto L_0x0813;
        L_0x0781:
            r27 = new java.io.RandomAccessFile;	 Catch:{ Throwable -> 0x058d }
            r5 = "r";
            r0 = r27;
            r1 = r21;
            r0.<init>(r1, r5);	 Catch:{ Throwable -> 0x058d }
            r6 = r27.length();	 Catch:{ Throwable -> 0x058d }
            r0 = (int) r6;	 Catch:{ Throwable -> 0x058d }
            r36 = r0;
            r40 = 0;
            r5 = org.telegram.messenger.ImageLoader.bytes;	 Catch:{ Throwable -> 0x058d }
            if (r5 == 0) goto L_0x07ff;
        L_0x079c:
            r5 = org.telegram.messenger.ImageLoader.bytes;	 Catch:{ Throwable -> 0x058d }
            r5 = r5.length;	 Catch:{ Throwable -> 0x058d }
            r0 = r36;
            if (r5 < r0) goto L_0x07ff;
        L_0x07a5:
            r23 = org.telegram.messenger.ImageLoader.bytes;	 Catch:{ Throwable -> 0x058d }
        L_0x07a9:
            if (r23 != 0) goto L_0x07b4;
        L_0x07ab:
            r0 = r36;
            r0 = new byte[r0];	 Catch:{ Throwable -> 0x058d }
            r23 = r0;
            org.telegram.messenger.ImageLoader.bytes = r23;	 Catch:{ Throwable -> 0x058d }
        L_0x07b4:
            r5 = 0;
            r0 = r27;
            r1 = r23;
            r2 = r36;
            r0.readFully(r1, r5, r2);	 Catch:{ Throwable -> 0x058d }
            r27.close();	 Catch:{ Throwable -> 0x058d }
            r26 = 0;
            if (r52 == 0) goto L_0x0802;
        L_0x07c5:
            r5 = 0;
            r0 = r23;
            r1 = r36;
            r2 = r52;
            org.telegram.messenger.secretmedia.EncryptedFileInputStream.decryptBytesWithKeyFile(r0, r5, r1, r2);	 Catch:{ Throwable -> 0x058d }
            r5 = 0;
            r0 = r23;
            r1 = r36;
            r31 = org.telegram.messenger.Utilities.computeSHA256(r0, r5, r1);	 Catch:{ Throwable -> 0x058d }
            if (r51 == 0) goto L_0x07e4;
        L_0x07da:
            r0 = r31;
            r1 = r51;
            r5 = java.util.Arrays.equals(r0, r1);	 Catch:{ Throwable -> 0x058d }
            if (r5 != 0) goto L_0x07e6;
        L_0x07e4:
            r26 = 1;
        L_0x07e6:
            r5 = 0;
            r5 = r23[r5];	 Catch:{ Throwable -> 0x058d }
            r0 = r5 & 255;
            r40 = r0;
            r36 = r36 - r40;
        L_0x07ef:
            if (r26 != 0) goto L_0x0741;
        L_0x07f1:
            r0 = r23;
            r1 = r40;
            r2 = r36;
            r3 = r41;
            r4 = android.graphics.BitmapFactory.decodeByteArray(r0, r1, r2, r3);	 Catch:{ Throwable -> 0x058d }
            goto L_0x0741;
        L_0x07ff:
            r23 = 0;
            goto L_0x07a9;
        L_0x0802:
            if (r34 == 0) goto L_0x07ef;
        L_0x0804:
            r5 = 0;
            r0 = r56;
            r6 = r0.cacheImage;	 Catch:{ Throwable -> 0x058d }
            r6 = r6.encryptionKeyPath;	 Catch:{ Throwable -> 0x058d }
            r0 = r23;
            r1 = r36;
            org.telegram.messenger.secretmedia.EncryptedFileInputStream.decryptBytesWithKeyFile(r0, r5, r1, r6);	 Catch:{ Throwable -> 0x058d }
            goto L_0x07ef;
        L_0x0813:
            if (r34 == 0) goto L_0x0832;
        L_0x0815:
            r35 = new org.telegram.messenger.secretmedia.EncryptedFileInputStream;	 Catch:{ Throwable -> 0x058d }
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x058d }
            r5 = r5.encryptionKeyPath;	 Catch:{ Throwable -> 0x058d }
            r0 = r35;
            r1 = r21;
            r0.<init>(r1, r5);	 Catch:{ Throwable -> 0x058d }
        L_0x0824:
            r5 = 0;
            r0 = r35;
            r1 = r41;
            r4 = android.graphics.BitmapFactory.decodeStream(r0, r5, r1);	 Catch:{ Throwable -> 0x058d }
            r35.close();	 Catch:{ Throwable -> 0x058d }
            goto L_0x0741;
        L_0x0832:
            r35 = new java.io.FileInputStream;	 Catch:{ Throwable -> 0x058d }
            r0 = r35;
            r1 = r21;
            r0.<init>(r1);	 Catch:{ Throwable -> 0x058d }
            goto L_0x0824;
        L_0x083c:
            r17 = 0;
            r0 = r56;
            r5 = r0.cacheImage;	 Catch:{ Throwable -> 0x058d }
            r5 = r5.filter;	 Catch:{ Throwable -> 0x058d }
            if (r5 == 0) goto L_0x08ae;
        L_0x0846:
            r5 = r4.getWidth();	 Catch:{ Throwable -> 0x058d }
            r14 = (float) r5;	 Catch:{ Throwable -> 0x058d }
            r5 = r4.getHeight();	 Catch:{ Throwable -> 0x058d }
            r13 = (float) r5;	 Catch:{ Throwable -> 0x058d }
            r0 = r41;
            r5 = r0.inPurgeable;	 Catch:{ Throwable -> 0x058d }
            if (r5 != 0) goto L_0x087d;
        L_0x0856:
            r5 = 0;
            r5 = (r55 > r5 ? 1 : (r55 == r5 ? 0 : -1));
            if (r5 == 0) goto L_0x087d;
        L_0x085b:
            r5 = (r14 > r55 ? 1 : (r14 == r55 ? 0 : -1));
            if (r5 == 0) goto L_0x087d;
        L_0x085f:
            r5 = 1101004800; // 0x41a00000 float:20.0 double:5.439686476E-315;
            r5 = r5 + r55;
            r5 = (r14 > r5 ? 1 : (r14 == r5 ? 0 : -1));
            if (r5 <= 0) goto L_0x087d;
        L_0x0867:
            r49 = r14 / r55;
            r0 = r55;
            r5 = (int) r0;	 Catch:{ Throwable -> 0x058d }
            r6 = r13 / r49;
            r6 = (int) r6;	 Catch:{ Throwable -> 0x058d }
            r7 = 1;
            r50 = org.telegram.messenger.Bitmaps.createScaledBitmap(r4, r5, r6, r7);	 Catch:{ Throwable -> 0x058d }
            r0 = r50;
            if (r4 == r0) goto L_0x087d;
        L_0x0878:
            r4.recycle();	 Catch:{ Throwable -> 0x058d }
            r4 = r50;
        L_0x087d:
            if (r4 == 0) goto L_0x08ae;
        L_0x087f:
            if (r15 == 0) goto L_0x08ae;
        L_0x0881:
            r5 = 1120403456; // 0x42c80000 float:100.0 double:5.53552857E-315;
            r5 = (r13 > r5 ? 1 : (r13 == r5 ? 0 : -1));
            if (r5 >= 0) goto L_0x08ae;
        L_0x0887:
            r5 = 1120403456; // 0x42c80000 float:100.0 double:5.53552857E-315;
            r5 = (r14 > r5 ? 1 : (r14 == r5 ? 0 : -1));
            if (r5 >= 0) goto L_0x08ae;
        L_0x088d:
            r5 = r4.getConfig();	 Catch:{ Throwable -> 0x058d }
            r6 = android.graphics.Bitmap.Config.ARGB_8888;	 Catch:{ Throwable -> 0x058d }
            if (r5 != r6) goto L_0x08ac;
        L_0x0895:
            r5 = 3;
            r0 = r41;
            r6 = r0.inPurgeable;	 Catch:{ Throwable -> 0x058d }
            if (r6 == 0) goto L_0x08bb;
        L_0x089c:
            r6 = 0;
        L_0x089d:
            r7 = r4.getWidth();	 Catch:{ Throwable -> 0x058d }
            r8 = r4.getHeight();	 Catch:{ Throwable -> 0x058d }
            r9 = r4.getRowBytes();	 Catch:{ Throwable -> 0x058d }
            org.telegram.messenger.Utilities.blurBitmap(r4, r5, r6, r7, r8, r9);	 Catch:{ Throwable -> 0x058d }
        L_0x08ac:
            r17 = 1;
        L_0x08ae:
            if (r17 != 0) goto L_0x016d;
        L_0x08b0:
            r0 = r41;
            r5 = r0.inPurgeable;	 Catch:{ Throwable -> 0x058d }
            if (r5 == 0) goto L_0x016d;
        L_0x08b6:
            org.telegram.messenger.Utilities.pinBitmap(r4);	 Catch:{ Throwable -> 0x058d }
            goto L_0x016d;
        L_0x08bb:
            r6 = 1;
            goto L_0x089d;
        L_0x08bd:
            r5 = 0;
            goto L_0x0177;
        L_0x08c0:
            r5 = move-exception;
            r46 = r47;
            goto L_0x01b0;
        L_0x08c5:
            r25 = move-exception;
            r46 = r47;
            goto L_0x01a0;
        L_0x08ca:
            r4 = r33;
            goto L_0x057c;
        L_0x08ce:
            r4 = r33;
            goto L_0x0552;
        L_0x08d2:
            r4 = r33;
            goto L_0x024e;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.ImageLoader.CacheOutTask.run():void");
        }

        private void onPostExecute(BitmapDrawable bitmapDrawable) {
            AndroidUtilities.runOnUIThread(new ImageLoader$CacheOutTask$$Lambda$0(this, bitmapDrawable));
        }

        final /* synthetic */ void lambda$onPostExecute$1$ImageLoader$CacheOutTask(BitmapDrawable bitmapDrawable) {
            BitmapDrawable toSet = null;
            if (bitmapDrawable instanceof AnimatedFileDrawable) {
                toSet = bitmapDrawable;
            } else if (bitmapDrawable != null) {
                toSet = ImageLoader.this.memCache.get(this.cacheImage.key);
                if (toSet == null) {
                    ImageLoader.this.memCache.put(this.cacheImage.key, bitmapDrawable);
                    toSet = bitmapDrawable;
                } else {
                    bitmapDrawable.getBitmap().recycle();
                }
            }
            ImageLoader.this.imageLoadQueue.postRunnable(new ImageLoader$CacheOutTask$$Lambda$1(this, toSet));
        }

        final /* synthetic */ void lambda$null$0$ImageLoader$CacheOutTask(BitmapDrawable toSetFinal) {
            this.cacheImage.setImageAndClear(toSetFinal);
        }

        public void cancel() {
            synchronized (this.sync) {
                try {
                    this.isCancelled = true;
                    if (this.runningThread != null) {
                        this.runningThread.interrupt();
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    private class HttpFileTask extends AsyncTask<Void, Void, Boolean> {
        private boolean canRetry = true;
        private int currentAccount;
        private String ext;
        private RandomAccessFile fileOutputStream = null;
        private int fileSize;
        private long lastProgressTime;
        private File tempFile;
        private String url;

        public HttpFileTask(String url, File tempFile, String ext, int currentAccount) {
            this.url = url;
            this.tempFile = tempFile;
            this.ext = ext;
            this.currentAccount = currentAccount;
        }

        private void reportProgress(float progress) {
            long currentTime = System.currentTimeMillis();
            if (progress == 1.0f || this.lastProgressTime == 0 || this.lastProgressTime < currentTime - 500) {
                this.lastProgressTime = currentTime;
                Utilities.stageQueue.postRunnable(new ImageLoader$HttpFileTask$$Lambda$0(this, progress));
            }
        }

        final /* synthetic */ void lambda$reportProgress$1$ImageLoader$HttpFileTask(float progress) {
            ImageLoader.this.fileProgresses.put(this.url, Float.valueOf(progress));
            AndroidUtilities.runOnUIThread(new ImageLoader$HttpFileTask$$Lambda$1(this, progress));
        }

        final /* synthetic */ void lambda$null$0$ImageLoader$HttpFileTask(float progress) {
            NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.FileLoadProgressChanged, this.url, Float.valueOf(progress));
        }

        protected Boolean doInBackground(Void... voids) {
            InputStream httpConnectionStream = null;
            boolean done = false;
            URLConnection httpConnection = null;
            try {
                httpConnection = new URL(this.url).openConnection();
                httpConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A5297c Safari/602.1");
                httpConnection.setConnectTimeout(DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
                httpConnection.setReadTimeout(DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
                if (httpConnection instanceof HttpURLConnection) {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) httpConnection;
                    httpURLConnection.setInstanceFollowRedirects(true);
                    int status = httpURLConnection.getResponseCode();
                    if (status == 302 || status == 301 || status == 303) {
                        String newUrl = httpURLConnection.getHeaderField("Location");
                        String cookies = httpURLConnection.getHeaderField("Set-Cookie");
                        httpConnection = new URL(newUrl).openConnection();
                        httpConnection.setRequestProperty("Cookie", cookies);
                        httpConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A5297c Safari/602.1");
                    }
                }
                httpConnection.connect();
                httpConnectionStream = httpConnection.getInputStream();
                this.fileOutputStream = new RandomAccessFile(this.tempFile, "rws");
            } catch (Throwable e) {
                if (e instanceof SocketTimeoutException) {
                    if (ConnectionsManager.isNetworkOnline()) {
                        this.canRetry = false;
                    }
                } else if (e instanceof UnknownHostException) {
                    this.canRetry = false;
                } else if (e instanceof SocketException) {
                    if (e.getMessage() != null && e.getMessage().contains("ECONNRESET")) {
                        this.canRetry = false;
                    }
                } else if (e instanceof FileNotFoundException) {
                    this.canRetry = false;
                }
                FileLog.e(e);
            }
            if (this.canRetry) {
                if (httpConnection != null) {
                    try {
                        if (httpConnection instanceof HttpURLConnection) {
                            int code = ((HttpURLConnection) httpConnection).getResponseCode();
                            if (!(code == 200 || code == 202 || code == 304)) {
                                this.canRetry = false;
                            }
                        }
                    } catch (Exception e2) {
                        FileLog.e(e2);
                    }
                }
                if (httpConnection != null) {
                    try {
                        Map<String, List<String>> headerFields = httpConnection.getHeaderFields();
                        if (headerFields != null) {
                            List values = (List) headerFields.get("content-Length");
                            if (!(values == null || values.isEmpty())) {
                                String length = (String) values.get(0);
                                if (length != null) {
                                    this.fileSize = Utilities.parseInt(length).intValue();
                                }
                            }
                        }
                    } catch (Exception e22) {
                        FileLog.e(e22);
                    }
                }
                if (httpConnectionStream != null) {
                    try {
                        byte[] data = new byte[32768];
                        int totalLoaded = 0;
                        while (!isCancelled()) {
                            int read = httpConnectionStream.read(data);
                            if (read > 0) {
                                this.fileOutputStream.write(data, 0, read);
                                totalLoaded += read;
                                if (this.fileSize > 0) {
                                    reportProgress(((float) totalLoaded) / ((float) this.fileSize));
                                }
                            } else if (read == -1) {
                                done = true;
                                if (this.fileSize != 0) {
                                    reportProgress(1.0f);
                                }
                            }
                        }
                    } catch (Exception e222) {
                        FileLog.e(e222);
                    } catch (Throwable e3) {
                        FileLog.e(e3);
                    }
                }
                try {
                    if (this.fileOutputStream != null) {
                        this.fileOutputStream.close();
                        this.fileOutputStream = null;
                    }
                } catch (Throwable e32) {
                    FileLog.e(e32);
                }
                if (httpConnectionStream != null) {
                    try {
                        httpConnectionStream.close();
                    } catch (Throwable e322) {
                        FileLog.e(e322);
                    }
                }
            }
            return Boolean.valueOf(done);
        }

        protected void onPostExecute(Boolean result) {
            ImageLoader.this.runHttpFileLoadTasks(this, result.booleanValue() ? 2 : 1);
        }

        protected void onCancelled() {
            ImageLoader.this.runHttpFileLoadTasks(this, 2);
        }
    }

    private class HttpImageTask extends AsyncTask<Void, Void, Boolean> {
        private CacheImage cacheImage;
        private boolean canRetry = true;
        private RandomAccessFile fileOutputStream;
        private HttpURLConnection httpConnection;
        private int imageSize;
        private long lastProgressTime;

        public HttpImageTask(CacheImage cacheImage, int size) {
            this.cacheImage = cacheImage;
            this.imageSize = size;
        }

        private void reportProgress(float progress) {
            long currentTime = System.currentTimeMillis();
            if (progress == 1.0f || this.lastProgressTime == 0 || this.lastProgressTime < currentTime - 500) {
                this.lastProgressTime = currentTime;
                Utilities.stageQueue.postRunnable(new ImageLoader$HttpImageTask$$Lambda$0(this, progress));
            }
        }

        final /* synthetic */ void lambda$reportProgress$1$ImageLoader$HttpImageTask(float progress) {
            ImageLoader.this.fileProgresses.put(this.cacheImage.url, Float.valueOf(progress));
            AndroidUtilities.runOnUIThread(new ImageLoader$HttpImageTask$$Lambda$8(this, progress));
        }

        final /* synthetic */ void lambda$null$0$ImageLoader$HttpImageTask(float progress) {
            NotificationCenter.getInstance(this.cacheImage.currentAccount).postNotificationName(NotificationCenter.FileLoadProgressChanged, this.cacheImage.url, Float.valueOf(progress));
        }

        protected Boolean doInBackground(Void... voids) {
            InputStream httpConnectionStream = null;
            boolean done = false;
            if (!isCancelled()) {
                try {
                    if (this.cacheImage.httpUrl.startsWith("https://static-maps") || this.cacheImage.httpUrl.startsWith("https://maps.googleapis")) {
                        int provider = MessagesController.getInstance(this.cacheImage.currentAccount).mapProvider;
                        if (provider == 3 || provider == 4) {
                            WebFile webFile = (WebFile) ImageLoader.this.testWebFile.get(this.cacheImage.httpUrl);
                            if (webFile != null) {
                                TLRPC$TL_upload_getWebFile req = new TLRPC$TL_upload_getWebFile();
                                req.location = webFile.location;
                                req.offset = 0;
                                req.limit = 0;
                                ConnectionsManager.getInstance(this.cacheImage.currentAccount).sendRequest(req, ImageLoader$HttpImageTask$$Lambda$1.$instance);
                            }
                        }
                    }
                    this.httpConnection = (HttpURLConnection) new URL(this.cacheImage.httpUrl).openConnection();
                    this.httpConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A5297c Safari/602.1");
                    this.httpConnection.setConnectTimeout(DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
                    this.httpConnection.setReadTimeout(DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
                    this.httpConnection.setInstanceFollowRedirects(true);
                    if (!isCancelled()) {
                        this.httpConnection.connect();
                        httpConnectionStream = this.httpConnection.getInputStream();
                        this.fileOutputStream = new RandomAccessFile(this.cacheImage.tempFilePath, "rws");
                    }
                } catch (Throwable e) {
                    if (e instanceof SocketTimeoutException) {
                        if (ConnectionsManager.isNetworkOnline()) {
                            this.canRetry = false;
                        }
                    } else if (e instanceof UnknownHostException) {
                        this.canRetry = false;
                    } else if (e instanceof SocketException) {
                        if (e.getMessage() != null && e.getMessage().contains("ECONNRESET")) {
                            this.canRetry = false;
                        }
                    } else if (e instanceof FileNotFoundException) {
                        this.canRetry = false;
                    }
                    FileLog.e(e);
                }
            }
            if (!isCancelled()) {
                try {
                    if (this.httpConnection != null && (this.httpConnection instanceof HttpURLConnection)) {
                        int code = this.httpConnection.getResponseCode();
                        if (!(code == Callback.DEFAULT_DRAG_ANIMATION_DURATION || code == 202 || code == 304)) {
                            this.canRetry = false;
                        }
                    }
                } catch (Exception e2) {
                    FileLog.e(e2);
                }
                if (this.imageSize == 0 && this.httpConnection != null) {
                    try {
                        Map<String, List<String>> headerFields = this.httpConnection.getHeaderFields();
                        if (headerFields != null) {
                            List values = (List) headerFields.get("content-Length");
                            if (!(values == null || values.isEmpty())) {
                                String length = (String) values.get(0);
                                if (length != null) {
                                    this.imageSize = Utilities.parseInt(length).intValue();
                                }
                            }
                        }
                    } catch (Exception e22) {
                        FileLog.e(e22);
                    }
                }
                if (httpConnectionStream != null) {
                    try {
                        byte[] data = new byte[8192];
                        int totalLoaded = 0;
                        while (!isCancelled()) {
                            int read = httpConnectionStream.read(data);
                            if (read > 0) {
                                totalLoaded += read;
                                this.fileOutputStream.write(data, 0, read);
                                if (this.imageSize != 0) {
                                    reportProgress(((float) totalLoaded) / ((float) this.imageSize));
                                }
                            } else if (read == -1) {
                                done = true;
                                if (this.imageSize != 0) {
                                    reportProgress(1.0f);
                                }
                            }
                        }
                    } catch (Exception e222) {
                        FileLog.e(e222);
                    } catch (Throwable e3) {
                        FileLog.e(e3);
                    }
                }
            }
            try {
                if (this.fileOutputStream != null) {
                    this.fileOutputStream.close();
                    this.fileOutputStream = null;
                }
            } catch (Throwable e32) {
                FileLog.e(e32);
            }
            try {
                if (this.httpConnection != null) {
                    this.httpConnection.disconnect();
                }
            } catch (Throwable th) {
            }
            if (httpConnectionStream != null) {
                try {
                    httpConnectionStream.close();
                } catch (Throwable e322) {
                    FileLog.e(e322);
                }
            }
            if (!(!done || this.cacheImage.tempFilePath == null || TurboUtils.renameTo(this.cacheImage.tempFilePath, this.cacheImage.finalFilePath))) {
                this.cacheImage.finalFilePath = this.cacheImage.tempFilePath;
            }
            return Boolean.valueOf(done);
        }

        static final /* synthetic */ void lambda$doInBackground$2$ImageLoader$HttpImageTask(TLObject response, TLRPC$TL_error error) {
        }

        protected void onPostExecute(Boolean result) {
            if (result.booleanValue() || !this.canRetry) {
                ImageLoader.this.fileDidLoaded(this.cacheImage.url, this.cacheImage.finalFilePath, 0);
            } else {
                ImageLoader.this.httpFileLoadError(this.cacheImage.url);
            }
            Utilities.stageQueue.postRunnable(new ImageLoader$HttpImageTask$$Lambda$2(this, result));
            ImageLoader.this.imageLoadQueue.postRunnable(new ImageLoader$HttpImageTask$$Lambda$3(this));
        }

        final /* synthetic */ void lambda$onPostExecute$4$ImageLoader$HttpImageTask(Boolean result) {
            ImageLoader.this.fileProgresses.remove(this.cacheImage.url);
            AndroidUtilities.runOnUIThread(new ImageLoader$HttpImageTask$$Lambda$7(this, result));
        }

        final /* synthetic */ void lambda$null$3$ImageLoader$HttpImageTask(Boolean result) {
            if (result.booleanValue()) {
                NotificationCenter.getInstance(this.cacheImage.currentAccount).postNotificationName(NotificationCenter.FileDidLoaded, this.cacheImage.url);
                return;
            }
            NotificationCenter.getInstance(this.cacheImage.currentAccount).postNotificationName(NotificationCenter.FileDidFailedLoad, this.cacheImage.url, Integer.valueOf(2));
        }

        final /* synthetic */ void lambda$onPostExecute$5$ImageLoader$HttpImageTask() {
            ImageLoader.this.runHttpTasks(true);
        }

        final /* synthetic */ void lambda$onCancelled$6$ImageLoader$HttpImageTask() {
            ImageLoader.this.runHttpTasks(true);
        }

        protected void onCancelled() {
            ImageLoader.this.imageLoadQueue.postRunnable(new ImageLoader$HttpImageTask$$Lambda$4(this));
            Utilities.stageQueue.postRunnable(new ImageLoader$HttpImageTask$$Lambda$5(this));
        }

        final /* synthetic */ void lambda$onCancelled$8$ImageLoader$HttpImageTask() {
            ImageLoader.this.fileProgresses.remove(this.cacheImage.url);
            AndroidUtilities.runOnUIThread(new ImageLoader$HttpImageTask$$Lambda$6(this));
        }

        final /* synthetic */ void lambda$null$7$ImageLoader$HttpImageTask() {
            NotificationCenter.getInstance(this.cacheImage.currentAccount).postNotificationName(NotificationCenter.FileDidFailedLoad, this.cacheImage.url, Integer.valueOf(1));
        }
    }

    private class ThumbGenerateInfo {
        private int count;
        private TLRPC$FileLocation fileLocation;
        private String filter;

        private ThumbGenerateInfo() {
        }
    }

    private class ThumbGenerateTask implements Runnable {
        private String filter;
        private int mediaType;
        private File originalPath;
        private TLRPC$FileLocation thumbLocation;

        public ThumbGenerateTask(int type, File path, TLRPC$FileLocation location, String f) {
            this.mediaType = type;
            this.originalPath = path;
            this.thumbLocation = location;
            this.filter = f;
        }

        private void removeTask() {
            if (this.thumbLocation != null) {
                ImageLoader.this.imageLoadQueue.postRunnable(new ImageLoader$ThumbGenerateTask$$Lambda$0(this, FileLoader.getAttachFileName(this.thumbLocation)));
            }
        }

        final /* synthetic */ void lambda$removeTask$0$ImageLoader$ThumbGenerateTask(String name) {
            ThumbGenerateTask thumbGenerateTask = (ThumbGenerateTask) ImageLoader.this.thumbGenerateTasks.remove(name);
        }

        public void run() {
            try {
                if (this.thumbLocation == null) {
                    removeTask();
                    return;
                }
                String key = this.thumbLocation.volume_id + "_" + this.thumbLocation.local_id;
                File thumbFile = new File(FileLoader.getDirectory(4), "q_" + key + ".jpg");
                if (thumbFile.exists() || !this.originalPath.exists()) {
                    removeTask();
                    return;
                }
                int size = Math.min(180, Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) / 4);
                Bitmap originalBitmap = null;
                if (this.mediaType == 0) {
                    originalBitmap = ImageLoader.loadBitmap(this.originalPath.toString(), null, (float) size, (float) size, false);
                } else if (this.mediaType == 2) {
                    originalBitmap = ThumbnailUtils.createVideoThumbnail(this.originalPath.toString(), 1);
                } else if (this.mediaType == 3) {
                    String path = this.originalPath.toString().toLowerCase();
                    if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") || path.endsWith(".gif")) {
                        originalBitmap = ImageLoader.loadBitmap(path, null, (float) size, (float) size, false);
                    } else {
                        removeTask();
                        return;
                    }
                }
                if (originalBitmap == null) {
                    removeTask();
                    return;
                }
                int w = originalBitmap.getWidth();
                int h = originalBitmap.getHeight();
                if (w == 0 || h == 0) {
                    removeTask();
                    return;
                }
                float scaleFactor = Math.min(((float) w) / ((float) size), ((float) h) / ((float) size));
                Bitmap scaledBitmap = Bitmaps.createScaledBitmap(originalBitmap, (int) (((float) w) / scaleFactor), (int) (((float) h) / scaleFactor), true);
                if (scaledBitmap != originalBitmap) {
                    originalBitmap.recycle();
                    originalBitmap = scaledBitmap;
                }
                FileOutputStream stream = new FileOutputStream(thumbFile);
                originalBitmap.compress(CompressFormat.JPEG, 60, stream);
                stream.close();
                AndroidUtilities.runOnUIThread(new ImageLoader$ThumbGenerateTask$$Lambda$1(this, key, new BitmapDrawable(originalBitmap)));
            } catch (Exception e) {
                FileLog.e(e);
            } catch (Throwable e2) {
                FileLog.e(e2);
                removeTask();
            }
        }

        final /* synthetic */ void lambda$run$1$ImageLoader$ThumbGenerateTask(String key, BitmapDrawable bitmapDrawable) {
            removeTask();
            String kf = key;
            if (this.filter != null) {
                kf = kf + "@" + this.filter;
            }
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.messageThumbGenerated, bitmapDrawable, kf);
            ImageLoader.this.memCache.put(kf, bitmapDrawable);
        }
    }

    public static ImageLoader getInstance() {
        ImageLoader localInstance = Instance;
        if (localInstance == null) {
            synchronized (ImageLoader.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        ImageLoader localInstance2 = new ImageLoader();
                        try {
                            Instance = localInstance2;
                            localInstance = localInstance2;
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            localInstance = localInstance2;
                            throw th2;
                        }
                    }
                } catch (Throwable th3) {
                    th2 = th3;
                    throw th2;
                }
            }
        }
        return localInstance;
    }

    public ImageLoader() {
        this.thumbGeneratingQueue.setPriority(1);
        this.memCache = new LruCache((Math.min(15, ((ActivityManager) ApplicationLoader.applicationContext.getSystemService("activity")).getMemoryClass() / 7) * 1024) * 1024) {
            protected int sizeOf(String key, BitmapDrawable value) {
                return value.getBitmap().getByteCount();
            }

            protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue, BitmapDrawable newValue) {
                if (ImageLoader.this.ignoreRemoval == null || key == null || !ImageLoader.this.ignoreRemoval.equals(key)) {
                    Integer count = (Integer) ImageLoader.this.bitmapUseCounts.get(key);
                    if (count == null || count.intValue() == 0) {
                        Bitmap b = oldValue.getBitmap();
                        if (!b.isRecycled()) {
                            b.recycle();
                        }
                    }
                }
            }
        };
        SparseArray<File> mediaDirs = new SparseArray();
        File cachePath = AndroidUtilities.getCacheDir();
        if (!cachePath.isDirectory()) {
            try {
                cachePath.mkdirs();
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        try {
            new File(cachePath, ".nomedia").createNewFile();
        } catch (Exception e2) {
            FileLog.e(e2);
        }
        mediaDirs.put(4, cachePath);
        for (int a = 0; a < 3; a++) {
            final int currentAccount = a;
            FileLoader.getInstance(a).setDelegate(new FileLoaderDelegate() {
                public void fileUploadProgressChanged(String location, float progress, boolean isEncrypted) {
                    ImageLoader.this.fileProgresses.put(location, Float.valueOf(progress));
                    long currentTime = System.currentTimeMillis();
                    if (ImageLoader.this.lastProgressUpdateTime == 0 || ImageLoader.this.lastProgressUpdateTime < currentTime - 500) {
                        ImageLoader.this.lastProgressUpdateTime = currentTime;
                        AndroidUtilities.runOnUIThread(new ImageLoader$2$$Lambda$0(currentAccount, location, progress, isEncrypted));
                    }
                }

                public void fileDidUploaded(String location, TLRPC$InputFile inputFile, TLRPC$InputEncryptedFile inputEncryptedFile, byte[] key, byte[] iv, long totalFileSize) {
                    Utilities.stageQueue.postRunnable(new ImageLoader$2$$Lambda$1(this, currentAccount, location, inputFile, inputEncryptedFile, key, iv, totalFileSize));
                }

                final /* synthetic */ void lambda$fileDidUploaded$2$ImageLoader$2(int currentAccount, String location, TLRPC$InputFile inputFile, TLRPC$InputEncryptedFile inputEncryptedFile, byte[] key, byte[] iv, long totalFileSize) {
                    AndroidUtilities.runOnUIThread(new ImageLoader$2$$Lambda$7(currentAccount, location, inputFile, inputEncryptedFile, key, iv, totalFileSize));
                    ImageLoader.this.fileProgresses.remove(location);
                }

                public void fileDidFailedUpload(String location, boolean isEncrypted) {
                    Utilities.stageQueue.postRunnable(new ImageLoader$2$$Lambda$2(this, currentAccount, location, isEncrypted));
                }

                final /* synthetic */ void lambda$fileDidFailedUpload$4$ImageLoader$2(int currentAccount, String location, boolean isEncrypted) {
                    AndroidUtilities.runOnUIThread(new ImageLoader$2$$Lambda$6(currentAccount, location, isEncrypted));
                    ImageLoader.this.fileProgresses.remove(location);
                }

                public void fileDidLoaded(String location, File finalFile, int type) {
                    ImageLoader.this.fileProgresses.remove(location);
                    AndroidUtilities.runOnUIThread(new ImageLoader$2$$Lambda$3(this, finalFile, location, currentAccount, type));
                }

                final /* synthetic */ void lambda$fileDidLoaded$5$ImageLoader$2(File finalFile, String location, int currentAccount, int type) {
                    if (SharedConfig.saveToGallery && ImageLoader.this.telegramPath != null && finalFile != null && ((location.endsWith(".mp4") || location.endsWith(".jpg")) && finalFile.toString().startsWith(ImageLoader.this.telegramPath.toString()))) {
                        AndroidUtilities.addMediaToGallery(finalFile.toString());
                    }
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.FileDidLoaded, location);
                    ImageLoader.this.fileDidLoaded(location, finalFile, type);
                }

                public void fileDidFailedLoad(String location, int canceled) {
                    ImageLoader.this.fileProgresses.remove(location);
                    AndroidUtilities.runOnUIThread(new ImageLoader$2$$Lambda$4(this, location, canceled, currentAccount));
                }

                final /* synthetic */ void lambda$fileDidFailedLoad$6$ImageLoader$2(String location, int canceled, int currentAccount) {
                    ImageLoader.this.fileDidFailedLoad(location, canceled);
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.FileDidFailedLoad, location, Integer.valueOf(canceled));
                }

                public void fileLoadProgressChanged(String location, float progress) {
                    ImageLoader.this.fileProgresses.put(location, Float.valueOf(progress));
                    long currentTime = System.currentTimeMillis();
                    if (ImageLoader.this.lastProgressUpdateTime == 0 || ImageLoader.this.lastProgressUpdateTime < currentTime - 500) {
                        ImageLoader.this.lastProgressUpdateTime = currentTime;
                        AndroidUtilities.runOnUIThread(new ImageLoader$2$$Lambda$5(currentAccount, location, progress));
                    }
                }
            });
        }
        FileLoader.setMediaDirs(mediaDirs);
        BroadcastReceiver receiver = new C08063();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_BAD_REMOVAL");
        filter.addAction("android.intent.action.MEDIA_CHECKING");
        filter.addAction("android.intent.action.MEDIA_EJECT");
        filter.addAction("android.intent.action.MEDIA_MOUNTED");
        filter.addAction("android.intent.action.MEDIA_NOFS");
        filter.addAction("android.intent.action.MEDIA_REMOVED");
        filter.addAction("android.intent.action.MEDIA_SHARED");
        filter.addAction("android.intent.action.MEDIA_UNMOUNTABLE");
        filter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        filter.addDataScheme("file");
        try {
            ApplicationLoader.applicationContext.registerReceiver(receiver, filter);
        } catch (Throwable th) {
        }
        checkMediaPaths();
    }

    public void checkMediaPaths() {
        this.cacheOutQueue.postRunnable(new ImageLoader$$Lambda$0(this));
    }

    final /* synthetic */ void lambda$checkMediaPaths$1$ImageLoader() {
        AndroidUtilities.runOnUIThread(new ImageLoader$$Lambda$10(createMediaPaths()));
    }

    public void addTestWebFile(String url, WebFile webFile) {
        if (url != null && webFile != null) {
            this.testWebFile.put(url, webFile);
        }
    }

    public void removeTestWebFile(String url) {
        if (url != null) {
            this.testWebFile.remove(url);
        }
    }

    public SparseArray<File> createMediaPaths() {
        SparseArray<File> mediaDirs = new SparseArray();
        File cachePath = AndroidUtilities.getCacheDir();
        if (!cachePath.isDirectory()) {
            try {
                cachePath.mkdirs();
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        try {
            new File(cachePath, ".nomedia").createNewFile();
        } catch (Exception e2) {
            FileLog.e(e2);
        }
        mediaDirs.put(4, cachePath);
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("cache path = " + cachePath);
        }
        try {
            if (TurboUtils.isExternalStorageMounted()) {
                this.telegramPath = new File(TurboUtils.getExternalStorageDirectory(), TurboConfig$Storage.folderName);
                this.telegramPath.mkdirs();
                if (this.telegramPath.isDirectory()) {
                    try {
                        File imagePath = new File(this.telegramPath, "Telegram Images");
                        imagePath.mkdir();
                        if (imagePath.isDirectory() && canMoveFiles(cachePath, imagePath, 0)) {
                            mediaDirs.put(0, imagePath);
                            if (BuildVars.LOGS_ENABLED) {
                                FileLog.d("image path = " + imagePath);
                            }
                        }
                    } catch (Exception e22) {
                        FileLog.e(e22);
                    }
                    try {
                        File videoPath = new File(this.telegramPath, "Telegram Video");
                        videoPath.mkdir();
                        if (videoPath.isDirectory() && canMoveFiles(cachePath, videoPath, 2)) {
                            mediaDirs.put(2, videoPath);
                            if (BuildVars.LOGS_ENABLED) {
                                FileLog.d("video path = " + videoPath);
                            }
                        }
                    } catch (Exception e222) {
                        FileLog.e(e222);
                    }
                    try {
                        File audioPath = new File(this.telegramPath, "Telegram Audio");
                        audioPath.mkdir();
                        if (audioPath.isDirectory() && canMoveFiles(cachePath, audioPath, 1)) {
                            new File(audioPath, ".nomedia").createNewFile();
                            mediaDirs.put(1, audioPath);
                            if (BuildVars.LOGS_ENABLED) {
                                FileLog.d("audio path = " + audioPath);
                            }
                        }
                    } catch (Exception e2222) {
                        FileLog.e(e2222);
                    }
                    try {
                        File documentPath = new File(this.telegramPath, "Telegram Documents");
                        documentPath.mkdir();
                        if (documentPath.isDirectory() && canMoveFiles(cachePath, documentPath, 3)) {
                            new File(documentPath, ".nomedia").createNewFile();
                            mediaDirs.put(3, documentPath);
                            if (BuildVars.LOGS_ENABLED) {
                                FileLog.d("documents path = " + documentPath);
                            }
                        }
                    } catch (Exception e22222) {
                        FileLog.e(e22222);
                    }
                }
            } else if (BuildVars.LOGS_ENABLED) {
                FileLog.d("this Android can't rename files");
            }
            SharedConfig.checkSaveToGalleryFiles();
        } catch (Exception e222222) {
            FileLog.e(e222222);
        }
        return mediaDirs;
    }

    private boolean canMoveFiles(File from, File to, int type) {
        File srcFile;
        Exception e;
        Throwable th;
        RandomAccessFile file = null;
        File srcFile2 = null;
        File dstFile = null;
        if (type == 0) {
            try {
                srcFile = new File(from, "000000000_999999_temp.jpg");
                try {
                    dstFile = new File(to, "000000000_999999.jpg");
                    srcFile2 = srcFile;
                } catch (Exception e2) {
                    e = e2;
                    srcFile2 = srcFile;
                    try {
                        FileLog.e(e);
                        if (file != null) {
                            try {
                                file.close();
                            } catch (Exception e3) {
                                FileLog.e(e3);
                            }
                        }
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        if (file != null) {
                            try {
                                file.close();
                            } catch (Exception e32) {
                                FileLog.e(e32);
                            }
                        }
                        throw th;
                    }
                }
            } catch (Exception e4) {
                e32 = e4;
                FileLog.e(e32);
                if (file != null) {
                    file.close();
                }
                return false;
            }
        } else if (type == 3) {
            srcFile = new File(from, "000000000_999999_temp.doc");
            dstFile = new File(to, "000000000_999999.doc");
            srcFile2 = srcFile;
        } else if (type == 1) {
            srcFile = new File(from, "000000000_999999_temp.ogg");
            dstFile = new File(to, "000000000_999999.ogg");
            srcFile2 = srcFile;
        } else if (type == 2) {
            srcFile = new File(from, "000000000_999999_temp.mp4");
            dstFile = new File(to, "000000000_999999.mp4");
            srcFile2 = srcFile;
        }
        byte[] buffer = new byte[1024];
        srcFile2.createNewFile();
        RandomAccessFile file2 = new RandomAccessFile(srcFile2, "rws");
        try {
            file2.write(buffer);
            file2.close();
            file = null;
            boolean canRename = TurboUtils.renameTo(srcFile2, dstFile);
            srcFile2.delete();
            dstFile.delete();
            if (!canRename) {
                if (file != null) {
                    try {
                        file.close();
                    } catch (Exception e322) {
                        FileLog.e(e322);
                    }
                }
                return false;
            } else if (file == null) {
                return true;
            } else {
                try {
                    file.close();
                    return true;
                } catch (Exception e3222) {
                    FileLog.e(e3222);
                    return true;
                }
            }
        } catch (Exception e5) {
            e3222 = e5;
            file = file2;
            FileLog.e(e3222);
            if (file != null) {
                file.close();
            }
            return false;
        } catch (Throwable th3) {
            th = th3;
            file = file2;
            if (file != null) {
                file.close();
            }
            throw th;
        }
    }

    public Float getFileProgress(String location) {
        if (location == null) {
            return null;
        }
        return (Float) this.fileProgresses.get(location);
    }

    public String getReplacedKey(String oldKey) {
        return (String) this.replacedBitmaps.get(oldKey);
    }

    private void performReplace(String oldKey, String newKey) {
        BitmapDrawable b = this.memCache.get(oldKey);
        this.replacedBitmaps.put(oldKey, newKey);
        if (b != null) {
            BitmapDrawable oldBitmap = this.memCache.get(newKey);
            boolean dontChange = false;
            if (!(oldBitmap == null || oldBitmap.getBitmap() == null || b.getBitmap() == null)) {
                Bitmap oldBitmapObject = oldBitmap.getBitmap();
                Bitmap newBitmapObject = b.getBitmap();
                if (oldBitmapObject.getWidth() > newBitmapObject.getWidth() || oldBitmapObject.getHeight() > newBitmapObject.getHeight()) {
                    dontChange = true;
                }
            }
            if (dontChange) {
                this.memCache.remove(oldKey);
            } else {
                this.ignoreRemoval = oldKey;
                this.memCache.remove(oldKey);
                this.memCache.put(newKey, b);
                this.ignoreRemoval = null;
            }
        }
        Integer val = (Integer) this.bitmapUseCounts.get(oldKey);
        if (val != null) {
            this.bitmapUseCounts.put(newKey, val);
            this.bitmapUseCounts.remove(oldKey);
        }
    }

    public void incrementUseCount(String key) {
        Integer count = (Integer) this.bitmapUseCounts.get(key);
        if (count == null) {
            this.bitmapUseCounts.put(key, Integer.valueOf(1));
        } else {
            this.bitmapUseCounts.put(key, Integer.valueOf(count.intValue() + 1));
        }
    }

    public boolean decrementUseCount(String key) {
        Integer count = (Integer) this.bitmapUseCounts.get(key);
        if (count == null) {
            return true;
        }
        if (count.intValue() == 1) {
            this.bitmapUseCounts.remove(key);
            return true;
        }
        this.bitmapUseCounts.put(key, Integer.valueOf(count.intValue() - 1));
        return false;
    }

    public void removeImage(String key) {
        this.bitmapUseCounts.remove(key);
        this.memCache.remove(key);
    }

    public boolean isInCache(String key) {
        return this.memCache.get(key) != null;
    }

    public void clearMemory() {
        this.memCache.evictAll();
    }

    private void removeFromWaitingForThumb(int TAG) {
        String location = (String) this.waitingForQualityThumbByTag.get(TAG);
        if (location != null) {
            ThumbGenerateInfo info = (ThumbGenerateInfo) this.waitingForQualityThumb.get(location);
            if (info != null) {
                info.count = info.count - 1;
                if (info.count == 0) {
                    this.waitingForQualityThumb.remove(location);
                }
            }
            this.waitingForQualityThumbByTag.remove(TAG);
        }
    }

    public void cancelLoadingForImageReceiver(ImageReceiver imageReceiver, int type) {
        if (imageReceiver != null) {
            this.imageLoadQueue.postRunnable(new ImageLoader$$Lambda$1(this, type, imageReceiver));
        }
    }

    final /* synthetic */ void lambda$cancelLoadingForImageReceiver$2$ImageLoader(int type, ImageReceiver imageReceiver) {
        int start = 0;
        int count = 2;
        if (type == 1) {
            count = 1;
        } else if (type == 2) {
            start = 1;
        }
        int a = start;
        while (a < count) {
            int TAG = imageReceiver.getTag(a == 0);
            if (a == 0) {
                removeFromWaitingForThumb(TAG);
            }
            if (TAG != 0) {
                CacheImage ei = (CacheImage) this.imageLoadingByTag.get(TAG);
                if (ei != null) {
                    ei.removeImageReceiver(imageReceiver);
                }
            }
            a++;
        }
    }

    public BitmapDrawable getImageFromMemory(String key) {
        return this.memCache.get(key);
    }

    public BitmapDrawable getImageFromMemory(TLObject fileLocation, String httpUrl, String filter) {
        if (fileLocation == null && httpUrl == null) {
            return null;
        }
        String key = null;
        if (httpUrl != null) {
            key = Utilities.MD5(httpUrl);
        } else if (fileLocation instanceof TLRPC$FileLocation) {
            TLRPC$FileLocation location = (TLRPC$FileLocation) fileLocation;
            key = location.volume_id + "_" + location.local_id;
        } else if (fileLocation instanceof TLRPC$Document) {
            TLRPC$Document location2 = (TLRPC$Document) fileLocation;
            if (location2.version == 0) {
                key = location2.dc_id + "_" + location2.id;
            } else {
                key = location2.dc_id + "_" + location2.id + "_" + location2.version;
            }
        } else if (fileLocation instanceof SecureDocument) {
            SecureDocument location3 = (SecureDocument) fileLocation;
            key = location3.secureFile.dc_id + "_" + location3.secureFile.id;
        } else if (fileLocation instanceof WebFile) {
            key = Utilities.MD5(((WebFile) fileLocation).url);
        }
        if (filter != null) {
            key = key + "@" + filter;
        }
        return this.memCache.get(key);
    }

    private void replaceImageInCacheInternal(String oldKey, String newKey, TLRPC$FileLocation newLocation) {
        ArrayList<String> arr = this.memCache.getFilterKeys(oldKey);
        if (arr != null) {
            for (int a = 0; a < arr.size(); a++) {
                String filter = (String) arr.get(a);
                performReplace(oldKey + "@" + filter, newKey + "@" + filter);
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didReplacedPhotoInMemCache, oldK, newK, newLocation);
            }
            return;
        }
        performReplace(oldKey, newKey);
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didReplacedPhotoInMemCache, oldKey, newKey, newLocation);
    }

    public void replaceImageInCache(String oldKey, String newKey, TLRPC$FileLocation newLocation, boolean post) {
        if (post) {
            AndroidUtilities.runOnUIThread(new ImageLoader$$Lambda$2(this, oldKey, newKey, newLocation));
        } else {
            replaceImageInCacheInternal(oldKey, newKey, newLocation);
        }
    }

    final /* synthetic */ void lambda$replaceImageInCache$3$ImageLoader(String oldKey, String newKey, TLRPC$FileLocation newLocation) {
        replaceImageInCacheInternal(oldKey, newKey, newLocation);
    }

    public void putImageToCache(BitmapDrawable bitmap, String key) {
        this.memCache.put(key, bitmap);
    }

    private void generateThumb(int mediaType, File originalPath, TLRPC$FileLocation thumbLocation, String filter) {
        if ((mediaType == 0 || mediaType == 2 || mediaType == 3) && originalPath != null && thumbLocation != null) {
            if (((ThumbGenerateTask) this.thumbGenerateTasks.get(FileLoader.getAttachFileName(thumbLocation))) == null) {
                this.thumbGeneratingQueue.postRunnable(new ThumbGenerateTask(mediaType, originalPath, thumbLocation, filter));
            }
        }
    }

    public void cancelForceLoadingForImageReceiver(ImageReceiver imageReceiver) {
        if (imageReceiver != null) {
            String key = imageReceiver.getKey();
            if (key != null) {
                this.imageLoadQueue.postRunnable(new ImageLoader$$Lambda$3(this, key));
            }
        }
    }

    final /* synthetic */ void lambda$cancelForceLoadingForImageReceiver$4$ImageLoader(String key) {
        Integer num = (Integer) this.forceLoadingImages.remove(key);
    }

    private void createLoadOperationForImageReceiver(ImageReceiver imageReceiver, String key, String url, String ext, TLObject imageLocation, String httpLocation, String filter, int size, int cacheType, int thumb) {
        if (imageReceiver != null && url != null && key != null) {
            int TAG = imageReceiver.getTag(thumb != 0);
            if (TAG == 0) {
                TAG = this.lastImageNum;
                imageReceiver.setTag(TAG, thumb != 0);
                this.lastImageNum++;
                if (this.lastImageNum == Integer.MAX_VALUE) {
                    this.lastImageNum = 0;
                }
            }
            this.imageLoadQueue.postRunnable(new ImageLoader$$Lambda$4(this, thumb, url, key, TAG, imageReceiver, filter, httpLocation, imageReceiver.isNeedsQualityThumb(), imageReceiver.getParentMessageObject(), imageLocation, imageReceiver.isShouldGenerateQualityThumb(), cacheType, size, ext, imageReceiver.getcurrentAccount()));
        }
    }

    final /* synthetic */ void lambda$createLoadOperationForImageReceiver$5$ImageLoader(int thumb, String url, String key, int finalTag, ImageReceiver imageReceiver, String filter, String httpLocation, boolean finalIsNeedsQualityThumb, MessageObject parentMessageObject, TLObject imageLocation, boolean shouldGenerateQualityThumb, int cacheType, int size, String ext, int currentAccount) {
        boolean added = false;
        if (thumb != 2) {
            CacheImage alreadyLoadingUrl = (CacheImage) this.imageLoadingByUrl.get(url);
            CacheImage alreadyLoadingCache = (CacheImage) this.imageLoadingByKeys.get(key);
            CacheImage alreadyLoadingImage = (CacheImage) this.imageLoadingByTag.get(finalTag);
            if (alreadyLoadingImage != null) {
                if (alreadyLoadingImage == alreadyLoadingCache) {
                    added = true;
                } else if (alreadyLoadingImage == alreadyLoadingUrl) {
                    if (alreadyLoadingCache == null) {
                        alreadyLoadingImage.replaceImageReceiver(imageReceiver, key, filter, thumb != 0);
                    }
                    added = true;
                } else {
                    alreadyLoadingImage.removeImageReceiver(imageReceiver);
                }
            }
            if (!(added || alreadyLoadingCache == null)) {
                alreadyLoadingCache.addImageReceiver(imageReceiver, key, filter, thumb != 0);
                added = true;
            }
            if (!(added || alreadyLoadingUrl == null)) {
                alreadyLoadingUrl.addImageReceiver(imageReceiver, key, filter, thumb != 0);
                added = true;
            }
        }
        if (!added) {
            boolean onlyCache = false;
            File cacheFile = null;
            boolean cacheFileExists = false;
            if (httpLocation != null) {
                if (!httpLocation.startsWith("http")) {
                    onlyCache = true;
                    int idx;
                    if (httpLocation.startsWith("thumb://")) {
                        idx = httpLocation.indexOf(":", 8);
                        if (idx >= 0) {
                            cacheFile = new File(httpLocation.substring(idx + 1));
                        }
                    } else if (httpLocation.startsWith("vthumb://")) {
                        idx = httpLocation.indexOf(":", 9);
                        if (idx >= 0) {
                            cacheFile = new File(httpLocation.substring(idx + 1));
                        }
                    } else {
                        cacheFile = new File(httpLocation);
                    }
                }
            } else if (thumb != 0) {
                if (finalIsNeedsQualityThumb) {
                    cacheFile = new File(FileLoader.getDirectory(4), "q_" + url);
                    if (cacheFile.exists()) {
                        cacheFileExists = true;
                    } else {
                        cacheFile = null;
                    }
                }
                if (parentMessageObject != null) {
                    File attachPath = null;
                    if (parentMessageObject.messageOwner.attachPath != null && parentMessageObject.messageOwner.attachPath.length() > 0) {
                        attachPath = new File(parentMessageObject.messageOwner.attachPath);
                        if (!attachPath.exists()) {
                            attachPath = null;
                        }
                    }
                    if (attachPath == null) {
                        attachPath = FileLoader.getPathToMessage(parentMessageObject.messageOwner);
                    }
                    if (finalIsNeedsQualityThumb && cacheFile == null) {
                        String location = parentMessageObject.getFileName();
                        ThumbGenerateInfo info = (ThumbGenerateInfo) this.waitingForQualityThumb.get(location);
                        if (info == null) {
                            ThumbGenerateInfo thumbGenerateInfo = new ThumbGenerateInfo();
                            thumbGenerateInfo.fileLocation = (TLRPC$FileLocation) imageLocation;
                            thumbGenerateInfo.filter = filter;
                            this.waitingForQualityThumb.put(location, thumbGenerateInfo);
                        }
                        info.count = info.count + 1;
                        this.waitingForQualityThumbByTag.put(finalTag, location);
                    }
                    if (attachPath.exists() && shouldGenerateQualityThumb) {
                        generateThumb(parentMessageObject.getFileType(), attachPath, (TLRPC$FileLocation) imageLocation, filter);
                    }
                }
            }
            if (thumb != 2) {
                boolean isEncrypted = (imageLocation instanceof TLRPC$TL_documentEncrypted) || (imageLocation instanceof TLRPC$TL_fileEncryptedLocation);
                CacheImage img = new CacheImage();
                if (httpLocation != null && !httpLocation.startsWith("vthumb") && !httpLocation.startsWith("thumb")) {
                    String trueExt = getHttpUrlExtension(httpLocation, "jpg");
                    if (trueExt.equals("mp4") || trueExt.equals("gif")) {
                        img.animatedFile = true;
                    }
                } else if (((imageLocation instanceof WebFile) && MessageObject.isGifDocument((WebFile) imageLocation)) || ((imageLocation instanceof TLRPC$Document) && (MessageObject.isGifDocument((TLRPC$Document) imageLocation) || MessageObject.isRoundVideoDocument((TLRPC$Document) imageLocation)))) {
                    img.animatedFile = true;
                }
                if (cacheFile == null) {
                    if (imageLocation instanceof SecureDocument) {
                        img.secureDocument = (SecureDocument) imageLocation;
                        onlyCache = img.secureDocument.secureFile.dc_id == Integer.MIN_VALUE;
                        cacheFile = new File(FileLoader.getDirectory(4), url);
                    } else if (cacheType != 0 || size <= 0 || httpLocation != null || isEncrypted) {
                        cacheFile = new File(FileLoader.getDirectory(4), url);
                        if (cacheFile.exists()) {
                            cacheFileExists = true;
                        } else if (cacheType == 2) {
                            cacheFile = new File(FileLoader.getDirectory(4), url + ".enc");
                        }
                    } else {
                        cacheFile = imageLocation instanceof TLRPC$Document ? MessageObject.isVideoDocument((TLRPC$Document) imageLocation) ? new File(FileLoader.getDirectory(2), url) : new File(FileLoader.getDirectory(3), url) : imageLocation instanceof WebFile ? new File(FileLoader.getDirectory(3), url) : new File(FileLoader.getDirectory(0), url);
                    }
                }
                img.selfThumb = thumb != 0;
                img.key = key;
                img.filter = filter;
                img.httpUrl = httpLocation;
                img.ext = ext;
                img.currentAccount = currentAccount;
                if (cacheType == 2) {
                    img.encryptionKeyPath = new File(FileLoader.getInternalCacheDir(), url + ".enc.key");
                }
                img.addImageReceiver(imageReceiver, key, filter, thumb != 0);
                if (onlyCache || cacheFileExists || cacheFile.exists()) {
                    img.finalFilePath = cacheFile;
                    img.cacheTask = new CacheOutTask(img);
                    this.imageLoadingByKeys.put(key, img);
                    if (thumb != 0) {
                        this.cacheThumbOutQueue.postRunnable(img.cacheTask);
                        return;
                    } else {
                        this.cacheOutQueue.postRunnable(img.cacheTask);
                        return;
                    }
                }
                img.url = url;
                img.location = imageLocation;
                this.imageLoadingByUrl.put(url, img);
                if (httpLocation == null) {
                    if (imageLocation instanceof TLRPC$FileLocation) {
                        TLRPC$FileLocation location2 = (TLRPC$FileLocation) imageLocation;
                        int localCacheType = cacheType;
                        if (localCacheType == 0 && (size <= 0 || location2.key != null)) {
                            localCacheType = 1;
                        }
                        FileLoader.getInstance(currentAccount).loadFile(location2, ext, size, localCacheType);
                    } else if (imageLocation instanceof TLRPC$Document) {
                        FileLoader.getInstance(currentAccount).loadFile((TLRPC$Document) imageLocation, true, cacheType);
                    } else if (imageLocation instanceof SecureDocument) {
                        FileLoader.getInstance(currentAccount).loadFile((SecureDocument) imageLocation, true);
                    } else if (imageLocation instanceof WebFile) {
                        FileLoader.getInstance(currentAccount).loadFile((WebFile) imageLocation, true, cacheType);
                    }
                    if (imageReceiver.isForceLoding()) {
                        this.forceLoadingImages.put(img.key, Integer.valueOf(0));
                        return;
                    }
                    return;
                }
                img.tempFilePath = new File(FileLoader.getDirectory(4), Utilities.MD5(httpLocation) + "_temp.jpg");
                img.finalFilePath = cacheFile;
                img.httpTask = new HttpImageTask(img, size);
                this.httpTasks.add(img.httpTask);
                runHttpTasks(false);
            }
        }
    }

    public void loadImageForImageReceiver(ImageReceiver imageReceiver) {
        if (imageReceiver != null) {
            BitmapDrawable bitmapDrawable;
            boolean imageSet = false;
            String key = imageReceiver.getKey();
            if (key != null) {
                bitmapDrawable = this.memCache.get(key);
                if (bitmapDrawable != null) {
                    cancelLoadingForImageReceiver(imageReceiver, 0);
                    imageReceiver.setImageBitmapByKey(bitmapDrawable, key, false, true);
                    imageSet = true;
                    if (!imageReceiver.isForcePreview()) {
                        return;
                    }
                }
            }
            boolean thumbSet = false;
            String thumbKey = imageReceiver.getThumbKey();
            if (thumbKey != null) {
                bitmapDrawable = this.memCache.get(thumbKey);
                if (bitmapDrawable != null) {
                    imageReceiver.setImageBitmapByKey(bitmapDrawable, thumbKey, true, true);
                    cancelLoadingForImageReceiver(imageReceiver, 1);
                    if (!imageSet || !imageReceiver.isForcePreview()) {
                        thumbSet = true;
                    } else {
                        return;
                    }
                }
            }
            TLObject thumbLocation = imageReceiver.getThumbLocation();
            TLObject imageLocation = imageReceiver.getImageLocation();
            String httpLocation = imageReceiver.getHttpImageLocation();
            boolean saveImageToCache = false;
            String url = null;
            String thumbUrl = null;
            key = null;
            thumbKey = null;
            String ext = imageReceiver.getExt();
            if (ext == null) {
                ext = "jpg";
            }
            if (httpLocation != null) {
                key = Utilities.MD5(httpLocation);
                url = key + "." + getHttpUrlExtension(httpLocation, "jpg");
            } else if (imageLocation != null) {
                if (imageLocation instanceof TLRPC$FileLocation) {
                    TLRPC$FileLocation location = (TLRPC$FileLocation) imageLocation;
                    key = location.volume_id + "_" + location.local_id;
                    url = key + "." + ext;
                    if (!(imageReceiver.getExt() == null && location.key == null && (location.volume_id != -2147483648L || location.local_id >= 0))) {
                        saveImageToCache = true;
                    }
                } else if (imageLocation instanceof WebFile) {
                    WebFile document = (WebFile) imageLocation;
                    String defaultExt = FileLoader.getExtensionByMime(document.mime_type);
                    key = Utilities.MD5(document.url);
                    url = key + "." + getHttpUrlExtension(document.url, defaultExt);
                } else if (imageLocation instanceof SecureDocument) {
                    SecureDocument document2 = (SecureDocument) imageLocation;
                    key = document2.secureFile.dc_id + "_" + document2.secureFile.id;
                    url = key + "." + ext;
                    if (null != null) {
                        thumbUrl = null + "." + ext;
                    }
                } else if (imageLocation instanceof TLRPC$Document) {
                    TLRPC$Document document3 = (TLRPC$Document) imageLocation;
                    if (document3.id != 0 && document3.dc_id != 0) {
                        if (document3.version == 0) {
                            key = document3.dc_id + "_" + document3.id;
                        } else {
                            key = document3.dc_id + "_" + document3.id + "_" + document3.version;
                        }
                        String docExt = FileLoader.getDocumentFileName(document3);
                        if (docExt != null) {
                            int idx = docExt.lastIndexOf(46);
                            if (idx != -1) {
                                docExt = docExt.substring(idx);
                                if (docExt.length() <= 1) {
                                    if (document3.mime_type == null && document3.mime_type.equals(MimeTypes.VIDEO_MP4)) {
                                        docExt = ".mp4";
                                    } else {
                                        docExt = "";
                                    }
                                }
                                url = key + docExt;
                                if (null != null) {
                                    thumbUrl = null + "." + ext;
                                }
                                if (!MessageObject.isGifDocument(document3) || MessageObject.isRoundVideoDocument((TLRPC$Document) imageLocation)) {
                                    saveImageToCache = false;
                                } else {
                                    saveImageToCache = true;
                                }
                            }
                        }
                        docExt = "";
                        if (docExt.length() <= 1) {
                            if (document3.mime_type == null) {
                            }
                            docExt = "";
                        }
                        url = key + docExt;
                        if (null != null) {
                            thumbUrl = null + "." + ext;
                        }
                        if (MessageObject.isGifDocument(document3)) {
                        }
                        saveImageToCache = false;
                    } else {
                        return;
                    }
                }
                if (imageLocation == thumbLocation) {
                    imageLocation = null;
                    key = null;
                    url = null;
                }
            }
            if (thumbLocation != null) {
                thumbKey = thumbLocation.volume_id + "_" + thumbLocation.local_id;
                thumbUrl = thumbKey + "." + ext;
            }
            String filter = imageReceiver.getFilter();
            String thumbFilter = imageReceiver.getThumbFilter();
            if (!(key == null || filter == null)) {
                key = key + "@" + filter;
            }
            if (!(thumbKey == null || thumbFilter == null)) {
                thumbKey = thumbKey + "@" + thumbFilter;
            }
            if (httpLocation != null) {
                createLoadOperationForImageReceiver(imageReceiver, thumbKey, thumbUrl, ext, thumbLocation, null, thumbFilter, 0, 1, thumbSet ? 2 : 1);
                createLoadOperationForImageReceiver(imageReceiver, key, url, ext, null, httpLocation, filter, 0, 1, 0);
                return;
            }
            int i;
            int cacheType = imageReceiver.getCacheType();
            if (cacheType == 0 && saveImageToCache) {
                cacheType = 1;
            }
            if (cacheType == 0) {
                i = 1;
            } else {
                i = cacheType;
            }
            createLoadOperationForImageReceiver(imageReceiver, thumbKey, thumbUrl, ext, thumbLocation, null, thumbFilter, 0, i, thumbSet ? 2 : 1);
            createLoadOperationForImageReceiver(imageReceiver, key, url, ext, imageLocation, null, filter, imageReceiver.getSize(), cacheType, 0);
        }
    }

    private void httpFileLoadError(String location) {
        this.imageLoadQueue.postRunnable(new ImageLoader$$Lambda$5(this, location));
    }

    final /* synthetic */ void lambda$httpFileLoadError$6$ImageLoader(String location) {
        CacheImage img = (CacheImage) this.imageLoadingByUrl.get(location);
        if (img != null) {
            HttpImageTask oldTask = img.httpTask;
            img.httpTask = new HttpImageTask(oldTask.cacheImage, oldTask.imageSize);
            this.httpTasks.add(img.httpTask);
            runHttpTasks(false);
        }
    }

    private void fileDidLoaded(String location, File finalFile, int type) {
        this.imageLoadQueue.postRunnable(new ImageLoader$$Lambda$6(this, location, type, finalFile));
    }

    final /* synthetic */ void lambda$fileDidLoaded$7$ImageLoader(String location, int type, File finalFile) {
        ThumbGenerateInfo info = (ThumbGenerateInfo) this.waitingForQualityThumb.get(location);
        if (info != null) {
            generateThumb(type, finalFile, info.fileLocation, info.filter);
            this.waitingForQualityThumb.remove(location);
        }
        CacheImage img = (CacheImage) this.imageLoadingByUrl.get(location);
        if (img != null) {
            int a;
            this.imageLoadingByUrl.remove(location);
            ArrayList<CacheOutTask> tasks = new ArrayList();
            for (a = 0; a < img.imageReceiverArray.size(); a++) {
                String key = (String) img.keys.get(a);
                String filter = (String) img.filters.get(a);
                Boolean thumb = (Boolean) img.thumbs.get(a);
                ImageReceiver imageReceiver = (ImageReceiver) img.imageReceiverArray.get(a);
                CacheImage cacheImage = (CacheImage) this.imageLoadingByKeys.get(key);
                if (cacheImage == null) {
                    cacheImage = new CacheImage();
                    cacheImage.secureDocument = img.secureDocument;
                    cacheImage.currentAccount = img.currentAccount;
                    cacheImage.finalFilePath = finalFile;
                    cacheImage.key = key;
                    cacheImage.httpUrl = img.httpUrl;
                    cacheImage.selfThumb = thumb.booleanValue();
                    cacheImage.ext = img.ext;
                    cacheImage.encryptionKeyPath = img.encryptionKeyPath;
                    cacheImage.cacheTask = new CacheOutTask(cacheImage);
                    cacheImage.filter = filter;
                    cacheImage.animatedFile = img.animatedFile;
                    this.imageLoadingByKeys.put(key, cacheImage);
                    tasks.add(cacheImage.cacheTask);
                }
                cacheImage.addImageReceiver(imageReceiver, key, filter, thumb.booleanValue());
            }
            for (a = 0; a < tasks.size(); a++) {
                CacheOutTask task = (CacheOutTask) tasks.get(a);
                if (task.cacheImage.selfThumb) {
                    this.cacheThumbOutQueue.postRunnable(task);
                } else {
                    this.cacheOutQueue.postRunnable(task);
                }
            }
        }
    }

    private void fileDidFailedLoad(String location, int canceled) {
        if (canceled != 1) {
            this.imageLoadQueue.postRunnable(new ImageLoader$$Lambda$7(this, location));
        }
    }

    final /* synthetic */ void lambda$fileDidFailedLoad$8$ImageLoader(String location) {
        CacheImage img = (CacheImage) this.imageLoadingByUrl.get(location);
        if (img != null) {
            img.setImageAndClear(null);
        }
    }

    private void runHttpTasks(boolean complete) {
        if (complete) {
            this.currentHttpTasksCount--;
        }
        while (this.currentHttpTasksCount < 4 && !this.httpTasks.isEmpty()) {
            ((HttpImageTask) this.httpTasks.poll()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[]{null, null, null});
            this.currentHttpTasksCount++;
        }
    }

    public boolean isLoadingHttpFile(String url) {
        return this.httpFileLoadTasksByKeys.containsKey(url);
    }

    public void loadHttpFile(String url, String defaultExt, int currentAccount) {
        if (url != null && url.length() != 0 && !this.httpFileLoadTasksByKeys.containsKey(url)) {
            String ext = getHttpUrlExtension(url, defaultExt);
            File file = new File(FileLoader.getDirectory(4), Utilities.MD5(url) + "_temp." + ext);
            file.delete();
            HttpFileTask task = new HttpFileTask(url, file, ext, currentAccount);
            this.httpFileLoadTasks.add(task);
            this.httpFileLoadTasksByKeys.put(url, task);
            runHttpFileLoadTasks(null, 0);
        }
    }

    public void cancelLoadHttpFile(String url) {
        HttpFileTask task = (HttpFileTask) this.httpFileLoadTasksByKeys.get(url);
        if (task != null) {
            task.cancel(true);
            this.httpFileLoadTasksByKeys.remove(url);
            this.httpFileLoadTasks.remove(task);
        }
        Runnable runnable = (Runnable) this.retryHttpsTasks.get(url);
        if (runnable != null) {
            AndroidUtilities.cancelRunOnUIThread(runnable);
        }
        runHttpFileLoadTasks(null, 0);
    }

    private void runHttpFileLoadTasks(HttpFileTask oldTask, int reason) {
        AndroidUtilities.runOnUIThread(new ImageLoader$$Lambda$8(this, oldTask, reason));
    }

    final /* synthetic */ void lambda$runHttpFileLoadTasks$10$ImageLoader(HttpFileTask oldTask, int reason) {
        if (oldTask != null) {
            this.currentHttpFileLoadTasksCount--;
        }
        if (oldTask != null) {
            if (reason == 1) {
                if (oldTask.canRetry) {
                    Runnable runnable = new ImageLoader$$Lambda$9(this, new HttpFileTask(oldTask.url, oldTask.tempFile, oldTask.ext, oldTask.currentAccount));
                    this.retryHttpsTasks.put(oldTask.url, runnable);
                    AndroidUtilities.runOnUIThread(runnable, 1000);
                } else {
                    this.httpFileLoadTasksByKeys.remove(oldTask.url);
                    NotificationCenter.getInstance(oldTask.currentAccount).postNotificationName(NotificationCenter.httpFileDidFailedLoad, oldTask.url, Integer.valueOf(0));
                }
            } else if (reason == 2) {
                this.httpFileLoadTasksByKeys.remove(oldTask.url);
                File file = new File(FileLoader.getDirectory(4), Utilities.MD5(oldTask.url) + "." + oldTask.ext);
                String result = oldTask.tempFile.renameTo(file) ? file.toString() : oldTask.tempFile.toString();
                NotificationCenter.getInstance(oldTask.currentAccount).postNotificationName(NotificationCenter.httpFileDidLoaded, oldTask.url, result);
            }
        }
        while (this.currentHttpFileLoadTasksCount < 2 && !this.httpFileLoadTasks.isEmpty()) {
            ((HttpFileTask) this.httpFileLoadTasks.poll()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[]{null, null, null});
            this.currentHttpFileLoadTasksCount++;
        }
    }

    final /* synthetic */ void lambda$null$9$ImageLoader(HttpFileTask newTask) {
        this.httpFileLoadTasks.add(newTask);
        runHttpFileLoadTasks(null, 0);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static android.graphics.Bitmap loadBitmap(java.lang.String r23, android.net.Uri r24, float r25, float r26, boolean r27) {
        /*
        r8 = new android.graphics.BitmapFactory$Options;
        r8.<init>();
        r2 = 1;
        r8.inJustDecodeBounds = r2;
        r15 = 0;
        if (r23 != 0) goto L_0x0025;
    L_0x000b:
        if (r24 == 0) goto L_0x0025;
    L_0x000d:
        r2 = r24.getScheme();
        if (r2 == 0) goto L_0x0025;
    L_0x0013:
        r14 = 0;
        r2 = r24.getScheme();
        r3 = "file";
        r2 = r2.contains(r3);
        if (r2 == 0) goto L_0x0061;
    L_0x0021:
        r23 = r24.getPath();
    L_0x0025:
        if (r23 == 0) goto L_0x006b;
    L_0x0027:
        r0 = r23;
        android.graphics.BitmapFactory.decodeFile(r0, r8);
    L_0x002c:
        r2 = r8.outWidth;
        r0 = (float) r2;
        r20 = r0;
        r2 = r8.outHeight;
        r0 = (float) r2;
        r19 = r0;
        if (r27 == 0) goto L_0x0094;
    L_0x0038:
        r2 = r20 / r25;
        r3 = r19 / r26;
        r22 = java.lang.Math.max(r2, r3);
    L_0x0040:
        r2 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r2 = (r22 > r2 ? 1 : (r22 == r2 ? 0 : -1));
        if (r2 >= 0) goto L_0x0048;
    L_0x0046:
        r22 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
    L_0x0048:
        r2 = 0;
        r8.inJustDecodeBounds = r2;
        r0 = r22;
        r2 = (int) r0;
        r8.inSampleSize = r2;
        r2 = r8.inSampleSize;
        r2 = r2 % 2;
        if (r2 == 0) goto L_0x00a1;
    L_0x0056:
        r21 = 1;
    L_0x0058:
        r2 = r21 * 2;
        r3 = r8.inSampleSize;
        if (r2 >= r3) goto L_0x009d;
    L_0x005e:
        r21 = r21 * 2;
        goto L_0x0058;
    L_0x0061:
        r23 = org.telegram.messenger.AndroidUtilities.getPath(r24);	 Catch:{ Throwable -> 0x0066 }
        goto L_0x0025;
    L_0x0066:
        r9 = move-exception;
        org.telegram.messenger.FileLog.e(r9);
        goto L_0x0025;
    L_0x006b:
        if (r24 == 0) goto L_0x002c;
    L_0x006d:
        r11 = 0;
        r2 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Throwable -> 0x008e }
        r2 = r2.getContentResolver();	 Catch:{ Throwable -> 0x008e }
        r0 = r24;
        r15 = r2.openInputStream(r0);	 Catch:{ Throwable -> 0x008e }
        r2 = 0;
        android.graphics.BitmapFactory.decodeStream(r15, r2, r8);	 Catch:{ Throwable -> 0x008e }
        r15.close();	 Catch:{ Throwable -> 0x008e }
        r2 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Throwable -> 0x008e }
        r2 = r2.getContentResolver();	 Catch:{ Throwable -> 0x008e }
        r0 = r24;
        r15 = r2.openInputStream(r0);	 Catch:{ Throwable -> 0x008e }
        goto L_0x002c;
    L_0x008e:
        r9 = move-exception;
        org.telegram.messenger.FileLog.e(r9);
        r1 = 0;
    L_0x0093:
        return r1;
    L_0x0094:
        r2 = r20 / r25;
        r3 = r19 / r26;
        r22 = java.lang.Math.min(r2, r3);
        goto L_0x0040;
    L_0x009d:
        r0 = r21;
        r8.inSampleSize = r0;
    L_0x00a1:
        r2 = android.os.Build.VERSION.SDK_INT;
        r3 = 21;
        if (r2 >= r3) goto L_0x00f4;
    L_0x00a7:
        r2 = 1;
    L_0x00a8:
        r8.inPurgeable = r2;
        r13 = 0;
        if (r23 == 0) goto L_0x00f6;
    L_0x00ad:
        r13 = r23;
    L_0x00af:
        r6 = 0;
        if (r13 == 0) goto L_0x00c9;
    L_0x00b2:
        r12 = new android.support.media.ExifInterface;	 Catch:{ Throwable -> 0x01a3 }
        r12.<init>(r13);	 Catch:{ Throwable -> 0x01a3 }
        r2 = "Orientation";
        r3 = 1;
        r18 = r12.getAttributeInt(r2, r3);	 Catch:{ Throwable -> 0x01a3 }
        r16 = new android.graphics.Matrix;	 Catch:{ Throwable -> 0x01a3 }
        r16.<init>();	 Catch:{ Throwable -> 0x01a3 }
        switch(r18) {
            case 3: goto L_0x0109;
            case 4: goto L_0x00c7;
            case 5: goto L_0x00c7;
            case 6: goto L_0x00fd;
            case 7: goto L_0x00c7;
            case 8: goto L_0x0111;
            default: goto L_0x00c7;
        };
    L_0x00c7:
        r6 = r16;
    L_0x00c9:
        r1 = 0;
        if (r23 == 0) goto L_0x0157;
    L_0x00cc:
        r0 = r23;
        r1 = android.graphics.BitmapFactory.decodeFile(r0, r8);	 Catch:{ Throwable -> 0x0119 }
        if (r1 == 0) goto L_0x0093;
    L_0x00d4:
        r2 = r8.inPurgeable;	 Catch:{ Throwable -> 0x0119 }
        if (r2 == 0) goto L_0x00db;
    L_0x00d8:
        org.telegram.messenger.Utilities.pinBitmap(r1);	 Catch:{ Throwable -> 0x0119 }
    L_0x00db:
        r2 = 0;
        r3 = 0;
        r4 = r1.getWidth();	 Catch:{ Throwable -> 0x0119 }
        r5 = r1.getHeight();	 Catch:{ Throwable -> 0x0119 }
        r7 = 1;
        r17 = org.telegram.messenger.Bitmaps.createBitmap(r1, r2, r3, r4, r5, r6, r7);	 Catch:{ Throwable -> 0x0119 }
        r0 = r17;
        if (r0 == r1) goto L_0x0093;
    L_0x00ee:
        r1.recycle();	 Catch:{ Throwable -> 0x0119 }
        r1 = r17;
        goto L_0x0093;
    L_0x00f4:
        r2 = 0;
        goto L_0x00a8;
    L_0x00f6:
        if (r24 == 0) goto L_0x00af;
    L_0x00f8:
        r13 = org.telegram.messenger.AndroidUtilities.getPath(r24);
        goto L_0x00af;
    L_0x00fd:
        r2 = 1119092736; // 0x42b40000 float:90.0 double:5.529052754E-315;
        r0 = r16;
        r0.postRotate(r2);	 Catch:{ Throwable -> 0x0105 }
        goto L_0x00c7;
    L_0x0105:
        r2 = move-exception;
        r6 = r16;
        goto L_0x00c9;
    L_0x0109:
        r2 = 1127481344; // 0x43340000 float:180.0 double:5.570497984E-315;
        r0 = r16;
        r0.postRotate(r2);	 Catch:{ Throwable -> 0x0105 }
        goto L_0x00c7;
    L_0x0111:
        r2 = 1132920832; // 0x43870000 float:270.0 double:5.597372625E-315;
        r0 = r16;
        r0.postRotate(r2);	 Catch:{ Throwable -> 0x0105 }
        goto L_0x00c7;
    L_0x0119:
        r9 = move-exception;
        org.telegram.messenger.FileLog.e(r9);
        r2 = getInstance();
        r2.clearMemory();
        if (r1 != 0) goto L_0x0135;
    L_0x0126:
        r0 = r23;
        r1 = android.graphics.BitmapFactory.decodeFile(r0, r8);	 Catch:{ Throwable -> 0x0151 }
        if (r1 == 0) goto L_0x0135;
    L_0x012e:
        r2 = r8.inPurgeable;	 Catch:{ Throwable -> 0x0151 }
        if (r2 == 0) goto L_0x0135;
    L_0x0132:
        org.telegram.messenger.Utilities.pinBitmap(r1);	 Catch:{ Throwable -> 0x0151 }
    L_0x0135:
        if (r1 == 0) goto L_0x0093;
    L_0x0137:
        r2 = 0;
        r3 = 0;
        r4 = r1.getWidth();	 Catch:{ Throwable -> 0x0151 }
        r5 = r1.getHeight();	 Catch:{ Throwable -> 0x0151 }
        r7 = 1;
        r17 = org.telegram.messenger.Bitmaps.createBitmap(r1, r2, r3, r4, r5, r6, r7);	 Catch:{ Throwable -> 0x0151 }
        r0 = r17;
        if (r0 == r1) goto L_0x0093;
    L_0x014a:
        r1.recycle();	 Catch:{ Throwable -> 0x0151 }
        r1 = r17;
        goto L_0x0093;
    L_0x0151:
        r10 = move-exception;
        org.telegram.messenger.FileLog.e(r10);
        goto L_0x0093;
    L_0x0157:
        if (r24 == 0) goto L_0x0093;
    L_0x0159:
        r2 = 0;
        r1 = android.graphics.BitmapFactory.decodeStream(r15, r2, r8);	 Catch:{ Throwable -> 0x018a }
        if (r1 == 0) goto L_0x017f;
    L_0x0160:
        r2 = r8.inPurgeable;	 Catch:{ Throwable -> 0x018a }
        if (r2 == 0) goto L_0x0167;
    L_0x0164:
        org.telegram.messenger.Utilities.pinBitmap(r1);	 Catch:{ Throwable -> 0x018a }
    L_0x0167:
        r2 = 0;
        r3 = 0;
        r4 = r1.getWidth();	 Catch:{ Throwable -> 0x018a }
        r5 = r1.getHeight();	 Catch:{ Throwable -> 0x018a }
        r7 = 1;
        r17 = org.telegram.messenger.Bitmaps.createBitmap(r1, r2, r3, r4, r5, r6, r7);	 Catch:{ Throwable -> 0x018a }
        r0 = r17;
        if (r0 == r1) goto L_0x017f;
    L_0x017a:
        r1.recycle();	 Catch:{ Throwable -> 0x018a }
        r1 = r17;
    L_0x017f:
        r15.close();	 Catch:{ Throwable -> 0x0184 }
        goto L_0x0093;
    L_0x0184:
        r9 = move-exception;
        org.telegram.messenger.FileLog.e(r9);
        goto L_0x0093;
    L_0x018a:
        r9 = move-exception;
        org.telegram.messenger.FileLog.e(r9);	 Catch:{ all -> 0x0199 }
        r15.close();	 Catch:{ Throwable -> 0x0193 }
        goto L_0x0093;
    L_0x0193:
        r9 = move-exception;
        org.telegram.messenger.FileLog.e(r9);
        goto L_0x0093;
    L_0x0199:
        r2 = move-exception;
        r15.close();	 Catch:{ Throwable -> 0x019e }
    L_0x019d:
        throw r2;
    L_0x019e:
        r9 = move-exception;
        org.telegram.messenger.FileLog.e(r9);
        goto L_0x019d;
    L_0x01a3:
        r2 = move-exception;
        goto L_0x00c9;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.ImageLoader.loadBitmap(java.lang.String, android.net.Uri, float, float, boolean):android.graphics.Bitmap");
    }

    public static void fillPhotoSizeWithBytes(TLRPC$PhotoSize photoSize) {
        if (photoSize != null && photoSize.bytes == null) {
            try {
                RandomAccessFile f = new RandomAccessFile(FileLoader.getPathToAttach(photoSize, true), "r");
                if (((int) f.length()) < 20000) {
                    photoSize.bytes = new byte[((int) f.length())];
                    f.readFully(photoSize.bytes, 0, photoSize.bytes.length);
                }
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
    }

    private static TLRPC$PhotoSize scaleAndSaveImageInternal(Bitmap bitmap, int w, int h, float photoW, float photoH, float scaleFactor, int quality, boolean cache, boolean scaleAnyway) throws Exception {
        Bitmap scaledBitmap;
        if (scaleFactor > 1.0f || scaleAnyway) {
            scaledBitmap = Bitmaps.createScaledBitmap(bitmap, w, h, true);
        } else {
            scaledBitmap = bitmap;
        }
        TLRPC$TL_fileLocation location = new TLRPC$TL_fileLocation();
        location.volume_id = -2147483648L;
        location.dc_id = Integer.MIN_VALUE;
        location.local_id = SharedConfig.getLastLocalId();
        TLRPC$PhotoSize size = new TLRPC$TL_photoSize();
        size.location = location;
        size.f794w = scaledBitmap.getWidth();
        size.f793h = scaledBitmap.getHeight();
        if (size.f794w <= 100 && size.f793h <= 100) {
            size.type = "s";
        } else if (size.f794w <= 320 && size.f793h <= 320) {
            size.type = "m";
        } else if (size.f794w <= 800 && size.f793h <= 800) {
            size.type = "x";
        } else if (size.f794w > 1280 || size.f793h > 1280) {
            size.type = "w";
        } else {
            size.type = "y";
        }
        FileOutputStream stream = new FileOutputStream(new File(FileLoader.getDirectory(4), location.volume_id + "_" + location.local_id + ".jpg"));
        scaledBitmap.compress(CompressFormat.JPEG, quality, stream);
        if (cache) {
            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
            scaledBitmap.compress(CompressFormat.JPEG, quality, stream2);
            size.bytes = stream2.toByteArray();
            size.size = size.bytes.length;
            stream2.close();
        } else {
            size.size = (int) stream.getChannel().size();
        }
        stream.close();
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle();
        }
        return size;
    }

    public static TLRPC$PhotoSize scaleAndSaveImage(Bitmap bitmap, float maxWidth, float maxHeight, int quality, boolean cache) {
        return scaleAndSaveImage(bitmap, maxWidth, maxHeight, quality, cache, 0, 0);
    }

    public static TLRPC$PhotoSize scaleAndSaveImage(Bitmap bitmap, float maxWidth, float maxHeight, int quality, boolean cache, int minWidth, int minHeight) {
        if (bitmap == null) {
            return null;
        }
        float photoW = (float) bitmap.getWidth();
        float photoH = (float) bitmap.getHeight();
        if (photoW == 0.0f || photoH == 0.0f) {
            return null;
        }
        boolean scaleAnyway = false;
        float scaleFactor = Math.max(photoW / maxWidth, photoH / maxHeight);
        if (!(minWidth == 0 || minHeight == 0 || (photoW >= ((float) minWidth) && photoH >= ((float) minHeight)))) {
            if (photoW < ((float) minWidth) && photoH > ((float) minHeight)) {
                scaleFactor = photoW / ((float) minWidth);
            } else if (photoW <= ((float) minWidth) || photoH >= ((float) minHeight)) {
                scaleFactor = Math.max(photoW / ((float) minWidth), photoH / ((float) minHeight));
            } else {
                scaleFactor = photoH / ((float) minHeight);
            }
            scaleAnyway = true;
        }
        int w = (int) (photoW / scaleFactor);
        int h = (int) (photoH / scaleFactor);
        if (h == 0 || w == 0) {
            return null;
        }
        try {
            return scaleAndSaveImageInternal(bitmap, w, h, photoW, photoH, scaleFactor, quality, cache, scaleAnyway);
        } catch (Throwable e2) {
            FileLog.e(e2);
            return null;
        }
    }

    public static String getHttpUrlExtension(String url, String defaultExt) {
        String ext = null;
        String last = Uri.parse(url).getLastPathSegment();
        if (!TextUtils.isEmpty(last) && last.length() > 1) {
            url = last;
        }
        int idx = url.lastIndexOf(46);
        if (idx != -1) {
            ext = url.substring(idx + 1);
        }
        if (ext == null || ext.length() == 0 || ext.length() > 4) {
            return defaultExt;
        }
        return ext;
    }

    public static void saveMessageThumbs(TLRPC$Message message) {
        int count;
        int a;
        TLObject photoSize = null;
        TLObject size;
        if (message.media instanceof TLRPC$TL_messageMediaPhoto) {
            count = message.media.photo.sizes.size();
            for (a = 0; a < count; a++) {
                size = (TLRPC$PhotoSize) message.media.photo.sizes.get(a);
                if (size instanceof TLRPC$TL_photoCachedSize) {
                    photoSize = size;
                    break;
                }
            }
        } else if (message.media instanceof TLRPC$TL_messageMediaDocument) {
            if (message.media.document.thumb instanceof TLRPC$TL_photoCachedSize) {
                photoSize = message.media.document.thumb;
            }
        } else if ((message.media instanceof TLRPC$TL_messageMediaWebPage) && message.media.webpage.photo != null) {
            count = message.media.webpage.photo.sizes.size();
            for (a = 0; a < count; a++) {
                size = (TLRPC$PhotoSize) message.media.webpage.photo.sizes.get(a);
                if (size instanceof TLRPC$TL_photoCachedSize) {
                    photoSize = size;
                    break;
                }
            }
        }
        if (photoSize != null && photoSize.bytes != null && photoSize.bytes.length != 0) {
            if (photoSize.location instanceof TLRPC$TL_fileLocationUnavailable) {
                photoSize.location = new TLRPC$TL_fileLocation();
                photoSize.location.volume_id = -2147483648L;
                photoSize.location.dc_id = Integer.MIN_VALUE;
                photoSize.location.local_id = SharedConfig.getLastLocalId();
            }
            File file = FileLoader.getPathToAttach(photoSize, true);
            boolean isEncrypted = false;
            if (MessageObject.shouldEncryptPhotoOrVideo(message)) {
                isEncrypted = true;
                file = new File(file.getAbsolutePath() + ".enc");
            }
            if (!file.exists()) {
                if (isEncrypted) {
                    try {
                        RandomAccessFile keyFile = new RandomAccessFile(new File(FileLoader.getInternalCacheDir(), file.getName() + ".key"), "rws");
                        long len = keyFile.length();
                        byte[] encryptKey = new byte[32];
                        byte[] encryptIv = new byte[16];
                        if (len <= 0 || len % 48 != 0) {
                            Utilities.random.nextBytes(encryptKey);
                            Utilities.random.nextBytes(encryptIv);
                            keyFile.write(encryptKey);
                            keyFile.write(encryptIv);
                        } else {
                            keyFile.read(encryptKey, 0, 32);
                            keyFile.read(encryptIv, 0, 16);
                        }
                        keyFile.close();
                        Utilities.aesCtrDecryptionByteArray(photoSize.bytes, encryptKey, encryptIv, 0, photoSize.bytes.length, 0);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rws");
                randomAccessFile.write(photoSize.bytes);
                randomAccessFile.close();
            }
            TLRPC$PhotoSize newPhotoSize = new TLRPC$TL_photoSize();
            newPhotoSize.w = photoSize.f794w;
            newPhotoSize.h = photoSize.f793h;
            newPhotoSize.location = photoSize.location;
            newPhotoSize.size = photoSize.size;
            newPhotoSize.type = photoSize.type;
            if (message.media instanceof TLRPC$TL_messageMediaPhoto) {
                count = message.media.photo.sizes.size();
                for (a = 0; a < count; a++) {
                    if (message.media.photo.sizes.get(a) instanceof TLRPC$TL_photoCachedSize) {
                        message.media.photo.sizes.set(a, newPhotoSize);
                        return;
                    }
                }
            } else if (message.media instanceof TLRPC$TL_messageMediaDocument) {
                message.media.document.thumb = newPhotoSize;
            } else if (message.media instanceof TLRPC$TL_messageMediaWebPage) {
                count = message.media.webpage.photo.sizes.size();
                for (a = 0; a < count; a++) {
                    if (message.media.webpage.photo.sizes.get(a) instanceof TLRPC$TL_photoCachedSize) {
                        message.media.webpage.photo.sizes.set(a, newPhotoSize);
                        return;
                    }
                }
            }
        }
    }

    public static void saveMessagesThumbs(ArrayList<TLRPC$Message> messages) {
        if (messages != null && !messages.isEmpty()) {
            for (int a = 0; a < messages.size(); a++) {
                saveMessageThumbs((TLRPC$Message) messages.get(a));
            }
        }
    }
}
