package com.leprincesylvain.ocproject7.go4lunch.controller.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.model.LatLng;
import com.leprincesylvain.ocproject7.go4lunch.R;
import com.leprincesylvain.ocproject7.go4lunch.adapters.MyRestaurantRecyclerViewAdapter;
import com.leprincesylvain.ocproject7.go4lunch.model.Restaurant;
import java.util.List;

public class RestaurantListViewFragment extends Fragment {
    public static final String TAG = "TAG";
    private List<Restaurant> restaurantsList;
    private LatLng latLng;

    public RestaurantListViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            restaurantsList = getArguments().getParcelableArrayList("list");
            latLng = getArguments().getParcelable("position");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_list_view, container, false);
        Context context = view.getContext();
        RecyclerView mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(new MyRestaurantRecyclerViewAdapter(restaurantsList, latLng));
        return view;
    }
}
