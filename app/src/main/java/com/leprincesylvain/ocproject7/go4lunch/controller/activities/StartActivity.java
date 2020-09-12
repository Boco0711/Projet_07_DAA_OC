package com.leprincesylvain.ocproject7.go4lunch.controller.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.leprincesylvain.ocproject7.go4lunch.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;

public class StartActivity extends AppCompatActivity {
    public static final String TAG = "StartActivity_TAG";

    Button googleLoginButton, facebookLoginButton;
    TwitterLoginButton twitterLoginButton;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    AccessTokenTracker accessTokenTracker;

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        TwitterAuthConfig mTwitterAuthConfig = new TwitterAuthConfig(getString(R.string.twitter_api_key),
                getString(R.string.twitter_secret_key));
        TwitterConfig twitterConfig = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(mTwitterAuthConfig)
                .debug(true)
                .build();
        Twitter.initialize(twitterConfig);

        setContentView(R.layout.activity_start);

        googleLoginButton = findViewById(R.id.start_activity_button_google_login);
        facebookLoginButton = findViewById(R.id.start_activity_button_facebook_login);
        twitterLoginButton = findViewById(R.id.start_activity_button_twitter_login);

        firebaseAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();
        getAccessTokenTracker();

        Log.d(TAG, "onCreate: ");

        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "Twitter success: ");
                signInToFirebaseWithTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "Twitter failure: " + exception);

            }
        });
    }


    private void signInToFirebaseWithTwitterSession(TwitterSession twitterSession) {
        Log.d(TAG, "signInToFirebaseWithTwitterSession: ");
        AuthCredential credential = TwitterAuthProvider.getCredential(twitterSession.getAuthToken().token, twitterSession.getAuthToken().secret);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "onComplete: sign in with twitter failed ");
                        }
                    }
                });
    }

    private void getFacebookLoginInstance() {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess: ");
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel: ");
                Toast.makeText(getApplicationContext(), "Cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onError: " + error);
                Toast.makeText(getApplicationContext(), "Fail", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void getAccessTokenTracker() {
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.d(TAG, "onCurrentAccessTokenChanged: ");
                if (currentAccessToken == null) {
                    firebaseAuth.signOut();
                }
            }
        };
    }

    private void handleFacebookToken(AccessToken accessToken) {
        Log.d(TAG, "handleFacebookToken: " + accessToken);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Sign in with credential Success");
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    updateUi(user);
                } else {
                    Log.d(TAG, "onComplete: Sign in with credential Fail ", task.getException());
                }
            }
        });
    }

    private void updateUi(FirebaseUser user) {
        Log.d(TAG, "updateUi: ");
        if (user != null) {
            Log.d(TAG, "updateUi: User != null");
            String userName = user.getDisplayName();
            String userEmail = user.getEmail();
            String userId = user.getUid();
            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            if (user.getPhotoUrl() != null) {
                Log.d(TAG, "updateUi: user.getPhotoUrl != null");
                String userPhoto = user.getPhotoUrl().toString();
                userPhoto = userPhoto + "?type=large";
                mainActivityIntent.putExtra("user_photo", userPhoto);
            } else
                Log.d(TAG, "updateUi: user.getPhotoUrl == null");
            mainActivityIntent.putExtra("user_name", userName);
            mainActivityIntent.putExtra("user_email", userEmail);
            mainActivityIntent.putExtra("user_id", userId);
            startActivity(mainActivityIntent);
        } else
            Log.d(TAG, "updateUi: User == null");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: test " + requestCode + " " + resultCode);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenOnLoginButtonClick();

        listenTheAuthStateListener();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (authStateListener != null) {
            Log.d(TAG, "onStop: ");
            firebaseAuth.removeAuthStateListener(authStateListener);
        } else {
            Log.d(TAG, "onStop: ");
        }
    }

    private void listenTheAuthStateListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "onAuthStateChanged: ");
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    updateUi(user);
                } else
                    updateUi(null);
            }
        };
    }

    private void listenOnLoginButtonClick() {
        Log.d(TAG, "listenOnLoginButtonClick: ");
        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: FacebookLoginButton Clicked");
                LoginManager.getInstance().logInWithReadPermissions(StartActivity.this, Arrays.asList("email", "public_profile"));
                getFacebookLoginInstance();
            }
        });
    }

    private void connectWithTwitter() {

    }
}
