package com.offlineprogrammer.braintrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
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

public class GameActivity extends AppCompatActivity implements OnAnswerListener {
    private static final String TAG = "GameActivity";

    RecyclerView mRecyclerView;
    ArrayList<Answer> mAnswerList;
    Answer mAnswer;
    AnswerAdapter myAdapter;
    private AdLayout adView; // The ad view used to load and display the ad.
    private static final String APP_KEY = "3967f616abb34b3c9f83c8d4c86eec34"; // Sample Application Key. Replace this value with your Application Key.
    private static final int INITIALIZATION_TIMEOUT_MS = 2000;
    String parentSKU;
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
        scoreTextView = findViewById(R.id.scoreTextView);

    }

    private void playTheGame() {
        myGame = new TheGame("+");
        myGame.setNumberOfQuestions(0);
        myGame.setScore(0);
        timerTextView.setText("30s");
        scoreTextView.setText(Integer.toString(myGame.getScore()) + "/" + Integer.toString(myGame.getNumberOfQuestions()));
        newQuestion();
        myGame.setActive(true);
        goButton.setImageResource(R.drawable.question);


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
                new Answer(10),
                new Answer(10),
                new Answer(10),
                new Answer(10));
        Log.i(TAG, "prepareData: Size " + mAnswerList.size());
        myAdapter = new AnswerAdapter(GameActivity.this, mAnswerList,this);
        mRecyclerView.setAdapter(myAdapter);

    }

    private void setupIAP() {
        parentSKU = "com.offlineprogrammer.braintrainer.removeads";

        PurchasingService.registerListener(this, purchasingListener);

        ImageButton button =  findViewById(R.id.removeAdsButton);
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
                if (msg.obj.equals("RemoveAds")) {
                    //button.setText("complete");
                    Log.i(TAG, "handleMessage: Complete");
                    clearAds();


                }
            }
        };
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

//getUserData() will query the Appstore for the Users information
        PurchasingService.getUserData();
//getPurchaseUpdates() will query the Appstore for any previous purchase
        PurchasingService.getPurchaseUpdates(true);
//getProductData will validate the SKUs with Amazon Appstore
        final Set<String> productSkus = new HashSet<String>();
        productSkus.add(parentSKU);
        PurchasingService.getProductData(productSkus);

        Log.v("Validating SKUs", "Validating SKUs with Amazon" );


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
                    final Map<String, Product> products = productDataResponse.getProductData();
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
                            m.obj="RemoveAds";
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
