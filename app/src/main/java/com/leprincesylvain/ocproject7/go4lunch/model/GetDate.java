package com.leprincesylvain.ocproject7.go4lunch.model;

import android.util.Log;

import java.util.Calendar;

public class GetDate {
    public static final String TAG = "GetDate: ";

    public static long getDate() {
        Calendar currentDate = Calendar.getInstance();

        int year = currentDate.get(Calendar.YEAR) * 10000;
        int month = currentDate.get(Calendar.MONTH) * 100;
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        long date = year + month + day;
        if (currentDate.get(Calendar.HOUR_OF_DAY) > 12)
            date++;
        Log.d(TAG, "getDate: " + date);
        return date;
    }
}
