package com.nebuxe.cse535assignment.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesValues {

    public static void setUsername(Context context, String username) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SharedPreferencesKeys.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(Constants.SharedPreferencesKeys.USERNAME, username);

        editor.apply();
    }
    public static String getUsername(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SharedPreferencesKeys.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constants.SharedPreferencesKeys.USERNAME, "");
    }
}
