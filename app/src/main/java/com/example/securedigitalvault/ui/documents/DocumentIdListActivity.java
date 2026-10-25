package com.example.securedigitalvault.ui.documents;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.repository.DocumentIdRepository;

public class DocumentIdListActivity extends AppCompatActivity
        implements DocumentIdListAdapter.OnDocumentActionListener {

    private RecyclerView recyclerView;
    private android.widget.TextView emptyStateText;
    private DocumentIdRepository documentRepository;
    private DocumentIdListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_id_list);

        documentRepository = new DocumentIdRepository(getApplicationContext());

        recyclerView = findViewById(R.id.documentRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        adapter = new DocumentIdListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.addButton).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditDocumentIdActivity.class)));

        documentRepository.getAllDocuments().observe(this, documents -> {
            adapter.submitList(documents);
            emptyStateText.setVisibility(documents.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(documents.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onRowClick(DecryptedDocumentId document) {
        Intent intent = new Intent(this, AddEditDocumentIdActivity.class);
        intent.putExtra(AddEditDocumentIdActivity.EXTRA_DOCUMENT_ID, document.id);
        startActivity(intent);
    }

    @Override
    public void onFavoriteToggle(DecryptedDocumentId document) {
        documentRepository.toggleFavorite(document.id, !document.isFavorite);
    }

    @Override
    public void onDeleteClick(DecryptedDocumentId document) {
        new AlertDialog.Builder(this)
                .setTitle("Delete this entry?")
                .setMessage("This will permanently delete \"" + document.documentName + "\". This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> documentRepository.deleteDocument(document.id))
                .setNegativeButton("Cancel", null)
                .show();
    }
}

