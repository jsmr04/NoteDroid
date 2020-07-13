package com.example.notedroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Base64;

import androidx.appcompat.app.AlertDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

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

    public static ArrayList<String> getPreferences(Context context, String key) {
        ArrayList<String> preferences = new ArrayList<>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        Map<String,?> keys = sharedPreferences.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            if (entry.getKey().contains(key)){
                preferences.add(entry.getValue().toString());
            }
        }

        return preferences;
    }

    public static void setPreference(Context context, String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putString(key, value )
                .apply();
    }

    public static String bitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public static Bitmap stringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static String fileToString(Path path) {
        String temp = "";
        byte[] b = new byte[0];
        try {
            b = Files.readAllBytes(path);
            temp = Base64.encodeToString(b, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }


    public static File stringToFile(String encodedString, String fileName) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            File file = new File(fileName);
            try (FileOutputStream fileOuputStream = new FileOutputStream(file)){
                fileOuputStream.write(encodeByte);
                return file;
            }

        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
}
