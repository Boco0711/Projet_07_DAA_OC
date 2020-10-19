package com.leprincesylvain.ocproject7.go4lunch.controller.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.leprincesylvain.ocproject7.go4lunch.R;
import com.leprincesylvain.ocproject7.go4lunch.adapters.WorkmatesAdapter;
import com.leprincesylvain.ocproject7.go4lunch.model.Workmate;

public class WorkmatesFragment extends Fragment {
    private static final String TAG = "WorkmatesFragment_TAG";

    private FirebaseFirestore firebaseFirestoreInstance = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = firebaseFirestoreInstance.collection("Users");

    private WorkmatesAdapter workmateAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        setUpRecyclerView(view);
        return view;
    }

    private void setUpRecyclerView(View view) {
        Log.d(TAG, "setUpRecyclerView: ");
        Query query = collectionReference
                .orderBy("dateOfChoice", Query.Direction.DESCENDING)
                .orderBy("userName", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Workmate> options = new FirestoreRecyclerOptions.Builder<Workmate>()
                .setQuery(query, Workmate.class)
                .build();

        workmateAdapter = new WorkmatesAdapter(options);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_workmates);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(workmateAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        workmateAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        workmateAdapter.stopListening();
    }
}
