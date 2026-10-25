package com.example.securedigitalvault.ui.recovery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecoveryCodeListAdapter extends RecyclerView.Adapter<RecoveryCodeListAdapter.RecoveryViewHolder> {

    public interface OnRecoveryActionListener {
        void onRowClick(DecryptedRecoveryCode entry);
        void onCopyClick(DecryptedRecoveryCode entry);
        void onDeleteClick(DecryptedRecoveryCode entry);
        void onFavoriteToggle(DecryptedRecoveryCode entry);
    }

    private List<DecryptedRecoveryCode> entryList = new ArrayList<>();
    private final Set<Integer> visibleIds = new HashSet<>();
    private final OnRecoveryActionListener listener;

    public RecoveryCodeListAdapter(OnRecoveryActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<DecryptedRecoveryCode> newList) {
        this.entryList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecoveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recovery_code_row, parent, false);
        return new RecoveryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecoveryViewHolder holder, int position) {
        DecryptedRecoveryCode item = entryList.get(position);
        boolean isVisible = visibleIds.contains(item.id);

        holder.serviceNameText.setText(item.serviceName);
        holder.codesPreviewText.setText(isVisible ? item.codes : "•••••• •••••• ••••••");

        holder.toggleVisibilityButton.setOnClickListener(v -> {
            if (isVisible) {
                visibleIds.remove(item.id);
            } else {
                visibleIds.add(item.id);
            }
            notifyItemChanged(holder.getAdapterPosition());
        });

        holder.favoriteButton.setImageResource(
                item.isFavorite ? android.R.drawable.star_on : android.R.drawable.star_off);

        holder.itemView.setOnClickListener(v -> listener.onRowClick(item));
        holder.copyButton.setOnClickListener(v -> listener.onCopyClick(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(item));
        holder.favoriteButton.setOnClickListener(v -> listener.onFavoriteToggle(item));
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    static class RecoveryViewHolder extends RecyclerView.ViewHolder {
        TextView serviceNameText, codesPreviewText;
        ImageButton toggleVisibilityButton, copyButton, deleteButton, favoriteButton;

        RecoveryViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceNameText = itemView.findViewById(R.id.rowServiceName);
            codesPreviewText = itemView.findViewById(R.id.rowCodesPreview);
            toggleVisibilityButton = itemView.findViewById(R.id.toggleVisibilityButton);
            copyButton = itemView.findViewById(R.id.copyButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
}

