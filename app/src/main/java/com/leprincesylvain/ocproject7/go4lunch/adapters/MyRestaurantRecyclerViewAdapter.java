package com.leprincesylvain.ocproject7.go4lunch.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.leprincesylvain.ocproject7.go4lunch.R;
import com.leprincesylvain.ocproject7.go4lunch.controller.activities.RestaurantDetailActivity;
import com.leprincesylvain.ocproject7.go4lunch.model.GetDate;
import com.leprincesylvain.ocproject7.go4lunch.model.Period;
import com.leprincesylvain.ocproject7.go4lunch.model.Restaurant;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyRestaurantRecyclerViewAdapter extends RecyclerView.Adapter<MyRestaurantRecyclerViewAdapter.ViewHolder> {
    private final static String TAG = "RecyclerViewAdapter_Tag";


    private final List<Restaurant> restaurantList;
    private final LatLng latLng;
    long date = GetDate.getDate();
    private int j;
    Context context;

    int intOfToday;
    int currentHourAsInt;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection("Users");

    public MyRestaurantRecyclerViewAdapter(List<Restaurant> restaurantList, LatLng latLng) {
        this.restaurantList = restaurantList;
        this.latLng = latLng;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        setSomeValue();
        final Restaurant restaurant = restaurantList.get(position);

        LatLng restaurantLatLng = new LatLng(Double.parseDouble(restaurant.getGeometry().getLocation().getLat()), Double.parseDouble(restaurant.getGeometry().getLocation().getLng()));
        int meterDistance = distanceBetweenTwoPoints(latLng, restaurantLatLng);
        String distance = meterDistance + "m";

        holder.mRestaurantName.setText(restaurant.getName());
        int iend = restaurant.getFormatted_address().indexOf(',');
        String adresse = restaurant.getFormatted_address().substring(0, iend);

        holder.mRestaurantAdresse.setText(adresse);
        holder.mRestaurantDistance.setText(distance);
        if (restaurant.getPhotos() != null && restaurant.getPhotos().get(0).getPhoto_reference().length() > 0) {
            Picasso.get().load(getPhoto(restaurant.getPhotos().get(0).getPhoto_reference())).into(holder.mRestaurantImage);
        } else {
            holder.mRestaurantImage.setImageResource(R.drawable.restaurant_no_image_found);
        }

        displayCorrectSentenceOnHourField(holder, restaurant);
        Double note = (restaurant.getRating());
        displayCorrectRatingForRestaurant(holder, note);
        displayCorrectNumberOfCoworker(holder, restaurant);

        holder.mRestaurantParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), RestaurantDetailActivity.class);
                intent.putExtra("restaurant", restaurant);
                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (restaurantList != null) {
            return restaurantList.size();
        } else
            return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mRestaurantName, mRestaurantAdresse, mRestaurantHours, mRestaurantDistance, mRestaurantCoworkers;
        ImageView mRestaurantImage, mRestaurantImageCoworkers, mRestaurantStar1, mRestaurantStar2, mRestaurantStar3;
        ConstraintLayout mRestaurantParentLayout, mRestaurantBoxCoworkers;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mRestaurantName = itemView.findViewById(R.id.restaurant_name);
            mRestaurantAdresse = itemView.findViewById(R.id.restaurant_info);
            mRestaurantHours = itemView.findViewById(R.id.restaurant_open_hours);
            mRestaurantDistance = itemView.findViewById(R.id.restaurant_distance);
            mRestaurantCoworkers = itemView.findViewById(R.id.restaurant_number_of_coworker);
            mRestaurantImage = itemView.findViewById(R.id.restaurant_image);
            mRestaurantImageCoworkers = itemView.findViewById(R.id.restaurant_coworker_logo);
            mRestaurantStar1 = itemView.findViewById(R.id.restaurant_stars_1);
            mRestaurantStar2 = itemView.findViewById(R.id.restaurant_stars_2);
            mRestaurantStar3 = itemView.findViewById(R.id.restaurant_stars_3);
            mRestaurantParentLayout = itemView.findViewById(R.id.restaurant_parent_layout);
            mRestaurantBoxCoworkers = itemView.findViewById(R.id.restaurant_box_coworker);
        }
    }

    private double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    private int distanceBetweenTwoPoints(LatLng latLng1, LatLng latLng2) {
        double earthRadius = 6371;
        double dLat = degreesToRadians(latLng2.latitude - latLng1.latitude);
        double dLon = degreesToRadians(latLng2.longitude - latLng1.longitude);
        double lat1 = degreesToRadians(latLng1.latitude);
        double lat2 = degreesToRadians(latLng2.latitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceInMeter = (earthRadius * c) * 1000;
        return (int) distanceInMeter;
    }

    private String getPhoto(String reference) {
        String baseUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=";
        String key = context.getString(R.string.google_api_key);
        return (baseUrl + reference + "&key=" + key);
    }

    private void displayCorrectSentenceOnHourField(ViewHolder holder, Restaurant restaurant) {
        Log.d(TAG, "displayCorrectSentenceOnHourField: ");
        //String heure = getCorrectSentence(restaurant);
        String heure = getNumberOfServiceToday(restaurant);
        char firstLettreOfHeure = heure.charAt(0);

        switch (firstLettreOfHeure) {
            case 'F':
            case 'C':
                holder.mRestaurantHours.setTextColor(Color.RED);
                break;
            case 'O':
                holder.mRestaurantHours.setTextColor(Color.GRAY);
                break;
            default:
                holder.mRestaurantHours.setTextColor(Color.BLACK);
                break;
        }
        holder.mRestaurantHours.setText(heure);
    }

    /*
    private String getCorrectSentence(Restaurant restaurant) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        Log.d(TAG, "getCorrectSentence: " + day);
        String hour;
        if (restaurant.getOpeningHours() != null) {
            for (Period period : restaurant.getOpeningHours().getPeriod()) {
                if (day == period.getOpen().getDay()) {
                    hour = getRestaurantStatus(period, calendar);
                    return hour;
                }
            }
        } else {
            Log.d(TAG, "onBindViewHolder: OpenHours = null");
            hour = context.getResources().getString(R.string.schedule_unavailable);
            return hour;
        }
        return "heure";
    }

    @SuppressLint("SimpleDateFormat")
    private String getRestaurantStatus(Period period, Calendar calendar) {
        Log.d(TAG, "getRestaurantStatus: ");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmm");
        int currentTime = Integer.parseInt(simpleDateFormat.format(calendar.getTime()));
        int openHour = Integer.parseInt(period.getOpen().getTime());
        int closeHour = Integer.parseInt(period.getClose().getTime());
        String closingTime = period.getClose().getTime().substring(0, 2) + ":" + period.getClose().getTime().substring(2);
        String openingTime = period.getOpen().getTime().substring(0, 2) + ":" + period.getOpen().getTime().substring(2);
        String openingOrClosingHour;
        String openingOrClosingHourToReturn;
        if ((currentTime > openHour && currentTime < closeHour) || (currentTime > openHour && openHour > closeHour)) {
            openingOrClosingHour = getCorrectOpeningOrClosingHour(closingTime);
            openingOrClosingHourToReturn = context.getResources().getString(R.string.open_until) + openingOrClosingHour.trim().toLowerCase().replace(" ", "");
        } else {
            openingOrClosingHour = getCorrectOpeningOrClosingHour(openingTime);
            openingOrClosingHourToReturn = context.getResources().getString(R.string.closed_until) + openingOrClosingHour.trim().toLowerCase().replace(" ", "");
        }
        return openingOrClosingHourToReturn;
    }

    @SuppressLint("SimpleDateFormat")
    private String getCorrectOpeningOrClosingHour(String hour) {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm");
        DateFormat outPut = new SimpleDateFormat("hh:mm aa");
        Date date = null;
        String correctOpeningOrClosingHour;
        try {
            date = dateFormat.parse(hour);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date != null)
            correctOpeningOrClosingHour = outPut.format(date);
        else
            return "Error while getting hours";
        if (correctOpeningOrClosingHour.substring(3, 5).equals("00")) {
            correctOpeningOrClosingHour = correctOpeningOrClosingHour.substring(0, 2) + correctOpeningOrClosingHour.substring(5);
        }
        correctOpeningOrClosingHour = correctOpeningOrClosingHour.replaceFirst("^0+(?!$)", "");
        return correctOpeningOrClosingHour;
    }
    */


    private void displayCorrectRatingForRestaurant(ViewHolder holder, Double note) {
        if (note != null) {
            note = (note * 3) / 5;
            if (note >= 2.51 && note <= 3) {
                holder.mRestaurantStar1.setVisibility(View.VISIBLE);
                holder.mRestaurantStar2.setVisibility(View.VISIBLE);
                holder.mRestaurantStar3.setVisibility(View.VISIBLE);
            } else if (note >= 1.51 && note <= 2.5) {
                holder.mRestaurantStar1.setVisibility(View.VISIBLE);
                holder.mRestaurantStar2.setVisibility(View.VISIBLE);
                holder.mRestaurantStar3.setVisibility(View.GONE);
            } else if (note >= 0.51 && note <= 1.5) {
                holder.mRestaurantStar1.setVisibility(View.VISIBLE);
                holder.mRestaurantStar2.setVisibility(View.GONE);
                holder.mRestaurantStar3.setVisibility(View.GONE);
            } else {
                holder.mRestaurantStar1.setVisibility(View.GONE);
                holder.mRestaurantStar2.setVisibility(View.GONE);
                holder.mRestaurantStar3.setVisibility(View.GONE);
            }
        }
    }

    private void displayCorrectNumberOfCoworker(final ViewHolder holder, Restaurant restaurant) {
        userRef.whereEqualTo("restaurantChoice", restaurant.getName())
                .whereEqualTo("dateOfChoice", date)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        j = 0;
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            j++;
                        }
                        String numberOfCoworker = "(" + j + ")";
                        if (j > 0) {
                            holder.mRestaurantImageCoworkers.setVisibility(View.VISIBLE);
                            holder.mRestaurantCoworkers.setText(numberOfCoworker);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    // Ici ça merde parce que lorsqu'on change de jour, on recupère directement les données du jour suivant et si des horaire sont après minuit pour le

    private String getNumberOfServiceToday(Restaurant restaurant) {
        int numberOfService = 0;
        String returned;
        List<Period> periodList = new ArrayList<>();
        for (Period period : restaurant.getOpeningHours().getPeriod()) {
            if (intOfToday == period.getOpen().getDay() || intOfToday == period.getClose().getDay()) {
                numberOfService++;
                periodList.add(period);
            }
        }
        if (numberOfService > 0) {
            Log.d(TAG, "getNumberOfServiceToday: serviceToday");
            returned = oneServicesOrMore(periodList, restaurant);
            returned = correctSentence(returned);
        } else {
            Log.d(TAG, "getNumberOfServiceToday: noServiceToday");
            returned = noServiceToday(restaurant);
            returned = correctSentence(returned);
        }
        Log.d(TAG, "getNumberOfServiceToday: " + restaurant.getName() + " " + returned);
        return returned;
    }

    private String oneServicesOrMore(List<Period> periodList, Restaurant restaurant) {
        String closingHour = openNowGetClosingHours(periodList);

        if (closingHour.equalsIgnoreCase("")) {
            Log.d(TAG, "oneServicesOrMore: " + closingHour);
            String openingHour = closedNowGetOpeningHours(periodList, restaurant);
            Log.d(TAG, "oneServicesOrMore: " + restaurant.getName() + " " + openingHour);
            return openingHour;
        } else {
            Log.d(TAG, "oneServicesOrMore: " + restaurant.getName() + " " + closingHour);
            return closingHour;
        }
    }

    private String openNowGetClosingHours(List<Period> periodList) {
        String closingHour;
        for (Period period : periodList) {
            int openDay = period.getOpen().getDay();
            int closedDay = period.getClose().getDay();
            int open = castStringToInt(period.getOpen().getTime());
            int closed = castStringToInt(period.getClose().getTime());
            int openMod = castIntToAnotherInt(open);
            int closedMod = castIntToAnotherInt(closed);
            Log.d(TAG, "openNowGetClosingHours: " + openMod + " " + closedMod);
            if ((currentHourAsInt >= openMod && currentHourAsInt < closedMod) || (currentHourAsInt < openMod && currentHourAsInt < closedMod && closedMod < openMod)) {
                if (currentHourAsInt >= closedMod - 15) {
                    Log.d(TAG, "openNowGetClosingHours: " + currentHourAsInt + " " + closedMod);
                    closingHour = "W";
                }
                else
                    closingHour = "O" + period.getClose().getTime();
                Log.d(TAG, "openNowGetClosingHours: " + currentHourAsInt + " " + closedMod);
                return closingHour;
            }
        }
        return "";
    }

    private String closedNowGetOpeningHours(List<Period> periodList, Restaurant restaurant) {
        String openingHour;
        int isFirst = 0;
        int isLast = 0;
        for (Period period : periodList) {
            int openPreMod = castStringToInt(period.getOpen().getTime());
            int closedPreMod = castStringToInt(period.getClose().getTime());
            int openPostMod = castIntToAnotherInt(openPreMod);
            int closedPostMod = castIntToAnotherInt(closedPreMod);
            if (currentHourAsInt < openPostMod) {
                openingHour = period.getOpen().getTime();
                isFirst++;
            }
            if (currentHourAsInt > closedPostMod) {
                isLast++;
            }
        }
        int indexOfDay;
        String dayWriten;
        if (isLast == periodList.size()) {
            openingHour = "C" + getFirstPeriodOfNextDayWithService(intOfToday, restaurant);
        } else if (isFirst == periodList.size()) {
            indexOfDay = periodList.get(0).getOpen().getDay();
            dayWriten = indexDayToDayWritten(indexOfDay);
            openingHour = "C" + periodList.get(0).getOpen().getTime() + dayWriten;
        } else {
            indexOfDay = periodList.get(0).getOpen().getDay();
            dayWriten = indexDayToDayWritten(indexOfDay);
            openingHour = "C" + periodList.get(isLast).getOpen().getTime() + dayWriten;

        }
        return openingHour;
    }

    private String indexDayToDayWritten(int indexOfDay) {
        String dayWritten;
        switch (indexOfDay) {
            case 0:
                dayWritten = " (Sun)";
                break;
            case 1:
                dayWritten = " (Mon)";
                break;
            case 2:
                dayWritten = " (Tue)";
                break;
            case 3:
                dayWritten = " (Wed)";
                break;
            case 4:
                dayWritten = " (Thu)";
                break;
            case 5:
                dayWritten = " (Fri)";
                break;
            case 6:
                dayWritten = " (Sat)";
                break;
            default:
                dayWritten = "noEntryFound";
                break;
        }
        return dayWritten;
    }

    private int castIntToAnotherInt(int i) {
        int hours = i / 100;
        int minutes = i % 100;
        hours = hours * 60;
        int plop = hours + minutes;
        return plop;
    }

    private String noServiceToday(Restaurant restaurant) {
        return ("C" + getFirstPeriodOfNextDayWithService(intOfToday, restaurant));
    }

    private String getFirstPeriodOfNextDayWithService(int day, Restaurant restaurant) {
        String time = new String();
        do {
            Log.d(TAG, "getFirstPeriodOfNextDayWithService: " + day);
            day++;
            if (day == 7)
                day = 0;
        } while (!noPeriodeThisDay(day, restaurant));
        String dayWritten;
        for (Period period : restaurant.getOpeningHours().getPeriod()) {
            Log.d(TAG, "getFirstPeriodOfNextDayWithService: " + day);
            if (day == period.getOpen().getDay()) {
                dayWritten = indexDayToDayWritten(day);
                time = period.getOpen().getTime() + dayWritten;
                Log.d(TAG, "getFirstPeriodOfNextDayWithService: " + time);
                return time;
            }
        }
        return "time";
    }

    private boolean noPeriodeThisDay(int day, Restaurant restaurant) {

        boolean isOpen = false;
        for (Period period : restaurant.getOpeningHours().getPeriod()) {
            if (day == period.getOpen().getDay()) {
                isOpen = true;
            }
        }
        Log.d(TAG, "noPeriodeThisDay: " + day +  " " + isOpen);
        return isOpen;
    }

    private int castStringToInt(String hours) {
        return Integer.parseInt(hours);
    }

    private String correctSentence(String string) {
        Log.d(TAG, "correctSentence: " + string);
        char firstLetter = string.charAt(0);
        if (firstLetter == 87) {
            string = "Closing soon !";
            return string;
        }
        String day = string.substring(5);
        string = string.substring(1, 3) + ":" + string.substring(3, 5);
        Log.d(TAG, "correctSentence: " + string);
        string = getCorrectOpeningOrClosingHour(string);
        if (firstLetter == 79) {
            Log.d(TAG, "correctSentence: " + string);
            string = context.getString(R.string.open_until) + string;
        } else if (firstLetter == 67) {
            Log.d(TAG, "correctSentence: " + day);
            string = context.getString(R.string.closed_until) + string + day;
        }
        return string;
    }

    @SuppressLint("SimpleDateFormat")
    private String getCorrectOpeningOrClosingHour(String hour) {

        String correctOpeningOrClosingHour = "";
        try {
            SimpleDateFormat _24H = new SimpleDateFormat("HH:mm");
            SimpleDateFormat _12H = new SimpleDateFormat("hh:mm aa");
            Date _24HDate = _24H.parse(hour);
            assert _24HDate != null;
            correctOpeningOrClosingHour = _12H.format(_24HDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (correctOpeningOrClosingHour.substring(3, 5).equals("00")) {
            correctOpeningOrClosingHour = correctOpeningOrClosingHour.substring(0, 2) + correctOpeningOrClosingHour.substring(5);
        }
        correctOpeningOrClosingHour = correctOpeningOrClosingHour.replaceFirst("^0+(?!$)", "");
        return correctOpeningOrClosingHour;
    }

    private void setSomeValue() {
        Calendar calendar = Calendar.getInstance();
        intOfToday = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int hours = calendar.get(Calendar.HOUR_OF_DAY) * 60;
        int minutes = calendar.get(Calendar.MINUTE);
        currentHourAsInt = hours + minutes;
    }
}
