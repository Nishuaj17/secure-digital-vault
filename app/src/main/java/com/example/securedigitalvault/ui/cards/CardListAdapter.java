package com.example.securedigitalvault.ui.cards;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;

import java.util.ArrayList;
import java.util.List;

/**
 * CardListAdapter
 *
 * Displays cards with a masked number by default. Tapping "Show
 * Details" triggers the host Activity's biometric/PIN re-check (via
 * the listener), and if approved, temporarily reveals the full number
 * for a short window before automatically re-masking.
 */
public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardViewHolder> {

    private static final long AUTO_REMASK_DELAY_MILLIS = 8_000; // 8 seconds

    public interface OnCardActionListener {
        void onShowDetailsRequested(DecryptedCard card, Runnable onApproved);
        void onDeleteClick(DecryptedCard card);
        void onFavoriteToggle(DecryptedCard card);
    }

    private List<DecryptedCard> cardList = new ArrayList<>();
    private final OnCardActionListener listener;

    public CardListAdapter(OnCardActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<DecryptedCard> newList) {
        this.cardList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_row, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        DecryptedCard item = cardList.get(position);

        holder.nicknameText.setText(item.nickname);
        holder.cardTypeText.setText(item.cardType.isEmpty() ? "Card" : item.cardType);
        holder.numberText.setText(item.getMaskedNumber());

        holder.showDetailsButton.setOnClickListener(v ->
                listener.onShowDetailsRequested(item, () -> {
                    holder.numberText.setText(formatFullNumber(item.cardNumber));
                    new Handler(Looper.getMainLooper()).postDelayed(() ->
                            holder.numberText.setText(item.getMaskedNumber()), AUTO_REMASK_DELAY_MILLIS);
                }));

        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(item));

        holder.favoriteButton.setImageResource(
                item.isFavorite ? android.R.drawable.star_on : android.R.drawable.star_off);
        holder.favoriteButton.setOnClickListener(v -> listener.onFavoriteToggle(item));
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    private String formatFullNumber(String rawNumber) {
        String digitsOnly = rawNumber.replaceAll("[^0-9]", "");
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < digitsOnly.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(digitsOnly.charAt(i));
        }
        return formatted.toString();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView nicknameText, cardTypeText, numberText;
        Button showDetailsButton;
        ImageButton deleteButton;
        ImageButton favoriteButton;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            nicknameText = itemView.findViewById(R.id.rowNickname);
            cardTypeText = itemView.findViewById(R.id.rowCardType);
            numberText = itemView.findViewById(R.id.rowNumber);
            showDetailsButton = itemView.findViewById(R.id.showDetailsButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
}

