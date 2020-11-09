package com.leprincesylvain.ocproject7.go4lunch.controller.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.leprincesylvain.ocproject7.go4lunch.R;

public class EmailPasswordActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

    private EditText usernameText, emailText, passwordText;
    private Button loginButton, createButton, cancelButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.email_password_login);
        usernameText = findViewById(R.id.username_field);
        emailText = findViewById(R.id.email_field);
        passwordText = findViewById(R.id.password_field);
        loginButton = findViewById(R.id.sign_in_button_email_login);
        createButton = findViewById(R.id.create_button_email_login);
        cancelButton = findViewById(R.id.cancel_button_email_login);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailText.getText() != null && passwordText.getText() != null) {
                    if (checkIfMailIsCorrect(emailText.getText())) {
                        signInWithMailAndPassword(emailText.getText().toString(), passwordText.getText().toString());
                    } else
                        Toast.makeText(getApplicationContext(), R.string.not_a_valid_email, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), R.string.all_field_required, Toast.LENGTH_SHORT).show();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usernameText.getText().length() > 0 && emailText.getText() != null && passwordText.getText() != null) {
                    if (checkIfMailIsCorrect(emailText.getText()) && passwordText.getText().length() >= 6) {
                        createWithMailAndPassword(usernameText.getText().toString(), emailText.getText().toString(), passwordText.getText().toString());
                    }
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void signInWithMailAndPassword(final String mail, final String password) {
        firebaseAuth.signInWithEmailAndPassword(mail, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUi(user);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.fail_to_sign_in, Toast.LENGTH_SHORT).show();
                            updateUi(null);
                        }
                    }
                });

    }

    public void createWithMailAndPassword(String username, String mail, String password) {
        firebaseAuth.createUserWithEmailAndPassword(mail, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser user = firebaseAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(usernameText.getText().toString())
                                    .build();
                            if (user != null)
                                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        userName = user.getDisplayName();
                                        updateUi(user);
                                    }
                                });
                        } else {
                            Toast.makeText(EmailPasswordActivity.this, "Creation failed.", Toast.LENGTH_SHORT).show();
                            updateUi(null);
                        }
                    }
                });
    }

    String userName;

    public void updateUi(final FirebaseUser user) {
        if (user != null) {
            userName = user.getDisplayName();
            String userEmail = user.getEmail();
            String userId = user.getUid();
            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            if (user.getPhotoUrl() != null) {
                String userPhoto = user.getPhotoUrl().toString();
                mainActivityIntent.putExtra("user_photo", userPhoto);
            }
            mainActivityIntent.putExtra("user_name", userName);
            mainActivityIntent.putExtra("user_email", userEmail);
            mainActivityIntent.putExtra("user_id", userId);
            startActivity(mainActivityIntent);
            this.finish();
        }
    }

    private boolean checkIfMailIsCorrect(Editable text) {
        return Patterns.EMAIL_ADDRESS.matcher(text.toString()).matches();
    }
}
