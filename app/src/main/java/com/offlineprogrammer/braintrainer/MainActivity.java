package com.offlineprogrammer.braintrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdRegistration;
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


import java.util.ArrayList;
import java.util.Random;




public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    Button goButton;


    TextView resultTextView;
    int score = 0;
    int numberOfQuestions = 0;
    TextView scoreTextView;
    Button button0 ;
    Button button1 ;
    Button button2 ;
    Button button3 ;
    private TextView sumTextView;
    TextView timerTextView;
    Button playAgain;
    ConstraintLayout gameLayout;
    ConstraintLayout configLayout;
    ConstraintLayout homeLayout;
    LinearLayout mhomeOpsLayout;
    CountDownTimer countDownTimer = null;
    ImageButton login_with_amazon;
    Button mLogoutButton;
    TextView textView;
    private boolean mIsLoggedIn;
    private TheGame myGame;

    private AdLayout adView; // The ad view used to load and display the ad.
    private static final String APP_KEY = "3967f616abb34b3c9f83c8d4c86eec34"; // Sample Application Key. Replace this value with your Application Key.
    private static final String LOG_TAG = "SimpleAdSample"; // Tag used to prefix all log messages.

    private RequestContext requestContext;
    private ProgressBar mLogInProgress;






    public void newQuestion(){
        ArrayList<Integer> answers ;
        answers = myGame.setupGame();
        sumTextView.setText(String.format("%s %s %s", Integer.toString(myGame.a), myGame.getOperation() , Integer.toString(myGame.b)));
        button0.setText(Integer.toString(answers.get(0)));
        button1.setText(Integer.toString(answers.get(1)));
        button2.setText(Integer.toString(answers.get(2)));
        button3.setText(Integer.toString(answers.get(3)));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUi();
        setupGame();
        setupAds();
        setupAuthorization();

    }

    private void setupUi() {
        goButton = findViewById(R.id.goButton);
        sumTextView = findViewById(R.id.sumTextView);
        resultTextView = findViewById(R.id.resultTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        timerTextView = findViewById(R.id.timerTextView);
        playAgain = findViewById(R.id.playAgainButton);
        gameLayout=findViewById(R.id.gameLayout);
        configLayout = findViewById(R.id.configLayout);
        login_with_amazon = findViewById(R.id.login_with_amazon);
        textView = findViewById(R.id.textView);
        mLogInProgress = findViewById(R.id.log_in_progress);
        mLogoutButton = findViewById(R.id.logout);
        homeLayout = findViewById(R.id.homeLayout);
        mhomeOpsLayout = findViewById(R.id.homeOpsLayout);


        homeLayout.setVisibility(View.VISIBLE);
        setHomeOpsVisibility(View.VISIBLE);
        configLayout.setVisibility(View.INVISIBLE);
        gameLayout.setVisibility(View.INVISIBLE);
    }

    private void setupGame(){
        myGame = new TheGame("+");
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestContext.onResume();
    }

    private void setupAds() {

        AdRegistration.setAppKey("3967f616abb34b3c9f83c8d4c86eec34");
        AdRegistration.enableLogging(true);
        // For debugging purposes flag all ad requests as tests, but set to false for production builds.
        AdRegistration.enableTesting(true);
        this.adView = findViewById(R.id.ad_view);
        try {
            AdRegistration.setAppKey(APP_KEY);
            loadAd();
        } catch (final IllegalArgumentException e) {
            Log.e(LOG_TAG, "IllegalArgumentException thrown: " + e.toString());
            return;
        }

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

    public void loadAd() {
        this.adView.loadAd();
    }

    public void start(View view){
        Log.i("goButton Clicked","Hide");
        //goButton.setVisibility(View.INVISIBLE);
        homeLayout.setVisibility(View.INVISIBLE);
        gameLayout.setVisibility(View.VISIBLE);
        myGame.setActive(true);
        newQuestion();
        playAgain(playAgain);


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
                        homeLayout.setVisibility(View.VISIBLE);
                        setHomeOpsVisibility(View.VISIBLE);
                        configLayout.setVisibility(View.INVISIBLE);
                        gameLayout.setVisibility(View.INVISIBLE);
                        myGame.setActive(false);
                    }
                });
            }

            @Override
            public void onError(AuthError authError) {
                Log.e(TAG, "Error clearing authorization state.", authError);
            }
        });
    }

    public void login(View view){
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

    @Override
    protected void onStart() {
        super.onStart();
        Scope[] scopes = {ProfileScope.profile(), ProfileScope.postalCode()};
        AuthorizationManager.getToken(this, scopes, new Listener<AuthorizeResult, AuthError>() {
            @Override
            public void onSuccess(AuthorizeResult result) {
                if (result.getAccessToken() != null) {
                    /* The user is signed in */
                    Log.i("Info","result.getAccessToken()");
                    fetchUserProfile();
                } else {
                    Log.i("Info","Else....result.getAccessToken()");
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
                setLoggedInState();
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



                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateProfileData(name, email, account, zipCode);
                        textView.setText(String.format("Welcome, %s!\n", name));
                        login_with_amazon.setVisibility( View.INVISIBLE);
                        homeLayout.setVisibility(View.VISIBLE);
                        setHomeOpsVisibility(View.VISIBLE);
                        configLayout.setVisibility(View.INVISIBLE);
                        gameLayout.setVisibility(View.INVISIBLE);
                        myGame.setActive(false);
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

    private void updateProfileView(String profileInfo) {
        Log.d(TAG, "Updating profile view");
        textView.setText(profileInfo);
    }

    private void showAuthToast(String authToastMessage) {
        Toast authToast = Toast.makeText(getApplicationContext(), authToastMessage, Toast.LENGTH_LONG);
        authToast.setGravity(Gravity.CENTER, 0, 0);
        authToast.show();
    }

    private void resetProfileView() {
        setLoggingInState(false);
        //mProfileText.setText(getString(R.string.default_message));
        textView.setText("Test your math speed");
    }


    private void setHomeOpsVisibility(int visibility){
        mhomeOpsLayout.setVisibility(visibility);
        if (mIsLoggedIn) {
            setLoggedInButtonsVisibility(Button.VISIBLE);
        } else {
            login_with_amazon.setVisibility(Button.VISIBLE);
            setLoggedInButtonsVisibility(Button.GONE);
        }

    }

    /**
     * Sets the state of the application to reflect that the user is currently authorized.
     */
    private void setLoggedInState() {
       // mLoginButton.setVisibility(Button.GONE);
        mIsLoggedIn = true;
        setLoggingInState(false);
        setLoggedInButtonsVisibility(Button.VISIBLE);
    }

    /**
     * Sets the state of the application to reflect that the user is not currently authorized.
     */
    private void setLoggedOutState() {
       // mLoginButton.setVisibility(Button.VISIBLE);
        setLoggedInButtonsVisibility(Button.GONE);
        mIsLoggedIn = false;
        resetProfileView();
    }

    /**
     * Changes the visibility for both of the buttons that are available during the logged in state
     *
     * @param visibility the visibility to which the buttons should be set
     */
    private void setLoggedInButtonsVisibility(int visibility) {
        mLogoutButton.setVisibility(visibility);
    }

    /**
     * Turns on/off display elements which indicate that the user is currently in the process of logging in
     *
     * @param loggingIn whether or not the user is currently in the process of logging in
     */
    private void setLoggingInState(final boolean loggingIn) {
        if (loggingIn) {
           // mLoginButton.setVisibility(Button.GONE);
            setHomeOpsVisibility(View.GONE);
            setLoggedInButtonsVisibility(Button.GONE);
            Log.d(TAG, "Showing the progress bar");
            mLogInProgress.setVisibility(View.VISIBLE);
            homeLayout.setVisibility(View.GONE);
            //goButton.setVisibility(View.GONE);
           // mProfileText.setVisibility(TextView.GONE);
        } else {
            setHomeOpsVisibility(View.VISIBLE);
            if (mIsLoggedIn) {
                setLoggedInButtonsVisibility(Button.VISIBLE);
            } else {
                login_with_amazon.setVisibility(Button.VISIBLE);
            }
            mLogInProgress.setVisibility(ProgressBar.GONE);
            homeLayout.setVisibility(View.VISIBLE);
            //goButton.setVisibility(View.VISIBLE);
           // mProfileText.setVisibility(TextView.VISIBLE);
        }
    }




    public void chooseOperation(View view){

        homeLayout.setVisibility(View.VISIBLE);
        gameLayout.setVisibility(View.INVISIBLE);
        configLayout.setVisibility(View.INVISIBLE);
        setHomeOpsVisibility(View.VISIBLE);

        myGame.setOperation(view.getTag().toString());

        if ( myGame.getOperation().equals("?")) {
            String[] list = {"+", "-", "*"};
            Random rand = new Random();

           myGame.setOperation(list[rand.nextInt(list.length)]);


        }
        Log.i("chooseOperation: ", " is" +myGame.getOperation());
        start(goButton);

    }


    public void configureGame(View view){
        Log.i("configureButton Clicked","Hide");

        homeLayout.setVisibility(View.INVISIBLE);
        setHomeOpsVisibility(View.INVISIBLE);
        gameLayout.setVisibility(View.INVISIBLE);
        configLayout.setVisibility(View.VISIBLE);


    }


    public void playAgain(View view){
        score=0;
        numberOfQuestions=0;
        timerTextView.setText("30s");
        scoreTextView.setText(Integer.toString(score) + "/" + Integer.toString(numberOfQuestions));
        resultTextView.setText("");
        newQuestion();
        loadAd();
        playAgain.setVisibility(View.INVISIBLE);
        myGame.setActive(true);
        if(countDownTimer  != null){
            countDownTimer.cancel();
        }
        countDownTimer =  new CountDownTimer(30100,1000){

            @Override
            public void onTick(long millisUntilFinished) {

                timerTextView.setText(String.valueOf(millisUntilFinished/1000)+"s");

            }

            @Override
            public void onFinish() {
                resultTextView.setText("Done!");
                playAgain.setVisibility(View.VISIBLE);
                myGame.setActive(false);


            }
        }.start();
    }

    public void chooseAnswer(View view){
        if (!myGame.isActive()){
            return;
        }
        Log.i("Selected button","is " + view.getTag().toString());
        int selectedButton = Integer.parseInt(view.getTag().toString());

        if (selectedButton == myGame.locationOfCorrectAnswer){
            resultTextView.setText("Correct!");
            score++;
            Log.i("Winner", "button" + selectedButton);
        } else {
            resultTextView.setText("Wrong :(");
            Log.i("Wrong :( ", "button" + selectedButton);

        }
        numberOfQuestions++;
        scoreTextView.setText(Integer.toString(score) + "/" + Integer.toString(numberOfQuestions));
        newQuestion();

    }
}
