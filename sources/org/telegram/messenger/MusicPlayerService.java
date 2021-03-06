package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.MediaStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.media.session.MediaSession;
import android.media.session.MediaSession.Callback;
import android.media.session.PlaybackState.Builder;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;
import com.baranak.turbogramf.R;
import com.google.android.exoplayer2.C0246C;
import com.google.android.exoplayer2.extractor.ts.PsExtractor;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import org.telegram.messenger.audioinfo.AudioInfo;
import org.telegram.ui.LaunchActivity;

public class MusicPlayerService extends Service implements NotificationCenter$NotificationCenterDelegate {
    private static final int ID_NOTIFICATION = 5;
    public static final String NOTIFY_CLOSE = "org.telegram.android.musicplayer.close";
    public static final String NOTIFY_NEXT = "org.telegram.android.musicplayer.next";
    public static final String NOTIFY_PAUSE = "org.telegram.android.musicplayer.pause";
    public static final String NOTIFY_PLAY = "org.telegram.android.musicplayer.play";
    public static final String NOTIFY_PREVIOUS = "org.telegram.android.musicplayer.previous";
    private static boolean supportBigNotifications;
    private static boolean supportLockScreenControls;
    private Bitmap albumArtPlaceholder;
    private AudioManager audioManager;
    private MediaSession mediaSession;
    private int notificationMessageID;
    private Builder playbackState;
    private RemoteControlClient remoteControlClient;

    /* renamed from: org.telegram.messenger.MusicPlayerService$1 */
    class C08571 extends Callback {
        C08571() {
        }

        public void onPlay() {
            MediaController.getInstance().playMessage(MediaController.getInstance().getPlayingMessageObject());
        }

        public void onPause() {
            MediaController.getInstance().pauseMessage(MediaController.getInstance().getPlayingMessageObject());
        }

        public void onSkipToNext() {
            MediaController.getInstance().playNextMessage();
        }

        public void onSkipToPrevious() {
            MediaController.getInstance().playPreviousMessage();
        }

        public void onStop() {
        }
    }

    /* renamed from: org.telegram.messenger.MusicPlayerService$2 */
    class C08582 implements Runnable {
        C08582() {
        }

        public void run() {
            MusicPlayerService.this.stopSelf();
        }
    }

    /* renamed from: org.telegram.messenger.MusicPlayerService$3 */
    class C08593 implements Runnable {
        C08593() {
        }

        public void run() {
            int i = 2;
            if (MusicPlayerService.this.remoteControlClient != null && MediaController.getInstance().getPlayingMessageObject() != null) {
                if (((long) MediaController.getInstance().getPlayingMessageObject().audioPlayerDuration) == C0246C.TIME_UNSET) {
                    AndroidUtilities.runOnUIThread(this, 500);
                    return;
                }
                MetadataEditor metadataEditor = MusicPlayerService.this.remoteControlClient.editMetadata(false);
                metadataEditor.putLong(9, ((long) MediaController.getInstance().getPlayingMessageObject().audioPlayerDuration) * 1000);
                metadataEditor.apply();
                RemoteControlClient access$000;
                if (VERSION.SDK_INT >= 18) {
                    access$000 = MusicPlayerService.this.remoteControlClient;
                    if (!MediaController.getInstance().isMessagePaused()) {
                        i = 3;
                    }
                    access$000.setPlaybackState(i, Math.max(((long) MediaController.getInstance().getPlayingMessageObject().audioProgressSec) * 1000, 100), MediaController.getInstance().isMessagePaused() ? 0.0f : 1.0f);
                    return;
                }
                access$000 = MusicPlayerService.this.remoteControlClient;
                if (!MediaController.getInstance().isMessagePaused()) {
                    i = 3;
                }
                access$000.setPlaybackState(i);
            }
        }
    }

    static {
        boolean z;
        boolean z2 = false;
        if (VERSION.SDK_INT >= 16) {
            z = true;
        } else {
            z = false;
        }
        supportBigNotifications = z;
        if (VERSION.SDK_INT < 21 || !TextUtils.isEmpty(AndroidUtilities.getSystemProperty("ro.miui.ui.version.code"))) {
            z2 = true;
        }
        supportLockScreenControls = z2;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        this.audioManager = (AudioManager) getSystemService(MimeTypes.BASE_TYPE_AUDIO);
        for (int a = 0; a < 3; a++) {
            NotificationCenter.getInstance(a).addObserver(this, NotificationCenter.messagePlayingDidSeek);
            NotificationCenter.getInstance(a).addObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
        }
        if (VERSION.SDK_INT >= 21) {
            this.mediaSession = new MediaSession(this, "telegramAudioPlayer");
            this.playbackState = new Builder();
            this.albumArtPlaceholder = Bitmap.createBitmap(AndroidUtilities.dp(102.0f), AndroidUtilities.dp(102.0f), Config.ARGB_8888);
            Drawable placeholder = getResources().getDrawable(R.drawable.nocover_big);
            placeholder.setBounds(0, 0, this.albumArtPlaceholder.getWidth(), this.albumArtPlaceholder.getHeight());
            placeholder.draw(new Canvas(this.albumArtPlaceholder));
            this.mediaSession.setCallback(new C08571());
            this.mediaSession.setActive(true);
        }
        super.onCreate();
    }

    @SuppressLint({"NewApi"})
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            try {
                if ((getPackageName() + ".STOP_PLAYER").equals(intent.getAction())) {
                    MediaController.getInstance().cleanupPlayer(true, true);
                    return 2;
                }
            } catch (Exception e) {
                ThrowableExtension.printStackTrace(e);
                return 1;
            }
        }
        MessageObject messageObject = MediaController.getInstance().getPlayingMessageObject();
        if (messageObject == null) {
            AndroidUtilities.runOnUIThread(new C08582());
            return 1;
        }
        if (supportLockScreenControls) {
            ComponentName remoteComponentName = new ComponentName(getApplicationContext(), MusicPlayerReceiver.class.getName());
            try {
                if (this.remoteControlClient == null) {
                    this.audioManager.registerMediaButtonEventReceiver(remoteComponentName);
                    Intent mediaButtonIntent = new Intent("android.intent.action.MEDIA_BUTTON");
                    mediaButtonIntent.setComponent(remoteComponentName);
                    this.remoteControlClient = new RemoteControlClient(PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0));
                    this.audioManager.registerRemoteControlClient(this.remoteControlClient);
                }
                this.remoteControlClient.setTransportControlFlags(PsExtractor.PRIVATE_STREAM_1);
            } catch (Exception e2) {
                FileLog.e(e2);
            }
        }
        createNotification(messageObject);
        return 1;
    }

    @SuppressLint({"NewApi"})
    private void createNotification(MessageObject messageObject) {
        String songName = messageObject.getMusicTitle();
        String authorName = messageObject.getMusicAuthor();
        AudioInfo audioInfo = MediaController.getInstance().getAudioInfo();
        Intent intent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
        intent.setAction("com.tmessages.openplayer");
        intent.setFlags(32768);
        PendingIntent contentIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, intent, 0);
        Bitmap albumArt;
        Notification notification;
        if (VERSION.SDK_INT >= 21) {
            albumArt = audioInfo != null ? audioInfo.getSmallCover() : null;
            Bitmap fullAlbumArt = audioInfo != null ? audioInfo.getCover() : null;
            boolean isPlaying = !MediaController.getInstance().isMessagePaused();
            PendingIntent pendingPrev = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_PREVIOUS).setComponent(new ComponentName(this, MusicPlayerReceiver.class)), 268435456);
            PendingIntent pendingStop = PendingIntent.getService(getApplicationContext(), 0, new Intent(this, getClass()).setAction(getPackageName() + ".STOP_PLAYER"), 268435456);
            PendingIntent pendingPlaypause = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(isPlaying ? NOTIFY_PAUSE : NOTIFY_PLAY).setComponent(new ComponentName(this, MusicPlayerReceiver.class)), 268435456);
            PendingIntent pendingNext = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_NEXT).setComponent(new ComponentName(this, MusicPlayerReceiver.class)), 268435456);
            Notification.Builder bldr = new Notification.Builder(this);
            int[] iArr = new int[3];
            bldr.setSmallIcon(R.drawable.player).setOngoing(isPlaying).setContentTitle(songName).setContentText(authorName).setSubText(audioInfo != null ? audioInfo.getAlbum() : null).setContentIntent(contentIntent).setDeleteIntent(pendingStop).setShowWhen(false).setCategory(NotificationCompat.CATEGORY_TRANSPORT).setPriority(2).setStyle(new MediaStyle().setMediaSession(this.mediaSession.getSessionToken()).setShowActionsInCompactView(new int[]{0, 1, 2}));
            if (VERSION.SDK_INT >= 26) {
                NotificationsController.checkOtherNotificationsChannel();
                bldr.setChannelId(NotificationsController.OTHER_NOTIFICATIONS_CHANNEL);
            }
            if (albumArt != null) {
                bldr.setLargeIcon(albumArt);
            } else {
                bldr.setLargeIcon(this.albumArtPlaceholder);
            }
            if (MediaController.getInstance().isDownloadingCurrentMessage()) {
                this.playbackState.setState(6, 0, 1.0f).setActions(0);
                bldr.addAction(new Action.Builder(R.drawable.ic_action_previous, "", pendingPrev).build()).addAction(new Action.Builder(R.drawable.loading_animation2, "", null).build()).addAction(new Action.Builder(R.drawable.ic_action_next, "", pendingNext).build());
            } else {
                this.playbackState.setState(isPlaying ? 3 : 2, ((long) MediaController.getInstance().getPlayingMessageObject().audioProgressSec) * 1000, isPlaying ? 1.0f : 0.0f).setActions(566);
                bldr.addAction(new Action.Builder(R.drawable.ic_action_previous, "", pendingPrev).build()).addAction(new Action.Builder(isPlaying ? R.drawable.ic_action_pause : R.drawable.ic_action_play, "", pendingPlaypause).build()).addAction(new Action.Builder(R.drawable.ic_action_next, "", pendingNext).build());
            }
            this.mediaSession.setPlaybackState(this.playbackState.build());
            this.mediaSession.setMetadata(new MediaMetadata.Builder().putBitmap("android.media.metadata.ALBUM_ART", fullAlbumArt).putString("android.media.metadata.ALBUM_ARTIST", authorName).putString("android.media.metadata.TITLE", songName).putString("android.media.metadata.ALBUM", audioInfo != null ? audioInfo.getAlbum() : null).build());
            bldr.setVisibility(1);
            notification = bldr.build();
            if (isPlaying) {
                startForeground(5, notification);
            } else {
                stopForeground(false);
                ((NotificationManager) getSystemService("notification")).notify(5, notification);
            }
        } else {
            RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.player_small_notification);
            RemoteViews expandedView = null;
            if (supportBigNotifications) {
                expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.player_big_notification);
            }
            notification = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.player).setContentIntent(contentIntent).setChannelId(NotificationsController.OTHER_NOTIFICATIONS_CHANNEL).setContentTitle(songName).build();
            notification.contentView = remoteViews;
            if (supportBigNotifications) {
                notification.bigContentView = expandedView;
            }
            setListeners(remoteViews);
            if (supportBigNotifications) {
                setListeners(expandedView);
            }
            albumArt = audioInfo != null ? audioInfo.getSmallCover() : null;
            if (albumArt != null) {
                notification.contentView.setImageViewBitmap(R.id.player_album_art, albumArt);
                if (supportBigNotifications) {
                    notification.bigContentView.setImageViewBitmap(R.id.player_album_art, albumArt);
                }
            } else {
                notification.contentView.setImageViewResource(R.id.player_album_art, R.drawable.nocover_small);
                if (supportBigNotifications) {
                    notification.bigContentView.setImageViewResource(R.id.player_album_art, R.drawable.nocover_big);
                }
            }
            if (MediaController.getInstance().isDownloadingCurrentMessage()) {
                notification.contentView.setViewVisibility(R.id.player_pause, 8);
                notification.contentView.setViewVisibility(R.id.player_play, 8);
                notification.contentView.setViewVisibility(R.id.player_next, 8);
                notification.contentView.setViewVisibility(R.id.player_previous, 8);
                notification.contentView.setViewVisibility(R.id.player_progress_bar, 0);
                if (supportBigNotifications) {
                    notification.bigContentView.setViewVisibility(R.id.player_pause, 8);
                    notification.bigContentView.setViewVisibility(R.id.player_play, 8);
                    notification.bigContentView.setViewVisibility(R.id.player_next, 8);
                    notification.bigContentView.setViewVisibility(R.id.player_previous, 8);
                    notification.bigContentView.setViewVisibility(R.id.player_progress_bar, 0);
                }
            } else {
                notification.contentView.setViewVisibility(R.id.player_progress_bar, 8);
                notification.contentView.setViewVisibility(R.id.player_next, 0);
                notification.contentView.setViewVisibility(R.id.player_previous, 0);
                if (supportBigNotifications) {
                    notification.bigContentView.setViewVisibility(R.id.player_next, 0);
                    notification.bigContentView.setViewVisibility(R.id.player_previous, 0);
                    notification.bigContentView.setViewVisibility(R.id.player_progress_bar, 8);
                }
                if (MediaController.getInstance().isMessagePaused()) {
                    notification.contentView.setViewVisibility(R.id.player_pause, 8);
                    notification.contentView.setViewVisibility(R.id.player_play, 0);
                    if (supportBigNotifications) {
                        notification.bigContentView.setViewVisibility(R.id.player_pause, 8);
                        notification.bigContentView.setViewVisibility(R.id.player_play, 0);
                    }
                } else {
                    notification.contentView.setViewVisibility(R.id.player_pause, 0);
                    notification.contentView.setViewVisibility(R.id.player_play, 8);
                    if (supportBigNotifications) {
                        notification.bigContentView.setViewVisibility(R.id.player_pause, 0);
                        notification.bigContentView.setViewVisibility(R.id.player_play, 8);
                    }
                }
            }
            notification.contentView.setTextViewText(R.id.player_song_name, songName);
            notification.contentView.setTextViewText(R.id.player_author_name, authorName);
            if (supportBigNotifications) {
                notification.bigContentView.setTextViewText(R.id.player_song_name, songName);
                notification.bigContentView.setTextViewText(R.id.player_author_name, authorName);
                RemoteViews remoteViews2 = notification.bigContentView;
                String album = (audioInfo == null || TextUtils.isEmpty(audioInfo.getAlbum())) ? "" : audioInfo.getAlbum();
                remoteViews2.setTextViewText(R.id.player_album_title, album);
            }
            notification.flags |= 2;
            startForeground(5, notification);
        }
        if (this.remoteControlClient != null) {
            MetadataEditor metadataEditor;
            int currentID = MediaController.getInstance().getPlayingMessageObject().getId();
            if (this.notificationMessageID != currentID) {
                this.notificationMessageID = currentID;
                metadataEditor = this.remoteControlClient.editMetadata(true);
                metadataEditor.putString(2, authorName);
                metadataEditor.putString(7, songName);
                if (!(audioInfo == null || TextUtils.isEmpty(audioInfo.getAlbum()))) {
                    metadataEditor.putString(1, audioInfo.getAlbum());
                }
                metadataEditor.putLong(9, ((long) MediaController.getInstance().getPlayingMessageObject().audioPlayerDuration) * 1000);
                if (!(audioInfo == null || audioInfo.getCover() == null)) {
                    try {
                        metadataEditor.putBitmap(100, audioInfo.getCover());
                    } catch (Throwable e) {
                        FileLog.e(e);
                    }
                }
                metadataEditor.apply();
                AndroidUtilities.runOnUIThread(new C08593(), 1000);
            }
            if (MediaController.getInstance().isDownloadingCurrentMessage()) {
                this.remoteControlClient.setPlaybackState(8);
                return;
            }
            metadataEditor = this.remoteControlClient.editMetadata(false);
            metadataEditor.putLong(9, ((long) MediaController.getInstance().getPlayingMessageObject().audioPlayerDuration) * 1000);
            metadataEditor.apply();
            if (VERSION.SDK_INT >= 18) {
                this.remoteControlClient.setPlaybackState(MediaController.getInstance().isMessagePaused() ? 2 : 3, Math.max(((long) MediaController.getInstance().getPlayingMessageObject().audioProgressSec) * 1000, 100), MediaController.getInstance().isMessagePaused() ? 0.0f : 1.0f);
            } else {
                this.remoteControlClient.setPlaybackState(MediaController.getInstance().isMessagePaused() ? 2 : 3);
            }
        }
    }

    public void setListeners(RemoteViews view) {
        view.setOnClickPendingIntent(R.id.player_previous, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_PREVIOUS), 134217728));
        view.setOnClickPendingIntent(R.id.player_close, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_CLOSE), 134217728));
        view.setOnClickPendingIntent(R.id.player_pause, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_PAUSE), 134217728));
        view.setOnClickPendingIntent(R.id.player_next, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_NEXT), 134217728));
        view.setOnClickPendingIntent(R.id.player_play, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_PLAY), 134217728));
    }

    @SuppressLint({"NewApi"})
    public void onDestroy() {
        super.onDestroy();
        if (this.remoteControlClient != null) {
            MetadataEditor metadataEditor = this.remoteControlClient.editMetadata(true);
            metadataEditor.clear();
            metadataEditor.apply();
            this.audioManager.unregisterRemoteControlClient(this.remoteControlClient);
        }
        if (VERSION.SDK_INT >= 21) {
            this.mediaSession.release();
        }
        for (int a = 0; a < 3; a++) {
            NotificationCenter.getInstance(a).removeObserver(this, NotificationCenter.messagePlayingDidSeek);
            NotificationCenter.getInstance(a).removeObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
        }
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        MessageObject messageObject;
        if (id == NotificationCenter.messagePlayingPlayStateChanged) {
            messageObject = MediaController.getInstance().getPlayingMessageObject();
            if (messageObject != null) {
                createNotification(messageObject);
            } else {
                stopSelf();
            }
        } else if (id == NotificationCenter.messagePlayingDidSeek) {
            messageObject = MediaController.getInstance().getPlayingMessageObject();
            if (this.remoteControlClient != null && VERSION.SDK_INT >= 18) {
                this.remoteControlClient.setPlaybackState(MediaController.getInstance().isMessagePaused() ? 2 : 3, ((long) Math.round(((Float) args[1]).floatValue() * ((float) messageObject.audioPlayerDuration))) * 1000, MediaController.getInstance().isMessagePaused() ? 0.0f : 1.0f);
            }
        }
    }
}
