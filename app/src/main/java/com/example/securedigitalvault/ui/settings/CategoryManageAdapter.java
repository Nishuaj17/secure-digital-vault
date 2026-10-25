package com.example.securedigitalvault.ui.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.local.entity.CategoryEntity;

import java.util.ArrayList;
import java.util.List;

public class CategoryManageAdapter extends RecyclerView.Adapter<CategoryManageAdapter.CategoryViewHolder> {

    public interface OnCategoryDeleteListener {
        void onDeleteClick(CategoryEntity category);
    }

    private List<CategoryEntity> categories = new ArrayList<>();
    private final OnCategoryDeleteListener listener;

    public CategoryManageAdapter(OnCategoryDeleteListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CategoryEntity> newList) {
        this.categories = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_manage_row, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryEntity item = categories.get(position);
        holder.nameText.setText(item.name);
        try {
            holder.colorSwatch.setBackgroundColor(android.graphics.Color.parseColor(item.colorTag));
        } catch (Exception e) {
            // Fall back silently if the stored color string is somehow invalid.
        }
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        View colorSwatch;
        TextView nameText;
        ImageButton deleteButton;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            colorSwatch = itemView.findViewById(R.id.colorSwatch);
            nameText = itemView.findViewById(R.id.categoryNameText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}


