package com.leprincesylvain.ocproject7.go4lunch.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ResponseToPlace {
    @SerializedName("html_attributions")
    @Expose
    private List<Object> html_attributions = new ArrayList<>();

    @SerializedName("results")
    @Expose
    private List<PlaceId> results = new ArrayList<>();

    @SerializedName("status")
    @Expose
    private String status;

    public List<Object> getHtml_attributions() {
        return html_attributions;
    }

    public void setHtml_attributions(List<Object> html_attributions) {
        this.html_attributions = html_attributions;
    }

    public List<PlaceId> getResults() {
        return results;
    }

    public void setResults(List<PlaceId> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
