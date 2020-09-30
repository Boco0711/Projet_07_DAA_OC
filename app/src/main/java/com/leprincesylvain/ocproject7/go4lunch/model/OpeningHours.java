package com.leprincesylvain.ocproject7.go4lunch.model;

public class OpeningHours {

    private boolean open_now;
    private String[] weekday_text;
    private Period[] periods;

    public boolean isOpenNow() {
        return open_now;
    }

    public String[] getWeekdayText() {
        return weekday_text;
    }

    public Period[] getPeriod() {
        return periods;
    }
}
