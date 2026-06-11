package com.calendar.app.data;

public class CalendarEvent {
    private long id;
    private String title;
    private long startMillis;
    private long endMillis;
    private boolean allDay;
    private String description;
    private String location;
    private int calendarColor;
    private String calendarName;
    private long calendarId;
    private String organizer;

    public CalendarEvent(long id, String title, long startMillis, long endMillis,
                         boolean allDay, String description, String location,
                         int calendarColor, String calendarName, long calendarId,
                         String organizer) {
        this.id = id;
        this.title = title;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.allDay = allDay;
        this.description = description;
        this.location = location;
        this.calendarColor = calendarColor;
        this.calendarName = calendarName;
        this.calendarId = calendarId;
        this.organizer = organizer;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public long getStartMillis() { return startMillis; }
    public long getEndMillis() { return endMillis; }
    public boolean isAllDay() { return allDay; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public int getCalendarColor() { return calendarColor; }
    public String getCalendarName() { return calendarName; }
    public long getCalendarId() { return calendarId; }
    public String getOrganizer() { return organizer; }
}
