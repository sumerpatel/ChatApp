package com.example.sumerpatel.chatapp.utils;

/**
 * Created by sumerpatel on 3/3/2017.
 */
public class UserDetails {
    public static String username = "";
    public static String password = "";
    static String time = "";
    static String title = "";

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "ah_firebase";

    public static final String TAG="chatbubbles";
    public static String chatWith = "";
}
