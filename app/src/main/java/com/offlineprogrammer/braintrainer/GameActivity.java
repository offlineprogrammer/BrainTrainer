package com.offlineprogrammer.braintrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.offlineprogrammer.braintrainer.answer.Answer;
import com.offlineprogrammer.braintrainer.answer.AnswerAdapter;
import com.offlineprogrammer.braintrainer.answer.AnswerGridItemDecoration;
import com.offlineprogrammer.braintrainer.answer.OnAnswerListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GameActivity extends AppCompatActivity implements OnAnswerListener {
    private static final String TAG = "GameActivity";

    RecyclerView mRecyclerView;
    ArrayList<Answer> mAnswerList;
    Answer mAnswer;
    AnswerAdapter myAdapter;
    private AdLayout adView; // The ad view used to load and display the ad.
    private static final String APP_KEY = "3967f616abb34b3c9f83c8d4c86eec34"; // Sample Application Key. Replace this value with your Application Key.
    private static final int INITIALIZATION_TIMEOUT_MS = 2000;

    String removeAdsSKU = "com.offlineprogrammer.braintrainer.removeads";
    String add10sSKU= "com.offlineprogrammer.braintrainer.add10s";
    Handler handler;
    //Define UserId and MarketPlace
    private String currentUserId;
    private String currentMarketplace;
    private TheGame myGame;
    ImageButton goButton;
    TextView timerTextView;
    TextView questionTextView;
    TextView scoreTextView;
    CountDownTimer countDownTimer = null;
    int gameTimer = 30;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mRecyclerView = findViewById(R.id.answers_recyclerview);
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(GameActivity.this, 2);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        int largePadding = getResources().getDimensionPixelSize(R.dimen.bt_answer_grid_spacing);
        int smallPadding = getResources().getDimensionPixelSize(R.dimen.bt_answer_grid_spacing_small);
        mRecyclerView.addItemDecoration(new AnswerGridItemDecoration(largePadding, smallPadding));

        prepareData();
        setupAds();
        setupIAP();

        goButton = findViewById(R.id.goButton);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myGame == null || !myGame.isActive()) {
                    playTheGame();
                }
            }
        });

        timerTextView = findViewById(R.id.timerTextView);
        questionTextView = findViewById(R.id.questionTextView);
        questionTextView.setText("??");
        scoreTextView = findViewById(R.id.scoreTextView);
        scoreTextView.setText("??");

    }

    private void playTheGame() {
        myGame = new TheGame("+", gameTimer);
        myGame.setNumberOfQuestions(0);

        myGame.setScore(0);
        long gameMillSeconds = TimeUnit.SECONDS.toMillis(myGame.getTimer());
        timerTextView.setText(String.format("%ds", myGame.getTimer()));
        scoreTextView.setText(String.format("%s/%s", Integer.toString(myGame.getScore()), Integer.toString(myGame.getNumberOfQuestions())));
        newQuestion();
        myGame.setActive(true);
        goButton.setImageResource(R.drawable.question);


        if(countDownTimer  != null){
            countDownTimer.cancel();
        }
        countDownTimer =  new CountDownTimer(gameMillSeconds+100,1000){

            @Override
            public void onTick(long millisUntilFinished) {

                timerTextView.setText(String.valueOf(millisUntilFinished/1000)+"s");

            }

            @Override
            public void onFinish() {
                timerTextView.setText("0s");

                goButton.setImageResource(R.drawable.playagain);
                myGame.setActive(false);


            }
        }.start();

    }

    private void newQuestion() {

        updateAnswers(myGame.setupGame());
        questionTextView.setText(String.format("%s %s %s", Integer.toString(myGame.a), myGame.getOperation() , Integer.toString(myGame.b)));


    }

    private void updateAnswers(ArrayList<Integer> setupGame) {
        mAnswerList.clear();

        Collections.addAll(mAnswerList,
                new Answer(setupGame.get(0)),
                new Answer(setupGame.get(1)),
                new Answer(setupGame.get(2)),
                new Answer(setupGame.get(3)));
        Log.i(TAG, "prepareData: Size " + mAnswerList.size());
        myAdapter.updateData(mAnswerList);

    }

    private void prepareData() {
        mAnswerList = new ArrayList<>(4);

        Collections.addAll(mAnswerList,
                new Answer(0),
                new Answer(0),
                new Answer(0),
                new Answer(0));
        Log.i(TAG, "prepareData: Size " + mAnswerList.size());
        myAdapter = new AnswerAdapter(GameActivity.this, mAnswerList,this);
        mRecyclerView.setAdapter(myAdapter);

    }

    private void setupIAP() {


        PurchasingService.registerListener(this, purchasingListener);



        //create a handler for the UI changes
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.equals(removeAdsSKU)) {
                    //button.setText("complete");
                    Log.i(TAG, "handleMessage removeAdsSKU: Complete");
                    clearAds();
                    configureIAPOptions(R.id.removeAdsCard, View.GONE, R.id.removeAdsButton, removeAdsSKU);


                }
            }
        };
    }

    private void disableIAP() {
        configureIAPOptions(R.id.removeAdsCard, View.GONE, R.id.removeAdsButton, removeAdsSKU);

        configureIAPOptions(R.id.add10sCard, View.GONE, R.id.removeAdsButton, add10sSKU);


    }

    private void configureIAPOptions(int p, int gone, int p2, String iapSKU) {
        CardView iapCard = findViewById(p);
        iapCard.setVisibility(gone);
        ImageButton button = findViewById(p2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PurchasingService.purchase(iapSKU);
            }
        });
    }

    private void enableIAP() {
        configureIAPOptions(R.id.removeAdsCard, View.VISIBLE, R.id.removeAdsButton, removeAdsSKU);

        configureIAPOptions(R.id.add10sCard, View.VISIBLE, R.id.add10sButton, add10sSKU);


    }

    private void clearAds() {

        this.adView = findViewById(R.id.ad_view);
        try {
            this.adView.destroy();
            Log.i(TAG, "clearAds: done");
        } catch (final IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException thrown: " + e.toString());
            return;
        }
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
            Log.e(TAG, "IllegalArgumentException thrown: " + e.toString());
            return;
        }

    }

    public void loadAd() {
        this.adView.loadAd();
    }

    @Override
    public void onAnswerClick(int position) {
        if (!myGame.isActive()){
            return;
        }
        Log.i("Selected button","is " + position);


        if (position == myGame.locationOfCorrectAnswer){
            goButton.setImageResource(R.drawable.correct);
            int score = myGame.getScore();
            myGame.incrementScore();
            Log.i(TAG, "onAnswerClick: myGame " + myGame.getScore());

        } else {
            goButton.setImageResource(R.drawable.wrong);


        }
        myGame.incrementNumberOfQuestions();
        scoreTextView.setText(Integer.toString(myGame.getScore()) + "/" + Integer.toString(myGame.getNumberOfQuestions()));
        Log.i(TAG, "onAnswerClick: scoreTextView " + scoreTextView.getText());
        newQuestion();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //getProductData will validate the SKUs with Amazon Appstore
        final Set<String> productSkus = new HashSet<String>();
        productSkus.add(removeAdsSKU);
        productSkus.add(add10sSKU);
        PurchasingService.getProductData(productSkus);
        //getUserData() will query the Appstore for the Users information
        PurchasingService.getUserData();
        //getPurchaseUpdates() will query the Appstore for any previous purchase
        Log.i(TAG, "onResume: Calling getPurchaseUpdates");
        PurchasingService.getPurchaseUpdates(true);

    }




    PurchasingListener purchasingListener = new PurchasingListener() {
        @Override
        public void onUserDataResponse(UserDataResponse response) {
            final UserDataResponse.RequestStatus status = response.getRequestStatus();
            switch (status) {
                case SUCCESSFUL:
                    currentUserId = response.getUserData().getUserId();
                    currentMarketplace = response.getUserData().getMarketplace();
                    Log.i(TAG, String.format(" userId : %s\n MarketPlace: %s\n", currentUserId, currentMarketplace));
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
                    final Map<String, Product> products = productDataResponse.getProductData();
                    for ( String key : products.keySet()) {
                        Product product = products.get(key);
                        Log.i(TAG, String.format( "Product: %s\n Type: %s\n SKU: %s\n Price: %s\n Description: %s\n" , product.getTitle(), product.getProductType(),
                                product.getSku(), product.getPrice(), product.getDescription()));
                    }
                    //get all unavailable SKUs
                    for ( String s : productDataResponse.getUnavailableSkus()) {
                        Log.i(TAG, "Unavailable SKU:" + s);
                    }
                    enableIAP();
                    break;
                case FAILED:
                    
                    Log.i(TAG, "onProductDataResponse: Failed");
                    break ;
                case NOT_SUPPORTED:
                    Log.i(TAG, "onProductDataResponse: NOT_SUPPORTED");
                    break ;
            }
        }
        @Override
        public void onPurchaseResponse(PurchaseResponse purchaseResponse) {
            switch (purchaseResponse.getRequestStatus()) {
                case SUCCESSFUL:
                    Receipt receipt = purchaseResponse.getReceipt();
                    Log.i(TAG, "onPurchaseResponse: SKU is "+ receipt.getSku());
                    if (receipt.getSku().equals(add10sSKU)) {
                    gameTimer = gameTimer+10;
                        timerTextView.setText(String.format("%ds", gameTimer));

                }
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
                    Log.i(TAG, "onPurchaseUpdatesResponse: response.getReceipts() is "+ response.getReceipts().size() );
                    for ( final Receipt receipt : response.getReceipts()) {

                        // Process receipts
                        if (!receipt.isCanceled()){
                            Log.i(TAG, "onPurchaseUpdatesResponse: SKU is " + receipt.getSku());
                            Message m= new Message();
                            m.obj=receipt.getSku();//"RemoveAds";
                            handler.handleMessage(m);
                        } else {
                            Log.i(TAG, "onPurchaseUpdatesResponse: cancelled SKU " + receipt.getSku());
                        }


                    }
                    if (response.hasMore()) {
                        Log.i(TAG, "onPurchaseUpdatesResponse: has more");
                        PurchasingService.getPurchaseUpdates(true);
                    }
                    break ;
                case FAILED:
                    Log.i(TAG, "onPurchaseUpdatesResponse: Failed");
                    break ;
            }

        }
    };
}
