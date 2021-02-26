package com.inmobiles.myapplication;

import android.app.Application;

import com.inmobiles.demosdk.LinphoneImpl;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LinphoneImpl.startLinphone(this);
    }
}
