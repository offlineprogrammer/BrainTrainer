package com.offlineprogrammer.braintrainer.answer;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineprogrammer.braintrainer.R;

import java.util.ArrayList;

public class AnswerAdapter extends RecyclerView.Adapter {
    private ArrayList<Answer> models = new ArrayList<>();
    private OnAnswerListener mOnAnswerListener;
    private static final String TAG = "AnswerAdapter";
    private Context mContext;

    public AnswerAdapter(Context mContext, @NonNull final ArrayList<Answer> viewModels, OnAnswerListener onAnswerListener) {
        this.models.addAll(viewModels);
        this.mOnAnswerListener =onAnswerListener;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new AnswerViewHolder(view, mOnAnswerListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((AnswerViewHolder) holder).bindData(models.get(position));
        switch (position){
            case 0:
                ((AnswerViewHolder) holder).answerTextView.setBackgroundColor(mContext.getResources().getColor(R.color.answer0Color));
                break;
            case 1:
                ((AnswerViewHolder) holder).answerTextView.setBackgroundColor(mContext.getResources().getColor(R.color.answer1Color));
                break;
            case 2:
                ((AnswerViewHolder) holder).answerTextView.setBackgroundColor(mContext.getResources().getColor(R.color.answer2Color));
                break;
            case 3:
                ((AnswerViewHolder) holder).answerTextView.setBackgroundColor(mContext.getResources().getColor(R.color.answer3Color));
                break;

        }

    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public ArrayList<Answer> getAllItems() {
        return models;
    }

    public void updateData(ArrayList<Answer> viewModels){
        models.clear();
        models.addAll(viewModels);
        notifyDataSetChanged();

    }

    public void delete(int position) {
        models.remove(position);
        notifyItemRemoved(position);
    }

    public void add(Answer item, int position){
        models.add(position, item);
        Log.i(TAG, "add: " + item.toString());
        notifyItemInserted(position);
        //notifyDataSetChanged();
        //notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.answer_itemview;
    }
}

