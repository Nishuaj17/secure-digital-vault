package com.example.securedigitalvault.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;

import java.util.List;

/**
 * DashboardAdapter
 *
 * Binds a list of CategoryItem objects to the RecyclerView grid on the
 * dashboard. Notifies the host Activity via OnCategoryClickListener
 * when a card is tapped, so navigation logic stays in the Activity
 * rather than the adapter.
 */
public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryItem item);
    }

    private final List<CategoryItem> categoryItems;
    private final OnCategoryClickListener clickListener;

    public DashboardAdapter(List<CategoryItem> categoryItems, OnCategoryClickListener clickListener) {
        this.categoryItems = categoryItems;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_card, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryItem item = categoryItems.get(position);

        holder.titleText.setText(item.getTitle());
        holder.countText.setText(item.getItemCount() + " items");
        holder.iconImage.setImageResource(item.getIconResId());
        holder.iconBackground.setBackgroundResource(item.getIconBackgroundColorResId());

        holder.itemView.setOnClickListener(v -> clickListener.onCategoryClick(item));
    }

    @Override
    public int getItemCount() {
        return categoryItems.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconBackground;
        ImageView iconImage;
        TextView titleText;
        TextView countText;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            iconImage = itemView.findViewById(R.id.categoryIcon);
            titleText = itemView.findViewById(R.id.categoryTitle);
            countText = itemView.findViewById(R.id.categoryCount);
        }
    }
}


