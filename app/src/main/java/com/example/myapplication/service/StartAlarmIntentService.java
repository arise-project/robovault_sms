package com.example.myapplication.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

import static com.example.myapplication.MainActivity.APP_PREFERENCES;
import static com.example.myapplication.MainActivity.APP_PREFERENCES_REPLAY;
import static com.example.myapplication.MainActivity.APP_PREFERENCES_TIME;


public class StartAlarmIntentService extends IntentService {
    final static int RQS_TIME = 1;
    private SharedPreferences mSettings;

    public StartAlarmIntentService() {
        super("StartAlarm");
    }

    public void onCreate() {
        super.onCreate();
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("qqqqqq", "StartAlarmIntentService");
        setAlarm();
    }

    private void setAlarm() {
        Calendar targetCal = Calendar.getInstance();
        boolean repeat = true;
        if (mSettings.contains(APP_PREFERENCES_TIME)) {
            targetCal.setTimeInMillis(mSettings.getLong(APP_PREFERENCES_TIME, 0));
        }
        if (mSettings.contains(APP_PREFERENCES_REPLAY)) {
            repeat = mSettings.getBoolean(APP_PREFERENCES_REPLAY, true);
        }

        Calendar calNow = Calendar.getInstance();
        if (targetCal.compareTo(calNow) <= 0) {
            // Если выбранное время на сегодня прошло, то переносим на
            // завтра
            targetCal.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), RQS_TIME, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(),
                pendingIntent);
        // Если флажок установлен
        if (repeat) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    targetCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
                    pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    targetCal.getTimeInMillis(), pendingIntent);
        }


    }
}