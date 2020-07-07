package com.example.notedroid.model;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;

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
}
