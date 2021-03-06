package com.google.android.gms.internal.ads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.util.Pair;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class zzcz {
    private static final String TAG = zzcz.class.getSimpleName();
    private volatile boolean zzqt = false;
    protected Context zzrt;
    private ExecutorService zzru;
    private DexClassLoader zzrv;
    private zzck zzrw;
    private byte[] zzrx;
    private volatile AdvertisingIdClient zzry = null;
    private Future zzrz = null;
    private boolean zzsa;
    private volatile zzba zzsb = null;
    private Future zzsc = null;
    private zzcc zzsd;
    private boolean zzse = false;
    private boolean zzsf = false;
    private Map<Pair<String, String>, zzeg> zzsg;
    private boolean zzsh = false;
    private boolean zzsi = true;
    private boolean zzsj = false;

    final class zza extends BroadcastReceiver {
        private final /* synthetic */ zzcz zzsl;

        private zza(zzcz zzcz) {
            this.zzsl = zzcz;
        }

        public final void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                this.zzsl.zzsi = true;
            } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                this.zzsl.zzsi = false;
            }
        }
    }

    private zzcz(Context context) {
        boolean z = true;
        Context applicationContext = context.getApplicationContext();
        if (applicationContext == null) {
            z = false;
        }
        this.zzsa = z;
        if (this.zzsa) {
            context = applicationContext;
        }
        this.zzrt = context;
        this.zzsg = new HashMap();
    }

    public static zzcz zza(Context context, String str, String str2, boolean z) {
        boolean z2 = true;
        zzcz zzcz = new zzcz(context);
        try {
            File file;
            zzcz.zzru = Executors.newCachedThreadPool(new zzda());
            zzcz.zzqt = z;
            if (z) {
                zzcz.zzrz = zzcz.zzru.submit(new zzdb(zzcz));
            }
            zzcz.zzru.execute(new zzdd(zzcz));
            try {
                GoogleApiAvailabilityLight instance = GoogleApiAvailabilityLight.getInstance();
                zzcz.zzse = instance.getApkVersion(zzcz.zzrt) > 0;
                if (instance.isGooglePlayServicesAvailable(zzcz.zzrt) != 0) {
                    z2 = false;
                }
                zzcz.zzsf = z2;
            } catch (Throwable th) {
            }
            zzcz.zza(0, true);
            if (zzdg.isMainThread()) {
                if (((Boolean) zzkb.zzik().zzd(zznk.zzbaz)).booleanValue()) {
                    throw new IllegalStateException("Task Context initialization must not be called from the UI thread.");
                }
            }
            zzcz.zzrw = new zzck(null);
            zzcz.zzrx = zzcz.zzrw.zzl(str);
            File cacheDir = zzcz.zzrt.getCacheDir();
            if (cacheDir == null) {
                cacheDir = zzcz.zzrt.getDir("dex", 0);
                if (cacheDir == null) {
                    throw new zzcw();
                }
            }
            File file2 = cacheDir;
            String str3 = "1521499837408";
            file = new File(String.format("%s/%s.jar", new Object[]{file2, str3}));
            if (!file.exists()) {
                byte[] zza = zzcz.zzrw.zza(zzcz.zzrx, str2);
                file.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(zza, 0, zza.length);
                fileOutputStream.close();
            }
            zzcz.zzb(file2, str3);
            zzcz.zzrv = new DexClassLoader(file.getAbsolutePath(), file2.getAbsolutePath(), null, zzcz.zzrt.getClassLoader());
            zzb(file);
            zzcz.zza(file2, str3);
            zzm(String.format("%s/%s.dex", new Object[]{file2, str3}));
            if (!zzcz.zzsj) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.USER_PRESENT");
                intentFilter.addAction("android.intent.action.SCREEN_OFF");
                zzcz.zzrt.registerReceiver(new zza(), intentFilter);
                zzcz.zzsj = true;
            }
            zzcz.zzsd = new zzcc(zzcz);
            zzcz.zzsh = true;
        } catch (Throwable e) {
            throw new zzcw(e);
        } catch (Throwable e2) {
            throw new zzcw(e2);
        } catch (Throwable e22) {
            throw new zzcw(e22);
        } catch (Throwable e222) {
            throw new zzcw(e222);
        } catch (Throwable e2222) {
            throw new zzcw(e2222);
        } catch (zzcw e3) {
        } catch (Throwable th2) {
            zzb(file);
            zzcz.zza(file2, str3);
            zzm(String.format("%s/%s.dex", new Object[]{file2, str3}));
        }
        return zzcz;
    }

    private final void zza(File file, String str) {
        FileOutputStream fileOutputStream;
        Throwable th;
        FileOutputStream fileOutputStream2;
        File file2 = new File(String.format("%s/%s.tmp", new Object[]{file, str}));
        if (!file2.exists()) {
            File file3 = new File(String.format("%s/%s.dex", new Object[]{file, str}));
            if (file3.exists()) {
                long length = file3.length();
                if (length > 0) {
                    byte[] bArr = new byte[((int) length)];
                    FileInputStream fileInputStream;
                    try {
                        fileInputStream = new FileInputStream(file3);
                        try {
                            if (fileInputStream.read(bArr) <= 0) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e) {
                                }
                                zzb(file3);
                                return;
                            }
                            zzbfi zzbe = new zzbe();
                            zzbe.zzgs = VERSION.SDK.getBytes();
                            zzbe.zzgr = str.getBytes();
                            bArr = this.zzrw.zzb(this.zzrx, bArr).getBytes();
                            zzbe.data = bArr;
                            zzbe.zzgq = zzbk.zzb(bArr);
                            file2.createNewFile();
                            fileOutputStream = new FileOutputStream(file2);
                            try {
                                byte[] zzb = zzbfi.zzb(zzbe);
                                fileOutputStream.write(zzb, 0, zzb.length);
                                fileOutputStream.close();
                                try {
                                    fileInputStream.close();
                                } catch (IOException e2) {
                                }
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e3) {
                                }
                                zzb(file3);
                            } catch (IOException e4) {
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e5) {
                                    }
                                }
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.close();
                                    } catch (IOException e6) {
                                    }
                                }
                                zzb(file3);
                            } catch (NoSuchAlgorithmException e7) {
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                if (fileOutputStream != null) {
                                    fileOutputStream.close();
                                }
                                zzb(file3);
                            } catch (zzcl e8) {
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                if (fileOutputStream != null) {
                                    fileOutputStream.close();
                                }
                                zzb(file3);
                            } catch (Throwable th2) {
                                th = th2;
                                fileOutputStream2 = fileOutputStream;
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e9) {
                                    }
                                }
                                if (fileOutputStream2 != null) {
                                    try {
                                        fileOutputStream2.close();
                                    } catch (IOException e10) {
                                    }
                                }
                                zzb(file3);
                                throw th;
                            }
                        } catch (IOException e11) {
                            fileOutputStream = null;
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                            zzb(file3);
                        } catch (NoSuchAlgorithmException e12) {
                            fileOutputStream = null;
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                            zzb(file3);
                        } catch (zzcl e13) {
                            fileOutputStream = null;
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                            zzb(file3);
                        } catch (Throwable th3) {
                            th = th3;
                            fileOutputStream2 = null;
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            if (fileOutputStream2 != null) {
                                fileOutputStream2.close();
                            }
                            zzb(file3);
                            throw th;
                        }
                    } catch (IOException e14) {
                        fileOutputStream = null;
                        fileInputStream = null;
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        zzb(file3);
                    } catch (NoSuchAlgorithmException e15) {
                        fileOutputStream = null;
                        fileInputStream = null;
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        zzb(file3);
                    } catch (zzcl e16) {
                        fileOutputStream = null;
                        fileInputStream = null;
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        zzb(file3);
                    } catch (Throwable th32) {
                        th = th32;
                        fileOutputStream2 = null;
                        fileInputStream = null;
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        if (fileOutputStream2 != null) {
                            fileOutputStream2.close();
                        }
                        zzb(file3);
                        throw th;
                    }
                }
            }
        }
    }

    private static boolean zza(int i, zzba zzba) {
        if (i < 4) {
            if (zzba == null) {
                return true;
            }
            if (((Boolean) zzkb.zzik().zzd(zznk.zzbbc)).booleanValue() && (zzba.zzcx == null || zzba.zzcx.equals("0000000000000000000000000000000000000000000000000000000000000000"))) {
                return true;
            }
            if (((Boolean) zzkb.zzik().zzd(zznk.zzbbd)).booleanValue() && (zzba.zzfn == null || zzba.zzfn.zzgl == null || zzba.zzfn.zzgl.longValue() == -2)) {
                return true;
            }
        }
        return false;
    }

    private final void zzal() {
        try {
            if (this.zzry == null && this.zzsa) {
                AdvertisingIdClient advertisingIdClient = new AdvertisingIdClient(this.zzrt);
                advertisingIdClient.start();
                this.zzry = advertisingIdClient;
            }
        } catch (GooglePlayServicesNotAvailableException e) {
            this.zzry = null;
        } catch (IOException e2) {
            this.zzry = null;
        } catch (GooglePlayServicesRepairableException e3) {
            this.zzry = null;
        }
    }

    @VisibleForTesting
    private final zzba zzam() {
        zzba zzba = null;
        try {
            zzba = zzatq.zzl(this.zzrt, this.zzrt.getPackageName(), Integer.toString(this.zzrt.getPackageManager().getPackageInfo(this.zzrt.getPackageName(), 0).versionCode));
        } catch (Throwable th) {
        }
        return zzba;
    }

    private static void zzb(File file) {
        if (file.exists()) {
            file.delete();
            return;
        }
        Log.d(TAG, String.format("File %s not found. No need for deletion", new Object[]{file.getAbsolutePath()}));
    }

    private final boolean zzb(File file, String str) {
        FileInputStream fileInputStream;
        FileOutputStream fileOutputStream;
        FileInputStream fileInputStream2;
        Throwable th;
        FileOutputStream fileOutputStream2;
        File file2 = new File(String.format("%s/%s.tmp", new Object[]{file, str}));
        if (!file2.exists()) {
            return false;
        }
        File file3 = new File(String.format("%s/%s.dex", new Object[]{file, str}));
        if (file3.exists()) {
            return false;
        }
        try {
            long length = file2.length();
            if (length <= 0) {
                zzb(file2);
                return false;
            }
            byte[] bArr = new byte[((int) length)];
            fileInputStream = new FileInputStream(file2);
            try {
                if (fileInputStream.read(bArr) <= 0) {
                    Log.d(TAG, "Cannot read the cache data.");
                    zzb(file2);
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                    }
                    return false;
                }
                zzbe zzbe = (zzbe) zzbfi.zza(new zzbe(), bArr);
                if (str.equals(new String(zzbe.zzgr)) && Arrays.equals(zzbe.zzgq, zzbk.zzb(zzbe.data)) && Arrays.equals(zzbe.zzgs, VERSION.SDK.getBytes())) {
                    byte[] zza = this.zzrw.zza(this.zzrx, new String(zzbe.data));
                    file3.createNewFile();
                    fileOutputStream = new FileOutputStream(file3);
                    try {
                        fileOutputStream.write(zza, 0, zza.length);
                        try {
                            fileInputStream.close();
                        } catch (IOException e2) {
                        }
                        try {
                            fileOutputStream.close();
                        } catch (IOException e3) {
                        }
                        return true;
                    } catch (IOException e4) {
                        fileInputStream2 = fileInputStream;
                        if (fileInputStream2 != null) {
                            try {
                                fileInputStream2.close();
                            } catch (IOException e5) {
                            }
                        }
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e6) {
                            }
                        }
                        return false;
                    } catch (NoSuchAlgorithmException e7) {
                        fileInputStream2 = fileInputStream;
                        if (fileInputStream2 != null) {
                            fileInputStream2.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        return false;
                    } catch (zzcl e8) {
                        fileInputStream2 = fileInputStream;
                        if (fileInputStream2 != null) {
                            fileInputStream2.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        fileOutputStream2 = fileOutputStream;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e9) {
                            }
                        }
                        if (fileOutputStream2 != null) {
                            try {
                                fileOutputStream2.close();
                            } catch (IOException e10) {
                            }
                        }
                        throw th;
                    }
                }
                zzb(file2);
                try {
                    fileInputStream.close();
                } catch (IOException e11) {
                }
                return false;
            } catch (IOException e12) {
                fileOutputStream = null;
                fileInputStream2 = fileInputStream;
                if (fileInputStream2 != null) {
                    fileInputStream2.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return false;
            } catch (NoSuchAlgorithmException e13) {
                fileOutputStream = null;
                fileInputStream2 = fileInputStream;
                if (fileInputStream2 != null) {
                    fileInputStream2.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return false;
            } catch (zzcl e14) {
                fileOutputStream = null;
                fileInputStream2 = fileInputStream;
                if (fileInputStream2 != null) {
                    fileInputStream2.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream2 = null;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream2 != null) {
                    fileOutputStream2.close();
                }
                throw th;
            }
        } catch (IOException e15) {
            fileOutputStream = null;
            fileInputStream2 = null;
            if (fileInputStream2 != null) {
                fileInputStream2.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return false;
        } catch (NoSuchAlgorithmException e16) {
            fileOutputStream = null;
            fileInputStream2 = null;
            if (fileInputStream2 != null) {
                fileInputStream2.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return false;
        } catch (zzcl e17) {
            fileOutputStream = null;
            fileInputStream2 = null;
            if (fileInputStream2 != null) {
                fileInputStream2.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return false;
        } catch (Throwable th32) {
            th = th32;
            fileOutputStream2 = null;
            fileInputStream = null;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (fileOutputStream2 != null) {
                fileOutputStream2.close();
            }
            throw th;
        }
    }

    private static void zzm(String str) {
        zzb(new File(str));
    }

    public final Context getContext() {
        return this.zzrt;
    }

    public final boolean isInitialized() {
        return this.zzsh;
    }

    public final Method zza(String str, String str2) {
        zzeg zzeg = (zzeg) this.zzsg.get(new Pair(str, str2));
        return zzeg == null ? null : zzeg.zzaw();
    }

    @VisibleForTesting
    final void zza(int i, boolean z) {
        if (this.zzsf) {
            Future submit = this.zzru.submit(new zzdc(this, i, z));
            if (i == 0) {
                this.zzsc = submit;
            }
        }
    }

    public final boolean zza(String str, String str2, Class<?>... clsArr) {
        if (this.zzsg.containsKey(new Pair(str, str2))) {
            return false;
        }
        this.zzsg.put(new Pair(str, str2), new zzeg(this, str, str2, clsArr));
        return true;
    }

    public final ExecutorService zzab() {
        return this.zzru;
    }

    public final DexClassLoader zzac() {
        return this.zzrv;
    }

    public final zzck zzad() {
        return this.zzrw;
    }

    public final byte[] zzae() {
        return this.zzrx;
    }

    public final boolean zzaf() {
        return this.zzse;
    }

    public final zzcc zzag() {
        return this.zzsd;
    }

    public final boolean zzah() {
        return this.zzsf;
    }

    public final boolean zzai() {
        return this.zzsi;
    }

    public final zzba zzaj() {
        return this.zzsb;
    }

    public final Future zzak() {
        return this.zzsc;
    }

    public final AdvertisingIdClient zzan() {
        if (!this.zzqt) {
            return null;
        }
        if (this.zzry != null) {
            return this.zzry;
        }
        if (this.zzrz != null) {
            try {
                this.zzrz.get(AdaptiveTrackSelection.DEFAULT_MIN_TIME_BETWEEN_BUFFER_REEVALUTATION_MS, TimeUnit.MILLISECONDS);
                this.zzrz = null;
            } catch (InterruptedException e) {
            } catch (ExecutionException e2) {
            } catch (TimeoutException e3) {
                this.zzrz.cancel(true);
            }
        }
        return this.zzry;
    }

    @VisibleForTesting
    final zzba zzb(int i, boolean z) {
        if (i > 0 && z) {
            try {
                Thread.sleep((long) (i * 1000));
            } catch (InterruptedException e) {
            }
        }
        return zzam();
    }

    public final int zzx() {
        return this.zzsd != null ? zzcc.zzx() : Integer.MIN_VALUE;
    }
}
