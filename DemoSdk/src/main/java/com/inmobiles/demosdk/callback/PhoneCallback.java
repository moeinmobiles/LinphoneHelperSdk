package com.inmobiles.demosdk.callback;

import org.linphone.core.Call;


public interface PhoneCallback {

    public void incomingCall(Call linphoneCall) ;


    public void outgoingInit() ;


    public void callConnected() ;

    public void callEnd() ;

    public void callReleased() ;

    public void error() ;
}