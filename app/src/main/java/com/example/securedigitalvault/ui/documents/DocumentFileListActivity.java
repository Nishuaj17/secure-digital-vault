package com.example.securedigitalvault.ui.documents;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.local.entity.DocumentFileEntity;
import com.example.securedigitalvault.data.repository.DocumentFileRepository;
import com.example.securedigitalvault.util.SecurityUtils;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class DocumentFileListActivity extends AppCompatActivity
        implements DocumentFileListAdapter.OnDocumentFileActionListener {

    private static final String FILE_PROVIDER_AUTHORITY =
            "com.example.securedigitalvault.fileprovider";

    private static final String EXTRA_OPEN_DOCUMENT_PICKER =
            "OPEN_DOCUMENT_PICKER";

    private RecyclerView recyclerView;
    private EditText searchInput;
    private TextView emptyStateText;

    private DocumentFileRepository documentFileRepository;
    private DocumentFileListAdapter adapter;

    private ActivityResultLauncher<String[]> documentPickerLauncher;

    private LiveData<List<DocumentFileEntity>> activeDocuments;

    private boolean isImporting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SecurityUtils.applySecureFlag(this);
        setContentView(R.layout.activity_document_file_list);

        documentFileRepository =
                new DocumentFileRepository(getApplicationContext());

        recyclerView = findViewById(R.id.documentRecyclerView);
        searchInput = findViewById(R.id.searchInput);
        emptyStateText = findViewById(R.id.emptyStateText);

        adapter = new DocumentFileListAdapter(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        registerDocumentPicker();

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        findViewById(R.id.importButton).setOnClickListener(
                v -> openDocumentPicker(false)
        );

        setupSearch();
        observeDocuments("");

        // Dashboard's "+ PDF" action opens the picker automatically.
        if (getIntent().getBooleanExtra(EXTRA_OPEN_DOCUMENT_PICKER, false)) {
            recyclerView.post(() -> openDocumentPicker(true));
        }
    }

    private void registerDocumentPicker() {
        documentPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::onDocumentSelected
        );
    }

    private void openDocumentPicker(boolean pdfOnly) {
        if (isFinishing() || isDestroyed() || isImporting) {
            return;
        }

        String[] mimeTypes = pdfOnly
                ? new String[]{"application/pdf"}
                : new String[]{"*/*"};

        documentPickerLauncher.launch(mimeTypes);
    }

    private void onDocumentSelected(Uri selectedUri) {
        if (selectedUri == null || isFinishing() || isDestroyed()) {
            return;
        }

        isImporting = true;

        Toast.makeText(
                this,
                "Encrypting and saving document...",
                Toast.LENGTH_SHORT
        ).show();

        documentFileRepository.insertDocument(
                selectedUri,
                success -> runOnUiThread(() -> {
                    isImporting = false;

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    String message = success
                            ? "Document saved securely."
                            : "Failed to save document. Please try again.";

                    Toast.makeText(
                            DocumentFileListActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                })
        );
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
                // No action needed.
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                observeDocuments(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed.
            }
        });
    }

    private void observeDocuments(String query) {
        if (activeDocuments != null) {
            activeDocuments.removeObservers(this);
        }

        if (query == null || query.trim().isEmpty()) {
            activeDocuments = documentFileRepository.getAllDocuments();
        } else {
            activeDocuments =
                    documentFileRepository.searchByFileName(query.trim());
        }

        activeDocuments.observe(this, this::renderList);
    }

    private void renderList(List<DocumentFileEntity> documents) {
        if (documents == null || documents.isEmpty()) {
            adapter.submitList(null);
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            adapter.submitList(documents);
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRowClick(DocumentFileEntity document) {
        if (document == null) {
            return;
        }

        Toast.makeText(
                this,
                "Decrypting for viewing...",
                Toast.LENGTH_SHORT
        ).show();

        documentFileRepository.openDocumentSecurely(
                document.id,
                new DocumentFileRepository.OpenCallback() {
                    @Override
                    public void onReady(File tempFile) {
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) {
                                if (tempFile.exists()) {
                                    tempFile.delete();
                                }
                                return;
                            }

                            openWithExternalViewer(tempFile);
                        });
                    }

                    @Override
                    public void onError() {
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            Toast.makeText(
                                    DocumentFileListActivity.this,
                                    "Could not open this document.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                }
        );
    }

    private void openWithExternalViewer(File tempFile) {
        try {
            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    FILE_PROVIDER_AUTHORITY,
                    tempFile
            );

            Intent viewIntent = new Intent(Intent.ACTION_VIEW);

            viewIntent.setDataAndType(
                    contentUri,
                    getMimeTypeForFile(tempFile.getName())
            );

            viewIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            startActivity(viewIntent);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    this,
                    "No app found to open this file type.",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (IllegalArgumentException e) {
            Toast.makeText(
                    this,
                    "Unable to securely share this document.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private String getMimeTypeForFile(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }

        if (lower.endsWith(".png")) {
            return "image/png";
        }

        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        if (lower.endsWith(".txt")) {
            return "text/plain";
        }

        if (lower.endsWith(".doc")) {
            return "application/msword";
        }

        if (lower.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }

        if (lower.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        }

        if (lower.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }

        return "*/*";
    }

    @Override
    public void onDeleteClick(DocumentFileEntity document) {
        if (document == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete this document?")
                .setMessage(
                        "This will permanently delete \""
                                + document.fileName
                                + "\". This cannot be undone."
                )
                .setPositiveButton("Delete", (dialog, which) -> {
                    documentFileRepository.deleteDocument(document.id);

                    Toast.makeText(
                            this,
                            "Document deletion requested.",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (activeDocuments != null) {
            activeDocuments.removeObservers(this);
        }

        if (documentFileRepository != null) {
            documentFileRepository.clearTempFiles();
        }

        super.onDestroy();
    }
}