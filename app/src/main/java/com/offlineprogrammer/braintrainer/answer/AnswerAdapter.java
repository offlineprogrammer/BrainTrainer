package com.offlineprogrammer.braintrainer.answer;

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
    private static final String TAG = "KidAdapter";

    public AnswerAdapter(@NonNull final ArrayList<Answer> viewModels, OnAnswerListener onAnswerListener) {
        this.models.addAll(viewModels);
        this.mOnAnswerListener =onAnswerListener;
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

