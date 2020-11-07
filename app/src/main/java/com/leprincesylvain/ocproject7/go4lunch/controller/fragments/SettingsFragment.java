package com.leprincesylvain.ocproject7.go4lunch.controller.fragments;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.leprincesylvain.ocproject7.go4lunch.R;
import com.leprincesylvain.ocproject7.go4lunch.controller.activities.MainActivity;

import java.util.Objects;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment_TAG";
    private TextView notificationStatus;
    private Button notificationButton, notificationPanel;
    SharedPreferences sharedPreferences;
    boolean isOk;
    int imp;
    boolean soundAllowed;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            NotificationManager manager = (NotificationManager) getContext().getSystemService(getContext().NOTIFICATION_SERVICE);
            imp = manager.getImportance();
            soundAllowed = imp < 0 || imp >= NotificationManager.IMPORTANCE_DEFAULT;
            Log.d(TAG, "onCreateView: " + imp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        notificationStatus = view.findViewById(R.id.text_notification);
        notificationButton = view.findViewById(R.id.notif_button);
        notificationPanel = view.findViewById(R.id.open_app_settings_notif);
        isOk = sharedPreferences.getBoolean("notif", true);
        if (isOk) {
            notificationButton.setText("Désactiver les notifications");
            notificationStatus.setText("Notification activée");
        } else {
            notificationButton.setText("Activer les notifications");
            notificationStatus.setText("Notification désactivée");
        }

        final Intent settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Settings.EXTRA_APP_PACKAGE, "com.leprincesylvain.ocproject7.go4lunch");

        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (isOk) {
                    editor.putBoolean("notif", false);
                    editor.apply();
                    isOk = false;
                    notificationButton.setText("Activer les notifications");
                    notificationStatus.setText("Notification désactivée");
                } else {
                    editor.putBoolean("notif", true);
                    editor.apply();
                    isOk = true;
                    notificationButton.setText("Désactiver les notifications");
                    notificationStatus.setText("Notification activée");
                    ((MainActivity) requireActivity()).creatOneTimeWorkRequest();
                }
            }
        });

        notificationPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(settingsIntent, 0);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            soundAllowed = imp < 0 || imp >= NotificationManager.IMPORTANCE_DEFAULT;
        }
    }
}
