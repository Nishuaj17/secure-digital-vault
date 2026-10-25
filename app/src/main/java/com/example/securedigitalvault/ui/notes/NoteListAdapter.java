package com.example.securedigitalvault.ui.notes;

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

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.NoteViewHolder> {

    public interface OnNoteActionListener {
        void onRowClick(DecryptedNote note);
        void onDeleteClick(DecryptedNote note);
        void onFavoriteToggle(DecryptedNote note);
    }

    private List<DecryptedNote> noteList = new ArrayList<>();
    private final OnNoteActionListener listener;

    public NoteListAdapter(OnNoteActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<DecryptedNote> newList) {
        this.noteList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note_row, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        DecryptedNote item = noteList.get(position);
        holder.titleText.setText(item.title);
        holder.previewText.setText(item.content.isEmpty() ? "(empty note)" : item.content);
        holder.favoriteButton.setImageResource(
                item.isFavorite ? android.R.drawable.star_on : android.R.drawable.star_off);

        holder.itemView.setOnClickListener(v -> listener.onRowClick(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(item));
        holder.favoriteButton.setOnClickListener(v -> listener.onFavoriteToggle(item));
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView previewText;
        ImageButton deleteButton;
        ImageButton favoriteButton;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.rowTitle);
            previewText = itemView.findViewById(R.id.rowPreview);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
}

