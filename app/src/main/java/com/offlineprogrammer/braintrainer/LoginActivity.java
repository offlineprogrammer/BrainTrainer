package com.offlineprogrammer.braintrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ProfileScope;
import com.amazon.identity.auth.device.api.authorization.Scope;
import com.amazon.identity.auth.device.api.authorization.User;
import com.amazon.identity.auth.device.api.workflow.RequestContext;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    ImageButton login_with_amazon;
    private RequestContext requestContext;
    private boolean mIsLoggedIn;
    private ProgressBar mLogInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupAuthorization();

        login_with_amazon = findViewById(R.id.login_with_amazon);
        mLogInProgress = findViewById(R.id.log_in_progress);

        login_with_amazon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login() {
        Log.i("loginButton Clicked","Start LWA");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // At this point we know the authorization completed, so remove the ability to return to the app to sign-in again
                setLoggingInState(true);
            }
        });
        AuthorizationManager.authorize(new AuthorizeRequest
                .Builder(requestContext)
                .addScopes(ProfileScope.profile(), ProfileScope.postalCode())
                .build());
    }

    public void logout(View view){
        Log.i("logout Clicked","Start LWA");
        AuthorizationManager.signOut(getApplicationContext(), new Listener<Void, AuthError>() {
            @Override
            public void onSuccess(Void response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setLoggedOutState();
                     //   recordEvent("User LoggedOut");
                    }
                });
            }

            @Override
            public void onError(AuthError authError) {
                Log.e(TAG, "Error clearing authorization state.", authError);
            }
        });
    }

    private void setupAuthorization() {
        requestContext = RequestContext.create(this);

        requestContext.registerListener(new AuthorizeListener() {
            /* Authorization was completed successfully. */
            @Override
            public void onSuccess(AuthorizeResult authorizeResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // At this point we know the authorization completed, so remove the ability to return to the app to sign-in again
                        setLoggingInState(true);
                    }
                });
                fetchUserProfile();
            }

            /* There was an error during the attempt to authorize the application */
            @Override
            public void onError(AuthError authError) {
                Log.e(TAG, "AuthError during authorization", authError);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAuthToast("Error during authorization.  Please try again.");
                        resetProfileView();
                        setLoggingInState(false);
                    }
                });
            }

            /* Authorization was cancelled before it could be completed. */
            @Override
            public void onCancel(AuthCancellation authCancellation) {
                Log.e(TAG, "User cancelled authorization");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAuthToast("Authorization cancelled");
                        resetProfileView();
                    }
                });
            }
        });
    }

    private void setLoggingInState(final boolean loggingIn) {
        if (loggingIn) {
            Log.d(TAG, "Showing the progress bar");
            mLogInProgress.setVisibility(View.VISIBLE);
            // mProfileText.setVisibility(TextView.GONE);
        } else {
            // mProfileText.setVisibility(TextView.VISIBLE);
        }
    }

    private void setLoggedOutState() {
        // mLoginButton.setVisibility(Button.VISIBLE);
        //setLoggedInButtonsVisibility(Button.GONE);
        mIsLoggedIn = false;
        resetProfileView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestContext.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Scope[] scopes = {ProfileScope.profile(), ProfileScope.postalCode()};
        AuthorizationManager.getToken(this, scopes, new Listener<AuthorizeResult, AuthError>() {
            @Override
            public void onSuccess(AuthorizeResult result) {
                if (result.getAccessToken() != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setLoggingInState(true);
                            //  setLoggedInState();
                        }
                    });

                    /* The user is signed in */
                    Log.i(TAG,"result.getAccessToken()");
                    fetchUserProfile();
                } else {
                    Log.i(TAG,"Else....result.getAccessToken()");
                    /* The user is not signed in */
                }
            }

            @Override
            public void onError(AuthError ae) {
                /* The user is not signed in */
            }
        });
    }

    private void updateProfileData(String name, String email, String account, String zipCode) {
        StringBuilder profileBuilder = new StringBuilder();
        profileBuilder.append(String.format("Welcome, %s!\n", name));
        profileBuilder.append(String.format("Your email is %s\n", email));
        profileBuilder.append(String.format("Your zipCode is %s\n", zipCode));
        final String profile = profileBuilder.toString();
        Log.d(TAG, "Profile Response: " + profile);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateProfileView(profile);
              //  setLoggedInState();
            }
        });
    }

    private void fetchUserProfile() {
        User.fetch(this, new Listener<User, AuthError>() {

            /* fetch completed successfully. */
            @Override
            public void onSuccess(User user) {
                final String name = user.getUserName();
                final String email = user.getUserEmail();
                final String account = user.getUserId();
                final String zipCode = user.getUserPostalCode();

                Log.i("Info","Welcome MM");
             //   recordEvent("User LoggedIn");




                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateProfileData(name, email, account, zipCode);
                        //textView.setText(String.format("Welcome, %s!\n", name));
                        launchMainActivity(user);
                    }
                });
            }

            /* There was an error during the attempt to get the profile. */
            @Override
            public void onError(AuthError ae) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setLoggedOutState();
                        String errorMessage = "Error retrieving profile information.\nPlease log in again";
                        Toast errorToast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG);
                        errorToast.setGravity(Gravity.CENTER, 0, 0);
                        errorToast.show();
                    }
                });
            }
        });
    }

    private void launchMainActivity(User user) {
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, GameActivity.class));
            finish();
        }
    }

    private void updateProfileView(String profileInfo) {
        Log.d(TAG, "Updating profile view");
        //textView.setText(profileInfo);
    }

    private void showAuthToast(String authToastMessage) {
        Toast authToast = Toast.makeText(getApplicationContext(), authToastMessage, Toast.LENGTH_LONG);
        authToast.setGravity(Gravity.CENTER, 0, 0);
        authToast.show();
    }

    private void resetProfileView() {
        setLoggingInState(false);
        //mProfileText.setText(getString(R.string.default_message));
        //textView.setText("Test your math speed");
    }
}
