package com.inmobiles.demosdk.linphone_managment;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Config;
import org.linphone.core.Core;
import org.linphone.core.CoreException;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.StreamType;
import org.linphone.core.TransportType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class LinphoneUtils {
    private static final String TAG = "LinphoneUtils";
    private static volatile LinphoneUtils sLinphoneUtils;
    private Core mLinphoneCore = null;

    public static LinphoneUtils getInstance() {
        if (sLinphoneUtils == null) {
            synchronized (LinphoneUtils.class) {
                if (sLinphoneUtils == null) {
                    sLinphoneUtils = new LinphoneUtils();
                }
            }
        }
        return sLinphoneUtils;
    }

    private LinphoneUtils() {
        mLinphoneCore = LinphoneManager.getLc();
        mLinphoneCore.enableEchoCancellation(true);
        mLinphoneCore.enableEchoLimiter(true);
    }


    public void registerUserAuth(String name, String password, String host) throws CoreException {

        String identify = "sip:" + name + "@" + host+";transport=tls";
        ProxyConfig prxCfg = mLinphoneCore.createProxyConfig();
        prxCfg.edit();
        Address identifyAddr = Factory.instance().createAddress(identify);
        identifyAddr.setTransport(TransportType.Tls);
//        identifyAddr.setPort(5060);
        prxCfg.setIdentityAddress(identifyAddr);
        prxCfg.setServerAddr(host);

        prxCfg.enableQualityReporting(false);
        prxCfg.setQualityReportingCollector(null);
        prxCfg.setQualityReportingInterval(0);
        prxCfg.enableRegister(true);
        prxCfg.done();

        Log.e(TAG, "registerUserAuth name = " + name);
        Log.e(TAG, "registerUserAuth pw = " + password);
        Log.e(TAG, "registerUserAuth host = " + host);


        AuthInfo authInfo = Factory.instance().createAuthInfo(name, null, password,
                null, host, host);
//        authInfo.clone();

        if (mLinphoneCore.getProxyConfigList().length>0) {
            for (ProxyConfig proxyConfig : mLinphoneCore.getProxyConfigList()) {
                if (prxCfg.getIdentityAddress().getUsername() == proxyConfig.getIdentityAddress().getUsername())
                    return;
                else {
                    mLinphoneCore.removeProxyConfig(proxyConfig);
                    mLinphoneCore.addProxyConfig(prxCfg);
                    mLinphoneCore.addAuthInfo(authInfo);
                    mLinphoneCore.setDefaultProxyConfig(prxCfg);
                }
            }
        }

        else
        {
            mLinphoneCore.addProxyConfig(prxCfg);
            mLinphoneCore.addAuthInfo(authInfo);
            mLinphoneCore.setDefaultProxyConfig(prxCfg);
        }
    }

    public Call startSingleCallingTo(User bean, Activity activity) {
        mLinphoneCore.enableMic(true);
        Address address;
        Call call = null;
        address = mLinphoneCore.interpretUrl("sip:"+bean.getUserName() + "@" + bean.getHost()+";transport=tls");
        address.setDisplayName(bean.getDisplayName());
        address.setDomain(bean.getHost());
        address.setTransport(TransportType.Tls);
//        address.setPort(5080);
        CallParams params = mLinphoneCore.createCallParams(null);

            params.enableVideo(false);
            params.enableAudio(true);

            Log.d("rtpprofile",params.getRtpProfile());

        params.addCustomSdpMediaAttribute(StreamType.Audio, "rtpmap", "0 PCMU/8000");

        call = mLinphoneCore.inviteAddressWithParams(address, params);
        return call;
    }

    public void hangUp() {
        Call currentCall = mLinphoneCore.getCurrentCall();
        if (currentCall != null) {
            mLinphoneCore.terminateAllCalls();
        } else if (mLinphoneCore.isInConference()) {
            mLinphoneCore.terminateConference();
        } else {
            mLinphoneCore.terminateAllCalls();
        }
    }

    public void toggleMicro(boolean isMicMuted) {
        mLinphoneCore.enableMic(!isMicMuted);
    }

    public static void copyIfNotExist(Context context, int resourceId, String target) throws IOException {
        File fileToCopy = new File(target);
        if (!fileToCopy.exists()) {
            copyFromPackage(context, resourceId, fileToCopy.getName());
        }
    }

    public static void copyFromPackage(Context context, int resourceId, String target) throws IOException {
        FileOutputStream outputStream = context.openFileOutput(target, 0);
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = inputStream.read(buff)) != -1) {
            outputStream.write(buff, 0, readByte);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    public static Config getConfig(Context context) {
        Core lc = getLc();
        if (lc != null) {
            return lc.getConfig();
        }

        if (LinphoneManager.isInstanceiated()) {
            org.linphone.mediastream.Log.w("LinphoneManager not instanciated yet...");
            return Factory.instance().createConfig(context.getFilesDir().getAbsolutePath() + "/.linphonerc");
        }

        return Factory.instance().createConfig(LinphoneManager.getInstance().mLinphoneConfigFile);
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Core getLc() {
        if (!LinphoneManager.isInstanceiated()) {
            return null;
        }
        return LinphoneManager.getLcIfManagerNotDestroyOrNull();
    }
}