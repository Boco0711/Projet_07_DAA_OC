package com.leprincesylvain.ocproject7.go4lunch.controller.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.leprincesylvain.ocproject7.go4lunch.R;
import com.leprincesylvain.ocproject7.go4lunch.adapters.WorkmatesAdapter;
import com.leprincesylvain.ocproject7.go4lunch.model.GetDate;
import com.leprincesylvain.ocproject7.go4lunch.model.Restaurant;
import com.leprincesylvain.ocproject7.go4lunch.model.Workmate;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RestaurantDetailActivity extends AppCompatActivity {
    private ImageView mRestaurantPicture, mRestaurantRating1, mRestaurantRating2, mRestaurantRating3, mRestaurantLike;
    private LinearLayout mRestaurantCallBox, mRestaurantLikeBox, mRestaurantWebsiteBox;
    private TextView mRestaurantName, mRestaurantAddress;
    private FloatingActionButton mRestaurantSelect;
    private RecyclerView restaurantRecyclerView;

    private Restaurant restaurant;

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private String currentUserUid = Objects.requireNonNull(currentUser).getUid();
    private FirebaseFirestore firebaseFirestoreInstance = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = firebaseFirestoreInstance.collection("Users");
    private DocumentReference userReference = collectionReference.document(currentUserUid);

    private static final String KEY_RESTAURANTCHOICE = "restaurantChoice";
    private static final String KEY_DATEOFCHOICE = "dateOfChoice";
    private static final String KEY_RESTAURANTID = "restaurantId";
    long date = GetDate.getDate();

    private int imageNumber;
    private int[] images = {R.drawable.restaurant_unselected, R.drawable.restaurant_selected};

    boolean userLikesIt;

    private WorkmatesAdapter workmatesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);
        makeBinding();

        if (getIntent().getExtras() != null) {
            restaurant = getIntent().getExtras().getParcelable("restaurant");
        }

        if (restaurant != null) {
            giveToActivityTheRestaurantDetails();
        }
    }

    private void makeBinding() {
        mRestaurantPicture = findViewById(R.id.detail_restaurant_picture);
        mRestaurantRating1 = findViewById(R.id.detail_restaurant_rating_1);
        mRestaurantRating2 = findViewById(R.id.detail_restaurant_rating_2);
        mRestaurantRating3 = findViewById(R.id.detail_restaurant_rating_3);
        mRestaurantLike = findViewById(R.id.detail_restaurant_like_button);
        mRestaurantCallBox = findViewById(R.id.detail_restaurant_call_box);
        mRestaurantLikeBox = findViewById(R.id.detail_restaurant_like_box);
        mRestaurantWebsiteBox = findViewById(R.id.detail_restaurant_website_box);
        mRestaurantName = findViewById(R.id.detail_restaurant_name);
        mRestaurantAddress = findViewById(R.id.detail_restaurant_adresse);
        mRestaurantSelect = findViewById(R.id.detail_restaurant_select);
        restaurantRecyclerView = findViewById(R.id.detail_restaurant_list_coworkers);
    }

    private void giveToActivityTheRestaurantDetails() {
        mRestaurantName.setText(restaurant.getName());
        mRestaurantAddress.setText(restaurant.getFormatted_address());
        setRestaurantPhoto();
        setRestaurantRating();
        checkWhichRestaurantUserHasSelectedForNextLunch();
        setListenerOnBoxes();
        getListOfCoworkerEatingThere();
    }

    private void setRestaurantPhoto() {
        String googleApiKey = getApplicationContext().getString(R.string.google_api_key);
        if (restaurant.getPhotos() != null && restaurant.getPhotos().size() > 0) {
            String reference = restaurant.getPhotos().get(0).getPhoto_reference();
            String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + reference + "&key=" + googleApiKey;
            Picasso.get().load(photoUrl).into(mRestaurantPicture);
        } else {
            mRestaurantPicture.setImageResource(R.drawable.restaurant_no_image_found);
        }
    }

    private void setRestaurantRating() {
        if (restaurant.getRating() != null) {
            double avis = (restaurant.getRating() * 3) / 5;
            if (avis > 0.5) {
                mRestaurantRating1.setImageResource(R.drawable.restaurant_star);
                if (avis > 1.5) {
                    mRestaurantRating2.setImageResource(R.drawable.restaurant_star);
                    if (avis > 2.5) {
                        mRestaurantRating3.setImageResource(R.drawable.restaurant_star);
                    }
                }
            }
        }
    }

    private void checkWhichRestaurantUserHasSelectedForNextLunch() {
        userReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String choiceOfRestaurant = "";
                            long dateOfChoice = 0;
                            if (documentSnapshot.getString(KEY_RESTAURANTCHOICE) != null && documentSnapshot.getLong(KEY_DATEOFCHOICE) != null) {
                                choiceOfRestaurant = documentSnapshot.getString(KEY_RESTAURANTCHOICE);
                                dateOfChoice = documentSnapshot.getLong(KEY_DATEOFCHOICE);
                            }
                            selectThisRestaurant(choiceOfRestaurant, dateOfChoice);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void selectThisRestaurant(String restaurantChoice, long dateOfChoice) {
        final long dateOfToday = this.date;
        final String restaurantName = restaurant.getName();
        final String restaurantId = restaurant.getId();
        if (!restaurantName.equalsIgnoreCase(restaurantChoice) || date != dateOfChoice) {
            mRestaurantSelect.setImageResource(R.drawable.restaurant_unselected);
            imageNumber = 0;
        } else {
            mRestaurantSelect.setImageResource(R.drawable.restaurant_selected);
            imageNumber = 1;
        }
        mRestaurantSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> user = new HashMap<>();
                imageNumber++;
                imageNumber = imageNumber % 2;
                mRestaurantSelect.setImageResource(images[imageNumber]);
                if (imageNumber == 0) {
                    user.put(KEY_RESTAURANTCHOICE, "");
                    user.put(KEY_RESTAURANTID, "");
                    user.put(KEY_DATEOFCHOICE, 0);
                } else {
                    user.put(KEY_RESTAURANTCHOICE, restaurantName);
                    user.put(KEY_RESTAURANTID, restaurantId);
                    user.put(KEY_DATEOFCHOICE, dateOfToday);
                }
                userReference.set(user, SetOptions.merge());
            }
        });
    }

    private void getListOfCoworkerEatingThere() {
        Query query = collectionReference
                .whereEqualTo("restaurantChoice", restaurant.getName())
                .whereEqualTo("dateOfChoice", date);

        FirestoreRecyclerOptions<Workmate> options = new FirestoreRecyclerOptions.Builder<Workmate>()
                .setQuery(query, Workmate.class)
                .build();

        workmatesAdapter = new WorkmatesAdapter(options, restaurant.getName());

        restaurantRecyclerView.setHasFixedSize(false);
        restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        restaurantRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        restaurantRecyclerView.setAdapter(workmatesAdapter);
    }

    private void setListenerOnBoxes() {
        if (restaurant.getWebsite() != null) {
            visitWebsite();
        }
        if (restaurant.getFormatted_phone_number() != null) {
            callRestaurant();
        }
        checkIfUserLikeThisRestaurant();
    }

    private void visitWebsite() {
        mRestaurantWebsiteBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (restaurant.getWebsite() != null) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(restaurant.getWebsite()));
                    startActivity(browserIntent);
                } else
                    Toast.makeText(getApplicationContext(), R.string.website_non_available, Toast.LENGTH_LONG).show();
            }
        });
    }


    public void callRestaurant() {
        mRestaurantCallBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (restaurant.getFormatted_phone_number() != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + restaurant.getFormatted_phone_number()));
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.no_number_available, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void checkIfUserLikeThisRestaurant() {
        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                List<?> restaurantLikeByUser = (List<?>) document.get("likes");
                String string;
                if (restaurantLikeByUser != null)
                    for (int i = 0; i < restaurantLikeByUser.size(); i++) {
                        string = (String) restaurantLikeByUser.get(i);
                        if (string.equals(restaurant.getId())) {
                            userLikesIt = true;
                            break;
                        }
                    }
                giveThisRestaurantALike();
            }
        });
    }

    public void giveThisRestaurantALike() {
        if (userLikesIt) {
            mRestaurantLike.setImageResource(R.drawable.restaurant_liked);
        }
        mRestaurantLikeBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userLikesIt) {
                    userReference.update("likes", FieldValue.arrayRemove(restaurant.getId()));
                    userLikesIt = false;
                    mRestaurantLike.setImageResource(R.drawable.restaurant_not_liked);
                } else {
                    userReference.update("likes", FieldValue.arrayUnion(restaurant.getId()));
                    userLikesIt = true;
                    mRestaurantLike.setImageResource(R.drawable.restaurant_liked);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        workmatesAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        workmatesAdapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
