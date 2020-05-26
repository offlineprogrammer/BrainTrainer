package com.offlineprogrammer.braintrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdRegistration;
import com.offlineprogrammer.braintrainer.answer.Answer;
import com.offlineprogrammer.braintrainer.answer.AnswerAdapter;
import com.offlineprogrammer.braintrainer.answer.AnswerGridItemDecoration;
import com.offlineprogrammer.braintrainer.answer.OnAnswerListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity implements OnAnswerListener {
    private static final String TAG = "GameActivity";

    RecyclerView mRecyclerView;
    ArrayList<Answer> mAnswerList;
    Answer mAnswer;
    AnswerAdapter myAdapter;
    private AdLayout adView; // The ad view used to load and display the ad.
    private static final String APP_KEY = "3967f616abb34b3c9f83c8d4c86eec34"; // Sample Application Key. Replace this value with your Application Key.
    private static final int INITIALIZATION_TIMEOUT_MS = 2000;

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

    }
}
