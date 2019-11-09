package com.example.myapplication;



import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.fragment.TimePickerFragment;
import com.example.myapplication.service.AlarmReceiver;
import com.example.myapplication.service.BootReceiver;
import com.example.myapplication.service.StartAlarmIntentService;

import java.util.Calendar;

import br.com.sapereaude.maskedEditText.MaskedEditText;


public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, TimePickerFragment.TimePickedListener {

    final static int RQS_TIME = 1;
    private CheckBox repeatCheckBox;
    private TextView textClock;
    private Button btn_SetTime, btn_Cancel;
    private MaskedEditText edt_phoneNumber;
    private EditText edt_messageText;
    private int mHour;
    private int mMinute;
    private Calendar timeSending;

    // это будет именем файла настроек
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_TIME = "time"; // время отправки SMS
    public static final String APP_PREFERENCES_REPLAY = "replay"; // переодичьность
    public static final String APP_PREFERENCES_PHONE_NUMBER = "phone number"; // номер телефона
    public static final String APP_PREFERENCES_TEXT = "text"; // текст сообщения
    public static final String APP_PREFERENCES_STATUS = "status"; // текст сообщения
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onStart() {
        super.onStart();
        if (mSettings.getBoolean(APP_PREFERENCES_STATUS, false)) {
            getRecordedData();
            fieldBlock();
        } else unFieldBlock();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);
        setLayout();

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                Log.e("permission", "Permission already granted.");
            } else {
                //If the app doesn’t have the SEND_SMS permission, request it//
                requestPermission();
            }
        }

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = mSettings.edit();

        currentTime();
    }


    public boolean checkPermission() {
        int smsPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.SEND_SMS);

        return smsPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        Manifest.permission.SEND_SMS
                }, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean smsPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (smsPermission) {
                        Toast.makeText(MainActivity.this,
                                "Permission accepted", Toast.LENGTH_LONG).show();
                        //If the permission is denied…
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Permission denied", Toast.LENGTH_LONG).show();
                        // disable the call button.//
                        btn_SetTime.setEnabled(false);
                    }
                    break;
                }
        }
    }

    private void currentTime() {
        // получаем текущее время
        final Calendar calendar = Calendar.getInstance();
        timeSending = calendar;
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);

        // выводим текущее время
        updateDisplay();
    }

    private void setLayout() {
        textClock = findViewById(R.id.tv_textClock);
        textClock.setOnClickListener(this);
        btn_SetTime = findViewById(R.id.btn_SetTime);
        btn_SetTime.setOnClickListener(this);
        btn_Cancel = findViewById(R.id.btn_Cancel);
        btn_Cancel.setOnClickListener(this);
        repeatCheckBox = findViewById(R.id.checkBoxRepeat);
        edt_phoneNumber = findViewById(R.id.phone_input);
        edt_messageText = findViewById(R.id.edt_messageText);
    }

    @Override
    public void onClick(View view) {
        ComponentName receiver;
        PackageManager pm;
        switch (view.getId()) {
            case R.id.tv_textClock:
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                DialogFragment timeFragment = new TimePickerFragment();
                timeFragment.show(getSupportFragmentManager(), "timePicker");
                break;
            case R.id.btn_SetTime:
                if (edt_phoneNumber.getRawText().toString().length() != 10)
                    edt_phoneNumber.setError(getString(R.string.text_setErrorNumber));
                else if (edt_messageText.getText().toString().trim().isEmpty()) {
                    edt_messageText.setHint(getString(R.string.text_setErrorText));
                    edt_messageText.requestFocus();
                    InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(edt_messageText, 0);
                } else {
                    receiver = new ComponentName(this, BootReceiver.class);
                    pm = this.getPackageManager();
                    pm.setComponentEnabledSetting(receiver,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);

                    editor.putLong(APP_PREFERENCES_TIME, timeSending.getTimeInMillis());
                    editor.putBoolean(APP_PREFERENCES_REPLAY, repeatCheckBox.isChecked());
                    editor.putString(APP_PREFERENCES_PHONE_NUMBER, edt_phoneNumber.getRawText().toString());
                    editor.putString(APP_PREFERENCES_TEXT, edt_messageText.getText().toString());
                    editor.putBoolean(APP_PREFERENCES_STATUS, true);
                    editor.apply();

                    setAlarm();
                    fieldBlock();

                    String alert_text = "";
                    if (repeatCheckBox.isChecked()) {
                        alert_text = this.getString(R.string.text_toastSetTime) + "\n"
                                + "\t\t\t" + timeSending.get(Calendar.HOUR_OF_DAY) + " : " + timeSending.get(Calendar.MINUTE) + "\n"
                                + "Повторяется каждые сутки";
                    } else {
                        alert_text = this.getString(R.string.text_toastSetTime) + "\n"
                                + "\t\t\t" + timeSending.get(Calendar.HOUR_OF_DAY) + " : " + timeSending.get(Calendar.MINUTE) + "\n"
                                + "Сработает один раз";
                    }
                    Toast toast = Toast.makeText(this, alert_text, Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
            case R.id.btn_Cancel:
                cancelAlarm();

                receiver = new ComponentName(this, BootReceiver.class);
                pm = this.getPackageManager();
                pm.setComponentEnabledSetting(receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);

                break;
        }
    }

    public void onTimePicked(Calendar time) {
        timeSending = time;
        // выводим выбранную дату в текстовой метке 1
        mHour = time.get(Calendar.HOUR_OF_DAY);
        mMinute = time.get(Calendar.MINUTE);

        updateDisplay();
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    // обновляем дату для вывода
    public void updateDisplay() {
        textClock.setText(new StringBuilder().append(pad(mHour)).append(":")
                .append(pad(mMinute)));
    }


    private void setAlarm() {
        Intent intentService = new Intent(this, StartAlarmIntentService.class);
        startService(intentService);
    }

    private void cancelAlarm() {
        Toast toast = Toast.makeText(this,
                this.getString(R.string.text_toastCancel), Toast.LENGTH_SHORT);
        toast.show();


        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), RQS_TIME, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        unFieldBlock();
    }

    // блокировка экрана
    private void fieldBlock() {
        textClock.setClickable(false);
        textClock.setTextColor(getResources().getColor(R.color.colorBlock));
        repeatCheckBox.setClickable(false);
        repeatCheckBox.setTextColor(getResources().getColor(R.color.colorBlock));
        edt_phoneNumber.setTextColor(getResources().getColor(R.color.colorBlock));
        edt_phoneNumber.setFocusable(false);
        edt_messageText.setTextColor(getResources().getColor(R.color.colorBlock));
        edt_messageText.setFocusable(false);
        btn_SetTime.setClickable(false);
        btn_SetTime.setTextColor(getResources().getColor(R.color.colorBlock));
        btn_Cancel.setClickable(true);
        btn_Cancel.setTextColor(getResources().getColor(R.color.colorUnBlock));
    }

    // установленные данные отправки SMS
    private void getRecordedData() {
        Long L = 0l;
        if (mSettings.contains(APP_PREFERENCES_TIME)) {
            L = (mSettings.getLong(APP_PREFERENCES_TIME, 0));
        }
        if (L == 0) currentTime();
        else {
            timeSending.setTimeInMillis(L);
            mHour = timeSending.get(Calendar.HOUR_OF_DAY);
            mMinute = timeSending.get(Calendar.MINUTE);
            updateDisplay();
        }
        if (mSettings.contains(APP_PREFERENCES_REPLAY)) {
            repeatCheckBox.setChecked(mSettings.getBoolean(APP_PREFERENCES_REPLAY, false));
        }
        if (mSettings.contains(APP_PREFERENCES_PHONE_NUMBER)) {
            edt_phoneNumber.setText(mSettings.getString(APP_PREFERENCES_PHONE_NUMBER, ""));
        }
        if (mSettings.contains(APP_PREFERENCES_TEXT)) {
            edt_messageText.setText(mSettings.getString(APP_PREFERENCES_TEXT, ""));
        }
    }

    // разблокировка экрана
    private void unFieldBlock() {
        textClock.setClickable(true);
        textClock.setTextColor(getResources().getColor(R.color.colorUnBlock));
        repeatCheckBox.setClickable(true);
        repeatCheckBox.setTextColor(getResources().getColor(R.color.colorUnBlock));
        edt_phoneNumber.setTextColor(getResources().getColor(R.color.colorUnBlock));
        edt_phoneNumber.setFocusableInTouchMode(true);
        edt_messageText.setTextColor(getResources().getColor(R.color.colorUnBlock));
        edt_messageText.setFocusableInTouchMode(true);
        btn_SetTime.setClickable(true);
        btn_SetTime.setTextColor(getResources().getColor(R.color.colorUnBlock));
        btn_Cancel.setClickable(false);
        btn_Cancel.setTextColor(getResources().getColor(R.color.colorBlock));

        currentTime();
        repeatCheckBox.setChecked(false);
        edt_phoneNumber.setText("");
        edt_messageText.setText("");
        editor.putBoolean(APP_PREFERENCES_STATUS, false);
        editor.apply();
    }

    public void replay() {
        unFieldBlock();
    }
}
