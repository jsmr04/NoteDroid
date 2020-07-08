package com.example.notedroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AlertDialog;

public class Util {
    public static void showMessage(Context context, int theme, String title, String message, String positiveText, DialogInterface.OnClickListener onClickListener){
        new AlertDialog.Builder(context, theme)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveText, onClickListener)
                .create()
                .show();
    }

    public static String getPreference(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, "");
    }

    public static void setPreference(Context context, String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putString(key, value )
                .apply();
    }
}
