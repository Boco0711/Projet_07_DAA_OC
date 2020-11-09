package com.leprincesylvain.ocproject7.go4lunch.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ResponseToDetail {
    @SerializedName("html_attributions")
    @Expose
    private List<Object> html_attributions= new ArrayList<>();

    @SerializedName("result")
    @Expose
    private Restaurant restaurant;

    public Restaurant getRestaurant(){
        return restaurant;
    }
}
