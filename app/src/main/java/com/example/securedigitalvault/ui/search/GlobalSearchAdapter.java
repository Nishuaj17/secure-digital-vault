package com.example.securedigitalvault.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;

import java.util.ArrayList;
import java.util.List;

public class GlobalSearchAdapter extends RecyclerView.Adapter<GlobalSearchAdapter.ResultViewHolder> {

    public interface OnResultClickListener {
        void onResultClick(GlobalSearchResult result);
    }

    private List<GlobalSearchResult> results = new ArrayList<>();
    private final OnResultClickListener listener;

    public GlobalSearchAdapter(OnResultClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<GlobalSearchResult> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result_row, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        GlobalSearchResult item = results.get(position);
        holder.titleText.setText(item.title);
        holder.typeText.setText(item.getTypeLabel());
        holder.iconImage.setImageResource(item.getIconResId());
        holder.itemView.setOnClickListener(v -> listener.onResultClick(item));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImage;
        TextView titleText;
        TextView typeText;

        ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.resultIcon);
            titleText = itemView.findViewById(R.id.resultTitle);
            typeText = itemView.findViewById(R.id.resultType);
        }
    }
}


