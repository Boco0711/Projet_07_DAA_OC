package com.leprincesylvain.ocproject7.go4lunch.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Restaurant implements Parcelable {
    private String id;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("formatted_address")
    @Expose
    private String formatted_address;

    @SerializedName("formatted_phone_number")
    @Expose
    private String formatted_phone_number;


    @SerializedName("website")
    @Expose
    private String website;

    @SerializedName("geometry")
    @Expose
    private Geometry geometry;

    @SerializedName("photos")
    @Expose
    private List<Photo> photos;

    @SerializedName("opening_hours")
    @Expose
    private OpeningHours openingHours;

    @SerializedName("rating")
    @Expose
    private double rating;


    public String getName() {
        return name;
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public String getFormatted_phone_number() {
        return formatted_phone_number;
    }

    public String getWebsite() {
        return website;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public Double getRating() {
        return rating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Restaurant(Parcel in) {
        id = in.readString();
        name = in.readString();
        formatted_address = in.readString();
        formatted_phone_number = in.readString();
        website = in.readString();
        rating = in.readDouble();
        photos = new ArrayList<Photo>();
        in.readList(photos, Photo.class.getClassLoader());
    }

    public static final Creator<Restaurant> CREATOR = new Creator<Restaurant>() {
        @Override
        public Restaurant createFromParcel(Parcel in) {
            return new Restaurant(in);
        }

        @Override
        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(formatted_address);
        dest.writeString(formatted_phone_number);
        dest.writeString(website);
        dest.writeDouble(rating);
        dest.writeList(photos);
    }
}
