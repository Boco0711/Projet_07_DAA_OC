package com.leprincesylvain.ocproject7.go4lunch.controller.api;

import com.leprincesylvain.ocproject7.go4lunch.model.ResponseToPlace;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface MapsCallApi {

    @GET
    Call<ResponseToPlace> getListOfRestaurants(@Url String url);
}
