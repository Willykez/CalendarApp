package com.calendar.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String PREFS_NAME = "calendar_prefs";
    private static final String KEY_SHOW_WEEK_NUMBERS = "show_week_numbers";
    private static final String KEY_START_WEEK_SUNDAY = "start_week_sunday";

    private final SharedPreferences prefs;

    public Prefs(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean showWeekNumbers() {
        return prefs.getBoolean(KEY_SHOW_WEEK_NUMBERS, true);
    }

    public void setShowWeekNumbers(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_WEEK_NUMBERS, show).apply();
    }

    public boolean startWeekOnSunday() {
        return prefs.getBoolean(KEY_START_WEEK_SUNDAY, true);
    }

    public void setStartWeekOnSunday(boolean sunday) {
        prefs.edit().putBoolean(KEY_START_WEEK_SUNDAY, sunday).apply();
    }
}
