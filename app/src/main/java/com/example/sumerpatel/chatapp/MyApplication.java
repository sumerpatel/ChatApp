package com.example.sumerpatel.chatapp;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by sumerpatel on 4/10/2018.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
