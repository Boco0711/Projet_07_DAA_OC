package com.leprincesylvain.ocproject7.go4lunch.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Workmate implements Parcelable {
    private String workmateId;
    private String userProfilePicture;
    private String userName;
    private String userMail;
    private String restaurantChoice;
    private int dateOfChoice;

    public Workmate() {
    }

    public Workmate(String userProfilePicture, String userName, String userMail) {
        this.userProfilePicture = userProfilePicture;
        this.userName = userName;
        this.userMail = userMail;
    }

    public static final Creator<Workmate> CREATOR = new Creator<Workmate>() {
        @Override
        public Workmate createFromParcel(Parcel source) {
            return new Workmate(source);
        }

        @Override
        public Workmate[] newArray(int size) {
            return new Workmate[size];
        }
    };

    public String getUserProfilePicture() {
        return userProfilePicture;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserMail() {
        return userMail;
    }

    public String getRestaurantChoice() {
        return restaurantChoice;
    }

    public int getDateOfChoice() {
        return dateOfChoice;
    }

    protected Workmate(Parcel in) {
        userProfilePicture = in.readString();
        userName = in.readString();
        userMail = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userProfilePicture);
        dest.writeString(userMail);
        dest.writeString(userName);
    }
}
