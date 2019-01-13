package com.wackymemes.library_tablet;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Map;

public class PreferenceManager {

    final static String PREFERENCES_IDENTIFIER = "LibraryPreferences";
    final static String PREFERENCES_BACKEND_AUTH_TOKEN = "authToken";
    final static String PREFERENCES_CONFIGURED = "preferencedConfigured";
    final static String PREFERENCES_BACKEND_BASE_URL = "baseUrl";

    final SharedPreferences preferences;

    private static PreferenceManager sharedInstance = null;

    public static PreferenceManager getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new PreferenceManager(context);
        }
        return sharedInstance;
    }

    private PreferenceManager(Context c) {
        preferences = c.getSharedPreferences(PREFERENCES_IDENTIFIER,
                Context.MODE_PRIVATE);
    }

    public boolean getApplicationConfigured() {
        return preferences.getBoolean(PREFERENCES_CONFIGURED, false);
    }

    public void setApplicationConfigured(boolean configured) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREFERENCES_CONFIGURED, configured);
        editor.apply();
    }

    public void removeAllSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        Map<String, ?> keys = preferences.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            editor.remove(entry.getKey());
        }
        editor.apply();
    }

    // TODO: remove the hardcoded URLs
    public String getBaseUrl() {
        return preferences.getString(PREFERENCES_BACKEND_BASE_URL, "http://naamataulu-backend.herokuapp.com/api/v1/");
    }

    public void setBaseUrl(String baseUrl) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCES_BACKEND_BASE_URL, baseUrl);
        editor.apply();
    }

    public String getToken() {
        return preferences.getString(PREFERENCES_BACKEND_AUTH_TOKEN, null);
    }

    public void setToken(String token) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCES_BACKEND_AUTH_TOKEN, token);
        editor.apply();
    }
}