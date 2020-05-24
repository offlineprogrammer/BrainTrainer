package com.offlineprogrammer.braintrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserDataResponse;
import com.amazon.device.messaging.ADM;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.analytics.AnalyticsException;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.analytics.pinpoint.AmazonPinpointAnalyticsPlugin;
import com.amplifyframework.core.AmplifyConfiguration;

import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobile.client.AWSMobileClient;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.analytics.AnalyticsException;
import com.amplifyframework.analytics.BasicAnalyticsEvent;
import com.amplifyframework.analytics.pinpoint.PinpointProperties;




public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    //Define UserId and MarketPlace
    private String currentUserId;
    private String currentMarketplace;

    int score = 0;
    int numberOfQuestions = 0;
    TextView scoreTextView;
    Button button0 ;
    Button button1 ;
    Button button2 ;
    Button button3 ;
    private TextView sumTextView;
    TextView timerTextView;

    ConstraintLayout gameLayout;
    ConstraintLayout configLayout;
    ConstraintLayout homeLayout;
    LinearLayout mhomeOpsLayout;
    CountDownTimer countDownTimer = null;
    ImageButton login_with_amazon;
    Button mLogoutButton;
    TextView textView;
    ImageView correctImageView;
    ImageView wrongImageView;
    ImageView playAgainImageView;

    private TheGame myGame;

    private AdLayout adView; // The ad view used to load and display the ad.
    private static final String APP_KEY = "3967f616abb34b3c9f83c8d4c86eec34"; // Sample Application Key. Replace this value with your Application Key.
    private static final String LOG_TAG = "SimpleAdSample"; // Tag used to prefix all log messages.
    private static final int INITIALIZATION_TIMEOUT_MS = 2000;


    private ProgressBar mLogInProgress;
    String parentSKU;
    Handler handler;


    private void recordEvent(String sEventName)  {
        try {
            Amplify.Analytics.recordEvent(sEventName);
            // Plugin will automatically flush events.
            // You do not have to do this in the app code.
            Amplify.Analytics.flushEvents();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }






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

        setupAmplify();
        setupUi();
        setupGame();
        setupAds();
      //  register();
        //setupIAP();

        startTheGame();

    }

    private void setupIAP() {
        parentSKU = "com.offlineprogrammer.braintrainer.removeads";

        PurchasingService.registerListener(this, purchasingListener);

        Button button = (Button) findViewById(R.id.iapButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PurchasingService.purchase(parentSKU);
            }
        });

        //create a handler for the UI changes
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.equals("Subscribed")) {
                    button.setText("complete");

                }
            }
        };
    }

    private void setupAmplify() {
        final AWSConfiguration awsConfiguration = new AWSConfiguration(getApplicationContext());
        final CountDownLatch mobileClientLatch = new CountDownLatch(1);
        AWSMobileClient.getInstance().initialize(getApplicationContext(), awsConfiguration,
                new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i(TAG, "Mobile client initialized");
                        mobileClientLatch.countDown();
                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e(TAG, "Error initializing AWS Mobile Client", exception);
                    }
                });

        try {
            if (!mobileClientLatch.await(INITIALIZATION_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                throw new AnalyticsException("Failed to initialize mobile client.",
                        "Please check your awsconfiguration json.");
            }
        } catch (InterruptedException | AnalyticsException exception) {
            throw new RuntimeException("Failed to initialize mobile client: " + exception.getLocalizedMessage());
        }

        // Configure Amplify framework
        AmplifyConfiguration configuration = new AmplifyConfiguration();
        try {
            configuration.populateFromConfigFile(getApplicationContext(), R.raw.amplifyconfiguration);
            Amplify.addPlugin(new AmazonPinpointAnalyticsPlugin());
            Amplify.configure(configuration, getApplicationContext());
        } catch (AmplifyException e) {
            e.printStackTrace();
        }
        Amplify.Analytics.recordEvent("App Started");
    }

    private void setupUi() {
        sumTextView = findViewById(R.id.sumTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        timerTextView = findViewById(R.id.timerTextView);

        gameLayout=findViewById(R.id.gameLayout);
        configLayout = findViewById(R.id.configLayout);
        login_with_amazon = findViewById(R.id.login_with_amazon);
        textView = findViewById(R.id.textView);
        mLogInProgress = findViewById(R.id.log_in_progress);
        mLogoutButton = findViewById(R.id.logout);
        homeLayout = findViewById(R.id.homeLayout);
        mhomeOpsLayout = findViewById(R.id.homeOpsLayout);
        correctImageView = findViewById(R.id.correctIimageView);
        wrongImageView = findViewById(R.id.wrongIimageView);
        playAgainImageView=findViewById(R.id.playAgainImageView);


        homeLayout.setVisibility(View.VISIBLE);
        setHomeOpsVisibility(View.VISIBLE);
        configLayout.setVisibility(View.INVISIBLE);
        gameLayout.setVisibility(View.INVISIBLE);
    }

    private void setupGame(){
        myGame = new TheGame("+");
        recordEvent("New Game " + myGame.getOperation());
    }

    @Override
    protected void onResume() {
        super.onResume();
/*
//getUserData() will query the Appstore for the Users information
        PurchasingService.getUserData();
//getPurchaseUpdates() will query the Appstore for any previous purchase
        PurchasingService.getPurchaseUpdates(true);
//getProductData will validate the SKUs with Amazon Appstore
        final Set<String> productSkus = new HashSet<String>();
        productSkus.add(parentSKU);
        PurchasingService.getProductData(productSkus);
        Log.v("Validating SKUs", "Validating SKUs with Amazon" );

 */
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



    private void register(){

        final ADM adm = new ADM(this);
        if (adm.isSupported())
        {
            if(adm.getRegistrationId()==null){
                adm.startRegister();
            }else {

                String sRegToken = adm.getRegistrationId();

                recordEvent(sRegToken);

                Log.i(TAG, "Registration Token is " + sRegToken);

                //adm.startUnregister();

            }
        }


    }

    public void loadAd() {
        this.adView.loadAd();
    }

    public void startTheGame(){
        Log.i("goButton Clicked","Hide");
        //goButton.setVisibility(View.INVISIBLE);
        homeLayout.setVisibility(View.INVISIBLE);
        gameLayout.setVisibility(View.VISIBLE);
        myGame.setActive(true);
        newQuestion();
        playAgain(playAgainImageView);


    }

    private void setHomeOpsVisibility(int visibility){
        mhomeOpsLayout.setVisibility(visibility);
    }

    public void chooseOperation(View view){

        homeLayout.setVisibility(View.VISIBLE);
        gameLayout.setVisibility(View.INVISIBLE);
        configLayout.setVisibility(View.INVISIBLE);
        setHomeOpsVisibility(View.VISIBLE);
        wrongImageView.setVisibility(View.GONE);
        correctImageView.setVisibility(View.GONE);
        playAgainImageView.setVisibility(View.GONE);

        myGame.setOperation(view.getTag().toString());

        if ( myGame.getOperation().equals("?")) {
            String[] list = {"+", "-", "*"};
            Random rand = new Random();

           myGame.setOperation(list[rand.nextInt(list.length)]);


        }
        Log.i("chooseOperation: ", " is" +myGame.getOperation());
        startTheGame();

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

        newQuestion();
        loadAd();
        playAgainImageView.setVisibility(View.GONE);
        //playAgain.setVisibility(View.INVISIBLE);
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

               // playAgain.setVisibility(View.VISIBLE);
                playAgainImageView.setVisibility(View.VISIBLE);
                wrongImageView.setVisibility(View.GONE);
                correctImageView.setVisibility(View.GONE);
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
            wrongImageView.setVisibility(View.GONE);
            correctImageView.setVisibility(View.VISIBLE);
            //resultTextView.setText("Correct!");
            score++;
            Log.i("Winner", "button" + selectedButton);
        } else {
            wrongImageView.setVisibility(View.VISIBLE);
            correctImageView.setVisibility(View.GONE);
            //resultTextView.setText("Wrong :(");
            Log.i("Wrong :( ", "button" + selectedButton);

        }
        numberOfQuestions++;
        scoreTextView.setText(Integer.toString(score) + "/" + Integer.toString(numberOfQuestions));
        newQuestion();

    }

    PurchasingListener purchasingListener = new PurchasingListener() {
        @Override
        public void onUserDataResponse(UserDataResponse response) {
            final UserDataResponse.RequestStatus status = response.getRequestStatus();
            switch (status) {
                case SUCCESSFUL:
                    currentUserId = response.getUserData().getUserId();
                    currentMarketplace = response.getUserData().getMarketplace();
                    break;
                case FAILED:
                case NOT_SUPPORTED:
                    // Fail gracefully.
                    break;
            }
        }
        @Override
        public void onProductDataResponse(ProductDataResponse productDataResponse) {
            switch (productDataResponse.getRequestStatus()) {
                case SUCCESSFUL:

                    //get informations for all IAP Items (parent SKUs)
                    final Map<String,Product> products = productDataResponse.getProductData();
                    for ( String key : products.keySet()) {
                        Product product = products.get(key);
                        Log.v("Product:", String.format( "Product: %s\n Type: %s\n SKU: %s\n Price: %s\n Description: %s\n" , product.getTitle(), product.getProductType(),
                                product.getSku(), product.getPrice(), product.getDescription()));
                    }
                    //get all unavailable SKUs
                    for ( String s : productDataResponse.getUnavailableSkus()) {
                        Log.v("Unavailable SKU:"+s, "Unavailable SKU:" + s);
                    }
                    break;
                case FAILED:
                    Log.v("FAILED", "FAILED" );
                    break ;
            }
        }
        @Override
        public void onPurchaseResponse(PurchaseResponse purchaseResponse) {
            switch (purchaseResponse.getRequestStatus()) {
                case SUCCESSFUL:
                    PurchasingService.notifyFulfillment(purchaseResponse.getReceipt().getReceiptId(),
                            FulfillmentResult.FULFILLED);
                    break ;
                case FAILED:
                    break ;
            }
        }
        @Override
        public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse response) {
            // Process receipts
            switch (response.getRequestStatus()) {
                case SUCCESSFUL:
                    for ( final Receipt receipt : response.getReceipts()) {
                        // Process receipts
                        if (!receipt.isCanceled()){
                            Message m= new Message();
                            m.obj="Subscribed";
                            handler.handleMessage(m);
                        }
                    }
                    if (response.hasMore()) {
                        PurchasingService.getPurchaseUpdates(true);
                    }
                    break ;
                case FAILED:
                    Log.d("FAILED","FAILED");
                    break ;
            }

        }
    };
}
