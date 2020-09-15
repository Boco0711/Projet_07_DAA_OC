package com.leprincesylvain.ocproject7.go4lunch.controller.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leprincesylvain.ocproject7.go4lunch.R;
import com.leprincesylvain.ocproject7.go4lunch.controller.fragments.MapViewFragment;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "MainActivity_TAG";

    // Ui component
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configureToolbar();
        configureDrawerLayout();
        configureNavigationView();
        configureBottomNavigationView();
        setRetrofitForLaterCall();
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
                return false;
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
        } else {
            callShowProperFragment(itemId);
        }
        this.drawer.closeDrawer(GravityCompat.START);

        return true;
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
        this.startTransactionFragment(this.mapViewFragment);
    }

    private void showRestaurantListViewFragment() {
        Log.d(TAG, "showRestaurantListViewFragment: ");
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
    }
}
