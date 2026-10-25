
package com.example.securedigitalvault.ui.passwords;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;

import java.util.ArrayList;
import java.util.List;

public class PasswordListAdapter
        extends RecyclerView.Adapter<PasswordListAdapter.PasswordViewHolder> {

    public interface OnPasswordActionListener {
        void onRowClick(DecryptedPassword password);

        void onCopyClick(DecryptedPassword password);

        void onDeleteClick(DecryptedPassword password);

        void onFavoriteToggle(DecryptedPassword password);
    }

    private final OnPasswordActionListener listener;
    private List<DecryptedPassword> passwordList = new ArrayList<>();

    public PasswordListAdapter(OnPasswordActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<DecryptedPassword> newList) {
        passwordList = newList == null
                ? new ArrayList<>()
                : new ArrayList<>(newList);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_password_row, parent, false);

        return new PasswordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PasswordViewHolder holder,
            int position
    ) {
        DecryptedPassword item = passwordList.get(position);

        String siteName = item.siteName == null
                || item.siteName.trim().isEmpty()
                ? "Unnamed account"
                : item.siteName;

        String username = item.username == null
                || item.username.trim().isEmpty()
                ? "(no username)"
                : item.username;

        holder.siteNameText.setText(siteName);
        holder.usernameText.setText(username);

        holder.favoriteButton.setImageResource(
                item.isFavorite
                        ? android.R.drawable.btn_star_big_on
                        : android.R.drawable.btn_star_big_off
        );

        holder.favoriteButton.setContentDescription(
                item.isFavorite
                        ? "Remove from favorites"
                        : "Add to favorites"
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRowClick(item);
            }
        });

        holder.copyButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCopyClick(item);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(item);
            }
        });

        holder.favoriteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteToggle(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return passwordList.size();
    }

    static class PasswordViewHolder extends RecyclerView.ViewHolder {

        final TextView siteNameText;
        final TextView usernameText;
        final ImageButton copyButton;
        final ImageButton deleteButton;
        final ImageButton favoriteButton;

        PasswordViewHolder(@NonNull View itemView) {
            super(itemView);

            siteNameText = itemView.findViewById(R.id.rowSiteName);
            usernameText = itemView.findViewById(R.id.rowUsername);
            copyButton = itemView.findViewById(R.id.copyButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
}
