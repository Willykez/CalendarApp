package com.calendar.app.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final SimpleDateFormat DAY_OF_WEEK_FORMAT =
            new SimpleDateFormat("EEE", Locale.getDefault());
    private static final SimpleDateFormat MONTH_FORMAT =
            new SimpleDateFormat("MMMM", Locale.getDefault());
    private static final SimpleDateFormat MONTH_SHORT_FORMAT =
            new SimpleDateFormat("MMM", Locale.getDefault());
    private static final SimpleDateFormat YEAR_FORMAT =
            new SimpleDateFormat("yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
    private static final SimpleDateFormat DATETIME_FORMAT =
            new SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("h:mm a", Locale.getDefault());
    private static final SimpleDateFormat FULL_DATE_FORMAT =
            new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());

    public static String formatMonth(int year, int month) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1);
        return MONTH_FORMAT.format(c.getTime());
    }

    public static String formatMonthShort(int month) {
        Calendar c = Calendar.getInstance();
        c.set(2000, month, 1);
        return MONTH_SHORT_FORMAT.format(c.getTime());
    }

    public static String formatYear(int year) {
        return String.valueOf(year);
    }

    public static String formatDate(long millis) {
        return DATE_FORMAT.format(new Date(millis));
    }

    public static String formatFullDate(long millis) {
        return FULL_DATE_FORMAT.format(new Date(millis));
    }

    public static String formatDateTimeRange(long startMillis, long endMillis, boolean allDay) {
        if (allDay) {
            return formatFullDate(startMillis) + " · All-day";
        }
        Date startDate = new Date(startMillis);
        Date endDate = new Date(endMillis);

        // Same day
        Calendar s = Calendar.getInstance();
        Calendar e = Calendar.getInstance();
        s.setTime(startDate);
        e.setTime(endDate);

        if (s.get(Calendar.YEAR) == e.get(Calendar.YEAR)
                && s.get(Calendar.DAY_OF_YEAR) == e.get(Calendar.DAY_OF_YEAR)) {
            return new SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(startDate)
                    + "\n" + TIME_FORMAT.format(startDate)
                    + " – " + TIME_FORMAT.format(endDate);
        }
        return DATETIME_FORMAT.format(startDate) + "\n– " + DATETIME_FORMAT.format(endDate);
    }

    public static String formatTime(long millis) {
        return TIME_FORMAT.format(new Date(millis));
    }

    public static String formatAgendaDateLabel(long millis) {
        // "Oct 2\nWed"
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        String month = MONTH_SHORT_FORMAT.format(c.getTime());
        int day = c.get(Calendar.DAY_OF_MONTH);
        String dow = DAY_OF_WEEK_FORMAT.format(c.getTime());
        return month + " " + day + "\n" + dow;
    }

    public static Calendar getStartOfDay(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    public static Calendar getEndOfDay(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, 23, 59, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c;
    }

    public static Calendar getStartOfMonth(int year, int month) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    public static Calendar getEndOfMonth(int year, int month) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.MILLISECOND, -1);
        return c;
    }

    public static boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isToday(int year, int month, int day) {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) == year
                && today.get(Calendar.MONTH) == month
                && today.get(Calendar.DAY_OF_MONTH) == day;
    }

    public static int getWeekNumber(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        return c.get(Calendar.WEEK_OF_YEAR);
    }

    public static String formatDayLabel(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        boolean todayFlag = isToday(year, month, day);
        String dayStr = new SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(c.getTime());
        return todayFlag ? dayStr + ", today" : dayStr;
    }

    public static String formatWeekTabDay(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        return new SimpleDateFormat("E", Locale.getDefault()).format(c.getTime())
                .substring(0, 1) + "\n" + day;
    }
}
