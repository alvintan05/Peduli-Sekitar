package com.alvin.projekuas.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Preference {

    private final static String PREFERENCE_NAME = "UasPref";
    private final static String ID_KEY = "IdKey";

    private final SharedPreferences sharedPreferences;
    private Context c;

    public Preference(Context c) {
        this.c = c;
        sharedPreferences = c.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserId(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ID_KEY, userId);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(ID_KEY, "");
    }

    public void removeUserId() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(ID_KEY);
        editor.apply();
    }
}
