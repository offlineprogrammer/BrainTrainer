package com.offlineprogrammer.braintrainer.answer;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineprogrammer.braintrainer.R;

public class AnswerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TextView answerTextView;
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
        answerTextView.setText(viewModel.getValue());
    }

    @Override
    public void onClick(View v) {
        onAnswerListener.onAnswerClick(getAdapterPosition());

    }
}

