package com.offlineprogrammer.braintrainer.answer;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineprogrammer.braintrainer.R;

public class AnswerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = "AnswerViewHolder";
    TextView answerTextView;
    OnAnswerListener onAnswerListener;
    private Context mContext;
    public AnswerViewHolder(@NonNull View itemView, OnAnswerListener onAnswerListener) {
        super(itemView);
        mContext = itemView.getContext();
        answerTextView = itemView.findViewById(R.id.answerTextView);
        this.onAnswerListener = onAnswerListener;
        itemView.setOnClickListener(this);
    }

    public void bindData(final Answer viewModel) {
        Log.i(TAG, "bindData: " + viewModel.getValue().toString());
        answerTextView.setText(viewModel.getValue().toString());
    }

    @Override
    public void onClick(View v) {
        onAnswerListener.onAnswerClick(getAdapterPosition());

    }
}

