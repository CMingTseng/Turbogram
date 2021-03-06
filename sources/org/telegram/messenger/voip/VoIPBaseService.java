package org.telegram.messenger.voip;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.support.v4.app.NotificationCompat;
import android.telecom.CallAudioState;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import com.baranak.turbogramf.R;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter$NotificationCenterDelegate;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.StatsController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.voip.VoIPController.ConnectionStateListener;
import org.telegram.messenger.voip.VoIPController.Stats;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$FileLocation;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.BottomSheet.BottomSheetCell;
import org.telegram.ui.ActionBar.BottomSheet.Builder;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.VoIPPermissionActivity;

public abstract class VoIPBaseService extends Service implements SensorEventListener, OnAudioFocusChangeListener, NotificationCenter$NotificationCenterDelegate, ConnectionStateListener {
    public static final String ACTION_HEADSET_PLUG = "android.intent.action.HEADSET_PLUG";
    public static final int AUDIO_ROUTE_BLUETOOTH = 2;
    public static final int AUDIO_ROUTE_EARPIECE = 0;
    public static final int AUDIO_ROUTE_SPEAKER = 1;
    public static final int DISCARD_REASON_DISCONNECT = 2;
    public static final int DISCARD_REASON_HANGUP = 1;
    public static final int DISCARD_REASON_LINE_BUSY = 4;
    public static final int DISCARD_REASON_MISSED = 3;
    protected static final int ID_INCOMING_CALL_NOTIFICATION = 202;
    protected static final int ID_ONGOING_CALL_NOTIFICATION = 201;
    protected static final int PROXIMITY_SCREEN_OFF_WAKE_LOCK = 32;
    public static final int STATE_ENDED = 11;
    public static final int STATE_ESTABLISHED = 3;
    public static final int STATE_FAILED = 4;
    public static final int STATE_RECONNECTING = 5;
    public static final int STATE_WAIT_INIT = 1;
    public static final int STATE_WAIT_INIT_ACK = 2;
    protected static final boolean USE_CONNECTION_SERVICE = isDeviceCompatibleWithConnectionServiceAPI();
    protected static VoIPBaseService sharedInstance;
    protected Runnable afterSoundRunnable = new C09371();
    protected boolean audioConfigured;
    protected int audioRouteToSet = 2;
    protected boolean bluetoothScoActive = false;
    protected BluetoothAdapter btAdapter;
    protected int callDiscardReason;
    protected VoIPController controller;
    protected boolean controllerStarted;
    protected WakeLock cpuWakelock;
    protected int currentAccount = -1;
    protected int currentState = 0;
    protected boolean haveAudioFocus;
    protected boolean isBtHeadsetConnected;
    protected boolean isHeadsetPlugged;
    protected boolean isOutgoing;
    protected boolean isProximityNear;
    protected int lastError;
    protected long lastKnownDuration = 0;
    protected NetworkInfo lastNetInfo;
    private Boolean mHasEarpiece = null;
    protected boolean micMute;
    protected boolean needPlayEndSound;
    protected boolean needSwitchToBluetoothAfterScoActivates = false;
    protected Notification ongoingCallNotification;
    protected boolean playingSound;
    protected Stats prevStats = new Stats();
    protected WakeLock proximityWakelock;
    protected BroadcastReceiver receiver = new C09382();
    protected MediaPlayer ringtonePlayer;
    protected int signalBarCount;
    protected SoundPool soundPool;
    protected int spBusyId;
    protected int spConnectingId;
    protected int spEndId;
    protected int spFailedID;
    protected int spPlayID;
    protected int spRingbackID;
    protected boolean speakerphoneStateToSet;
    protected ArrayList<StateListener> stateListeners = new ArrayList();
    protected Stats stats = new Stats();
    protected CallConnection systemCallConnection;
    protected Runnable timeoutRunnable;
    protected Vibrator vibrator;
    private boolean wasEstablished;

    /* renamed from: org.telegram.messenger.voip.VoIPBaseService$1 */
    class C09371 implements Runnable {
        C09371() {
        }

        public void run() {
            VoIPBaseService.this.soundPool.release();
            if (!VoIPBaseService.USE_CONNECTION_SERVICE) {
                if (VoIPBaseService.this.isBtHeadsetConnected) {
                    ((AudioManager) ApplicationLoader.applicationContext.getSystemService(MimeTypes.BASE_TYPE_AUDIO)).stopBluetoothSco();
                }
                ((AudioManager) ApplicationLoader.applicationContext.getSystemService(MimeTypes.BASE_TYPE_AUDIO)).setSpeakerphoneOn(false);
            }
        }
    }

    /* renamed from: org.telegram.messenger.voip.VoIPBaseService$2 */
    class C09382 extends BroadcastReceiver {
        C09382() {
        }

        public void onReceive(Context context, Intent intent) {
            boolean z = true;
            VoIPBaseService voIPBaseService;
            if (VoIPBaseService.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                voIPBaseService = VoIPBaseService.this;
                if (intent.getIntExtra("state", 0) != 1) {
                    z = false;
                }
                voIPBaseService.isHeadsetPlugged = z;
                if (VoIPBaseService.this.isHeadsetPlugged && VoIPBaseService.this.proximityWakelock != null && VoIPBaseService.this.proximityWakelock.isHeld()) {
                    VoIPBaseService.this.proximityWakelock.release();
                }
                VoIPBaseService.this.isProximityNear = false;
                VoIPBaseService.this.updateOutputGainControlState();
            } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                VoIPBaseService.this.updateNetworkType();
            } else if ("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED".equals(intent.getAction())) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("bt headset state = " + intent.getIntExtra("android.bluetooth.profile.extra.STATE", 0));
                }
                voIPBaseService = VoIPBaseService.this;
                if (intent.getIntExtra("android.bluetooth.profile.extra.STATE", 0) != 2) {
                    z = false;
                }
                voIPBaseService.updateBluetoothHeadsetState(z);
            } else if ("android.media.ACTION_SCO_AUDIO_STATE_UPDATED".equals(intent.getAction())) {
                int state = intent.getIntExtra("android.media.extra.SCO_AUDIO_STATE", 0);
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("Bluetooth SCO state updated: " + state);
                }
                if (state == 0 && VoIPBaseService.this.isBtHeadsetConnected && (!VoIPBaseService.this.btAdapter.isEnabled() || VoIPBaseService.this.btAdapter.getProfileConnectionState(1) != 2)) {
                    VoIPBaseService.this.updateBluetoothHeadsetState(false);
                    return;
                }
                boolean z2;
                VoIPBaseService voIPBaseService2 = VoIPBaseService.this;
                if (state == 1) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                voIPBaseService2.bluetoothScoActive = z2;
                if (VoIPBaseService.this.bluetoothScoActive && VoIPBaseService.this.needSwitchToBluetoothAfterScoActivates) {
                    VoIPBaseService.this.needSwitchToBluetoothAfterScoActivates = false;
                    AudioManager am = (AudioManager) VoIPBaseService.this.getSystemService(MimeTypes.BASE_TYPE_AUDIO);
                    am.setSpeakerphoneOn(false);
                    am.setBluetoothScoOn(true);
                }
                Iterator it = VoIPBaseService.this.stateListeners.iterator();
                while (it.hasNext()) {
                    ((StateListener) it.next()).onAudioSettingsChanged();
                }
            } else if ("android.intent.action.PHONE_STATE".equals(intent.getAction())) {
                if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(intent.getStringExtra("state"))) {
                    VoIPBaseService.this.hangUp();
                }
            }
        }
    }

    /* renamed from: org.telegram.messenger.voip.VoIPBaseService$3 */
    class C09393 implements OnClickListener {
        C09393() {
        }

        public void onClick(DialogInterface dialog, int which) {
            AudioManager am = (AudioManager) VoIPBaseService.this.getSystemService(MimeTypes.BASE_TYPE_AUDIO);
            if (VoIPBaseService.getSharedInstance() != null) {
                if (VoIPBaseService.USE_CONNECTION_SERVICE && VoIPBaseService.this.systemCallConnection != null) {
                    switch (which) {
                        case 0:
                            VoIPBaseService.this.systemCallConnection.setAudioRoute(2);
                            break;
                        case 1:
                            VoIPBaseService.this.systemCallConnection.setAudioRoute(5);
                            break;
                        case 2:
                            VoIPBaseService.this.systemCallConnection.setAudioRoute(8);
                            break;
                    }
                } else if (!VoIPBaseService.this.audioConfigured || VoIPBaseService.USE_CONNECTION_SERVICE) {
                    switch (which) {
                        case 0:
                            VoIPBaseService.this.audioRouteToSet = 2;
                            break;
                        case 1:
                            VoIPBaseService.this.audioRouteToSet = 0;
                            break;
                        case 2:
                            VoIPBaseService.this.audioRouteToSet = 1;
                            break;
                        default:
                            break;
                    }
                } else {
                    switch (which) {
                        case 0:
                            if (!VoIPBaseService.this.bluetoothScoActive) {
                                VoIPBaseService.this.needSwitchToBluetoothAfterScoActivates = true;
                                try {
                                    am.startBluetoothSco();
                                    break;
                                } catch (Throwable th) {
                                    break;
                                }
                            }
                            am.setBluetoothScoOn(true);
                            am.setSpeakerphoneOn(false);
                            break;
                        case 1:
                            if (VoIPBaseService.this.bluetoothScoActive) {
                                am.stopBluetoothSco();
                            }
                            am.setSpeakerphoneOn(false);
                            am.setBluetoothScoOn(false);
                            break;
                        case 2:
                            if (VoIPBaseService.this.bluetoothScoActive) {
                                am.stopBluetoothSco();
                            }
                            am.setBluetoothScoOn(false);
                            am.setSpeakerphoneOn(true);
                            break;
                    }
                    VoIPBaseService.this.updateOutputGainControlState();
                }
                Iterator it = VoIPBaseService.this.stateListeners.iterator();
                while (it.hasNext()) {
                    ((StateListener) it.next()).onAudioSettingsChanged();
                }
            }
        }
    }

    /* renamed from: org.telegram.messenger.voip.VoIPBaseService$4 */
    class C09404 implements OnPreparedListener {
        C09404() {
        }

        public void onPrepared(MediaPlayer mediaPlayer) {
            VoIPBaseService.this.ringtonePlayer.start();
        }
    }

    /* renamed from: org.telegram.messenger.voip.VoIPBaseService$5 */
    class C09415 implements Runnable {
        C09415() {
        }

        public void run() {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didEndedCall, new Object[0]);
        }
    }

    /* renamed from: org.telegram.messenger.voip.VoIPBaseService$7 */
    class C09437 implements Runnable {
        C09437() {
        }

        public void run() {
            if (VoIPBaseService.this.controller != null) {
                StatsController.getInstance(VoIPBaseService.this.currentAccount).incrementTotalCallsTime(VoIPBaseService.this.getStatsNetworkType(), 5);
                AndroidUtilities.runOnUIThread(this, DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
            }
        }
    }

    /* renamed from: org.telegram.messenger.voip.VoIPBaseService$8 */
    class C09448 implements Runnable {
        C09448() {
        }

        public void run() {
            if (VoIPBaseService.this.systemCallConnection != null) {
                switch (VoIPBaseService.this.callDiscardReason) {
                    case 1:
                        VoIPBaseService.this.systemCallConnection.setDisconnected(new DisconnectCause(VoIPBaseService.this.isOutgoing ? 2 : 6));
                        break;
                    case 2:
                        VoIPBaseService.this.systemCallConnection.setDisconnected(new DisconnectCause(1));
                        break;
                    case 3:
                        VoIPBaseService.this.systemCallConnection.setDisconnected(new DisconnectCause(VoIPBaseService.this.isOutgoing ? 4 : 5));
                        break;
                    case 4:
                        VoIPBaseService.this.systemCallConnection.setDisconnected(new DisconnectCause(7));
                        break;
                    default:
                        VoIPBaseService.this.systemCallConnection.setDisconnected(new DisconnectCause(3));
                        break;
                }
                VoIPBaseService.this.systemCallConnection.destroy();
                VoIPBaseService.this.systemCallConnection = null;
            }
        }
    }

    @TargetApi(26)
    public class CallConnection extends Connection {
        public CallConnection() {
            setConnectionProperties(128);
            setAudioModeIsVoip(true);
        }

        public void onCallAudioStateChanged(CallAudioState state) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("ConnectionService call audio state changed: " + state);
            }
            Iterator it = VoIPBaseService.this.stateListeners.iterator();
            while (it.hasNext()) {
                ((StateListener) it.next()).onAudioSettingsChanged();
            }
        }

        public void onDisconnect() {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("ConnectionService onDisconnect");
            }
            setDisconnected(new DisconnectCause(2));
            destroy();
            VoIPBaseService.this.systemCallConnection = null;
            VoIPBaseService.this.hangUp();
        }

        public void onAnswer() {
            VoIPBaseService.this.acceptIncomingCallFromNotification();
        }

        public void onReject() {
            VoIPBaseService.this.needPlayEndSound = false;
            VoIPBaseService.this.declineIncomingCall(1, null);
        }

        public void onShowIncomingCallUi() {
            VoIPBaseService.this.startRinging();
        }

        public void onStateChanged(int state) {
            super.onStateChanged(state);
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("ConnectionService onStateChanged " + state);
            }
        }

        public void onCallEvent(String event, Bundle extras) {
            super.onCallEvent(event, extras);
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("ConnectionService onCallEvent " + event);
            }
        }

        public void onSilence() {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("onSlience");
            }
            VoIPBaseService.this.stopRinging();
        }
    }

    public interface StateListener {
        void onAudioSettingsChanged();

        void onSignalBarsCountChanged(int i);

        void onStateChanged(int i);
    }

    public abstract void acceptIncomingCall();

    public abstract void declineIncomingCall();

    public abstract void declineIncomingCall(int i, Runnable runnable);

    public abstract long getCallID();

    public abstract CallConnection getConnectionAndStartCall();

    protected abstract Class<? extends Activity> getUIActivityClass();

    public abstract void hangUp();

    public abstract void hangUp(Runnable runnable);

    protected abstract void showNotification();

    protected abstract void startRinging();

    protected abstract void updateServerConfig();

    public boolean hasEarpiece() {
        if (!USE_CONNECTION_SERVICE || this.systemCallConnection == null || this.systemCallConnection.getCallAudioState() == null) {
            if (((TelephonyManager) getSystemService("phone")).getPhoneType() != 0) {
                return true;
            }
            if (this.mHasEarpiece != null) {
                return this.mHasEarpiece.booleanValue();
            }
            try {
                AudioManager am = (AudioManager) getSystemService(MimeTypes.BASE_TYPE_AUDIO);
                Method method = AudioManager.class.getMethod("getDevicesForStream", new Class[]{Integer.TYPE});
                int earpieceFlag = AudioManager.class.getField("DEVICE_OUT_EARPIECE").getInt(null);
                if ((((Integer) method.invoke(am, new Object[]{Integer.valueOf(0)})).intValue() & earpieceFlag) == earpieceFlag) {
                    this.mHasEarpiece = Boolean.TRUE;
                } else {
                    this.mHasEarpiece = Boolean.FALSE;
                }
            } catch (Throwable error) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("Error while checking earpiece! ", error);
                }
                this.mHasEarpiece = Boolean.TRUE;
            }
            return this.mHasEarpiece.booleanValue();
        } else if ((this.systemCallConnection.getCallAudioState().getSupportedRouteMask() & 5) != 0) {
            return true;
        } else {
            return false;
        }
    }

    protected int getStatsNetworkType() {
        if (this.lastNetInfo == null || this.lastNetInfo.getType() != 0) {
            return 1;
        }
        return this.lastNetInfo.isRoaming() ? 2 : 0;
    }

    public void registerStateListener(StateListener l) {
        this.stateListeners.add(l);
        if (this.currentState != 0) {
            l.onStateChanged(this.currentState);
        }
        if (this.signalBarCount != 0) {
            l.onSignalBarsCountChanged(this.signalBarCount);
        }
    }

    public void unregisterStateListener(StateListener l) {
        this.stateListeners.remove(l);
    }

    public void setMicMute(boolean mute) {
        this.micMute = mute;
        if (this.controller != null) {
            this.controller.setMicMute(mute);
        }
    }

    public boolean isMicMute() {
        return this.micMute;
    }

    public void toggleSpeakerphoneOrShowRouteSheet(Activity activity) {
        if (isBluetoothHeadsetConnected() && hasEarpiece()) {
            BottomSheet sheet = new Builder(activity).setItems(new CharSequence[]{LocaleController.getString("VoipAudioRoutingBluetooth", R.string.VoipAudioRoutingBluetooth), LocaleController.getString("VoipAudioRoutingEarpiece", R.string.VoipAudioRoutingEarpiece), LocaleController.getString("VoipAudioRoutingSpeaker", R.string.VoipAudioRoutingSpeaker)}, new int[]{R.drawable.ic_bluetooth_white_24dp, R.drawable.ic_phone_in_talk_white_24dp, R.drawable.ic_volume_up_white_24dp}, new C09393()).create();
            sheet.setBackgroundColor(-13948117);
            sheet.show();
            ViewGroup container = sheet.getSheetContainer();
            for (int i = 0; i < container.getChildCount(); i++) {
                ((BottomSheetCell) container.getChildAt(i)).setTextColor(-1);
            }
            return;
        }
        if (!USE_CONNECTION_SERVICE || this.systemCallConnection == null || this.systemCallConnection.getCallAudioState() == null) {
            if (!this.audioConfigured || USE_CONNECTION_SERVICE) {
                this.speakerphoneStateToSet = !this.speakerphoneStateToSet;
            } else {
                AudioManager am = (AudioManager) getSystemService(MimeTypes.BASE_TYPE_AUDIO);
                if (hasEarpiece()) {
                    am.setSpeakerphoneOn(!am.isSpeakerphoneOn());
                } else {
                    am.setBluetoothScoOn(!am.isBluetoothScoOn());
                }
                updateOutputGainControlState();
            }
        } else if (hasEarpiece()) {
            int i2;
            CallConnection callConnection = this.systemCallConnection;
            if (this.systemCallConnection.getCallAudioState().getRoute() == 8) {
                i2 = 5;
            } else {
                i2 = 8;
            }
            callConnection.setAudioRoute(i2);
        } else {
            this.systemCallConnection.setAudioRoute(this.systemCallConnection.getCallAudioState().getRoute() == 2 ? 5 : 2);
        }
        Iterator it = this.stateListeners.iterator();
        while (it.hasNext()) {
            ((StateListener) it.next()).onAudioSettingsChanged();
        }
    }

    public boolean isSpeakerphoneOn() {
        if (USE_CONNECTION_SERVICE && this.systemCallConnection != null && this.systemCallConnection.getCallAudioState() != null) {
            int route = this.systemCallConnection.getCallAudioState().getRoute();
            if (hasEarpiece()) {
                if (route == 8) {
                    return true;
                }
                return false;
            } else if (route != 2) {
                return false;
            } else {
                return true;
            }
        } else if (!this.audioConfigured || USE_CONNECTION_SERVICE) {
            return this.speakerphoneStateToSet;
        } else {
            AudioManager am = (AudioManager) getSystemService(MimeTypes.BASE_TYPE_AUDIO);
            return hasEarpiece() ? am.isSpeakerphoneOn() : am.isBluetoothScoOn();
        }
    }

    public int getCurrentAudioRoute() {
        if (USE_CONNECTION_SERVICE) {
            if (!(this.systemCallConnection == null || this.systemCallConnection.getCallAudioState() == null)) {
                switch (this.systemCallConnection.getCallAudioState().getRoute()) {
                    case 1:
                    case 4:
                        return 0;
                    case 2:
                        return 2;
                    case 8:
                        return 1;
                }
            }
            return this.audioRouteToSet;
        } else if (!this.audioConfigured) {
            return this.audioRouteToSet;
        } else {
            AudioManager am = (AudioManager) getSystemService(MimeTypes.BASE_TYPE_AUDIO);
            if (am.isBluetoothScoOn()) {
                return 2;
            }
            if (am.isSpeakerphoneOn()) {
                return 1;
            }
            return 0;
        }
    }

    public String getDebugString() {
        return this.controller.getDebugString();
    }

    public long getCallDuration() {
        if (!this.controllerStarted || this.controller == null) {
            return this.lastKnownDuration;
        }
        long callDuration = this.controller.getCallDuration();
        this.lastKnownDuration = callDuration;
        return callDuration;
    }

    public static VoIPBaseService getSharedInstance() {
        return sharedInstance;
    }

    public void stopRinging() {
        if (this.ringtonePlayer != null) {
            this.ringtonePlayer.stop();
            this.ringtonePlayer.release();
            this.ringtonePlayer = null;
        }
        if (this.vibrator != null) {
            this.vibrator.cancel();
            this.vibrator = null;
        }
    }

    protected void showNotification(String name, TLRPC$FileLocation photo, Class<? extends Activity> activity) {
        Intent intent = new Intent(this, activity);
        intent.addFlags(805306368);
        Notification.Builder builder = new Notification.Builder(this).setContentTitle(LocaleController.getString("VoipOutgoingCall", R.string.VoipOutgoingCall)).setContentText(name).setSmallIcon(R.drawable.notification).setContentIntent(PendingIntent.getActivity(this, 0, intent, 0));
        if (VERSION.SDK_INT >= 16) {
            Intent endIntent = new Intent(this, VoIPActionsReceiver.class);
            endIntent.setAction(getPackageName() + ".END_CALL");
            builder.addAction(R.drawable.ic_call_end_white_24dp, LocaleController.getString("VoipEndCall", R.string.VoipEndCall), PendingIntent.getBroadcast(this, 0, endIntent, 134217728));
            builder.setPriority(2);
        }
        if (VERSION.SDK_INT >= 17) {
            builder.setShowWhen(false);
        }
        if (VERSION.SDK_INT >= 21) {
            builder.setColor(-13851168);
        }
        if (VERSION.SDK_INT >= 26) {
            NotificationsController.checkOtherNotificationsChannel();
            builder.setChannelId(NotificationsController.OTHER_NOTIFICATIONS_CHANNEL);
        }
        if (photo != null) {
            BitmapDrawable img = ImageLoader.getInstance().getImageFromMemory(photo, null, "50_50");
            if (img != null) {
                builder.setLargeIcon(img.getBitmap());
            } else {
                try {
                    float scaleFactor = 160.0f / ((float) AndroidUtilities.dp(50.0f));
                    Options options = new Options();
                    options.inSampleSize = scaleFactor < 1.0f ? 1 : (int) scaleFactor;
                    Bitmap bitmap = BitmapFactory.decodeFile(FileLoader.getPathToAttach(photo, true).toString(), options);
                    if (bitmap != null) {
                        builder.setLargeIcon(bitmap);
                    }
                } catch (Throwable e) {
                    FileLog.e(e);
                }
            }
        }
        this.ongoingCallNotification = builder.getNotification();
        startForeground(ID_ONGOING_CALL_NOTIFICATION, this.ongoingCallNotification);
    }

    protected void startRingtoneAndVibration(int chatID) {
        SharedPreferences prefs = MessagesController.getNotificationsSettings(this.currentAccount);
        AudioManager am = (AudioManager) getSystemService(MimeTypes.BASE_TYPE_AUDIO);
        boolean needRing = am.getRingerMode() != 0;
        if (VERSION.SDK_INT >= 21) {
            try {
                int mode = Global.getInt(getContentResolver(), "zen_mode");
                if (needRing) {
                    needRing = mode == 0;
                }
            } catch (Exception e) {
            }
        }
        if (needRing) {
            int vibrate;
            if (!USE_CONNECTION_SERVICE) {
                am.requestAudioFocus(this, 2, 1);
            }
            this.ringtonePlayer = new MediaPlayer();
            this.ringtonePlayer.setOnPreparedListener(new C09404());
            this.ringtonePlayer.setLooping(true);
            this.ringtonePlayer.setAudioStreamType(2);
            try {
                String notificationUri;
                if (prefs.getBoolean("custom_" + chatID, false)) {
                    notificationUri = prefs.getString("ringtone_path_" + chatID, RingtoneManager.getDefaultUri(1).toString());
                } else {
                    notificationUri = prefs.getString("CallsRingtonePath", RingtoneManager.getDefaultUri(1).toString());
                }
                this.ringtonePlayer.setDataSource(this, Uri.parse(notificationUri));
                this.ringtonePlayer.prepareAsync();
            } catch (Exception e2) {
                FileLog.e(e2);
                if (this.ringtonePlayer != null) {
                    this.ringtonePlayer.release();
                    this.ringtonePlayer = null;
                }
            }
            if (prefs.getBoolean("custom_" + chatID, false)) {
                vibrate = prefs.getInt("calls_vibrate_" + chatID, 0);
            } else {
                vibrate = prefs.getInt("vibrate_calls", 0);
            }
            if ((vibrate != 2 && vibrate != 4 && (am.getRingerMode() == 1 || am.getRingerMode() == 2)) || (vibrate == 4 && am.getRingerMode() == 1)) {
                this.vibrator = (Vibrator) getSystemService("vibrator");
                long duration = 700;
                if (vibrate == 1) {
                    duration = 700 / 2;
                } else if (vibrate == 3) {
                    duration = 700 * 2;
                }
                this.vibrator.vibrate(new long[]{0, duration, 500}, 0);
            }
        }
    }

    public void onDestroy() {
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("=============== VoIPService STOPPING ===============");
        }
        stopForeground(true);
        stopRinging();
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.appDidLogout);
        SensorManager sm = (SensorManager) getSystemService("sensor");
        if (sm.getDefaultSensor(8) != null) {
            sm.unregisterListener(this);
        }
        if (this.proximityWakelock != null && this.proximityWakelock.isHeld()) {
            this.proximityWakelock.release();
        }
        unregisterReceiver(this.receiver);
        if (this.timeoutRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(this.timeoutRunnable);
            this.timeoutRunnable = null;
        }
        super.onDestroy();
        sharedInstance = null;
        AndroidUtilities.runOnUIThread(new C09415());
        if (this.controller != null && this.controllerStarted) {
            this.lastKnownDuration = this.controller.getCallDuration();
            updateStats();
            StatsController.getInstance(this.currentAccount).incrementTotalCallsTime(getStatsNetworkType(), ((int) (this.lastKnownDuration / 1000)) % 5);
            onControllerPreRelease();
            this.controller.release();
            this.controller = null;
        }
        this.cpuWakelock.release();
        AudioManager am = (AudioManager) getSystemService(MimeTypes.BASE_TYPE_AUDIO);
        if (!USE_CONNECTION_SERVICE) {
            if (this.isBtHeadsetConnected && !this.playingSound) {
                am.stopBluetoothSco();
                am.setSpeakerphoneOn(false);
            }
            try {
                am.setMode(0);
            } catch (SecurityException x) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("Error setting audio more to normal", x);
                }
            }
            am.abandonAudioFocus(this);
        }
        am.unregisterMediaButtonEventReceiver(new ComponentName(this, VoIPMediaButtonReceiver.class));
        if (this.haveAudioFocus) {
            am.abandonAudioFocus(this);
        }
        if (!this.playingSound) {
            this.soundPool.release();
        }
        if (!(!USE_CONNECTION_SERVICE || this.systemCallConnection == null || this.playingSound)) {
            this.systemCallConnection.destroy();
        }
        ConnectionsManager.getInstance(this.currentAccount).setAppPaused(true, false);
        VoIPHelper.lastCallTime = System.currentTimeMillis();
    }

    protected void onControllerPreRelease() {
    }

    protected VoIPController createController() {
        return new VoIPController();
    }

    protected void initializeAccountRelatedThings() {
        updateServerConfig();
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.appDidLogout);
        ConnectionsManager.getInstance(this.currentAccount).setAppPaused(false, false);
        this.controller = createController();
        this.controller.setConnectionStateListener(this);
    }

    public void onCreate() {
        super.onCreate();
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("=============== VoIPService STARTING ===============");
        }
        AudioManager am = (AudioManager) getSystemService(MimeTypes.BASE_TYPE_AUDIO);
        if (VERSION.SDK_INT < 17 || am.getProperty("android.media.property.OUTPUT_FRAMES_PER_BUFFER") == null) {
            VoIPController.setNativeBufferSize(AudioTrack.getMinBufferSize(48000, 4, 2) / 2);
        } else {
            VoIPController.setNativeBufferSize(Integer.parseInt(am.getProperty("android.media.property.OUTPUT_FRAMES_PER_BUFFER")));
        }
        try {
            this.cpuWakelock = ((PowerManager) getSystemService("power")).newWakeLock(1, "telegram-voip");
            this.cpuWakelock.acquire();
            this.btAdapter = am.isBluetoothScoAvailableOffCall() ? BluetoothAdapter.getDefaultAdapter() : null;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            if (!USE_CONNECTION_SERVICE) {
                filter.addAction(ACTION_HEADSET_PLUG);
                if (this.btAdapter != null) {
                    filter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
                    filter.addAction("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");
                }
                filter.addAction("android.intent.action.PHONE_STATE");
            }
            registerReceiver(this.receiver, filter);
            this.soundPool = new SoundPool(1, 0, 0);
            this.spConnectingId = this.soundPool.load(this, R.raw.voip_connecting, 1);
            this.spRingbackID = this.soundPool.load(this, R.raw.voip_ringback, 1);
            this.spFailedID = this.soundPool.load(this, R.raw.voip_failed, 1);
            this.spEndId = this.soundPool.load(this, R.raw.voip_end, 1);
            this.spBusyId = this.soundPool.load(this, R.raw.voip_busy, 1);
            am.registerMediaButtonEventReceiver(new ComponentName(this, VoIPMediaButtonReceiver.class));
            if (!USE_CONNECTION_SERVICE && this.btAdapter != null && this.btAdapter.isEnabled()) {
                boolean z;
                if (this.btAdapter.getProfileConnectionState(1) == 2) {
                    z = true;
                } else {
                    z = false;
                }
                updateBluetoothHeadsetState(z);
                Iterator it = this.stateListeners.iterator();
                while (it.hasNext()) {
                    ((StateListener) it.next()).onAudioSettingsChanged();
                }
            }
        } catch (Exception x) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.e("error initializing voip controller", x);
            }
            callFailed();
        }
    }

    protected void dispatchStateChanged(int state) {
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("== Call " + getCallID() + " state changed to " + state + " ==");
        }
        this.currentState = state;
        if (USE_CONNECTION_SERVICE && state == 3 && this.systemCallConnection != null) {
            this.systemCallConnection.setActive();
        }
        for (int a = 0; a < this.stateListeners.size(); a++) {
            ((StateListener) this.stateListeners.get(a)).onStateChanged(state);
        }
    }

    protected void updateStats() {
        this.controller.getStats(this.stats);
        long wifiSentDiff = this.stats.bytesSentWifi - this.prevStats.bytesSentWifi;
        long wifiRecvdDiff = this.stats.bytesRecvdWifi - this.prevStats.bytesRecvdWifi;
        long mobileSentDiff = this.stats.bytesSentMobile - this.prevStats.bytesSentMobile;
        long mobileRecvdDiff = this.stats.bytesRecvdMobile - this.prevStats.bytesRecvdMobile;
        Stats tmp = this.stats;
        this.stats = this.prevStats;
        this.prevStats = tmp;
        if (wifiSentDiff > 0) {
            StatsController.getInstance(this.currentAccount).incrementSentBytesCount(1, 0, wifiSentDiff);
        }
        if (wifiRecvdDiff > 0) {
            StatsController.getInstance(this.currentAccount).incrementReceivedBytesCount(1, 0, wifiRecvdDiff);
        }
        if (mobileSentDiff > 0) {
            StatsController instance = StatsController.getInstance(this.currentAccount);
            int i = (this.lastNetInfo == null || !this.lastNetInfo.isRoaming()) ? 0 : 2;
            instance.incrementSentBytesCount(i, 0, mobileSentDiff);
        }
        if (mobileRecvdDiff > 0) {
            instance = StatsController.getInstance(this.currentAccount);
            i = (this.lastNetInfo == null || !this.lastNetInfo.isRoaming()) ? 0 : 2;
            instance.incrementReceivedBytesCount(i, 0, mobileRecvdDiff);
        }
    }

    protected void configureDeviceForCall() {
        this.needPlayEndSound = true;
        AudioManager am = (AudioManager) getSystemService(MimeTypes.BASE_TYPE_AUDIO);
        if (!USE_CONNECTION_SERVICE) {
            am.setMode(3);
            am.requestAudioFocus(this, 0, 1);
            if (isBluetoothHeadsetConnected() && hasEarpiece()) {
                switch (this.audioRouteToSet) {
                    case 0:
                        am.setBluetoothScoOn(false);
                        am.setSpeakerphoneOn(false);
                        break;
                    case 1:
                        am.setBluetoothScoOn(false);
                        am.setSpeakerphoneOn(true);
                        break;
                    case 2:
                        if (!this.bluetoothScoActive) {
                            this.needSwitchToBluetoothAfterScoActivates = true;
                            try {
                                am.startBluetoothSco();
                                break;
                            } catch (Throwable th) {
                                break;
                            }
                        }
                        am.setBluetoothScoOn(true);
                        am.setSpeakerphoneOn(false);
                        break;
                }
            } else if (isBluetoothHeadsetConnected()) {
                am.setBluetoothScoOn(this.speakerphoneStateToSet);
            } else {
                am.setSpeakerphoneOn(this.speakerphoneStateToSet);
            }
        }
        updateOutputGainControlState();
        this.audioConfigured = true;
        SensorManager sm = (SensorManager) getSystemService("sensor");
        Sensor proximity = sm.getDefaultSensor(8);
        if (proximity != null) {
            try {
                this.proximityWakelock = ((PowerManager) getSystemService("power")).newWakeLock(32, "telegram-voip-prx");
                sm.registerListener(this, proximity, 3);
            } catch (Exception x) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("Error initializing proximity sensor", x);
                }
            }
        }
    }

    @SuppressLint({"NewApi"})
    public void onSensorChanged(SensorEvent event) {
        boolean newIsNear = true;
        if (event.sensor.getType() == 8) {
            AudioManager am = (AudioManager) getSystemService(MimeTypes.BASE_TYPE_AUDIO);
            if (!this.isHeadsetPlugged && !am.isSpeakerphoneOn()) {
                if (!isBluetoothHeadsetConnected() || !am.isBluetoothScoOn()) {
                    if (event.values[0] >= Math.min(event.sensor.getMaximumRange(), 3.0f)) {
                        newIsNear = false;
                    }
                    if (newIsNear != this.isProximityNear) {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("proximity " + newIsNear);
                        }
                        this.isProximityNear = newIsNear;
                        try {
                            if (this.isProximityNear) {
                                this.proximityWakelock.acquire();
                            } else {
                                this.proximityWakelock.release(1);
                            }
                        } catch (Exception x) {
                            FileLog.e(x);
                        }
                    }
                }
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public boolean isBluetoothHeadsetConnected() {
        if (!USE_CONNECTION_SERVICE || this.systemCallConnection == null || this.systemCallConnection.getCallAudioState() == null) {
            return this.isBtHeadsetConnected;
        }
        return (this.systemCallConnection.getCallAudioState().getSupportedRouteMask() & 2) != 0;
    }

    public void onAudioFocusChange(int focusChange) {
        if (focusChange == 1) {
            this.haveAudioFocus = true;
        } else {
            this.haveAudioFocus = false;
        }
    }

    protected void updateBluetoothHeadsetState(boolean connected) {
        if (connected != this.isBtHeadsetConnected) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("updateBluetoothHeadsetState: " + connected);
            }
            this.isBtHeadsetConnected = connected;
            final AudioManager am = (AudioManager) getSystemService(MimeTypes.BASE_TYPE_AUDIO);
            if (!connected || isRinging() || this.currentState == 0) {
                this.bluetoothScoActive = false;
            } else if (this.bluetoothScoActive) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("SCO already active, setting audio routing");
                }
                am.setSpeakerphoneOn(false);
                am.setBluetoothScoOn(true);
            } else {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("startBluetoothSco");
                }
                this.needSwitchToBluetoothAfterScoActivates = true;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        try {
                            am.startBluetoothSco();
                        } catch (Throwable th) {
                        }
                    }
                }, 500);
            }
            Iterator it = this.stateListeners.iterator();
            while (it.hasNext()) {
                ((StateListener) it.next()).onAudioSettingsChanged();
            }
        }
    }

    public int getLastError() {
        return this.lastError;
    }

    public int getCallState() {
        return this.currentState;
    }

    protected void updateNetworkType() {
        NetworkInfo info = ((ConnectivityManager) getSystemService("connectivity")).getActiveNetworkInfo();
        this.lastNetInfo = info;
        int type = 0;
        if (info != null) {
            switch (info.getType()) {
                case 0:
                    switch (info.getSubtype()) {
                        case 1:
                            type = 1;
                            break;
                        case 2:
                        case 7:
                            type = 2;
                            break;
                        case 3:
                        case 5:
                            type = 3;
                            break;
                        case 6:
                        case 8:
                        case 9:
                        case 10:
                        case 12:
                        case 15:
                            type = 4;
                            break;
                        case 13:
                            type = 5;
                            break;
                        default:
                            type = 11;
                            break;
                    }
                case 1:
                    type = 6;
                    break;
                case 9:
                    type = 7;
                    break;
            }
        }
        if (this.controller != null) {
            this.controller.setNetworkType(type);
        }
    }

    protected void callFailed() {
        int lastError = (this.controller == null || !this.controllerStarted) ? 0 : this.controller.getLastError();
        callFailed(lastError);
    }

    protected Bitmap getRoundAvatarBitmap(TLObject userOrChat) {
        Bitmap bitmap = null;
        BitmapDrawable img;
        Options opts;
        if (userOrChat instanceof User) {
            User user = (User) userOrChat;
            if (!(user.photo == null || user.photo.photo_small == null)) {
                img = ImageLoader.getInstance().getImageFromMemory(user.photo.photo_small, null, "50_50");
                if (img != null) {
                    bitmap = img.getBitmap().copy(Config.ARGB_8888, true);
                } else {
                    try {
                        opts = new Options();
                        opts.inMutable = true;
                        bitmap = BitmapFactory.decodeFile(FileLoader.getPathToAttach(user.photo.photo_small, true).toString(), opts);
                    } catch (Throwable e) {
                        FileLog.e(e);
                    }
                }
            }
        } else {
            TLRPC$Chat chat = (TLRPC$Chat) userOrChat;
            if (!(chat.photo == null || chat.photo.photo_small == null)) {
                img = ImageLoader.getInstance().getImageFromMemory(chat.photo.photo_small, null, "50_50");
                if (img != null) {
                    bitmap = img.getBitmap().copy(Config.ARGB_8888, true);
                } else {
                    try {
                        opts = new Options();
                        opts.inMutable = true;
                        bitmap = BitmapFactory.decodeFile(FileLoader.getPathToAttach(chat.photo.photo_small, true).toString(), opts);
                    } catch (Throwable e2) {
                        FileLog.e(e2);
                    }
                }
            }
        }
        if (bitmap == null) {
            AvatarDrawable placeholder;
            Theme.createDialogsResources(this);
            if (userOrChat instanceof User) {
                placeholder = new AvatarDrawable((User) userOrChat);
            } else {
                placeholder = new AvatarDrawable((TLRPC$Chat) userOrChat);
            }
            bitmap = Bitmap.createBitmap(AndroidUtilities.dp(42.0f), AndroidUtilities.dp(42.0f), Config.ARGB_8888);
            placeholder.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            placeholder.draw(new Canvas(bitmap));
        }
        Canvas canvas = new Canvas(bitmap);
        Path circlePath = new Path();
        circlePath.addCircle((float) (bitmap.getWidth() / 2), (float) (bitmap.getHeight() / 2), (float) (bitmap.getWidth() / 2), Direction.CW);
        circlePath.toggleInverseFillType();
        Paint paint = new Paint(1);
        paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        canvas.drawPath(circlePath, paint);
        return bitmap;
    }

    protected void showIncomingNotification(String name, CharSequence subText, TLObject userOrChat, List<User> list, int additionalMemberCount, Class<? extends Activity> activityOnClick) {
        Intent intent = new Intent(this, activityOnClick);
        intent.addFlags(805306368);
        Notification.Builder builder = new Notification.Builder(this).setContentTitle(LocaleController.getString("VoipInCallBranding", R.string.VoipInCallBranding)).setContentText(name).setSmallIcon(R.drawable.notification).setSubText(subText).setContentIntent(PendingIntent.getActivity(this, 0, intent, 0));
        if (VERSION.SDK_INT >= 26) {
            SharedPreferences nprefs = MessagesController.getGlobalNotificationsSettings();
            int chanIndex = nprefs.getInt("calls_notification_channel", 0);
            NotificationManager nm = (NotificationManager) getSystemService("notification");
            NotificationChannel existingChannel = nm.getNotificationChannel("incoming_calls" + chanIndex);
            boolean needCreate = true;
            if (existingChannel != null) {
                if (existingChannel.getImportance() >= 4 && existingChannel.getSound() == null && existingChannel.getVibrationPattern() == null) {
                    needCreate = false;
                } else {
                    FileLog.d("User messed up the notification channel; deleting it and creating a proper one");
                    nm.deleteNotificationChannel("incoming_calls" + chanIndex);
                    chanIndex++;
                    nprefs.edit().putInt("calls_notification_channel", chanIndex).commit();
                }
            }
            if (needCreate) {
                NotificationChannel chan = new NotificationChannel("incoming_calls" + chanIndex, LocaleController.getString("IncomingCalls", R.string.IncomingCalls), 4);
                chan.setSound(null, null);
                chan.enableVibration(false);
                chan.enableLights(false);
                nm.createNotificationChannel(chan);
            }
            builder.setChannelId("incoming_calls" + chanIndex);
        }
        Intent endIntent = new Intent(this, VoIPActionsReceiver.class);
        endIntent.setAction(getPackageName() + ".DECLINE_CALL");
        endIntent.putExtra("call_id", getCallID());
        CharSequence endTitle = LocaleController.getString("VoipDeclineCall", R.string.VoipDeclineCall);
        if (VERSION.SDK_INT >= 24) {
            CharSequence endTitle2 = new SpannableString(endTitle);
            ((SpannableString) endTitle2).setSpan(new ForegroundColorSpan(-769226), 0, endTitle2.length(), 0);
            endTitle = endTitle2;
        }
        PendingIntent endPendingIntent = PendingIntent.getBroadcast(this, 0, endIntent, 268435456);
        builder.addAction(R.drawable.ic_call_end_white_24dp, endTitle, endPendingIntent);
        Intent answerIntent = new Intent(this, VoIPActionsReceiver.class);
        answerIntent.setAction(getPackageName() + ".ANSWER_CALL");
        answerIntent.putExtra("call_id", getCallID());
        CharSequence answerTitle = LocaleController.getString("VoipAnswerCall", R.string.VoipAnswerCall);
        if (VERSION.SDK_INT >= 24) {
            CharSequence answerTitle2 = new SpannableString(answerTitle);
            ((SpannableString) answerTitle2).setSpan(new ForegroundColorSpan(-16733696), 0, answerTitle2.length(), 0);
            answerTitle = answerTitle2;
        }
        PendingIntent answerPendingIntent = PendingIntent.getBroadcast(this, 0, answerIntent, 268435456);
        builder.addAction(R.drawable.ic_call_white_24dp, answerTitle, answerPendingIntent);
        builder.setPriority(2);
        if (VERSION.SDK_INT >= 17) {
            builder.setShowWhen(false);
        }
        if (VERSION.SDK_INT >= 21) {
            builder.setColor(-13851168);
            builder.setVibrate(new long[0]);
            builder.setCategory(NotificationCompat.CATEGORY_CALL);
            builder.setFullScreenIntent(PendingIntent.getActivity(this, 0, intent, 0), true);
        }
        Notification incomingNotification = builder.getNotification();
        if (VERSION.SDK_INT >= 21) {
            RemoteViews customView = new RemoteViews(getPackageName(), LocaleController.isRTL ? R.layout.call_notification_rtl : R.layout.call_notification);
            customView.setTextViewText(R.id.name, name);
            User self;
            if (TextUtils.isEmpty(subText)) {
                customView.setViewVisibility(R.id.subtitle, 8);
                if (UserConfig.getActivatedAccountsCount() > 1) {
                    self = UserConfig.getInstance(this.currentAccount).getCurrentUser();
                    customView.setTextViewText(R.id.title, LocaleController.formatString("VoipInCallBrandingWithName", R.string.VoipInCallBrandingWithName, new Object[]{ContactsController.formatName(self.first_name, self.last_name)}));
                } else {
                    customView.setTextViewText(R.id.title, LocaleController.getString("VoipInCallBranding", R.string.VoipInCallBranding));
                }
            } else {
                if (UserConfig.getActivatedAccountsCount() > 1) {
                    self = UserConfig.getInstance(this.currentAccount).getCurrentUser();
                    customView.setTextViewText(R.id.subtitle, LocaleController.formatString("VoipAnsweringAsAccount", R.string.VoipAnsweringAsAccount, new Object[]{ContactsController.formatName(self.first_name, self.last_name)}));
                } else {
                    customView.setViewVisibility(R.id.subtitle, 8);
                }
                customView.setTextViewText(R.id.title, subText);
            }
            customView.setTextViewText(R.id.answer_text, LocaleController.getString("VoipAnswerCall", R.string.VoipAnswerCall));
            customView.setTextViewText(R.id.decline_text, LocaleController.getString("VoipDeclineCall", R.string.VoipDeclineCall));
            customView.setImageViewBitmap(R.id.photo, getRoundAvatarBitmap(userOrChat));
            customView.setOnClickPendingIntent(R.id.answer_btn, answerPendingIntent);
            customView.setOnClickPendingIntent(R.id.decline_btn, endPendingIntent);
            incomingNotification.bigContentView = customView;
            incomingNotification.headsUpContentView = customView;
        }
        startForeground(ID_INCOMING_CALL_NOTIFICATION, incomingNotification);
    }

    protected void callFailed(int errorCode) {
        try {
            throw new Exception("Call " + getCallID() + " failed with error code " + errorCode);
        } catch (Exception x) {
            FileLog.e(x);
            this.lastError = errorCode;
            dispatchStateChanged(4);
            if (!(errorCode == -3 || this.soundPool == null)) {
                this.playingSound = true;
                this.soundPool.play(this.spFailedID, 1.0f, 1.0f, 0, 0, 1.0f);
                AndroidUtilities.runOnUIThread(this.afterSoundRunnable, 1000);
            }
            if (USE_CONNECTION_SERVICE && this.systemCallConnection != null) {
                this.systemCallConnection.setDisconnected(new DisconnectCause(1));
                this.systemCallConnection.destroy();
                this.systemCallConnection = null;
            }
            stopSelf();
        }
    }

    void callFailedFromConnectionService() {
        if (this.isOutgoing) {
            callFailed(-5);
        } else {
            hangUp();
        }
    }

    public void onConnectionStateChanged(int newState) {
        if (newState == 4) {
            callFailed();
            return;
        }
        if (newState == 3) {
            if (this.spPlayID != 0) {
                this.soundPool.stop(this.spPlayID);
                this.spPlayID = 0;
            }
            if (!this.wasEstablished) {
                this.wasEstablished = true;
                if (!this.isProximityNear) {
                    Vibrator vibrator = (Vibrator) getSystemService("vibrator");
                    if (vibrator.hasVibrator()) {
                        vibrator.vibrate(100);
                    }
                }
                AndroidUtilities.runOnUIThread(new C09437(), DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
                if (this.isOutgoing) {
                    StatsController.getInstance(this.currentAccount).incrementSentItemsCount(getStatsNetworkType(), 0, 1);
                } else {
                    StatsController.getInstance(this.currentAccount).incrementReceivedItemsCount(getStatsNetworkType(), 0, 1);
                }
            }
        }
        if (newState == 5) {
            if (this.spPlayID != 0) {
                this.soundPool.stop(this.spPlayID);
            }
            this.spPlayID = this.soundPool.play(this.spConnectingId, 1.0f, 1.0f, 0, -1, 1.0f);
        }
        dispatchStateChanged(newState);
    }

    public void onSignalBarCountChanged(int newCount) {
        this.signalBarCount = newCount;
        for (int a = 0; a < this.stateListeners.size(); a++) {
            ((StateListener) this.stateListeners.get(a)).onSignalBarsCountChanged(newCount);
        }
    }

    protected void callEnded() {
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("Call " + getCallID() + " ended");
        }
        dispatchStateChanged(11);
        if (this.needPlayEndSound) {
            this.playingSound = true;
            this.soundPool.play(this.spEndId, 1.0f, 1.0f, 0, 0, 1.0f);
            AndroidUtilities.runOnUIThread(this.afterSoundRunnable, 700);
        }
        if (this.timeoutRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(this.timeoutRunnable);
            this.timeoutRunnable = null;
        }
        if (USE_CONNECTION_SERVICE) {
            Runnable r = new C09448();
            if (this.needPlayEndSound) {
                AndroidUtilities.runOnUIThread(r, 700);
            } else {
                r.run();
            }
        }
        stopSelf();
    }

    public boolean isOutgoing() {
        return this.isOutgoing;
    }

    public void handleNotificationAction(Intent intent) {
        if ((getPackageName() + ".END_CALL").equals(intent.getAction())) {
            stopForeground(true);
            hangUp();
        } else if ((getPackageName() + ".DECLINE_CALL").equals(intent.getAction())) {
            stopForeground(true);
            declineIncomingCall(4, null);
        } else if ((getPackageName() + ".ANSWER_CALL").equals(intent.getAction())) {
            acceptIncomingCallFromNotification();
        }
    }

    private void acceptIncomingCallFromNotification() {
        showNotification();
        if (VERSION.SDK_INT < 23 || checkSelfPermission("android.permission.RECORD_AUDIO") == 0) {
            acceptIncomingCall();
            try {
                PendingIntent.getActivity(this, 0, new Intent(this, getUIActivityClass()).addFlags(805306368), 0).send();
                return;
            } catch (Exception x) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("Error starting incall activity", x);
                    return;
                }
                return;
            }
        }
        try {
            PendingIntent.getActivity(this, 0, new Intent(this, VoIPPermissionActivity.class).addFlags(268435456), 0).send();
        } catch (Exception x2) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.e("Error starting permission activity", x2);
            }
        }
    }

    public void updateOutputGainControlState() {
        int i = 0;
        int i2 = 1;
        if (this.controller != null && this.controllerStarted) {
            VoIPController voIPController;
            if (USE_CONNECTION_SERVICE) {
                boolean isEarpiece;
                if (this.systemCallConnection.getCallAudioState().getRoute() == 1) {
                    isEarpiece = true;
                } else {
                    isEarpiece = false;
                }
                this.controller.setAudioOutputGainControlEnabled(isEarpiece);
                voIPController = this.controller;
                if (!isEarpiece) {
                    i = 1;
                }
                voIPController.setEchoCancellationStrength(i);
                return;
            }
            boolean z;
            AudioManager am = (AudioManager) getSystemService(MimeTypes.BASE_TYPE_AUDIO);
            VoIPController voIPController2 = this.controller;
            if (!hasEarpiece() || am.isSpeakerphoneOn() || am.isBluetoothScoOn() || this.isHeadsetPlugged) {
                z = false;
            } else {
                z = true;
            }
            voIPController2.setAudioOutputGainControlEnabled(z);
            voIPController = this.controller;
            if (this.isHeadsetPlugged || !(!hasEarpiece() || am.isSpeakerphoneOn() || am.isBluetoothScoOn() || this.isHeadsetPlugged)) {
                i2 = 0;
            }
            voIPController.setEchoCancellationStrength(i2);
        }
    }

    public int getAccount() {
        return this.currentAccount;
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.appDidLogout) {
            callEnded();
        }
    }

    public static boolean isAnyKindOfCallActive() {
        if (VoIPService.getSharedInstance() == null || VoIPService.getSharedInstance().getCallState() == 15) {
            return false;
        }
        return true;
    }

    protected boolean isFinished() {
        return this.currentState == 11 || this.currentState == 4;
    }

    protected boolean isRinging() {
        return false;
    }

    @TargetApi(26)
    protected PhoneAccountHandle addAccountToTelecomManager() {
        TelecomManager tm = (TelecomManager) getSystemService("telecom");
        User self = UserConfig.getInstance(this.currentAccount).getCurrentUser();
        PhoneAccountHandle handle = new PhoneAccountHandle(new ComponentName(this, TelegramConnectionService.class), "" + self.id);
        tm.registerPhoneAccount(new PhoneAccount.Builder(handle, ContactsController.formatName(self.first_name, self.last_name)).setCapabilities(2048).setIcon(Icon.createWithResource(this, R.drawable.ic_launcher)).setHighlightColor(-13851168).addSupportedUriScheme("sip").build());
        return handle;
    }

    private static boolean isDeviceCompatibleWithConnectionServiceAPI() {
        if (VERSION.SDK_INT < 26) {
            return false;
        }
        if ("angler".equals(Build.PRODUCT) || "bullhead".equals(Build.PRODUCT) || "sailfish".equals(Build.PRODUCT) || "marlin".equals(Build.PRODUCT) || "walleye".equals(Build.PRODUCT) || "taimen".equals(Build.PRODUCT) || MessagesController.getGlobalMainSettings().getBoolean("dbg_force_connection_service", false)) {
            return true;
        }
        return false;
    }
}
