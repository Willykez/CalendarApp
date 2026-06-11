package com.calendar.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class CalendarRepository {

    private final ContentResolver resolver;

    private static final String[] EVENT_PROJECTION = {
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.CALENDAR_COLOR,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.ORGANIZER,
    };

    private static final String[] CALENDAR_PROJECTION = {
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR,
    };

    public CalendarRepository(Context context) {
        this.resolver = context.getContentResolver();
    }

    /**
     * Load all event instances between startMillis and endMillis.
     * Uses CalendarContract.Instances so recurring events are properly expanded.
     */
    public List<CalendarEvent> getEvents(long startMillis, long endMillis) {
        List<CalendarEvent> events = new ArrayList<>();

        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        String[] instanceProjection = {
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ALL_DAY,
                CalendarContract.Instances.DESCRIPTION,
                CalendarContract.Instances.EVENT_LOCATION,
                CalendarContract.Instances.CALENDAR_COLOR,
                CalendarContract.Instances.CALENDAR_ID,
                CalendarContract.Instances.ORGANIZER,
        };

        try (Cursor cursor = resolver.query(
                builder.build(),
                instanceProjection,
                null,
                null,
                CalendarContract.Instances.BEGIN + " ASC")) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String title = cursor.getString(1);
                    if (title == null) title = "(No Title)";
                    long begin = cursor.getLong(2);
                    long end = cursor.getLong(3);
                    boolean allDay = cursor.getInt(4) == 1;
                    String desc = cursor.getString(5);
                    String loc = cursor.getString(6);
                    int color = cursor.getInt(7);
                    long calId = cursor.getLong(8);
                    String organizer = cursor.getString(9);

                    events.add(new CalendarEvent(id, title, begin, end, allDay,
                            desc, loc, color, null, calId, organizer));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return enrichWithCalendarNames(events);
    }

    /**
     * Get all available calendars on the device.
     */
    public List<CalendarInfo> getCalendars() {
        List<CalendarInfo> calendars = new ArrayList<>();
        try (Cursor cursor = resolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                CALENDAR_PROJECTION,
                null, null, null)) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    String account = cursor.getString(2);
                    int color = cursor.getInt(3);
                    if (name == null) name = account;
                    calendars.add(new CalendarInfo(id, name, account, color));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calendars;
    }

    /**
     * Insert a new event into the device calendar.
     */
    public long insertEvent(String title, long startMillis, long endMillis,
                            boolean allDay, String description, String location,
                            long calendarId, String timeZone) {
        ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        cv.put(CalendarContract.Events.TITLE, title);
        cv.put(CalendarContract.Events.DTSTART, startMillis);
        cv.put(CalendarContract.Events.DTEND, endMillis);
        cv.put(CalendarContract.Events.ALL_DAY, allDay ? 1 : 0);
        if (description != null && !description.isEmpty()) {
            cv.put(CalendarContract.Events.DESCRIPTION, description);
        }
        if (location != null && !location.isEmpty()) {
            cv.put(CalendarContract.Events.EVENT_LOCATION, location);
        }
        cv.put(CalendarContract.Events.EVENT_TIMEZONE,
                timeZone != null ? timeZone : TimeZone.getDefault().getID());

        try {
            Uri uri = resolver.insert(CalendarContract.Events.CONTENT_URI, cv);
            if (uri != null) {
                return Long.parseLong(uri.getLastPathSegment());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Delete an event by its ID.
     */
    public boolean deleteEvent(long eventId) {
        Uri deleteUri = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI, eventId);
        try {
            int rows = resolver.delete(deleteUri, null, null);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------------
    // Helpers
    // ---------------------

    private List<CalendarEvent> enrichWithCalendarNames(List<CalendarEvent> events) {
        if (events.isEmpty()) return events;
        List<CalendarInfo> calendars = getCalendars();
        List<CalendarEvent> enriched = new ArrayList<>();
        for (CalendarEvent event : events) {
            String calName = null;
            int calColor = event.getCalendarColor();
            for (CalendarInfo cal : calendars) {
                if (cal.getId() == event.getCalendarId()) {
                    calName = cal.getName();
                    if (calColor == 0) calColor = cal.getColor();
                    break;
                }
            }
            enriched.add(new CalendarEvent(
                    event.getId(), event.getTitle(),
                    event.getStartMillis(), event.getEndMillis(),
                    event.isAllDay(), event.getDescription(),
                    event.getLocation(), calColor,
                    calName, event.getCalendarId(),
                    event.getOrganizer()));
        }
        return enriched;
    }
}
