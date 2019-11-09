package com.example.myapplication.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.widget.Toast;

import static com.example.myapplication.MainActivity.APP_PREFERENCES;
import static com.example.myapplication.MainActivity.APP_PREFERENCES_PHONE_NUMBER;
import static com.example.myapplication.MainActivity.APP_PREFERENCES_REPLAY;
import static com.example.myapplication.MainActivity.APP_PREFERENCES_STATUS;
import static com.example.myapplication.MainActivity.APP_PREFERENCES_TEXT;


public class AlarmReceiver extends BroadcastReceiver {
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;
    private String phone_number, text;
    private Boolean replay;

    @Override
    public void onReceive(Context context, Intent intent) {


        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = mSettings.edit();

        if (mSettings.contains(APP_PREFERENCES_PHONE_NUMBER)) {
            phone_number = mSettings.getString(APP_PREFERENCES_PHONE_NUMBER, "");
        }
        if (mSettings.contains(APP_PREFERENCES_TEXT)) {
            text = mSettings.getString(APP_PREFERENCES_TEXT, "   ");
        }
        if (mSettings.contains(APP_PREFERENCES_REPLAY)) {
            replay = mSettings.getBoolean(APP_PREFERENCES_REPLAY, false);
        }
        if (!replay) {
            editor.putBoolean(APP_PREFERENCES_STATUS, false);
            editor.apply();

        }

        SmsManager smsManager = SmsManager.getDefault();
        // отправляем сообщение
        smsManager
                .sendTextMessage(phone_number, null, text, null, null);
        Toast toast = Toast.makeText(context,
                "SMS отправленно", Toast.LENGTH_SHORT);
        toast.show();

    }
}