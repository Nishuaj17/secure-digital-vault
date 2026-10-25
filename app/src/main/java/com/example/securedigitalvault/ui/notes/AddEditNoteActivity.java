package com.example.securedigitalvault.ui.notes;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.repository.SecureNoteRepository;
import com.example.securedigitalvault.util.SecurityUtils;

public class AddEditNoteActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE_ID = "note_id";
    private static final int NO_ID = -1;

    private TextView screenTitle;
    private EditText titleInput;
    private EditText contentInput;
    private TextView errorText;
    private Button saveButton;

    private SecureNoteRepository noteRepository;

    private int editingNoteId = NO_ID;
    private long originalCreatedAt = 0;
    private boolean originalIsFavorite = false;
    private int originalCategoryId = 0;
    private boolean isSaving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SecurityUtils.applySecureFlag(this);
        setContentView(R.layout.activity_add_edit_note);

        noteRepository = new SecureNoteRepository(getApplicationContext());

        screenTitle = findViewById(R.id.screenTitle);
        titleInput = findViewById(R.id.titleInput);
        contentInput = findViewById(R.id.contentInput);
        errorText = findViewById(R.id.errorText);
        saveButton = findViewById(R.id.saveButton);

        View backButton = findViewById(R.id.backButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (saveButton != null) {
            saveButton.setOnClickListener(v -> handleSaveClicked());
        }

        editingNoteId = getIntent().getIntExtra(EXTRA_NOTE_ID, NO_ID);

        if (editingNoteId != NO_ID) {
            loadExistingNote(editingNoteId);
        } else {
            screenTitle.setText("Add Note");
            saveButton.setText("Save Note");
        }
    }

    private void loadExistingNote(int id) {
        screenTitle.setText("Edit Note");
        saveButton.setText("Update Note");
        saveButton.setEnabled(false);

        noteRepository.getNoteByIdAsync(id, decrypted -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            if (decrypted == null) {
                Toast.makeText(
                        AddEditNoteActivity.this,
                        "Could not load this note.",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
                return;
            }

            titleInput.setText(decrypted.title);
            contentInput.setText(decrypted.content);

            originalCreatedAt = decrypted.createdAt;
            originalIsFavorite = decrypted.isFavorite;
            originalCategoryId = decrypted.categoryId;

            saveButton.setEnabled(true);
        });
    }

    private void handleSaveClicked() {
        if (isSaving) {
            return;
        }

        String title = titleInput.getText().toString().trim();
        String content = contentInput.getText().toString();

        if (title.isEmpty()) {
            showError("Please enter a title.");
            titleInput.requestFocus();
            return;
        }

        isSaving = true;
        saveButton.setEnabled(false);
        errorText.setVisibility(View.GONE);

        if (editingNoteId == NO_ID) {
            noteRepository.insertNote(
                    title,
                    content,
                    0,
                    false,
                    (success, errorMessage) -> {
                        isSaving = false;

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        saveButton.setEnabled(true);

                        if (success) {
                            Toast.makeText(
                                    AddEditNoteActivity.this,
                                    "Note saved successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        } else {
                            showError(
                                    errorMessage != null
                                            ? errorMessage
                                            : "Could not save the note."
                            );
                        }
                    }
            );
        } else {
            DecryptedNote updated = new DecryptedNote(
                    editingNoteId,
                    title,
                    content,
                    originalCategoryId,
                    originalIsFavorite,
                    originalCreatedAt,
                    System.currentTimeMillis()
            );

            noteRepository.updateNote(
                    updated,
                    (success, errorMessage) -> {
                        isSaving = false;

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        saveButton.setEnabled(true);

                        if (success) {
                            Toast.makeText(
                                    AddEditNoteActivity.this,
                                    "Note updated successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        } else {
                            showError(
                                    errorMessage != null
                                            ? errorMessage
                                            : "Could not update the note."
                            );
                        }
                    }
            );
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}