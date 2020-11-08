package com.leprincesylvain.ocproject7.go4lunch.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.leprincesylvain.ocproject7.go4lunch.R;
import com.leprincesylvain.ocproject7.go4lunch.controller.api.CircleTransform;
import com.leprincesylvain.ocproject7.go4lunch.controller.api.RecyclerViewOnClickListener;
import com.leprincesylvain.ocproject7.go4lunch.model.GetDate;
import com.leprincesylvain.ocproject7.go4lunch.model.Workmate;
import com.squareup.picasso.Picasso;

public class WorkmatesAdapter extends FirestoreRecyclerAdapter<Workmate, WorkmatesAdapter.WorkmatesHolder> {
    private static final String TAG = "WorkmateAdapter_TAG";
    private String restaurantName;
    long dateOfToday = GetDate.getDate();
    private RecyclerViewOnClickListener mListener;

    public WorkmatesAdapter(@NonNull FirestoreRecyclerOptions<Workmate> options, RecyclerViewOnClickListener listener) {
        super(options);
        this.mListener = listener;
        Log.d(TAG, "WorkmatesAdapter: empty" + options);
    }

    public WorkmatesAdapter(@NonNull FirestoreRecyclerOptions<Workmate> options, String name) {
        super(options);
        Log.d(TAG, "WorkmatesAdapter: restaurantName ");
        this.restaurantName = name;
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkmatesHolder workmatesHolder, int i, @NonNull Workmate workmate) {
        Log.d(TAG, "onBindViewHolder: ");
        Context context = workmatesHolder.itemView.getContext();
        if (workmate.getUserProfilePicture() != null)
            Picasso.get().load(workmate.getUserProfilePicture()).transform(new CircleTransform()).into(workmatesHolder.mWorkmatePicture);
        else
            workmatesHolder.mWorkmatePicture.setImageResource(R.drawable.workmate_no_image_found);
        Log.d(TAG, "onBindViewHolder: " + workmate.getUserProfilePicture());

        String workmateName = workmate.getUserName();
        String workmateRestaurantChoice = workmate.getRestaurantChoice();
        long workmateDateOfChoice = workmate.getDateOfChoice();
        final String workmateRestaurantChoiceId = workmate.getRestaurantId();

        String hasSelectedThisRestaurant = workmateName + context.getString(R.string.is_joining);
        String hasChooseARestaurant = workmateName + context.getString(R.string.hasChoose) + workmateRestaurantChoice;
        String hasNotChoose = workmateName + context.getString(R.string.has_not_choose);

        if (workmateRestaurantChoice != null && workmateDateOfChoice == dateOfToday) {
            Log.d(TAG, "onBindViewHolder: if");
            if (restaurantName != null) {
                Log.d(TAG, "onBindViewHolder: if if");
                workmatesHolder.mWorkmateText.setText(hasSelectedThisRestaurant);
            } else {
                Log.d(TAG, "onBindViewHolder: if else");
                workmatesHolder.mWorkmateText.setText(hasChooseARestaurant);
            }
            Typeface bold = Typeface.defaultFromStyle(Typeface.BOLD);
            workmatesHolder.mWorkmateText.setTypeface(bold);
        } else {
            Log.d(TAG, "onBindViewHolder: else");
            workmatesHolder.mWorkmateText.setText(hasNotChoose);
        }
    }

    @NonNull
    @Override
    public WorkmatesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workmate, parent, false);
        return new WorkmatesHolder(view, mListener);
    }

    static class WorkmatesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mWorkmateText;
        ImageView mWorkmatePicture;
        RecyclerViewOnClickListener listener;

        public WorkmatesHolder(@NonNull View itemView, RecyclerViewOnClickListener listener) {
            super(itemView);
            Log.d(TAG, "WorkmatesHolder: ");
            mWorkmatePicture = itemView.findViewById(R.id.workmate_picture);
            mWorkmateText = itemView.findViewById(R.id.workmate_text);
            this.listener = listener;

            if (this.listener != null)
                itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.recyclerviewClick(getAdapterPosition());
        }
    }
}
