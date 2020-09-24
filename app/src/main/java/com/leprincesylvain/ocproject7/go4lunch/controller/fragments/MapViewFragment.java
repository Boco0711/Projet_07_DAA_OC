package com.leprincesylvain.ocproject7.go4lunch.controller.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.model.LatLng;
import com.leprincesylvain.ocproject7.go4lunch.R;

public class MapViewFragment extends Fragment implements OnMapReadyCallback {
    public static final String TAG = "MapViewFragment_TAG";

    private GoogleMap googleMap;
    private Context mContext;
    private LatLng latLng;

    public MapViewFragment(){
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        if (getArguments() != null) {
            Log.d(TAG, "onCreate: getArguments() != null");
            latLng = getArguments().getParcelable("latlng");
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
        Log.d(TAG, "moveCameraIn: Lat: " +latLng.latitude + " Lng: " + latLng.longitude + " zoom: " + zoom);
        if (googleMap != null) {
            Log.d(TAG, "moveCameraIn: neither element are null");
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
    }
}
