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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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
    private static final int RC_SIGN_IN = 123;

    Button emailLoginButton, googleLoginButton, facebookLoginButton;
    TwitterLoginButton twitterLoginButton;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    AccessTokenTracker accessTokenTracker;

    private CallbackManager callbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private EmailAuthCredential emailAuthCredential;


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

        emailLoginButton = findViewById(R.id.start_activity_button_email_login);
        googleLoginButton = findViewById(R.id.start_activity_button_google_login);
        facebookLoginButton = findViewById(R.id.start_activity_button_facebook_login);
        twitterLoginButton = findViewById(R.id.start_activity_button_twitter_login);

        firebaseAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();
        getAccessTokenTracker();

        Log.d(TAG, "onCreate: ");

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    @Override
    protected void onStart() {
        super.onStart();
        listenOnAllLoginButtonClick();
        listenTheAuthStateListener();
        firebaseAuth.addAuthStateListener(authStateListener);
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

    private void listenOnAllLoginButtonClick() {
        Log.d(TAG, "listenOnAllLoginButtonClick: ");
        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: FacebookLoginButton Clicked");
                LoginManager.getInstance().logInWithReadPermissions(StartActivity.this, Arrays.asList("email", "public_profile"));
                startSignInWithFacebook();
            }
        });

        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "Twitter success: ");
                firebaseAuthWithTwitter(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "Twitter failure: " + exception);
            }
        });

        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignInWithGoogle();
            }
        });

        emailLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignInWithEmail();
            }
        });
    }

    private void startSignInWithEmail() {
        Intent intent = new Intent(this, EmailPasswordActivity.class);
        startActivity(intent);
        this.finish();
    }

    private void startSignInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void startSignInWithFacebook() {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess: ");
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                firebaseAuthWithFacebook(loginResult.getAccessToken());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: test " + requestCode + " " + resultCode);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:");
                if (null != account)
                    firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + idToken);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        signIn(credential);
    }

    private void firebaseAuthWithFacebook(AccessToken accessToken) {
        Log.d(TAG, "handleFacebookToken: " + accessToken);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        signIn(credential);
    }

    private void firebaseAuthWithTwitter(TwitterSession twitterSession) {
        Log.d(TAG, "signInToFirebaseWithTwitterSession: ");

        AuthCredential credential = TwitterAuthProvider.getCredential(twitterSession.getAuthToken().token, twitterSession.getAuthToken().secret);
        signIn(credential);
    }

    private void signIn(AuthCredential credential) {
        Log.d(TAG, "signIn: with one of 3 means ");
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: sign in success");
                    firebaseAuth.getCurrentUser();
                } else {
                    Log.d(TAG, "onComplete: sign in failed ");
                }
            }
        });
    }

    private void listenTheAuthStateListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "onAuthStateChanged: ");
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged: user != null");
                    updateUi(user);
                } else {
                    Log.d(TAG, "onAuthStateChanged: user = null");
                    updateUi(null);
                }
            }
        };
    }

    public void updateUi(FirebaseUser user) {
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
                mainActivityIntent.putExtra("user_photo", userPhoto);
            } else
                Log.d(TAG, "updateUi: user.getPhotoUrl == null");
            mainActivityIntent.putExtra("user_name", userName);
            mainActivityIntent.putExtra("user_email", userEmail);
            mainActivityIntent.putExtra("user_id", userId);
            startActivity(mainActivityIntent);
            this.finish();
        } else
            Log.d(TAG, "updateUi: User == null");
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

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }
}
