package com.leprincesylvain.ocproject7.go4lunch.model;

public class Location {

    private String lat;
    private String lng;

    public Location(String s1, String s2) {
        this.lat = s1;
        this.lng = s2;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }
}
