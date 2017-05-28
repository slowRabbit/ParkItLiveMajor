package com.devlovepreet.parkitlive.utils;

import android.content.Context;
import android.content.SharedPreferences;

import timber.log.Timber;

/**
 * Created by devlovepreet on 24/5/17.
 */

public class SessionManager {
    // LogCat tag
//    private static String TAG = SessionManager.class.getSimpleName();

    // Shared preferences file name
    private static final String PREF_NAME = "LoginPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_TOKEN = "";
    // Shared Preferences
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    // Shared pref mode
    int PRIVATE_MODE = 0;

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn, String token) {

        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putString(KEY_TOKEN, token);
        // commit changes
        editor.commit();

        Timber.d("User login session modified!");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getToken() {
        return pref.getString(KEY_TOKEN, "");
    }
}
