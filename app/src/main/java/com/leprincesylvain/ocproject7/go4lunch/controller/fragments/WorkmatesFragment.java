package com.leprincesylvain.ocproject7.go4lunch.controller.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.leprincesylvain.ocproject7.go4lunch.R;
import com.leprincesylvain.ocproject7.go4lunch.adapters.WorkmatesAdapter;
import com.leprincesylvain.ocproject7.go4lunch.controller.activities.RestaurantDetailActivity;
import com.leprincesylvain.ocproject7.go4lunch.controller.api.RecyclerViewOnClickListener;
import com.leprincesylvain.ocproject7.go4lunch.model.GetDate;
import com.leprincesylvain.ocproject7.go4lunch.model.Restaurant;
import com.leprincesylvain.ocproject7.go4lunch.model.Workmate;

import java.util.List;
import java.util.Objects;

public class WorkmatesFragment extends Fragment implements RecyclerViewOnClickListener {
    private FirebaseFirestore firebaseFirestoreInstance = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = firebaseFirestoreInstance.collection("Users");
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private String currentUserUid = Objects.requireNonNull(currentUser).getUid();

    private WorkmatesAdapter workmateAdapter;

    private List<Restaurant> restaurantList;
    private List<Workmate> workmateList;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            restaurantList = getArguments().getParcelableArrayList("restaurantList");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        setUpRecyclerView(view);
        return view;
    }

    private void setUpRecyclerView(View view) {
        Query query = collectionReference
                .orderBy("dateOfChoice", Query.Direction.DESCENDING)
                .orderBy("userName", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Workmate> options = new FirestoreRecyclerOptions.Builder<Workmate>()
                .setQuery(query, Workmate.class)
                .build();

        workmateList = options.getSnapshots();
        workmateAdapter = new WorkmatesAdapter(options, this);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_workmates);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(workmateAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        workmateAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        workmateAdapter.stopListening();
    }

    @Override
    public void recyclerviewClick(int position) {
        String restaurantId = null;
        long dateOfChoice = 0;
        if (workmateList != null) {
            Workmate workmate = workmateList.get(position);
            restaurantId = workmate.getRestaurantId();
            dateOfChoice = workmate.getDateOfChoice();
        }
        if (restaurantId != null && dateOfChoice == GetDate.getDate()) {
            for (Restaurant restaurant : restaurantList) {
                if (restaurant.getId().equalsIgnoreCase(restaurantId)) {
                    Intent intent = new Intent(getContext(), RestaurantDetailActivity.class);
                    intent.putExtra("restaurant", restaurant);
                    getContext().startActivity(intent);
                    break;
                }
            }
        }
    }
}
