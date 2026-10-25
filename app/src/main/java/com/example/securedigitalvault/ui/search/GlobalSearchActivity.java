package com.example.securedigitalvault.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.local.VaultDatabase;
import com.example.securedigitalvault.data.local.entity.CardEntity;
import com.example.securedigitalvault.data.local.entity.DocumentIdEntity;
import com.example.securedigitalvault.data.local.entity.PasswordEntity;
import com.example.securedigitalvault.data.local.entity.RecoveryCodeEntity;
import com.example.securedigitalvault.data.local.entity.SecureNoteEntity;
import com.example.securedigitalvault.ui.cards.AddEditCardActivity;
import com.example.securedigitalvault.ui.documents.AddEditDocumentIdActivity;
import com.example.securedigitalvault.ui.notes.AddEditNoteActivity;
import com.example.securedigitalvault.ui.passwords.AddEditPasswordActivity;
import com.example.securedigitalvault.ui.recovery.AddEditRecoveryCodeActivity;
import com.example.securedigitalvault.util.SecurityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * GlobalSearchActivity
 *
 * Searches across Passwords, Secure Notes, IDs & Documents, Cards,
 * and Recovery Codes by their plain (non-sensitive) name/title fields
 * only. No decryption happens anywhere in this screen — we work
 * directly with Room entities' plain columns, never their encrypted
 * byte[] fields or any repository's decrypted models.
 *
 * We keep one plain-entity list per module (updated via LiveData
 * observers) and recombine + filter them into one result list every
 * time either the underlying data or the search text changes.
 */
public class GlobalSearchActivity extends AppCompatActivity implements GlobalSearchAdapter.OnResultClickListener {

    private EditText searchInput;
    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private GlobalSearchAdapter adapter;

    private List<PasswordEntity> allPasswords = new ArrayList<>();
    private List<SecureNoteEntity> allNotes = new ArrayList<>();
    private List<DocumentIdEntity> allDocumentIds = new ArrayList<>();
    private List<CardEntity> allCards = new ArrayList<>();
    private List<RecoveryCodeEntity> allRecoveryCodes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecurityUtils.applySecureFlag(this);
        setContentView(R.layout.activity_global_search);

        searchInput = findViewById(R.id.searchInput);
        recyclerView = findViewById(R.id.resultsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        adapter = new GlobalSearchAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        observeAllSources();
        setupSearchInput();
    }

    private void observeAllSources() {
        VaultDatabase db = VaultDatabase.getInstance(getApplicationContext());

        db.passwordDao().getAllPasswords().observe(this, list -> { allPasswords = list; applyFilter(); });
        db.secureNoteDao().getAllNotes().observe(this, list -> { allNotes = list; applyFilter(); });
        db.documentIdDao().getAllDocuments().observe(this, list -> { allDocumentIds = list; applyFilter(); });
        db.cardDao().getAllCards().observe(this, list -> { allCards = list; applyFilter(); });
        db.recoveryCodeDao().getAllEntries().observe(this, list -> { allRecoveryCodes = list; applyFilter(); });
    }

    private void setupSearchInput() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilter() {
        String query = searchInput.getText().toString().trim().toLowerCase(Locale.getDefault());

        if (query.isEmpty()) {
            adapter.submitList(new ArrayList<>());
            emptyStateText.setText("Start typing to search across your passwords, notes, IDs, cards, and recovery codes.");
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        List<GlobalSearchResult> combined = new ArrayList<>();

        for (PasswordEntity p : allPasswords) {
            if (p.siteName.toLowerCase(Locale.getDefault()).contains(query)) {
                combined.add(new GlobalSearchResult(p.id, p.siteName, GlobalSearchResult.ResultType.PASSWORD));
            }
        }
        for (SecureNoteEntity n : allNotes) {
            if (n.title.toLowerCase(Locale.getDefault()).contains(query)) {
                combined.add(new GlobalSearchResult(n.id, n.title, GlobalSearchResult.ResultType.NOTE));
            }
        }
        for (DocumentIdEntity d : allDocumentIds) {
            if (d.documentName.toLowerCase(Locale.getDefault()).contains(query)) {
                combined.add(new GlobalSearchResult(d.id, d.documentName, GlobalSearchResult.ResultType.DOCUMENT_ID));
            }
        }
        for (CardEntity c : allCards) {
            if (c.nickname.toLowerCase(Locale.getDefault()).contains(query)) {
                combined.add(new GlobalSearchResult(c.id, c.nickname, GlobalSearchResult.ResultType.CARD));
            }
        }
        for (RecoveryCodeEntity r : allRecoveryCodes) {
            if (r.serviceName.toLowerCase(Locale.getDefault()).contains(query)) {
                combined.add(new GlobalSearchResult(r.id, r.serviceName, GlobalSearchResult.ResultType.RECOVERY_CODE));
            }
        }

        adapter.submitList(combined);
        emptyStateText.setText("No matches found.");
        emptyStateText.setVisibility(combined.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(combined.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResultClick(GlobalSearchResult result) {
        Intent intent;
        switch (result.type) {
            case PASSWORD:
                intent = new Intent(this, AddEditPasswordActivity.class);
                intent.putExtra(AddEditPasswordActivity.EXTRA_PASSWORD_ID, result.entityId);
                break;
            case NOTE:
                intent = new Intent(this, AddEditNoteActivity.class);
                intent.putExtra(AddEditNoteActivity.EXTRA_NOTE_ID, result.entityId);
                break;
            case DOCUMENT_ID:
                intent = new Intent(this, AddEditDocumentIdActivity.class);
                intent.putExtra(AddEditDocumentIdActivity.EXTRA_DOCUMENT_ID, result.entityId);
                break;
            case CARD:
                intent = new Intent(this, AddEditCardActivity.class);
                intent.putExtra(AddEditCardActivity.EXTRA_CARD_ID, result.entityId);
                break;
            case RECOVERY_CODE:
                intent = new Intent(this, AddEditRecoveryCodeActivity.class);
                intent.putExtra(AddEditRecoveryCodeActivity.EXTRA_ENTRY_ID, result.entityId);
                break;
            default:
                return;
        }
        startActivity(intent);
    }
}


