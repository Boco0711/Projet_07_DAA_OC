package com.leprincesylvain.ocproject7.go4lunch.controller.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.leprincesylvain.ocproject7.go4lunch.R;
import com.leprincesylvain.ocproject7.go4lunch.model.GetDate;
import com.leprincesylvain.ocproject7.go4lunch.model.Restaurant;

import java.util.List;

public class MapViewFragment extends Fragment implements OnMapReadyCallback {
    public static final String TAG = "MapViewFragment_TAG";

    private GoogleMap googleMap;
    private Context mContext;
    private LatLng latLng;
    private int zoom = 16;
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
        Log.d(TAG, "onCreate: ");
        if (getArguments() != null) {
            Log.d(TAG, "onCreate: getArguments() != null");
            latLng = getArguments().getParcelable("latlng");
            restaurantList = getArguments().getParcelableArrayList("list");
        }
        if (restaurantList.size() > 0) {
            putMarkerOnMap(restaurantList);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        mContext = this.getContext();
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        final SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_view_fragment);
        if (supportMapFragment != null) {
            Log.d(TAG, "onCreateView: supportMapFragment != null");
            supportMapFragment.getMapAsync(this);
        }
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        this.googleMap = googleMap;
        googleMap.setMapType(R.raw.custommap);
        if (latLng != null) {
            Log.d(TAG, "onMapReady: latlng != null");
            moveCameraIn(latLng, 16);
        }
    }

    public void moveCameraIn(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCameraIn: Lat: " + latLng.latitude + " Lng: " + latLng.longitude + " zoom: " + zoom);
        if (googleMap != null) {
            Log.d(TAG, "moveCameraIn: neither element are null");
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
    }

    public void putMarkerOnMap(List<Restaurant> restaurants) {

        Log.d(TAG, "putMarkerOnMap: ");

        if (restaurants != null) {
            Log.d(TAG, "putMarkerOnMap: " + restaurants.size());
            for (final Restaurant restaurant : restaurants) {
                Log.d(TAG, "putMarkerOnMap: " + restaurant);
                final String name = restaurant.getName();
                String website = restaurant.getWebsite();
                String address = restaurant.getFormatted_address();
                String phoneNumber = restaurant.getFormatted_phone_number();
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
                                    Log.d(TAG, "onSuccess: " + queryDocumentSnapshots + restaurant.getName());
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title(name)
                                            .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(mContext, R.drawable.pinpointfinal_restaurant_selected))));
                                } else {
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title(name)
                                            .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(mContext, R.drawable.pinpointfinal_restaurant_not_selected))));
                                }
                            }
                        });
            }
        }
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            assert drawable != null;
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(120,
                120, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        assert drawable != null;
        drawable.setBounds(0, 0, 120, 120);
        drawable.draw(canvas);

        return bitmap;
    }
}
