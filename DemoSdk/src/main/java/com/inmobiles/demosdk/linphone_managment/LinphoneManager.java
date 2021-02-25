package com.inmobiles.demosdk.linphone_managment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;


import com.inmobiles.demosdk.R;

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
import org.linphone.core.Factory;
import org.linphone.core.Friend;
import org.linphone.core.FriendList;
import org.linphone.core.GlobalState;
import org.linphone.core.InfoMessage;
import org.linphone.core.PayloadType;
import org.linphone.core.PresenceModel;
import org.linphone.core.ProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.RegistrationState;
import org.linphone.core.SubscriptionState;
import org.linphone.core.Transports;
import org.linphone.core.VersionUpdateCheckResult;
import org.linphone.mediastream.Log;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class LinphoneManager  implements CoreListener{
    private static final String TAG = "LinphoneManager";
    private static LinphoneManager instance;
    private Context mServiceContext;
    private Core mLc;
    private Timer mTimer;
    private static boolean sExited;

    private String mLPConfigXsd = null;
    private String mLinphoneFactoryConfigFile = null;
    public String mLinphoneConfigFile = null;
    private String mLinphoneRootCaFile = null;
    private String mRingSoundFile = null;
    private String mRingBackSoundFile = null;
    private String mPauseSoundFile = null;
    private String mChatDatabaseFile = null;

    public LinphoneManager(Context serviceContext) {
        mServiceContext = serviceContext;
        Factory.instance().setDebugMode(true, "LinphoneSDK");
        sExited = false;

        String basePath = mServiceContext.getFilesDir().getAbsolutePath();
        mLPConfigXsd = basePath + "/lpconfig.xsd";
        mLinphoneFactoryConfigFile = basePath + "/linphonerc";
        mLinphoneConfigFile = basePath + "/.linphonerc";
        mLinphoneRootCaFile = basePath + "/rootca.pem";
        mRingSoundFile = basePath + "/oldphone_mono.wav";
        mRingBackSoundFile = basePath + "/ringback.wav";
        mPauseSoundFile = basePath + "/toy_mono.wav";
//        mChatDatabaseFile = basePath + "/linphone-history.db";
//        mErrorToneFile = basePath + "/error.wav";

//        mAudioManager = ((AudioManager)mServiceContext.getSystemService(Context.AUDIO_SERVICE));
//        mVibrator = (Vibrator) mServiceContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public synchronized static final LinphoneManager createAndStart(Context context) {
        if (instance != null) {
            throw new RuntimeException("Linphone Manager is already initialized");
        }
        instance = new LinphoneManager(context);
        instance.startLibLinphone(context);
        return instance;
    }

    public static synchronized Core getLcIfManagerNotDestroyOrNull() {
        if (sExited || instance == null) {
            Log.e("Trying to get linphone core while LinphoneManager already destroyed or not created");
            return null;
        }
        return getLc();
    }

    public static final boolean isInstanceiated() {
        return instance != null;
    }

    public static synchronized final Core getLc() {
        return getInstance().mLc;
    }

    public static synchronized final LinphoneManager getInstance() {
        if (instance != null) {
            return instance;
        }
        if (sExited) {
            throw new RuntimeException("Linphone Manager was already destroyed. "
                    + "Better use getLcIfManagerNotDestroyed and check returned value");
        }
        throw new RuntimeException("Linphone Manager should be created before accessed");
    }

    private synchronized void startLibLinphone(Context context) {

            //TODO: UNCOMMENT
        try {
            copyAssetsFromPackage();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "startLibLinphone: cannot start linphone");
        }
            mLc = Factory.instance().createCore( mLinphoneConfigFile,
                    mLinphoneFactoryConfigFile,  context);
            mLc.addListener((CoreListener) context);

        Transports transports = mLc.getTransports();
        transports.setUdpPort(0);
        transports.setTcpPort(5080);
        transports.setTlsPort(5060);
        mLc.setTransports(transports);

            mLc.start();
            mLc.enableKeepAlive(true);

//            if (mLc.hasBuiltinEchoCanceller())
//                mLc.enableEchoCancellation(false);

            initLibLinphone();

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mLc != null) {
                                mLc.iterate();
                            }
                        }
                    });
                }
            };
            mTimer = new Timer("Linphone Scheduler");
            mTimer.schedule(task, 0, 20);

    }

    private synchronized void initLibLinphone(){

        setUserAgent();
        mLc.setRemoteRingbackTone(mRingSoundFile);
        mLc.setRing(mRingSoundFile);
        mLc.setPlayFile(mPauseSoundFile);
//        mLc.setCallErrorTone(Reason.NotFound, mErrorToneFile);

        int availableCores = Runtime.getRuntime().availableProcessors();
        Log.w(TAG, "MediaStreamer : " + availableCores + " cores detected and configured");
//        mLc.setCpuCount(availableCores);

        mLc.setNetworkReachable(true);

        mLc.enableEchoCancellation(true);

        mLc.enableAdaptiveRateControl(true);


//        mLc.enableVideoCapture(false);
//        mLc.enableVideoDisplay(false);
//        mLc.enableQrcodeVideoPreview(false);
//        mLc.enableVideoSourceReuse(false);
//        mLc.enableVideoAdaptiveJittcomp(false);
//        mLc.enableVideoMulticast(false);
//        mLc.enableVideoPreview(false);
//        mLc.enableSelfView(false);

        LinphoneUtils.getConfig(mServiceContext).setInt("audio", "codec_bitrate_limit", 36);

        mLc.setUploadBandwidth(1536);
        mLc.setDownloadBandwidth(1536);

        setCodecMime();

//        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
//        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        mServiceContext.registerReceiver(mKeepAliveReceiver, filter);
    }

    private void setCodecMime() {
        PayloadType[] ptList = mLc.getAudioPayloadTypes();
        for (PayloadType pt : ptList) {

            Log.d("payloadaudio",pt.getMimeType());
            if (/*pt.getMimeType().equalsIgnoreCase(codecName) ||*/ pt.getMimeType().equalsIgnoreCase("pcmu")) {
                pt.enable(true);

            } else {
                pt.enable(false);
            }
        }
        mLc.setAudioPayloadTypes(ptList);
//        for (PayloadType payloadType : mLc.getAudioPayloadTypes()) {
//
//                if ( payloadType.getMimeType().equalsIgnoreCase("pcmu")) {
//                    payloadType.enable(true);
//
//                } else {
//                    payloadType.enable(false);
//                }
//            }
//            payloadType.enable(true);
//            android.util.Log.e(TAG, "setCodecMime = " + payloadType.getMime() + " Rate " + payloadType.getRate() + " receviceFmtp " + payloadType.getRecvFmtp());
//            if (payloadType.getMime().equals("PCMA") && payloadType.getRate() == 8000) {
//                try {
//                    android.util.Log.e(TAG, "setCodecMime: " + payloadType.getMime() + " " + payloadType.getRate());
//                    mLc.enablePayloadType(payloadType, true);
//                } catch (LinphoneCoreException e) {
//                    android.util.Log.e(TAG, "setCodecMime: " + e);
//                }
//            } else {
//                try {
//                    mLc.enablePayloadType(payloadType, false);
//                } catch (LinphoneCoreException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        for (PayloadType payloadType : mLc.getVideoPayloadTypes()) {
//            payloadType.enable(false);
////                android.util.Log.e(TAG, "setCodecMime: mime: " + payloadType.getMime() + " rate: " + payloadType.getRate());
////                mLc.enablePayloadType(payloadType, true);
////                mLc.enableVideoCapture(false);
//        }
    }


//TODO: UNCOMMENT
    private void copyAssetsFromPackage() throws IOException {
//        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.oldphone_mono, mRingSoundFile);
//        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.ringback, mRingBackSoundFile);
//        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.toy_mono, mPauseSoundFile);
        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.linphonerc_default, mLinphoneConfigFile);
        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.linphonerc_factory, new File(mLinphoneFactoryConfigFile).getName());
        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.lpconfig, mLPConfigXsd);
//        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.rootca, mLinphoneRootCaFile);
    }

    private void setUserAgent() {
        try {
            String versionName = mServiceContext.getPackageManager().getPackageInfo(mServiceContext.getPackageName(),
                    0).versionName;
            if (versionName == null) {
                versionName = String.valueOf(mServiceContext.getPackageManager().getPackageInfo(mServiceContext.getPackageName(), 0).versionCode);
            }
//            mLc.setUserAgent("TelScale Restcomm Android Client ", "versionName");
            mLc.setUserAgent("TelScale Restcomm Android Client ", "");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            mLc.setUserAgent("TelScale Restcomm Android Client ", "");
        }
    }

    public static synchronized void destroy() {
        if (instance == null) {
            return;
        }
        sExited = true;
        instance.doDestroy();
    }

    private void doDestroy() {
        try {
            mTimer.cancel();
            mLc.stop();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            mLc = null;
            instance = null;
        }
    }

    @Override
    public void onTransferStateChanged(Core lc, Call transfered, Call.State newCallState) {

    }

    @Override
    public void onFriendListCreated(Core lc, FriendList list) {

    }

    @Override
    public void onSubscriptionStateChanged(Core lc, Event lev, SubscriptionState state) {

    }

    @Override
    public void onCallLogUpdated(Core lc, CallLog newcl) {

    }

    @Override
    public void onCallStateChanged(final Core lc, final Call call, final Call.State state, final String message) {

    }

    @Override
    public void onAuthenticationRequested(Core lc, AuthInfo authInfo, AuthMethod method) {

    }

    @Override
    public void onNotifyPresenceReceivedForUriOrTel(Core lc, Friend lf, String uriOrTel, PresenceModel presenceModel) {

    }

    @Override
    public void onChatRoomStateChanged(Core lc, ChatRoom cr, ChatRoom.State state) {

    }

    @Override
    public void onBuddyInfoUpdated(Core lc, Friend lf) {

    }

    @Override
    public void onNetworkReachable(Core lc, boolean reachable) {

    }

    @Override
    public void onNotifyReceived(Core lc, Event lev, String notifiedEvent, Content body) {

    }

    @Override
    public void onNewSubscriptionRequested(Core lc, Friend lf, String url) {

    }

    @Override
    public void onCallStatsUpdated(Core lc, Call call, CallStats stats) {

    }

    @Override
    public void onNotifyPresenceReceived(Core lc, Friend lf) {

    }

    @Override
    public void onEcCalibrationAudioInit(Core lc) {

    }

    @Override
    public void onMessageReceived(Core lc, ChatRoom room, ChatMessage message) {

    }

    @Override
    public void onEcCalibrationResult(Core lc, EcCalibratorStatus status, int delayMs) {

    }

    @Override
    public void onSubscribeReceived(Core lc, Event lev, String subscribeEvent, Content body) {

    }

    @Override
    public void onInfoReceived(Core lc, Call call, InfoMessage msg) {

    }

    @Override
    public void onChatRoomRead(Core lc, ChatRoom room) {

    }

    @Override
    public void onRegistrationStateChanged(Core lc, ProxyConfig cfg, RegistrationState cstate, String message) {

    }

    @Override
    public void onFriendListRemoved(Core lc, FriendList list) {

    }

    @Override
    public void onReferReceived(Core lc, String referTo) {

    }

    @Override
    public void onQrcodeFound(Core lc, String result) {

    }

    @Override
    public void onConfiguringStatus(Core lc, ConfiguringState status, String message) {

    }

    @Override
    public void onCallCreated(Core lc, Call call) {

    }

    @Override
    public void onPublishStateChanged(Core lc, Event lev, PublishState state) {

    }

    @Override
    public void onCallEncryptionChanged(Core lc, Call call, boolean on, String authenticationToken) {

    }

    @Override
    public void onIsComposingReceived(Core lc, ChatRoom room) {

    }

    @Override
    public void onMessageReceivedUnableDecrypt(Core lc, ChatRoom room, ChatMessage message) {

    }

    @Override
    public void onLogCollectionUploadProgressIndication(Core lc, int offset, int total) {

    }

    @Override
    public void onChatRoomSubjectChanged(Core lc, ChatRoom cr) {

    }

    @Override
    public void onVersionUpdateCheckResultReceived(Core lc, VersionUpdateCheckResult result, String version, String url) {

    }

    @Override
    public void onEcCalibrationAudioUninit(Core lc) {

    }

    @Override
    public void onGlobalStateChanged(Core lc, GlobalState gstate, String message) {

    }

    @Override
    public void onLogCollectionUploadStateChanged(Core lc, Core.LogCollectionUploadState state, String info) {

    }

    @Override
    public void onDtmfReceived(Core lc, Call call, int dtmf) {

    }

    @Override
    public void onChatRoomEphemeralMessageDeleted(Core lc, ChatRoom cr) {

    }

    @Override
    public void onMessageSent(Core lc, ChatRoom room, ChatMessage message) {

    }



}