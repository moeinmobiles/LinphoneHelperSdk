package com.inmobiles.demosdk.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.inmobiles.demosdk.R;
import com.inmobiles.demosdk.activity.CallIncomingActivity;
import com.inmobiles.demosdk.activity.CallOutgoingActivity;
import com.inmobiles.demosdk.callback.PhoneCallback;
import com.inmobiles.demosdk.callback.RegistrationCallback;
import com.inmobiles.demosdk.linphone_managment.LinphoneManager;

import org.linphone.core.AuthInfo;
import org.linphone.core.AuthMethod;
import org.linphone.core.Call;
import org.linphone.core.CallLog;
import org.linphone.core.CallStats;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.ConfiguringState;
import org.linphone.core.Content;
import org.linphone.core.Core;
import org.linphone.core.CoreListener;
import org.linphone.core.EcCalibratorStatus;
import org.linphone.core.Event;
import org.linphone.core.Friend;
import org.linphone.core.FriendList;
import org.linphone.core.GlobalState;
import org.linphone.core.InfoMessage;
import org.linphone.core.PresenceModel;
import org.linphone.core.ProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.RegistrationState;
import org.linphone.core.SubscriptionState;
import org.linphone.core.VersionUpdateCheckResult;
import org.linphone.mediastream.video.capture.hwconf.Hacks;

import static android.media.AudioManager.MODE_RINGTONE;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_VOICE_CALL;


public class LinphoneService extends Service implements CoreListener {
    private static final String TAG = "LinphoneService";
    private static LinphoneService instance;
    private static PhoneCallback sPhoneCallback;
    private static RegistrationCallback sRegistrationCallback;

    private Call ringingCall;

    private AudioManager mAudioManager;
    private boolean mAudioFocused;
    private MediaPlayer mRingerPlayer;
    private Vibrator mVibrator;

    String callingUSer="";
    boolean isCallRunning = false;
    boolean isIncomingCall=true;

    private NotificationManager notificationManager;

    public static boolean isReady() {
        return instance != null;
    }
    NotificationCompat.Builder notif;
    Notification notification;
    @Override
    public void onCreate() {
        super.onCreate();
//        LinphoneCoreFactoryImpl.instance();
        LinphoneManager.createAndStart(LinphoneService.this);
        instance = this;

        notificationManager = (NotificationManager)
               getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            setupChannels();

       notif = new NotificationCompat.Builder(this, "channeladmin");
        notif.setContentTitle("Linphone Service");
        notif.setContentText("");
        notif.setSmallIcon(R.mipmap.ic_launcher);
        notif.setCategory(Notification.CATEGORY_SERVICE);

         notification= notif.build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            startForeground(1, notification);
        }

        notificationManager.notify(1,notification);

        mAudioManager = ((AudioManager)getSystemService(Context.AUDIO_SERVICE));
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels() {
        CharSequence adminChannelName  = "notifications_admin_channel_name";
        String adminChannelDescription = "notifications_admin_channel_description";
        NotificationChannel adminChannel = new NotificationChannel(
                "channeladmin",
                adminChannelName,
                NotificationManager.IMPORTANCE_LOW
        );
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.setSound(null, null);
        notificationManager.createNotificationChannel(adminChannel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "LinphoneService onDestroy execute");
        removeAllCallback();
        LinphoneManager.getLc().stop();
        LinphoneManager.destroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void addPhoneCallback(PhoneCallback phoneCallback) {
        sPhoneCallback = phoneCallback;
    }

    public static void removePhoneCallback() {
        if (sPhoneCallback != null) {
            sPhoneCallback = null;
        }
    }

    public static void addRegistrationCallback(RegistrationCallback registrationCallback) {
        sRegistrationCallback = registrationCallback;
    }

    public static void removeRegistrationCallback() {
        if (sRegistrationCallback != null) {
            sRegistrationCallback = null;
        }
    }

    public void removeAllCallback() {
        removePhoneCallback();
        removeRegistrationCallback();
    }

    private void callThroughMobile(String channel) {
//        mChannel = channel;
//        if (LinphoneManager.getLc().isIncall()) {
//            LinphoneUtils.getInstance().hangUp();
//            MediaUtils.stop();
//        }
//        SPUtils.save(this, "channel", mChannel);
//        callNowChannel();
    }

    private void callNowChannel() {
//        if (!LinphoneManager.getLc().isIncall()) {
//            if (!mChannel.equals("")) {
//                PhoneBean phone = new PhoneBean();
//                phone.setUserName(mChannel);
//                phone.setHost("115.159.84.73");
//                LinphoneUtils.getInstance().startSingleCallingTo(phone);
//            }
//        }
    }



    @Override
    public void onTransferStateChanged(@NonNull Core core, @NonNull Call transfered, Call.State callState) {
        Log.e(TAG, "onTransferStateChanged: " + callState.name());
    }

    @Override
    public void onFriendListCreated(@NonNull Core core, @NonNull FriendList friendList) {

    }

    @Override
    public void onSubscriptionStateChanged(@NonNull Core core, @NonNull Event linphoneEvent, SubscriptionState state) {

    }

    @Override
    public void onCallLogUpdated(@NonNull Core core, @NonNull CallLog callLog) {
        Log.e(TAG, "onCallLogUpdated: " +"callLog : " + callLog.getErrorInfo().getReason().name());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCallStateChanged(@NonNull Core core, @NonNull Call call, Call.State state, @NonNull String message) {
        Log.e(TAG, "callState: " + state.toString() + "  " + message);
        if (state == Call.State.IncomingReceived) {
            if (sPhoneCallback!=null)
            sPhoneCallback.incomingCall(call);
            Log.d("testAddress",call.getRemoteAddress().getUsername());
            if (core.getCallsNb() == 1) {
                requestAudioFocus(STREAM_RING);

                ringingCall = call;
                startRinging();

                Intent intent = new Intent(LinphoneService.this, CallIncomingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("usercalling",call.getRemoteAddress().getUsername()+"@"+call.getRemoteAddress().getDomain());
                intent.putExtra("isCallRunning",false);
                this.startActivity(intent);
                // otherwise there is the beep
            }
//            startActivity(new Intent(getApplicationContext(), CallIncomingActivity.class));
        }
        else if (call == ringingCall && isRinging) {
            //previous state was ringing, so stop ringing
            stopRinging();
        }

        if (state == Call.State.OutgoingInit) {
            try {
                Log.d(TAG,"outgoing  "  + call.getToAddress().getUsername());
            }
            catch (Exception e){

            }
            if (sPhoneCallback!=null)
            sPhoneCallback.outgoingInit();

            setAudioManagerInCallMode();
            requestAudioFocus(STREAM_VOICE_CALL);
            startBluetooth();
            Intent intent;
            intent = new Intent(LinphoneService.this, CallOutgoingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("usercalling",call.getRemoteAddress().getUsername()+"@"+call.getRemoteAddress().getDomain());
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notif.setFullScreenIntent(pendingIntent,true);

            notification = notif.build();
            notificationManager.notify(1,notification);
            setupChannels();
            startForeground(1, notification);
        }

        if (state == Call.State.Connected) {
            if (sPhoneCallback!=null)
            sPhoneCallback.callConnected();
            Intent intent;
            if (call.getDir() == Call.Dir.Incoming){
                setAudioManagerInCallMode();
                //mAudioManager.abandonAudioFocus(null);
                requestAudioFocus(STREAM_VOICE_CALL);
                isCallRunning=true;
                isIncomingCall=true;
                callingUSer=call.getRemoteAddress().getUsername()+"@"+call.getRemoteAddress().getDomain();
                 intent = new Intent(LinphoneService.this, CallIncomingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                intent.putExtra("usercalling",callingUSer);
                intent.putExtra("isCallRunning",true);
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notif.setFullScreenIntent(pendingIntent,true);
            }
            else if (call.getDir() == Call.Dir.Outgoing){
                intent = new Intent(LinphoneService.this, CallOutgoingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("usercalling",call.getRemoteAddress().getUsername()+"@"+call.getRemoteAddress().getDomain());
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notif.setFullScreenIntent(pendingIntent,true);
                isCallRunning=true;
                isIncomingCall=false;
                callingUSer=call.getRemoteAddress().getUsername()+"@"+call.getRemoteAddress().getDomain();
            }

                    //It is for incoming calls, because outgoing calls enter MODE_IN_COMMUNICATION immediately when they start.
                    //However, incoming call first use the MODE_RINGING to play the local ring.
//                    enableSpeaker(true);

            notification = notif.build();
            notificationManager.notify(1,notification);
            setupChannels();
            startForeground(1, notification);
        }

        if (state == Call.State.Error ) {
            clearPendingIntent();
            isCallRunning=false;
            if (sPhoneCallback!=null)
            sPhoneCallback.error();
            int res = 0;
            if (mAudioFocused) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (focusRequest != null)
                        res = mAudioManager.abandonAudioFocusRequest(focusRequest);
                } else {
                    res = mAudioManager.abandonAudioFocus(null);
                }

                /* int res = mAudioManager.abandonAudioFocus(null);*/
                org.linphone.mediastream.Log.d("Audio focus released a bit later: " + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "Granted" : "Denied"));
                mAudioFocused = false;
            }
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                    org.linphone.mediastream.Log.d("---AudioManager: back to MODE_NORMAL");
                    mAudioManager.setMode(AudioManager.MODE_NORMAL);
                    org.linphone.mediastream.Log.d("All call terminated, routing back to earpiece");
                    routeAudioToReceiver();

            }
        }

        if (state == Call.State.End ) {
            clearPendingIntent();
            isCallRunning=false;
            if (sPhoneCallback!=null)
            sPhoneCallback.callEnd();
            int res = 0;
            if (mAudioFocused) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (focusRequest != null)
                        res = mAudioManager.abandonAudioFocusRequest(focusRequest);
                } else {
                    res = mAudioManager.abandonAudioFocus(null);
                }

                /* int res = mAudioManager.abandonAudioFocus(null);*/
                org.linphone.mediastream.Log.d("Audio focus released a bit later: " + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "Granted" : "Denied"));
                mAudioFocused = false;
            }
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                org.linphone.mediastream.Log.d("---AudioManager: back to MODE_NORMAL");
                mAudioManager.setMode(AudioManager.MODE_NORMAL);
                org.linphone.mediastream.Log.d("All call terminated, routing back to earpiece");
                routeAudioToReceiver();
            }
        }

        if (state == Call.State.Released ) {
            clearPendingIntent();
            isCallRunning=false;
            if (sPhoneCallback!=null)
            sPhoneCallback.callReleased();
        }

        if (state == Call.State.StreamsRunning) {
            isCallRunning=true;
            startBluetooth();
            requestAudioFocus(STREAM_VOICE_CALL);
            setAudioManagerInCallMode();
//            enableSpeaker(true);
        }
    }

    private void clearPendingIntent() {
        stopForeground(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notif = new NotificationCompat.Builder(this, "channeladmin");
            notif.setContentTitle("Linphone Service");
            notif.setContentText("");
            notif.setSmallIcon(R.mipmap.ic_launcher);
            notif.setCategory(Notification.CATEGORY_SERVICE);

            notification= notif.build();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setupChannels();
                startForeground(2, notification);
            }

            notificationManager.notify(2,notification);
        }
        else
        {
            notificationManager.cancelAll();
        }

    }

    @Override
    public void onAuthenticationRequested(@NonNull Core core, @NonNull AuthInfo authInfo, @NonNull AuthMethod method) {
        Log.e(TAG, "onAuthenticationRequested: " +authInfo.getRealm() + "  " + "method :  " + method.name());
    }

    @Override
    public void onNotifyPresenceReceivedForUriOrTel(@NonNull Core core, @NonNull Friend linphoneFriend, @NonNull String uriOrTel, @NonNull PresenceModel presenceModel) {

    }

    @Override
    public void onChatRoomStateChanged(@NonNull Core core, @NonNull ChatRoom chatRoom, ChatRoom.State state) {

    }

    @Override
    public void onBuddyInfoUpdated(@NonNull Core core, @NonNull Friend linphoneFriend) {

    }

    @Override
    public void onNetworkReachable(@NonNull Core core, boolean reachable) {
    }

    @Override
    public void onNotifyReceived(@NonNull Core core, @NonNull Event linphoneEvent, @NonNull String notifiedEvent, @NonNull Content body) {
    }

    @Override
    public void onNewSubscriptionRequested(@NonNull Core core, @NonNull Friend linphoneFriend, @NonNull String url) {
    }

    @Override
    public void onCallStatsUpdated(@NonNull Core core, @NonNull Call call, @NonNull CallStats callStats) {
    }

    @Override
    public void onNotifyPresenceReceived(@NonNull Core core, @NonNull Friend linphoneFriend) {
    }

    @Override
    public void onEcCalibrationAudioInit(@NonNull Core core) {
    }

    @Override
    public void onMessageReceived(@NonNull Core core, @NonNull ChatRoom chatRoom, @NonNull ChatMessage message) {
    }

    @Override
    public void onEcCalibrationResult(@NonNull Core core, EcCalibratorStatus status, int delayMs) {
    }

    @Override
    public void onSubscribeReceived(@NonNull Core core, @NonNull Event linphoneEvent, @NonNull String subscribeEvent, @NonNull Content body) {

    }

    @Override
    public void onInfoReceived(@NonNull Core core, @NonNull Call call, @NonNull InfoMessage message) {

    }

    @Override
    public void onChatRoomRead(@NonNull Core core, @NonNull ChatRoom chatRoom) {

    }

    @Override
    public void onRegistrationStateChanged(@NonNull Core core, @NonNull ProxyConfig proxyConfig, RegistrationState state, @NonNull String message) {
        Log.d(TAG,"proxyconfig  " +proxyConfig.getContact().getUsername());
        Log.d(TAG,"registrationState: " + state.name());
        if (sRegistrationCallback != null ){
            if (state.equals(RegistrationState.None.toString())) {
                sRegistrationCallback.registrationNone();
            } else if (state.equals(RegistrationState.Progress)) {
                sRegistrationCallback.registrationProgress();
            } else if (state.equals(RegistrationState.Ok)) {
                sRegistrationCallback.registrationOk();
            } else if (state.equals(RegistrationState.Cleared)) {
                sRegistrationCallback.registrationCleared();
            } else if (state.equals(RegistrationState.Failed)) {
                sRegistrationCallback.registrationFailed();
            }
        }

    }
    @Override
    public void onFriendListRemoved(@NonNull Core core, @NonNull FriendList friendList) {

    }

    @Override
    public void onReferReceived(@NonNull Core core, @NonNull String referTo) {

    }

    @Override
    public void onQrcodeFound(@NonNull Core core, @Nullable String result) {

    }

    @Override
    public void onConfiguringStatus(@NonNull Core core, ConfiguringState status, @Nullable String message) {

    }

    @Override
    public void onCallCreated(@NonNull Core core, @NonNull Call call) {

    }

    @Override
    public void onPublishStateChanged(@NonNull Core core, @NonNull Event linphoneEvent, PublishState state) {

    }

    @Override
    public void onCallEncryptionChanged(@NonNull Core core, @NonNull Call call, boolean mediaEncryptionEnabled, @Nullable String authenticationToken) {

    }

    @Override
    public void onIsComposingReceived(@NonNull Core core, @NonNull ChatRoom chatRoom) {

    }

    @Override
    public void onMessageReceivedUnableDecrypt(@NonNull Core core, @NonNull ChatRoom chatRoom, @NonNull ChatMessage message) {

    }

    @Override
    public void onLogCollectionUploadProgressIndication(@NonNull Core core, int offset, int total) {

    }

    @Override
    public void onChatRoomSubjectChanged(Core lc, ChatRoom cr) {

    }

//    @Override
//    public void onChatRoomSubjectChanged(Core lc, ChatRoom cr) {
//
//    }

    @Override
    public void onVersionUpdateCheckResultReceived(@NonNull Core core, @NonNull VersionUpdateCheckResult result, String version, @Nullable String url) {

    }

    @Override
    public void onEcCalibrationAudioUninit(@NonNull Core core) {

    }

    @Override
    public void onGlobalStateChanged(@NonNull Core core, GlobalState state, @NonNull String message) {

    }

    @Override
    public void onLogCollectionUploadStateChanged(@NonNull Core core, Core.LogCollectionUploadState state, @NonNull String info) {

    }

    @Override
    public void onDtmfReceived(@NonNull Core core, @NonNull Call call, int dtmf) {

    }

    @Override
    public void onChatRoomEphemeralMessageDeleted(Core lc, ChatRoom cr) {

    }

//    @Override
//    public void onChatRoomEphemeralMessageDeleted(Core lc, ChatRoom cr) {
//
//    }


    @Override
    public void onMessageSent(@NonNull Core core, @NonNull ChatRoom chatRoom, @NonNull ChatMessage message) {

    }


//    public void startEcCalibration() throws CoreException {
//        routeAudioToSpeaker();
//        setAudioManagerInCallMode();
//        org.linphone.mediastream.Log.i("Set audio mode on 'Voice Communication'");
//        requestAudioFocus(STREAM_VOICE_CALL);
//        int oldVolume = mAudioManager.getStreamVolume(STREAM_VOICE_CALL);
//        int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_VOICE_CALL);
//        mAudioManager.setStreamVolume(STREAM_VOICE_CALL, maxVolume, 0);
//        mLc.startEchoCancellerCalibration();
//        mAudioManager.setStreamVolume(STREAM_VOICE_CALL, oldVolume, 0);
//    }

//    public int startEchoTester() throws CoreException {
//        routeAudioToSpeaker();
//        setAudioManagerInCallMode();
//        org.linphone.mediastream.Log.i("Set audio mode on 'Voice Communication'");
//        requestAudioFocus(STREAM_VOICE_CALL);
//        int oldVolume = mAudioManager.getStreamVolume(STREAM_VOICE_CALL);
//        int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_VOICE_CALL);
//        int sampleRate = 44100;
//        mAudioManager.setStreamVolume(STREAM_VOICE_CALL, maxVolume, 0);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            String sampleRateProperty = mAudioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
//            sampleRate = Integer.parseInt(sampleRateProperty);
//        }
//        /*int status = */
//        mLc.startEchoTester(sampleRate);
//        /*if (status > 0)*/
////        echoTesterIsRunning = true;
//		/*else {
//			echoTesterIsRunning = false;
//			routeAudioToReceiver();
//			mAudioManager.setStreamVolume(STREAM_VOICE_CALL, oldVolume, 0);
//			((AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE)).setMode(AudioManager.MODE_NORMAL);
//			Log.i("Set audio mode on 'Normal'");
//		}*/
//        return 1;
//        //return status;
//    }

//    public int stopEchoTester() throws CoreException {
//        echoTesterIsRunning = false;
//        /*int status = */
//        mLc.stopEchoTester();
//        routeAudioToReceiver();
//        ((AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE)).setMode(AudioManager.MODE_NORMAL);
//        org.linphone.mediastream.Log.i("Set audio mode on 'Normal'");
//        return 1;//status;
//    }

//    public boolean getEchoTesterStatus() {
//        return echoTesterIsRunning;
//    }

    private boolean isRinging;

    private void requestAudioFocus(int stream) {
        if (!mAudioFocused) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setLegacyStreamType(stream)
                        .build();
                focusRequest =
                        new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                                .setAudioAttributes(playbackAttributes)
                                .setAcceptsDelayedFocusGain(true)
                                .setOnAudioFocusChangeListener(
                                        new AudioManager.OnAudioFocusChangeListener() {
                                            @Override
                                            public void onAudioFocusChange(int i) {
                                            }
                                        })
                                .build();
                int res = mAudioManager.requestAudioFocus(focusRequest);
                org.linphone.mediastream.Log.d("Audio focus requested: " + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "Granted" : "Denied"));

                if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;

            } else {
                int res = mAudioManager.requestAudioFocus(null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
                org.linphone.mediastream.Log.d("Audio focus requested: " + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "Granted" : "Denied"));
                if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;
            }
        }
    }

    AudioFocusRequest focusRequest;


//    public void enableDeviceRingtone(boolean use) {
//        if (use) {
//            mLc.setRing(null);
//        } else {
//            mLc.setRing(mRingSoundFile);
//        }
//    }

    private synchronized void startRinging() {
        routeAudioToSpeaker();

        mAudioManager.setMode(MODE_RINGTONE);

        try {
            if ((mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) && mVibrator != null) {
                long[] patern = {0, 1000, 1000};
//                mVibrator.vibrate(patern, 1);
            }
            if (mRingerPlayer == null) {
                requestAudioFocus(STREAM_RING);
                mRingerPlayer = new MediaPlayer();
                mRingerPlayer.setAudioStreamType(STREAM_RING);
                Uri ringuri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                mRingerPlayer.setDataSource(this,ringuri);
                mRingerPlayer.prepare();
                mRingerPlayer.setLooping(true);
                mRingerPlayer.start();
            } else {
                org.linphone.mediastream.Log.w("already ringing");
            }
        } catch (Exception e) {
            e.printStackTrace();
            org.linphone.mediastream.Log.e(e, "cannot handle incoming call");
        }
        isRinging = true;
    }

    private synchronized void stopRinging() {

        if (mRingerPlayer != null) {
            mRingerPlayer.stop();
            mRingerPlayer.release();
            mRingerPlayer = null;
        }
        if (mVibrator != null) {
//            mVibrator.cancel();
        }

        if (Hacks.needGalaxySAudioHack())
            mAudioManager.setMode(AudioManager.MODE_NORMAL);

        isRinging = false;


        //TODO : TO BE APPLIED LATER

        // You may need to call galaxys audio hack after this method
//        if (!BluetoothManager.getInstance().isBluetoothHeadsetAvailable()) {
//            if (mServiceContext.getResources().getBoolean(R.bool.isTablet)) {
//                org.linphone.mediastream.Log.d("Stopped ringing, routing back to speaker");
//                routeAudioToSpeaker();
//            } else {
//                org.linphone.mediastream.Log.d("Stopped ringing, routing back to earpiece");
//                routeAudioToReceiver();
//            }
//        }
    }

    //TODO: TO BE APPLIED LATER
    public void startBluetooth() {
//        if (BluetoothManager.getInstance().isBluetoothHeadsetAvailable()) {
//            BluetoothManager.getInstance().routeAudioToBluetooth();
//        }
    }

    public void setAudioManagerInCallMode() {
        if (mAudioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            org.linphone.mediastream.Log.w("[AudioManager] already in MODE_IN_COMMUNICATION, skipping...");
            return;
        }
        org.linphone.mediastream.Log.d("[AudioManager] Mode: MODE_IN_COMMUNICATION");

        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }


    private void routeAudioToSpeakerHelper(boolean speakerOn) {
        org.linphone.mediastream.Log.w("Routing audio to " + (speakerOn ? "speaker" : "earpiece") + ", disabling bluetooth audio route");

        enableSpeaker(speakerOn);
    }

    public void enableSpeaker(boolean enable) {
        mAudioManager.setSpeakerphoneOn(enable);
    }

    public void routeAudioToSpeaker() {
        routeAudioToSpeakerHelper(true);
    }

    public void routeAudioToReceiver() {
        routeAudioToSpeakerHelper(false);
    }
}