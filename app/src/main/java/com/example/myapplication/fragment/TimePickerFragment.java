package com.example.myapplication.fragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;

import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements
        TimePickerDialog.OnTimeSetListener {

    private TimePickedListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // установим текущее время в диалоговом окне
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // создадим экземпляр класса TimePickerDialog и вернем его
        TimePickerDialog dialog = new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // when the fragment is initially shown (i.e. attached to the activity),
        // cast the activity to the callback interface type
        try {
            mListener = (TimePickedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement " + TimePickedListener.class.getName());
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Пользователь выбрал время. Выводим его в текстовой метке
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        mListener.onTimePicked(calendar);
    }

    public interface TimePickedListener {
        void onTimePicked(Calendar time);
    }
}