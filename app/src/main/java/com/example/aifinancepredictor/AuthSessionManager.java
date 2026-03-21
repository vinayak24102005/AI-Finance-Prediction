package com.example.aifinancepredictor;

import android.content.Context;
import android.content.SharedPreferences;

public final class AuthSessionManager {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";

    // Resets when app process restarts.
    private static boolean securityVerifiedThisProcess = false;

    private AuthSessionManager() {
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_LOGGED_IN, false);
    }

    public static void setLoggedIn(Context context, boolean loggedIn) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply();
        if (!loggedIn) {
            securityVerifiedThisProcess = false;
        }
    }

    public static void setRememberedEmail(Context context, String email) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public static String getRememberedEmail(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    public static boolean isSecurityVerifiedThisProcess() {
        return securityVerifiedThisProcess;
    }

    public static void markSecurityVerifiedThisProcess() {
        securityVerifiedThisProcess = true;
    }
}

