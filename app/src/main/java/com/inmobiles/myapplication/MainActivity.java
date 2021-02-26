package com.inmobiles.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.inmobiles.demosdk.LinphoneImpl;
import com.inmobiles.demosdk.callback.RegistrationCallback;


public class MainActivity extends AppCompatActivity implements RegistrationCallback {

    Button btnCall,btnRegister;
    EditText etUserName,etPassword,etDomain,etCallUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinphoneImpl.addCallback(this);

        btnCall = (Button) findViewById(R.id.btnCall);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        etUserName = (EditText) findViewById(R.id.etUserName);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etDomain = (EditText) findViewById(R.id.etDomain);
        etCallUser = (EditText) findViewById(R.id.etCallUser);


        btnCall.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(etCallUser.getText().toString()))
                LinphoneImpl.callTo(etCallUser.getText().toString().trim(), MainActivity.this);
        });

        btnRegister.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(etUserName.getText().toString()) && !TextUtils.isEmpty(etPassword.getText().toString())
                    &&!TextUtils.isEmpty(etDomain.getText().toString()) )
                LinphoneImpl.setAccount(etUserName.getText().toString().trim(), etPassword.getText().toString().trim(),
                        etDomain.getText().toString().trim());
        });

    }

    @Override
    public void registrationNone() {
        Log.d("RegCallBAck", "registrationNone");
    }

    @Override
    public void registrationProgress() {
        Log.d("RegCallBAck", "registrationProgress");
    }

    @Override
    public void registrationOk() {
        Log.d("RegCallBAck", "registrationOk");
    }

    @Override
    public void registrationCleared() {
        Log.d("RegCallBAck", "registrationCleared");
    }

    @Override
    public void registrationFailed() {
        Log.d("RegCallBAck", "registrationFailed");
    }
}