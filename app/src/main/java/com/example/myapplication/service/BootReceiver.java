package com.example.myapplication.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;



public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here
            Log.d("qqqqq", "BootReceiver");
            Intent intentService = new Intent(context, StartAlarmIntentService.class);
            context.startService(intentService);

        }
    }
}
