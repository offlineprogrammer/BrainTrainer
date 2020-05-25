package com.offlineprogrammer.braintrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

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

    }

    private void prepareData() {
        mAnswerList = new ArrayList<>(4);

        Collections.addAll(mAnswerList,
                new Answer(10),
                new Answer(10),
                new Answer(10),
                new Answer(10));
        Log.i(TAG, "prepareData: Size " + mAnswerList.size());
        myAdapter = new AnswerAdapter(mAnswerList,this);
        mRecyclerView.setAdapter(myAdapter);

    }

    @Override
    public void onAnswerClick(int position) {

    }
}
