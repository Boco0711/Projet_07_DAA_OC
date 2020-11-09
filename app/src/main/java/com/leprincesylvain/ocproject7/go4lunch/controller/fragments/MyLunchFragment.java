package com.leprincesylvain.ocproject7.go4lunch.controller.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private String currentUserUid = Objects.requireNonNull(currentUser).getUid();
    private FirebaseFirestore firebaseFirestoreInstance = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = firebaseFirestoreInstance.collection("Users");
    private DocumentReference userReference = collectionReference.document(currentUserUid);

    private static final String KEY_RESTAURANTCHOICE = "restaurantChoice";
    private static final String KEY_DATEOFCHOICE = "dateOfChoice";
    long date = GetDate.getDate();

    private TextView restaurantName;
    private Button restaurantDetailButton;
    private List<Restaurant> restaurantList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            restaurantList = getArguments().getParcelableArrayList("list");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mylunch, container, false);
        restaurantName = view.findViewById(R.id.my_lunch_selected_restaurant_text);
        restaurantDetailButton = view.findViewById(R.id.my_lunch_detail_button);
        getIfUserHasSelectedARestaurant();
        return view;
    }

    private void getIfUserHasSelectedARestaurant() {
        userReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.getString(KEY_RESTAURANTCHOICE) != null && documentSnapshot.getLong(KEY_DATEOFCHOICE) != null) {
                                String choiceOfRestaurant = documentSnapshot.getString(KEY_RESTAURANTCHOICE);
                                long dateOfChoice = documentSnapshot.getLong(KEY_DATEOFCHOICE);
                                getRestaurantIfExist(choiceOfRestaurant, dateOfChoice);
                            } else {
                                restaurantName.setText(requireContext().getString(R.string.not_selected_restaurant_mylunch));
                                restaurantDetailButton.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }

    private void getRestaurantIfExist(String choiceOfRestaurant, long dateOfChoice) {
        int i = 0;
        for (final Restaurant restaurant : restaurantList) {
            if ((restaurant.getName().equalsIgnoreCase(choiceOfRestaurant)) && (date == dateOfChoice)) {
                i++;
                restaurantDetailButton.setText(R.string.see_restaurant_details);
                String willEatAt = requireContext().getString(R.string.selected_a_restaurant_mylunch) + choiceOfRestaurant;
                restaurantName.setText(willEatAt);
                restaurantDetailButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(requireContext(), RestaurantDetailActivity.class);
                        intent.putExtra("restaurant", restaurant);
                        requireContext().startActivity(intent);
                    }
                });
            }
        }
        if (i == 0) {
            restaurantName.setText(requireContext().getString(R.string.not_selected_restaurant_mylunch));
            restaurantDetailButton.setVisibility(View.GONE);
        }
    }
}
