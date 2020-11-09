package com.leprincesylvain.ocproject7.go4lunch.controller.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.leprincesylvain.ocproject7.go4lunch.R;
import com.leprincesylvain.ocproject7.go4lunch.controller.activities.RestaurantDetailActivity;
import com.leprincesylvain.ocproject7.go4lunch.model.GetDate;
import com.leprincesylvain.ocproject7.go4lunch.model.Restaurant;

import java.util.List;

public class MapViewFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private Context mContext;
    private LatLng latLng;
    private float zoom = 16;
    private List<Restaurant> restaurantList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");

    long date = GetDate.getDate();

    public MapViewFragment() {
    }

    public void setRestaurantList(List<Restaurant> restaurantList) {
        this.restaurantList = restaurantList;
    }

    public List<Restaurant> getRestaurantList() {
        return restaurantList;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            latLng = getArguments().getParcelable("latlng");
            restaurantList = getArguments().getParcelableArrayList("list");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = this.getContext();
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        final SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_view_fragment);
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(R.raw.custommap);
        if (latLng != null) {
            moveCameraIn(latLng, zoom);
        }
        if (restaurantList.size() > 0) {
            putMarkerOnMap(restaurantList);
        }
    }

    public void moveCameraIn(LatLng latLng, float zoom) {
        if (googleMap != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
    }

    public void putMarkerOnMap(List<Restaurant> restaurants) {
        if (restaurants != null) {
            for (final Restaurant restaurant : restaurants) {
                final String name = restaurant.getName();
                final double lat = Double.parseDouble(restaurant.getGeometry().getLocation().getLat());
                final double lng = Double.parseDouble(restaurant.getGeometry().getLocation().getLng());
                usersRef.whereEqualTo("restaurantChoice", restaurant.getName()).whereEqualTo("dateOfChoice", date)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                int i = 0;
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    String documentId = documentSnapshot.getString("restaurantChoice") + documentSnapshot.getString("userName");
                                    i++;
                                }
                                if (i > 0) {
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title(name)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green)));
                                } else {
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title(name)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_orange)));
                                }
                            }
                        });
            }
        }
        listenOnMarkerClick();
    }

    public void listenOnMarkerClick() {
        if (googleMap != null) {
            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Intent intent = new Intent(mContext, RestaurantDetailActivity.class);
                    for (Restaurant restaurant : restaurantList) {
                        if (marker.getTitle().equalsIgnoreCase(restaurant.getName())) {
                            intent.putExtra("restaurant", restaurant);
                        }
                    }
                    mContext.startActivity(intent);
                }
            });
        }
    }


    public void clearMapFromMarker() {
        if (googleMap != null)
            googleMap.clear();
    }
}
