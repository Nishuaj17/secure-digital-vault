
package com.example.securedigitalvault.ui.recovery;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.repository.RecoveryCodeRepository;
import com.example.securedigitalvault.util.SecurityUtils;

public class AddEditRecoveryCodeActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_ID = "recovery_entry_id";
    private static final int NO_ID = -1;

    private TextView screenTitle;
    private EditText serviceNameInput;
    private EditText codesInput;
    private EditText notesInput;
    private TextView errorText;
    private Button saveButton;

    private RecoveryCodeRepository recoveryCodeRepository;

    private int editingId = NO_ID;
    private long originalCreatedAt = 0;
    private boolean originalIsFavorite = false;
    private int originalCategoryId = 0;
    private boolean saveInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SecurityUtils.applySecureFlag(this);
        setContentView(R.layout.activity_add_edit_recovery_code);

        recoveryCodeRepository =
                new RecoveryCodeRepository(getApplicationContext());

        screenTitle = findViewById(R.id.screenTitle);
        serviceNameInput = findViewById(R.id.serviceNameInput);
        codesInput = findViewById(R.id.codesInput);
        notesInput = findViewById(R.id.notesInput);
        errorText = findViewById(R.id.errorText);
        saveButton = findViewById(R.id.saveButton);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> handleSaveClicked());

        editingId = getIntent().getIntExtra(EXTRA_ENTRY_ID, NO_ID);

        if (editingId != NO_ID) {
            loadExisting(editingId);
        }
    }

    private void loadExisting(int id) {
        screenTitle.setText("Edit Recovery Codes");
        saveButton.setText("Loading...");
        saveButton.setEnabled(false);

        recoveryCodeRepository.getEntryByIdAsync(id, decrypted -> {
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

            serviceNameInput.setText(decrypted.serviceName);
            codesInput.setText(decrypted.codes);
            notesInput.setText(decrypted.notes);

            originalCreatedAt = decrypted.createdAt;
            originalIsFavorite = decrypted.isFavorite;
            originalCategoryId = decrypted.categoryId;

            saveButton.setText("Update");
            saveButton.setEnabled(true);
        });
    }

    private void handleSaveClicked() {
        if (saveInProgress) {
            return;
        }

        String serviceName =
                serviceNameInput.getText().toString().trim();
        String codes = codesInput.getText().toString().trim();
        String notes = notesInput.getText().toString().trim();

        if (serviceName.isEmpty()) {
            showError("Please enter a service or app name.");
            serviceNameInput.requestFocus();
            return;
        }

        if (codes.isEmpty()) {
            showError("Please enter at least one recovery code.");
            codesInput.requestFocus();
            return;
        }

        errorText.setVisibility(View.GONE);
        saveInProgress = true;
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        RecoveryCodeRepository.SaveCallback callback = success -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            saveInProgress = false;
            saveButton.setEnabled(true);
            saveButton.setText(
                    editingId == NO_ID ? "Save" : "Update"
            );

            if (success) {
                Toast.makeText(
                        this,
                        editingId == NO_ID
                                ? "Recovery codes saved successfully."
                                : "Recovery codes updated successfully.",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            } else {
                showError(
                        "Could not save recovery codes. Your entries are still here. Please try again."
                );
            }
        };

        if (editingId == NO_ID) {
            recoveryCodeRepository.insertEntry(
                    serviceName,
                    codes,
                    notes,
                    0,
                    false,
                    callback
            );
        } else {
            DecryptedRecoveryCode updated = new DecryptedRecoveryCode(
                    editingId,
                    serviceName,
                    codes,
                    notes,
                    originalCategoryId,
                    originalIsFavorite,
                    originalCreatedAt,
                    System.currentTimeMillis()
            );

            recoveryCodeRepository.updateEntry(updated, callback);
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
