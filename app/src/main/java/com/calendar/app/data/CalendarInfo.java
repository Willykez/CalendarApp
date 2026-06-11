package com.calendar.app.data;

public class CalendarInfo {
    private long id;
    private String name;
    private String accountName;
    private int color;

    public CalendarInfo(long id, String name, String accountName, int color) {
        this.id = id;
        this.name = name;
        this.accountName = accountName;
        this.color = color;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getAccountName() { return accountName; }
    public int getColor() { return color; }

    @Override
    public String toString() {
        return name;
    }
}
