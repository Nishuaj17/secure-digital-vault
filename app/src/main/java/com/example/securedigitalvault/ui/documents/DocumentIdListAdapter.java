package com.example.securedigitalvault.ui.documents;

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

public class DocumentIdListAdapter extends RecyclerView.Adapter<DocumentIdListAdapter.DocumentViewHolder> {

    public interface OnDocumentActionListener {
        void onRowClick(DecryptedDocumentId document);
        void onDeleteClick(DecryptedDocumentId document);
        void onFavoriteToggle(DecryptedDocumentId document);
    }

    private List<DecryptedDocumentId> documentList = new ArrayList<>();
    private final OnDocumentActionListener listener;

    public DocumentIdListAdapter(OnDocumentActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<DecryptedDocumentId> newList) {
        this.documentList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document_id_row, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        DecryptedDocumentId item = documentList.get(position);
        holder.nameText.setText(item.documentName);
        holder.typeText.setText(item.documentType.isEmpty() ? "(no type set)" : item.documentType);
        holder.favoriteButton.setImageResource(
                item.isFavorite ? android.R.drawable.star_on : android.R.drawable.star_off);

        holder.itemView.setOnClickListener(v -> listener.onRowClick(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(item));
        holder.favoriteButton.setOnClickListener(v -> listener.onFavoriteToggle(item));
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView typeText;
        ImageButton deleteButton;
        ImageButton favoriteButton;

        DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.rowName);
            typeText = itemView.findViewById(R.id.rowType);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
}

