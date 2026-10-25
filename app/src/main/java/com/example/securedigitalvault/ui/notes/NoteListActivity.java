
package com.example.securedigitalvault.ui.notes;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.repository.SecureNoteRepository;

import java.util.Collections;

public class NoteListActivity extends AppCompatActivity
        implements NoteListAdapter.OnNoteActionListener {

    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private SecureNoteRepository noteRepository;
    private NoteListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        noteRepository = new SecureNoteRepository(getApplicationContext());

        recyclerView = findViewById(R.id.noteRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        adapter = new NoteListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        View backButton = findViewById(R.id.backButton);
        View addButton = findViewById(R.id.addButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (addButton == null) {
            Toast.makeText(this, "Plus button view not found", Toast.LENGTH_LONG).show();
        } else {
            addButton.setOnClickListener(v -> {
                Toast.makeText(
                        NoteListActivity.this,
                        "Plus button clicked",
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(
                        NoteListActivity.this,
                        AddEditNoteActivity.class
                );
                startActivity(intent);
            });
        }

        noteRepository.getAllNotes().observe(this, notes -> {
            if (notes == null) {
                adapter.submitList(Collections.emptyList());
                emptyStateText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                return;
            }

            adapter.submitList(notes);

            boolean isEmpty = notes.isEmpty();
            emptyStateText.setVisibility(
                    isEmpty ? View.VISIBLE : View.GONE
            );
            recyclerView.setVisibility(
                    isEmpty ? View.GONE : View.VISIBLE
            );
        });
    }

    @Override
    public void onRowClick(DecryptedNote note) {
        if (note == null) return;

        Intent intent = new Intent(
                NoteListActivity.this,
                AddEditNoteActivity.class
        );
        intent.putExtra(AddEditNoteActivity.EXTRA_NOTE_ID, note.id);
        startActivity(intent);
    }

    @Override
    public void onFavoriteToggle(DecryptedNote note) {
        if (note == null) return;
        noteRepository.toggleFavorite(note.id, !note.isFavorite);
    }

    @Override
    public void onDeleteClick(DecryptedNote note) {
        if (note == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete note?")
                .setMessage(
                        "This will permanently delete \""
                                + note.title
                                + "\". This cannot be undone."
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                noteRepository.deleteNote(note.id)
                )
                .setNegativeButton("Cancel", null)
                .show();
    }
}
