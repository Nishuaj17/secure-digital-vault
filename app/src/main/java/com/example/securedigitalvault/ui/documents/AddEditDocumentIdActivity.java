
package com.example.securedigitalvault.ui.documents;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.repository.DocumentIdRepository;
import com.example.securedigitalvault.util.SecurityUtils;

public class AddEditDocumentIdActivity extends AppCompatActivity {

    public static final String EXTRA_DOCUMENT_ID = "document_id";
    private static final int NO_ID = -1;

    private TextView screenTitle;
    private EditText nameInput;
    private EditText typeInput;
    private EditText numberInput;
    private EditText issueDateInput;
    private EditText expiryDateInput;
    private EditText notesInput;
    private TextView errorText;
    private Button saveButton;

    private DocumentIdRepository documentRepository;
    private int editingId = NO_ID;
    private long originalCreatedAt = 0;
    private boolean originalIsFavorite = false;
    private int originalCategoryId = 0;
    private boolean saveInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SecurityUtils.applySecureFlag(this);
        setContentView(R.layout.activity_add_edit_document_id);

        documentRepository =
                new DocumentIdRepository(getApplicationContext());

        screenTitle = findViewById(R.id.screenTitle);
        nameInput = findViewById(R.id.nameInput);
        typeInput = findViewById(R.id.typeInput);
        numberInput = findViewById(R.id.numberInput);
        issueDateInput = findViewById(R.id.issueDateInput);
        expiryDateInput = findViewById(R.id.expiryDateInput);
        notesInput = findViewById(R.id.notesInput);
        errorText = findViewById(R.id.errorText);
        saveButton = findViewById(R.id.saveButton);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> handleSaveClicked());

        editingId = getIntent().getIntExtra(
                EXTRA_DOCUMENT_ID, NO_ID);

        if (editingId != NO_ID) {
            loadExisting(editingId);
        }
    }

    private void loadExisting(int id) {
        screenTitle.setText("Edit ID / Document");
        saveButton.setText("Update");
        saveButton.setEnabled(false);

        documentRepository.getDocumentByIdAsync(id, decrypted -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            if (decrypted == null) {
                Toast.makeText(
                        this,
                        "Could not load this entry.",
                        Toast.LENGTH_LONG
                ).show();
                finish();
                return;
            }

            nameInput.setText(decrypted.documentName);
            typeInput.setText(decrypted.documentType);
            numberInput.setText(decrypted.documentNumber);
            issueDateInput.setText(decrypted.issueDate);
            expiryDateInput.setText(decrypted.expiryDate);
            notesInput.setText(decrypted.notes);

            originalCreatedAt = decrypted.createdAt;
            originalIsFavorite = decrypted.isFavorite;
            originalCategoryId = decrypted.categoryId;

            saveButton.setEnabled(true);
        });
    }

    private void handleSaveClicked() {
        if (saveInProgress) {
            return;
        }

        String name = nameInput.getText().toString().trim();
        String type = typeInput.getText().toString().trim();
        String number = numberInput.getText().toString().trim();
        String issueDate = issueDateInput.getText().toString().trim();
        String expiryDate = expiryDateInput.getText().toString().trim();
        String notes = notesInput.getText().toString().trim();

        if (name.isEmpty()) {
            showError("Please enter a document name.");
            nameInput.requestFocus();
            return;
        }

        if (number.isEmpty()) {
            showError("Please enter the document number.");
            numberInput.requestFocus();
            return;
        }

        errorText.setVisibility(View.GONE);
        saveInProgress = true;
        saveButton.setEnabled(false);

        DocumentIdRepository.SaveCallback callback = success -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            saveInProgress = false;
            saveButton.setEnabled(true);

            if (success) {
                Toast.makeText(
                        this,
                        editingId == NO_ID
                                ? "Document saved successfully."
                                : "Document updated successfully.",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            } else {
                showError(
                        "Could not save this entry. Your form is still open. Please try again."
                );
            }
        };

        if (editingId == NO_ID) {
            documentRepository.insertDocument(
                    name,
                    type,
                    number,
                    issueDate,
                    expiryDate,
                    notes,
                    0,
                    false,
                    callback
            );
        } else {
            DecryptedDocumentId updated = new DecryptedDocumentId(
                    editingId,
                    name,
                    type,
                    number,
                    issueDate,
                    expiryDate,
                    notes,
                    originalCategoryId,
                    originalIsFavorite,
                    originalCreatedAt,
                    System.currentTimeMillis()
            );

            documentRepository.updateDocument(updated, callback);
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
