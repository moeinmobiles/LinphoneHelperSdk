package com.inmobiles.demosdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.inmobiles.demosdk.activity.CallOutgoingActivity;
import com.inmobiles.demosdk.callback.PhoneCallback;
import com.inmobiles.demosdk.callback.RegistrationCallback;
import com.inmobiles.demosdk.linphone_managment.LinphoneManager;
import com.inmobiles.demosdk.linphone_managment.LinphoneUtils;
import com.inmobiles.demosdk.linphone_managment.User;
import com.inmobiles.demosdk.service.LinphoneService;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreException;
import org.linphone.core.Reason;

import static java.lang.Thread.sleep;


public class LinphoneImpl {
    private static RegServiceWaitThread mRegServiceWaitThread;
    private static PhoneServiceWaitThread mPhoneServiceWaitThread;
    private static String mUsername, mPassword, mServerIP;


    public static void startLinphone(Context context){
        startService(context);

//        listenToEvents(context);
    }

    private static void listenToEvents(Context context) {
   addPhoneCallBack(new PhoneCallback() {
       @Override
       public void incomingCall(Call linphoneCall) {

       }

       @Override
       public void outgoingInit() {

       }

       @Override
       public void callConnected() {

       }

       @Override
       public void callEnd() {

       }

       @Override
       public void callReleased() {

       }

       @Override
       public void error() {

       }
   });
   addCallback(new RegistrationCallback() {
       @Override
       public void registrationNone() {

       }

       @Override
       public void registrationProgress() {

       }

       @Override
       public void registrationOk() {

       }

       @Override
       public void registrationCleared() {

       }

       @Override
       public void registrationFailed() {

       }
   });
    }


    private static void startService(Context context) {
        if (!LinphoneService.isReady()) {
            Intent intent = new    Intent(context, LinphoneService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intent);
            else
            context.startService(intent);
        }
    }

    public static void setAccount(String username, String password, String serverIP) {
        mUsername = username;
        mPassword = password;
        mServerIP = serverIP;

        login();
    }

    public static void addCallback(RegistrationCallback registrationCallback) {
        if (LinphoneService.isReady()) {
            LinphoneService.addRegistrationCallback(registrationCallback);
        } else {
            mRegServiceWaitThread = new RegServiceWaitThread(registrationCallback);
            mRegServiceWaitThread.start();
        }
    }

    public static void addPhoneCallBack(PhoneCallback phoneCallback) {
        if (LinphoneService.isReady()) {
            LinphoneService.addPhoneCallback(phoneCallback);
        } else {
            mPhoneServiceWaitThread = new PhoneServiceWaitThread( phoneCallback);
            mPhoneServiceWaitThread.start();
        }
    }

    private static void login() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!LinphoneService.isReady()) {
                    try {
                        sleep(80);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                loginToServer();
            }
        }).start();
    }

    public static void callTo(String num, Activity context) {
        if (!LinphoneService.isReady() || !LinphoneManager.isInstanceiated()) {
            Log.d("testStartActivyt","dinotstart");
            return;
        }

        Intent intent = new Intent(context, CallOutgoingActivity.class);
        intent.putExtra("usercalling",num+"@"+mServerIP);
        context.startActivity(intent);

        Log.d("testStartActivyt","started");

        if (!num.equals("")) {
            User phone = new User();
            phone.setUserName(num);
            phone.setHost(mServerIP);
            LinphoneUtils.getInstance().startSingleCallingTo(phone,context);
        }
    }


    public static void acceptCall() {
        Call currentCall = LinphoneManager.getLc().getCurrentCall();
        CallParams params = LinphoneManager.getLc().createCallParams(currentCall);
        params.enableVideo(false);
        currentCall.acceptWithParams(params);

//            LinphoneManager.getLc().acceptCall(LinphoneManager.getLc().getCurrentCall());
    }

    public static void declineCall(){
        Call currentCall = LinphoneManager.getLc().getCurrentCall();
        currentCall.decline(Reason.Declined);
    }

    public static void hangUp() {
        LinphoneUtils.getInstance().hangUp();
    }


    public static void toggleMicro(boolean isMicMuted) {
        LinphoneUtils.getInstance().toggleMicro(isMicMuted);
    }


    private static void toggleSpeaker(boolean isSpeakerEnabled) {
//        LinphoneUtils.getInstance().toggleSpeaker(isSpeakerEnabled);
    }

    private static class RegServiceWaitThread extends Thread {

        private RegistrationCallback mRegistrationCallback;

        RegServiceWaitThread(RegistrationCallback registrationCallback) {
            mRegistrationCallback = registrationCallback;
        }

        @Override
        public void run() {
            super.run();
            while (!LinphoneService.isReady()) {
                try {
                    sleep(80);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            LinphoneService.addRegistrationCallback(mRegistrationCallback);
            mRegServiceWaitThread = null;
        }
    }

    private static class PhoneServiceWaitThread extends Thread {
        private PhoneCallback mPhoneCallback;

        PhoneServiceWaitThread(PhoneCallback phoneCallback) {
            mPhoneCallback = phoneCallback;
        }

        @Override
        public void run() {
            super.run();
            while (!LinphoneService.isReady()) {
                try {
                    sleep(80);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            LinphoneService.addPhoneCallback(mPhoneCallback);
            mPhoneServiceWaitThread = null;
        }
    }


    private static void loginToServer() {
        try {
            if (mUsername == null || mPassword == null || mServerIP == null) {
                throw new RuntimeException("The sip account is not configured.");
            }
            LinphoneUtils.getInstance().registerUserAuth(mUsername, mPassword, mServerIP);
        } catch (CoreException e) {
            Log.d("testCoreException",e.toString());
            e.printStackTrace();
        }
    }

    public static Core getLC() {
        return LinphoneManager.getLc();
    }



}