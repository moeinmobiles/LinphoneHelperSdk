package com.inmobiles.demosdk.callback;

public interface RegistrationCallback {
    public void registrationNone() ;

    public void registrationProgress() ;

    public void registrationOk() ;

    public void registrationCleared() ;

    public void registrationFailed() ;
}