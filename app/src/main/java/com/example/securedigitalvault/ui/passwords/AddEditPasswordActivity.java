
package com.example.securedigitalvault.ui.passwords;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.repository.PasswordRepository;
import com.example.securedigitalvault.data.repository.PasswordRepository.SaveCallback;
import com.example.securedigitalvault.ui.passwords.DecryptedPassword;
import com.example.securedigitalvault.util.SecurityUtils;

public class AddEditPasswordActivity extends AppCompatActivity {

    public static final String EXTRA_PASSWORD_ID = "password_id";
    private static final int NO_ID = -1;

    private TextView screenTitle;
    private EditText siteNameInput;
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText urlInput;
    private EditText notesInput;
    private ImageButton toggleVisibilityButton;
    private ImageButton backButton;
    private Button saveButton;
    private TextView errorText;

    private PasswordRepository passwordRepository;

    private int editingPasswordId = NO_ID;
    private boolean isPasswordVisible = false;
    private boolean isLoading = false;
    private boolean isSaving = false;

    private long originalCreatedAt;
    private boolean originalIsFavorite;
    private int originalCategoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SecurityUtils.applySecureFlag(this);
        setContentView(R.layout.activity_add_edit_password);

        passwordRepository =
                new PasswordRepository(getApplicationContext());

        bindViews();
        setupListeners();

        editingPasswordId = getIntent().getIntExtra(
                EXTRA_PASSWORD_ID,
                NO_ID
        );

        if (editingPasswordId != NO_ID) {
            setLoading(true);
            loadExistingPassword(editingPasswordId);
        }
    }

    private void bindViews() {
        screenTitle = findViewById(R.id.screenTitle);
        siteNameInput = findViewById(R.id.siteNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        urlInput = findViewById(R.id.urlInput);
        notesInput = findViewById(R.id.notesInput);
        toggleVisibilityButton =
                findViewById(R.id.toggleVisibilityButton);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        errorText = findViewById(R.id.errorText);

        errorText.setVisibility(View.GONE);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        toggleVisibilityButton.setOnClickListener(
                v -> togglePasswordVisibility()
        );
        saveButton.setOnClickListener(v -> handleSaveClicked());
    }

    private void loadExistingPassword(int id) {
        screenTitle.setText("Edit Password");
        saveButton.setText("Loading...");

        passwordRepository.getPasswordByIdAsync(id, decrypted -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            if (decrypted == null) {
                setLoading(false);
                Toast.makeText(
                        this,
                        "Could not load this password entry.",
                        Toast.LENGTH_LONG
                ).show();
                finish();
                return;
            }

            siteNameInput.setText(decrypted.siteName);
            usernameInput.setText(decrypted.username);
            passwordInput.setText(decrypted.password);
            urlInput.setText(decrypted.url);
            notesInput.setText(decrypted.notes);

            originalCreatedAt = decrypted.createdAt;
            originalIsFavorite = decrypted.isFavorite;
            originalCategoryId = decrypted.categoryId;

            setLoading(false);
            saveButton.setText("Update Password");
        });
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        int cursorPosition = passwordInput.getSelectionStart();

        if (isPasswordVisible) {
            passwordInput.setInputType(
                    InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );
            toggleVisibilityButton.setImageResource(
                    android.R.drawable.ic_menu_close_clear_cancel
            );
            toggleVisibilityButton.setContentDescription("Hide password");
        } else {
            passwordInput.setInputType(
                    InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            toggleVisibilityButton.setImageResource(
                    android.R.drawable.ic_menu_view
            );
            toggleVisibilityButton.setContentDescription("Show password");
        }

        if (cursorPosition >= 0
                && cursorPosition <= passwordInput.length()) {
            passwordInput.setSelection(cursorPosition);
        }
    }

    private void handleSaveClicked() {
        if (isLoading || isSaving) {
            return;
        }

        String siteName = siteNameInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String url = urlInput.getText().toString().trim();
        String notes = notesInput.getText().toString().trim();

        if (siteName.isEmpty()) {
            showError("Enter a website or app name.");
            siteNameInput.requestFocus();
            return;
        }

        if (password.trim().isEmpty()) {
            showError("Enter a password.");
            passwordInput.requestFocus();
            return;
        }

        errorText.setVisibility(View.GONE);
        setSaving(true);

        if (editingPasswordId == NO_ID) {
            try {
                passwordRepository.insertPassword(
                        siteName,
                        username,
                        password,
                        url,
                        notes,
                        0,
                        false,
                        new SaveCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> {
                                    if (isFinishing() || isDestroyed()) {
                                        return;
                                    }

                                    Toast.makeText(
                                            AddEditPasswordActivity.this,
                                            "Password saved successfully.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    finish();
                                });
                            }

                            @Override
                            public void onError() {
                                runOnUiThread(() -> {
                                    if (isFinishing() || isDestroyed()) {
                                        return;
                                    }

                                    setSaving(false);
                                    showError(
                                            "Failed to save password. Please try again."
                                    );
                                });
                            }
                        }
                );
            } catch (Exception e) {
                setSaving(false);
                showError("Could not save the password. Please try again.");
            }
        } else {
            try {
                DecryptedPassword updated = new DecryptedPassword(
                        editingPasswordId,
                        siteName,
                        username,
                        password,
                        url,
                        notes,
                        originalCategoryId,
                        originalIsFavorite,
                        originalCreatedAt,
                        System.currentTimeMillis()
                );

                passwordRepository.updatePassword(updated);

                Toast.makeText(
                        this,
                        "Password update started.",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            } catch (Exception e) {
                setSaving(false);
                showError("Could not update the password. Please try again.");
            }
        }
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        siteNameInput.setEnabled(!loading);
        usernameInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
        urlInput.setEnabled(!loading);
        notesInput.setEnabled(!loading);
        toggleVisibilityButton.setEnabled(!loading);
        saveButton.setEnabled(!loading && !isSaving);
    }

    private void setSaving(boolean saving) {
        isSaving = saving;
        saveButton.setEnabled(!saving && !isLoading);

        if (saving) {
            saveButton.setText("Saving...");
        } else if (editingPasswordId != NO_ID) {
            saveButton.setText("Update Password");
        } else {
            saveButton.setText("Save Password");
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
