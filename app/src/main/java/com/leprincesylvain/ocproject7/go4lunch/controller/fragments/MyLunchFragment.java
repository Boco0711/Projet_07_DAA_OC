package com.leprincesylvain.ocproject7.go4lunch.controller.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.leprincesylvain.ocproject7.go4lunch.R;
import com.leprincesylvain.ocproject7.go4lunch.controller.activities.RestaurantDetailActivity;
import com.leprincesylvain.ocproject7.go4lunch.model.GetDate;
import com.leprincesylvain.ocproject7.go4lunch.model.Restaurant;

import java.util.List;
import java.util.Objects;

public class MyLunchFragment extends Fragment {
    public static final String TAG = "MyLnchFragment_TAG";

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private String currentUserUid = Objects.requireNonNull(currentUser).getUid();
    private FirebaseFirestore firebaseFirestoreInstance = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = firebaseFirestoreInstance.collection("Users");
    private DocumentReference userReference = collectionReference.document(currentUserUid);

    private static final String KEY_RESTAURANTCHOICE = "restaurantChoice";
    private static final String KEY_DATEOFCHOICE = "dateOfChoice";
    long date = GetDate.getDate();

    private TextView restaurantName;
    private List<Restaurant> restaurantList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            restaurantList = getArguments().getParcelableArrayList("list");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_mylunch, container, false);
        restaurantName = view.findViewById(R.id.text_selected_restaurant);

        checkIfUserHasSelectedARestaurant();
        return view;
    }

    private void checkIfUserHasSelectedARestaurant() {
        Log.d(TAG, "checkIfUserHasSelectedARestaurant: ");
        userReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {

                            String choiceOfRestaurant = documentSnapshot.getString(KEY_RESTAURANTCHOICE);
                            long dateOfChoice = documentSnapshot.getLong(KEY_DATEOFCHOICE);
                            Log.d(TAG, "onSuccess: 1 " + choiceOfRestaurant + " " + dateOfChoice);
                            for (Restaurant restaurant : restaurantList) {
                                if ((restaurant.getName().equalsIgnoreCase(choiceOfRestaurant)) && (date == dateOfChoice)) {
                                    Log.d(TAG, "onSuccess: 2");
                                    Intent intent = new Intent(requireContext(), RestaurantDetailActivity.class);
                                    intent.putExtra("restaurant", restaurant);
                                    requireContext().startActivity(intent);
                                }
                            }

                        }
                    }
                });
    }
}
