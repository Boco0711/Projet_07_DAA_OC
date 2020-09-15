package com.leprincesylvain.ocproject7.go4lunch.controller.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.leprincesylvain.ocproject7.go4lunch.R;

public class MapViewFragment extends Fragment implements OnMapReadyCallback {
    public static final String TAG = "MapViewFragment_TAG";

    private GoogleMap googleMap;
    private Context mContext;

    private Boolean mLocationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    public MapViewFragment(){
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        if (getArguments() != null) {
            Log.d(TAG, "onCreate: getArguments() != null");
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
        if (mLocationPermissionGranted) {
            Log.d(TAG, "onMapReady: permission is granted you may proceed");
        } else {
            Log.d(TAG, "onMapReady: permission is not granted get permission first and try again");
            getLocationPermission();
        }
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: ");
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocationPermission: set permission on true");
            mLocationPermissionGranted = true;
            onMapReady(googleMap);
        } else {
            Log.d(TAG, "getLocationPermission: open authorization page");
            if (this.getActivity() != null) {
                Log.d(TAG, "getLocationPermission: this.getActivity() != null");
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult: ");
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionResult: permission ");
                onMapReady(googleMap);
            } else {
                Log.d(TAG, "onRequestPermissionResult: permission denied");
                Toast.makeText(mContext, "If you want to use our app you need to allow device location", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getLocationPermission();
                    }
                }, 3500);
            }
        }
    }
}
