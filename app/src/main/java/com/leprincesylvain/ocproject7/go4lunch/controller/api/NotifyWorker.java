package com.leprincesylvain.ocproject7.go4lunch.controller.api;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.leprincesylvain.ocproject7.go4lunch.R;
import com.leprincesylvain.ocproject7.go4lunch.model.GetDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class NotifyWorker extends Worker {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection("Users");
    private DocumentReference documentReference;
    String userId;


    private static final String KEY_RESTAURANTCHOICE = "restaurantChoice";
    private static final String KEY_DATEOFCHOICE = "dateOfChoice";
    private static final String KEY_RESTAURANTID = "restaurantId";


    public NotifyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("TAG", "creat doWork: ");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Log.d("TAG", "creat doWork: " + sharedPreferences.getBoolean("notif", true));
        if (sharedPreferences.getBoolean("notif", true)) {
            userId = getInputData().getString("userId");
            createNotification();
        }
        return Result.success();
    }

    private void createNotification() {
        Log.d("TAG", "createNotification: ");
        DocumentReference userReference = userRef.document(userId);

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
                                if (choiceOfRestaurant != null && dateOfChoice == GetDate.getDate()) {
                                    getAllWorkmateEatingWithUser(choiceOfRestaurant, dateOfChoice);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private List<String> workmates = new ArrayList<>();

    private void getAllWorkmateEatingWithUser(final String choice, long dateOfChoice) {
        Log.d("TAG", "getAllWorkmateEatingWithUser: ");

        userRef.whereEqualTo("restaurantChoice", choice)
                .whereEqualTo("dateOfChoice", dateOfChoice)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (!documentSnapshot.getId().equalsIgnoreCase(userId)) {
                                String workmateName = documentSnapshot.getString("userName");
                                workmates.add(workmateName);
                            }
                        }
                        displayNotification(choice, workmates);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

    }

    private void displayNotification(String restaurantChoice, List<String> workmates) {
        Log.d("TAG", "displayNotification: " + restaurantChoice);
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("testingchannel", "testingchannelname", NotificationManager.IMPORTANCE_DEFAULT);
            Objects.requireNonNull(manager).createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "testingchannel")
                .setContentTitle("Votre repas est prévus à " + restaurantChoice)
                .setSmallIcon(R.drawable.logo_app_foreground);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        if (workmates.size() > 0)
            inboxStyle.addLine("Se joindrons à vous :");
        else
            inboxStyle.addLine("Personne ne se joind à vous");

        for (String string : workmates) {
            inboxStyle.addLine(string);
        }
        builder.setStyle(inboxStyle);

        Objects.requireNonNull(manager).notify(1, builder.build());

        Data inputData = new Data.Builder()
                .putString("userId", userId)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(NotifyWorker.class)
                .setInputData(inputData)
                .setInitialDelay(86400, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance().enqueue(request);
    }
}
