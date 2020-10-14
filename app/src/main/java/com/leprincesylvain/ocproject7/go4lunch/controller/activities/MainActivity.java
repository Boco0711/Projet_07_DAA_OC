package com.leprincesylvain.ocproject7.go4lunch.controller.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.leprincesylvain.ocproject7.go4lunch.R;
import com.leprincesylvain.ocproject7.go4lunch.controller.api.MapsCallApi;
import com.leprincesylvain.ocproject7.go4lunch.controller.fragments.MapViewFragment;
import com.leprincesylvain.ocproject7.go4lunch.controller.fragments.RestaurantListViewFragment;
import com.leprincesylvain.ocproject7.go4lunch.model.ResponseToDetail;
import com.leprincesylvain.ocproject7.go4lunch.model.ResponseToPlace;
import com.leprincesylvain.ocproject7.go4lunch.model.Restaurant;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener, LocationListener {
    public static final String TAG = "MainActivity_TAG";

    // Ui component
    private Toolbar toolbar;
    private DrawerLayout drawer;

    // Fragment iD
    public static final int MAPVIEW_FRAGMENT = R.id.bottom_nav_mapview;
    public static final int RESTAURANT_LISTVIEW_FRAGMENT = R.id.bottom_nav_listview;
    public static final int COWORKER_LISTVIEW_FRAGMENT = R.id.bottom_nav_workmates;
    private static final int MY_LUNCH_FRAGMENT = R.id.drawer_nav_mylunch;
    private static final int SETTINGS_FRAGMENT = R.id.drawer_nav_settings;

    // Fragment Declaration
    private MapViewFragment mapViewFragment = (MapViewFragment) getSupportFragmentManager().findFragmentById(R.id.map_view_fragment);
    private Fragment restaurantListViewFragment;
    private Fragment workmatesListViewFragment;
    private Fragment myLunchFragment;
    private Fragment settingsFragment;

    private MapsCallApi mapsCallApi;
    private Boolean mLocationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private LatLng latLng;
    private ArrayList<Restaurant> restaurantList = new ArrayList<>();

    // User Details
    private static final String KEY_USERNAME = "userName";
    private static final String KEY_USERMAIL = "userMail";
    private static final String KEY_USERPICTURE = "userProfilPicture";
    private String userId;
    private String username;
    private String userPicture;
    private String usermail;
    private Map<String, Object> user = new HashMap<>();

    // Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    private int numberOfRestaurantFromPlaceCall = 0;
    private String googleApiKey = getResources().getString(R.string.google_api_key);
    private int numberOfCallToDetail = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getUserDetails();
        if (userId != null) {
            createUserInFirestoreIfNotExisting();
        }
        configureToolbar();
        configureDrawerLayout();
        configureNavigationView();
        configureBottomNavigationView();
        setRetrofitForLaterCall();
        getLocationPermission();
    }

    private void getUserDetails() {
        Log.d(TAG, "getUserDetails: ");
        if (getIntent().getExtras() != null) {
            Log.d(TAG, "getUserDetails: getExtras() != null");
            username = getIntent().getExtras().getString("user_name");
            userPicture = getIntent().getExtras().getString("user_photo");
            usermail = getIntent().getExtras().getString("user_email");
            userId = getIntent().getExtras().getString("user_id");
        } else {
            Log.d(TAG, "getUserDetails: getExtras() == null");
        }
    }

    private void createUserInFirestoreIfNotExisting() {
        Log.d(TAG, "createUserInFirestoreIfNotExisting: ");
        user.put(KEY_USERNAME, username);
        user.put(KEY_USERMAIL, usermail);
        user.put(KEY_USERPICTURE, userPicture);
        collectionReference.document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: ");
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && !documentSnapshot.exists()) {
                                Log.d(TAG, "onComplete: documentSnapshot!exist");
                                collectionReference.document(userId).set(user);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                })
        ;
    }

    private void configureToolbar() {
        this.toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void configureDrawerLayout() {
        this.drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar();
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar();
                TextView nameUser = findViewById(R.id.nav_header_profil_name);
                TextView emailUser = findViewById(R.id.nav_header_profil_email);
                ImageView pictureUser = findViewById(R.id.nav_header_profile_image);
                if (username != null) {
                    nameUser.setText(username);
                }
                if (usermail != null) {
                    emailUser.setText(usermail);
                }
                if (userPicture != null) {
                    Picasso.get().load(userPicture).into(pictureUser);
                }
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void configureNavigationView() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void configureBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                callShowProperFragment(item.getItemId());
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (this.drawer.isDrawerOpen(GravityCompat.START)) {
            this.drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected: ");
        int itemId = item.getItemId();
        if (itemId == R.id.drawer_nav_logout) {
            Log.d(TAG, "onNavigationItemSelected: click on logout");
            logOutTheCurrentUser();
        } else {
            callShowProperFragment(itemId);
        }
        this.drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void logOutTheCurrentUser() {
        Log.d(TAG, "logOutTheCurrentUser: ");
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Log.d(TAG, "logOutTheCurrentUser: signOut currentUser and start the LoginActivity");
            FirebaseAuth.getInstance().signOut();
            GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut();
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
        }
    }

    private void callShowProperFragment(int fragmentIdentifier) {
        switch (fragmentIdentifier) {
            case MAPVIEW_FRAGMENT:
                Log.d(TAG, "showFragment: MapView");
                this.showMapViewFragment();
                break;
            case RESTAURANT_LISTVIEW_FRAGMENT:
                Log.d(TAG, "showFragment: RestaurantListView");
                this.showRestaurantListViewFragment();
                break;
            case COWORKER_LISTVIEW_FRAGMENT:
                Log.d(TAG, "showFragment: CoworkerListView");
                this.showCoworkerListViewFragment();
                break;
            case MY_LUNCH_FRAGMENT:
                Log.d(TAG, "showFragment: MyLunch");
                this.showMyLunchFragment();
                break;
            case SETTINGS_FRAGMENT:
                Log.d(TAG, "showFragment: Settings");
                this.showSettingsFragment();
                break;
            default:
                break;
        }
    }

    private void showMapViewFragment() {
        Log.d(TAG, "showMapViewFragment: creating a new MapViewFragment");
        this.mapViewFragment = new MapViewFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("latlng", latLng);
        bundle.putParcelableArrayList("list", restaurantList);
        this.mapViewFragment.setArguments(bundle);
        this.startTransactionFragment(this.mapViewFragment);
    }

    private void showRestaurantListViewFragment() {
        Log.d(TAG, "showRestaurantListViewFragment: ");
        if (this.restaurantListViewFragment == null) {
            restaurantListViewFragment = new RestaurantListViewFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("list", restaurantList);
            bundle.putParcelable("position", latLng);
            restaurantListViewFragment.setArguments(bundle);
        }
        this.startTransactionFragment(this.restaurantListViewFragment);
    }

    private void showCoworkerListViewFragment() {
        Log.d(TAG, "showCoworkerListViewFragment: ");
    }

    private void showMyLunchFragment() {
        Log.d(TAG, "showMyLunchFragment: ");
    }

    private void showSettingsFragment() {
        Log.d(TAG, "showSettingsFragment: ");
    }

    private void startTransactionFragment(Fragment fragment) {
        Log.d(TAG, "startTransactionFragment: ");
        if (!fragment.isVisible()) {
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
        }
    }

    private void setRetrofitForLaterCall() {
        Log.d(TAG, "setRetrofitForLaterCall: ");
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        mapsCallApi = retrofit.create(MapsCallApi.class);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: ");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocationPermission: set permission on true");
            mLocationPermissionGranted = true;
            getDeviceLocation();
        } else {
            Log.d(TAG, "getLocationPermission: open authorization page");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult: ");
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionResult: permission granted");
                mLocationPermissionGranted = true;
                getDeviceLocation();
            } else {
                Log.d(TAG, "onRequestPermissionResult: permission denied");
                mLocationPermissionGranted = false;
                Toast.makeText(this, "If you want to use our app you need to allow device location", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getLocationPermission();
                    }
                }, 3500);
            }
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: ");
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, this);
            }
        } catch (SecurityException securityException) {
            Log.d(TAG, "getDeviceLocation: ", securityException);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: new latitude: " + location.getLatitude() + " longitude: " + location.getLongitude());
        updateLocationOnMapsFragment(location);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: ");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: ");
    }

    // Never Called
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged: ");
    }

    private void updateLocationOnMapsFragment(Location location) {
        Log.d(TAG, "updateLocation: ");
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (this.mapViewFragment == null) {
            Log.d(TAG, "updateLocation: mapViewFragment == null");
            showMapViewFragment();
        } else {
            Log.d(TAG, "updateLocation: mapViewFragment != null");
            this.mapViewFragment.moveCameraIn(latLng, 16);
        }
        getListOfRestaurants(latLng);
    }

    private void getListOfRestaurants(LatLng latLng) {
        Log.d(TAG, "getListOfRestaurant: ");
        String placePreUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=";
        String placePostUrl = "&radius=100&type=restaurant&key=";
        String url = placePreUrl + latLng.latitude + "," + latLng.longitude + placePostUrl + googleApiKey;
        try {
            Call<ResponseToPlace> call = mapsCallApi.getListOfRestaurants(url);
            call.enqueue(new Callback<ResponseToPlace>() {
                @Override
                public void onResponse(@NonNull Call<ResponseToPlace> call, @NonNull Response<ResponseToPlace> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "getListOfRest onResponse: is Successfull");
                        if (response.body() != null) {
                            for (int j = 0; j < response.body().getResults().size(); j++) {
                                Log.d(TAG, "onResponse: " + j);
                                numberOfRestaurantFromPlaceCall++;
                                String place_id = response.body().getResults().get(j).getPlaceId();
                                getDetail(place_id);
                            }
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ResponseToPlace> call, @NonNull Throwable t) {
                    Log.d(TAG, "getListOfRest onFailure: " + t.getMessage());
                }


            });
        } catch (IllegalStateException | JsonSyntaxException exception) {
            Log.d(TAG, "getListOfRest Exception: " + exception.getMessage());
        }
    }

    private void getDetail(String placeId) {
        Log.d(TAG, "getDetail: ");
        String detailPreUrl = "https://maps.googleapis.com/maps/api/place/details/json?place_id=";
        String detailPostUrl = "&fields=name,formatted_address,formatted_phone_number,opening_hours,website,geometry,rating,photo&key=";
        String url = detailPreUrl + placeId + detailPostUrl + googleApiKey;
        Call<ResponseToDetail> call = mapsCallApi.getRestaurantDetails(url);
        call.enqueue(new Callback<ResponseToDetail>() {
            @Override
            public void onResponse(@NonNull Call<ResponseToDetail> call, @NonNull Response<ResponseToDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    numberOfCallToDetail++;
                    Restaurant restaurant = response.body().getRestaurant();
                    if (restaurant != null)
                        restaurantList.add(restaurant);
                    if (mapViewFragment != null && numberOfCallToDetail == numberOfRestaurantFromPlaceCall) {
                        mapViewFragment.setRestaurantList(restaurantList);
                        mapViewFragment.putMarkerOnMap(restaurantList);
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponseToDetail> call, @NonNull Throwable t) {
            }
        });
    }
}
