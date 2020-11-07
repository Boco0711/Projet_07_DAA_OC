package com.leprincesylvain.ocproject7.go4lunch.model;

public class Prediction {
    private String placeId;
    private String name;

    public Prediction(String placeId, String name) {
        this.placeId = placeId;
        this.name = name;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getName() {
        return name;
    }
}
