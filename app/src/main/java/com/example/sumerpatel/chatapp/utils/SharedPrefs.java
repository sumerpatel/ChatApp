package com.example.sumerpatel.chatapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

/**
 * Created by sumerpatel on 4/6/2018.
 */

public class SharedPrefs {
    private static SharedPrefs SHARED_PREFS_INST;
    private final SecurePreferences preferences;

    public SharedPrefs(Context context) {
        this.preferences = new SecurePreferences(context);
    }

    public static synchronized SharedPrefs getInstance(Context context){
        if (SHARED_PREFS_INST == null) {
            SHARED_PREFS_INST = new SharedPrefs(context);
        }
        return SHARED_PREFS_INST;
    }

    public void clearAppPrefs() {
        preferences.edit().clear().apply();
    }

    public void saveUserSignedIn(boolean signedUp){
        preferences.edit().putBoolean("signed_up", signedUp).apply();
    }

    public boolean getUserSignedIn(){
        return preferences.getBoolean("signed_up", false);
    }

    public void saveUsername(String userName){
        preferences.edit().putString("username", userName).apply();
    }

    public String getUsername(){
        return preferences.getString("username", "");
    }

    public void savePassword(String password){
        preferences.edit().putString("password", password).apply();
    }

    public String getPassword(){
        return preferences.getString("password", "");
    }

    public void saveName(String userMobileNumber, String name) {
        preferences.edit().putString(userMobileNumber + "_name", name).apply();
    }

    public String getName(String userMobileNumber){
        return preferences.getString(userMobileNumber + "_name", "");
    }
}
