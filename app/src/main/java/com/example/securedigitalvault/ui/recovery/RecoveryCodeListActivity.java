package com.example.securedigitalvault.ui.recovery;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.repository.RecoveryCodeRepository;
import com.example.securedigitalvault.util.ClipboardHelper;

public class RecoveryCodeListActivity extends AppCompatActivity
        implements RecoveryCodeListAdapter.OnRecoveryActionListener {

    private RecyclerView recyclerView;
    private android.widget.TextView emptyStateText;
    private RecoveryCodeRepository recoveryCodeRepository;
    private RecoveryCodeListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_code_list);

        recoveryCodeRepository = new RecoveryCodeRepository(getApplicationContext());

        recyclerView = findViewById(R.id.recoveryRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        adapter = new RecoveryCodeListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.addButton).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditRecoveryCodeActivity.class)));

        recoveryCodeRepository.getAllEntries().observe(this, entries -> {
            adapter.submitList(entries);
            emptyStateText.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onRowClick(DecryptedRecoveryCode entry) {
        Intent intent = new Intent(this, AddEditRecoveryCodeActivity.class);
        intent.putExtra(AddEditRecoveryCodeActivity.EXTRA_ENTRY_ID, entry.id);
        startActivity(intent);
    }

    @Override
    public void onCopyClick(DecryptedRecoveryCode entry) {
        ClipboardHelper.copyWithAutoClear(this, "Recovery codes", entry.codes);
    }

    @Override
    public void onFavoriteToggle(DecryptedRecoveryCode entry) {
        recoveryCodeRepository.toggleFavorite(entry.id, !entry.isFavorite);
    }

    @Override
    public void onDeleteClick(DecryptedRecoveryCode entry) {
        new AlertDialog.Builder(this)
                .setTitle("Delete recovery codes?")
                .setMessage("This will permanently delete the codes for \"" + entry.serviceName + "\". This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> recoveryCodeRepository.deleteEntry(entry.id))
                .setNegativeButton("Cancel", null)
                .show();
    }
}

