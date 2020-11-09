package com.leprincesylvain.ocproject7.go4lunch.controller.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.android.SphericalUtil;
import com.leprincesylvain.ocproject7.go4lunch.R;
import com.leprincesylvain.ocproject7.go4lunch.controller.api.CircleTransform;
import com.leprincesylvain.ocproject7.go4lunch.controller.api.MapsCallApi;
import com.leprincesylvain.ocproject7.go4lunch.controller.api.NotifyWorker;
import com.leprincesylvain.ocproject7.go4lunch.controller.fragments.MapViewFragment;
import com.leprincesylvain.ocproject7.go4lunch.controller.fragments.MyLunchFragment;
import com.leprincesylvain.ocproject7.go4lunch.controller.fragments.RestaurantListViewFragment;
import com.leprincesylvain.ocproject7.go4lunch.controller.fragments.SettingsFragment;
import com.leprincesylvain.ocproject7.go4lunch.controller.fragments.WorkmatesFragment;
import com.leprincesylvain.ocproject7.go4lunch.model.Prediction;
import com.leprincesylvain.ocproject7.go4lunch.model.ResponseToDetail;
import com.leprincesylvain.ocproject7.go4lunch.model.Restaurant;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;

    // Fragment iD
    private static final int MAPVIEW_FRAGMENT = R.id.bottom_nav_mapview;
    private static final int RESTAURANT_LISTVIEW_FRAGMENT = R.id.bottom_nav_listview;
    private static final int COWORKER_LISTVIEW_FRAGMENT = R.id.bottom_nav_workmates;
    private static final int MY_LUNCH_FRAGMENT = R.id.drawer_nav_mylunch;
    private static final int SETTINGS_FRAGMENT = R.id.drawer_nav_settings;
    private int fragmentSelected;
    AutocompleteSupportFragment autocompleteSupportFragment;

    // Fragment Declaration
    private MapViewFragment mapViewFragment = (MapViewFragment) getSupportFragmentManager().findFragmentById(R.id.map_view_fragment);
    private Fragment restaurantListViewFragment;
    private Fragment workmatesFragment;
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
    private static final String KEY_USERPICTURE = "userProfilePicture";
    private static final String KEY_DATEOFCHOICE = "dateOfChoice";
    public static String userId;
    private String username;
    private String userPicture;
    private String usermail;
    private Map<String, Object> user = new HashMap<>();

    // Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");
    private DocumentReference documentReference;

    private int numberOfRestaurantFromPlaceCall = 0;
    private String googleApiKey;
    private int numberOfCallToDetail = 0;

    private List<String> placesId = new ArrayList<>();
    private ArrayList<Restaurant> restaurantsToUse = new ArrayList<>();
    List<Prediction> predictionList = new ArrayList<>();
    ArrayList<Restaurant> restaurantListMatchingPrediction = new ArrayList<>();
    private int autocompleteCallDetail = 0;

    PlacesClient placesClient;

    private PendingIntent pendingIntent;
    private AlarmManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleApiKey = getResources().getString(R.string.google_api_key);
        if (savedInstanceState != null)
            fragmentSelected = savedInstanceState.getInt("FragmentSelected");
        callShowProperFragment(fragmentSelected);
        setContentView(R.layout.activity_main);
        getUserDetails();
        if (userId != null) {
            createUserInFirestoreIfNotExisting();
            documentReference = collectionReference.document(userId);
        }
        configureToolbar();
        configureDrawerLayout();
        configureNavigationView();
        configureBottomNavigationView();
        setRetrofitForLaterCall();
        getLocationPermission();
        if (autocompleteCallDetail == 0) {
            restaurantsToUse = restaurantList;
        }
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_api_key));
        placesClient = Places.createClient(this);
        creatOneTimeWorkRequest();
    }

    public void creatOneTimeWorkRequest() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Data inputData = new Data.Builder()
                .putString("userId", userId)
                .build();

        Calendar calendar = Calendar.getInstance();
        long nowInMillis = (Calendar.HOUR_OF_DAY * 60) + Calendar.MINUTE;

        int now = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        long wantedTime = 720;

        long initDelai = wantedTime - now;
        if (initDelai < 0)
            initDelai = (initDelai + 1440) * 60;

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(NotifyWorker.class)
                .setInputData(inputData)
                .setInitialDelay(initDelai, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance().enqueue(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.menu_search_icon);
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) item.getActionView();

                searchView.setBackgroundColor(getResources().getColor(R.color.white));
                searchView.setQueryHint(getString(R.string.search_restaurant));

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        predictionList.clear();
                        restaurantListMatchingPrediction.clear();
                        if (newText.length() == 0) {
                            autocompleteCallDetail = 0;
                            autoCompleteWidget(newText);
                        }
                        if (newText.length() > 2) {
                            autoCompleteWidget(newText);
                        }
                        return true;
                    }
                });
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                restaurantsToUse = restaurantList;
                autocompleteCallDetail = 0;
                if (getFragmentSelected == RESTAURANT_LISTVIEW_FRAGMENT) {
                    restaurantListViewFragment.onDestroy();
                    showRestaurantListViewFragment();
                }
                return true;
            }
        });
        return true;
    }


    public void autoCompleteWidget(String string) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        RectangularBounds bounds = RectangularBounds.newInstance(
                toBounds(latLng, 2000).southwest,
                toBounds(latLng, 2000).northeast);

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationRestriction(bounds)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setQuery(string)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onSuccess(FindAutocompletePredictionsResponse findAutocompletePredictionsResponse) {
                for (AutocompletePrediction prediction : findAutocompletePredictionsResponse.getAutocompletePredictions()) {
                    for (Place.Type type : prediction.getPlaceTypes()) {
                        if (type.toString().equalsIgnoreCase("restaurant")) {
                            Prediction itemPrediction = new Prediction(prediction.getPlaceId(), prediction.getPrimaryText(null).toString());
                            predictionList.add(itemPrediction);
                        }
                    }
                }
                restaurantListMatchingPrediction.clear();
                if (predictionList.size() > 0) {
                    for (Prediction prediction : predictionList) {
                        autocompleteCallDetail++;
                        getDetail(prediction.getPlaceId());
                    }
                } else {
                    if (mapViewFragment != null) {
                        restaurantsToUse = restaurantList;
                        mapViewFragment.clearMapFromMarker();
                        mapViewFragment.putMarkerOnMap(restaurantsToUse);
                    }
                }
            }
        });
    }

    public LatLngBounds toBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void getUserDetails() {
        if (getIntent().getExtras() != null) {
            username = getIntent().getExtras().getString("user_name");
            userPicture = getIntent().getExtras().getString("user_photo");
            usermail = getIntent().getExtras().getString("user_email");
            userId = getIntent().getExtras().getString("user_id");
        }
    }

    private void createUserInFirestoreIfNotExisting() {
        user.put(KEY_USERNAME, username);
        user.put(KEY_USERMAIL, usermail);
        user.put(KEY_USERPICTURE, userPicture);
        user.put(KEY_DATEOFCHOICE, 0);
        collectionReference.document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && !documentSnapshot.exists()) {
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
                    Picasso.get().load(userPicture).transform(new CircleTransform()).into(pictureUser);
                } else
                    pictureUser.setImageResource(R.drawable.workmate_no_image_found);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void configureNavigationView() {
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void configureBottomNavigationView() {
        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                callShowProperFragment(item.getItemId());
                bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
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
        int itemId = item.getItemId();
        if (itemId == R.id.drawer_nav_logout) {
            logOutTheCurrentUser();
        } else {
            callShowProperFragment(itemId);
            bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
        }
        this.drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void logOutTheCurrentUser() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().signOut();
            GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut();
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
        }
    }

    private int getFragmentSelected;

    private void callShowProperFragment(int fragmentIdentifier) {
        fragmentSelected = fragmentIdentifier;
        switch (fragmentIdentifier) {
            case MAPVIEW_FRAGMENT:
                this.showMapViewFragment();
                setTitle(getString(R.string.hungry));
                getFragmentSelected = MAPVIEW_FRAGMENT;
                break;
            case RESTAURANT_LISTVIEW_FRAGMENT:
                this.showRestaurantListViewFragment();
                setTitle(getString(R.string.hungry));
                getFragmentSelected = RESTAURANT_LISTVIEW_FRAGMENT;
                break;
            case COWORKER_LISTVIEW_FRAGMENT:
                this.showCoworkerListViewFragment();
                setTitle(getString(R.string.available_workmate));
                break;
            case MY_LUNCH_FRAGMENT:
                this.showMyLunchFragment();
                setTitle(getString(R.string.my_lunch));
                break;
            case SETTINGS_FRAGMENT:
                this.showSettingsFragment();
                setTitle(getString(R.string.settings_title));
                break;
            default:
                break;
        }
    }

    private void showMapViewFragment() {
        this.mapViewFragment = new MapViewFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("latlng", latLng);
        bundle.putParcelableArrayList("list", restaurantsToUse);
        this.mapViewFragment.setArguments(bundle);
        this.startTransactionFragment(this.mapViewFragment);
    }

    private void showRestaurantListViewFragment() {
        restaurantListViewFragment = new RestaurantListViewFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("list", restaurantsToUse);
        bundle.putParcelable("position", latLng);
        restaurantListViewFragment.setArguments(bundle);
        this.startTransactionFragment(this.restaurantListViewFragment);
    }

    private void showCoworkerListViewFragment() {
        if (this.workmatesFragment == null) {
            workmatesFragment = new WorkmatesFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("restaurantList", restaurantsToUse);
            workmatesFragment.setArguments(bundle);
        }
        this.startTransactionFragment(this.workmatesFragment);
    }

    private void showMyLunchFragment() {
        if (this.myLunchFragment == null) {
            myLunchFragment = new MyLunchFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("list", restaurantList);
            myLunchFragment.setArguments(bundle);
        }
        this.startTransactionFragment(myLunchFragment);
    }

    private void showSettingsFragment() {
        settingsFragment = new SettingsFragment();
        startTransactionFragment(settingsFragment);
    }

    private void startTransactionFragment(Fragment fragment) {
        if (!fragment.isVisible()) {
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
        }
    }

    private void setRetrofitForLaterCall() {
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                getDeviceLocation();
            } else {
                mLocationPermissionGranted = false;
                Toast.makeText(this, R.string.request_device_location, Toast.LENGTH_LONG).show();
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
        updateLocationOnMapsFragment(location);
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    private void updateLocationOnMapsFragment(Location location) {
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (this.mapViewFragment == null) {
            showMapViewFragment();
        } else {
            this.mapViewFragment.moveCameraIn(latLng, 16);
        }
        getListOfRestaurants(latLng);
    }

    private void getListOfRestaurants(LatLng latLng) {
        /*Log.d(TAG, "getListOfRestaurant: ");
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
                                String placeId = response.body().getResults().get(j).getPlaceId();
                                placesId.add(placeId);
                            }
                        }
                    }
                    getLikedRestaurantIfNotInNearby();
                }

                @Override
                public void onFailure(@NonNull Call<ResponseToPlace> call, @NonNull Throwable t) {
                    Log.d(TAG, "getListOfRest onFailure: " + t.getMessage());
                }
            });
        } catch (IllegalStateException | JsonSyntaxException exception) {
            Log.d(TAG, "getListOfRest Exception: " + exception.getMessage());
        }
*/
        getJson();
    }

    public void getDetail(final String placeId) {
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
                    if (restaurant != null) {
                        restaurant.setId(placeId);
                        if (mapViewFragment != null) {
                            if (autocompleteCallDetail > 0) {
                                restaurantListMatchingPrediction.add(restaurant);
                                restaurantsToUse = restaurantListMatchingPrediction;
                                mapViewFragment.clearMapFromMarker();
                                mapViewFragment.putMarkerOnMap(restaurantListMatchingPrediction);
                                if (getFragmentSelected == RESTAURANT_LISTVIEW_FRAGMENT) {
                                    showRestaurantListViewFragment();
                                } else if (getFragmentSelected == MAPVIEW_FRAGMENT) {
                                    showMapViewFragment();
                                }
                            } else {
                                restaurantList.add(restaurant);
                                restaurantsToUse = restaurantList;
                                mapViewFragment.setRestaurantList(restaurantList);
                                mapViewFragment.putMarkerOnMap(restaurantList);
                            }
                        }
                        if (numberOfCallToDetail >= placesId.size()) {
                            LatLng latLng = new LatLng(Double.parseDouble(restaurant.getGeometry().getLocation().getLat()), Double.parseDouble(restaurant.getGeometry().getLocation().getLng()));
                            mapViewFragment.moveCameraIn(latLng, 16);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseToDetail> call, @NonNull Throwable t) {
            }
        });
    }

    private void getJson() {
        String json;
        try {
            InputStream inputStream = getAssets().open("placenearbysearch.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String placeName = jsonObject.getString("name");
            }
            int[] ressources = {R.raw.hugo, R.raw.miranchitopaisa, R.raw.veggietasty};
            List<String> ressourcesId = new ArrayList<>();
            ressourcesId.add("ChIJw0QnSxVu5kcRYvvj231mXv8");
            ressourcesId.add("ChIJidCwC0Bu5kcRhyADcTSSsrY");
            ressourcesId.add("ChIJOd3F_j9u5kcR58fp0EX2S2c");
            int j = 0;
            for (int i : ressources) {
                String myJson = getDetailOfJsonObject(this.getResources().openRawResource(i));
                String id = ressourcesId.get(j);
                Restaurant restaurant = new Gson().fromJson(myJson, Restaurant.class);
                restaurant.setId(id);
                String placeId = restaurant.getId();
                placesId.add(placeId);
                j++;
            }
            getLikedRestaurantIfNotInNearby();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private String getDetailOfJsonObject(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, bytes.length);
            return new String(bytes);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getAllRestaurantDetails() {
        for (String string : placesId) {
            getDetail(string);
        }
    }

    private void getLikedRestaurantIfNotInNearby() {
        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<String> stringList = (List<String>) documentSnapshot.get("likes");
                            if (stringList != null)
                                for (String string : stringList) {
                                    boolean isInTheList = false;
                                    for (String idString : placesId) {
                                        if (string.equalsIgnoreCase(idString)) {
                                            isInTheList = true;
                                        }
                                    }
                                    if (!isInTheList) {
                                        placesId.add(string);
                                    }
                                }
                        }
                        getAllWorkmateRestaurant();
                    }
                });

    }

    private void getAllWorkmateRestaurant() {
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        String restoId = documentSnapshot.getString("restaurantId");
                        boolean isInTheList = false;
                        if (restoId != null && restoId.length() > 0) {
                            for (String string : placesId) {
                                if (string.equalsIgnoreCase(restoId))
                                    isInTheList = true;
                            }
                            if (!isInTheList) {
                                placesId.add(restoId);
                            }
                        }
                    }
                    getAllRestaurantDetails();
                }
            }
        });
    }
}
