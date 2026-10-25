
package com.example.securedigitalvault.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.ui.auth.LockScreenActivity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity
        implements DashboardAdapter.OnCategoryClickListener {

    private RecyclerView categoryRecyclerView;
    private ImageButton lockButton;
    private ImageButton settingsButton;

    private com.example.securedigitalvault.data.repository.PasswordRepository passwordRepository;
    private List<CategoryItem> categories;
    private DashboardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        lockButton = findViewById(R.id.lockButton);
        settingsButton = findViewById(R.id.settingsButton);

        setupCategoryGrid();
        setupHeaderActions();
        setupQuickActions();
    }

    private void setupCategoryGrid() {
        categories = buildCategoryList();

        adapter = new DashboardAdapter(categories, this);
        categoryRecyclerView.setLayoutManager(
                new GridLayoutManager(this, 2)
        );
        categoryRecyclerView.setAdapter(adapter);

        try {
            passwordRepository =
                    new com.example.securedigitalvault.data.repository.PasswordRepository(
                            getApplicationContext()
                    );

            passwordRepository.getPasswordCountLive().observe(this, count -> {
                updateCategoryCount(
                        "passwords",
                        "Passwords",
                        R.drawable.ic_password,
                        R.drawable.circle_mustard,
                        count
                );
            });

            com.example.securedigitalvault.data.repository.SecureNoteRepository noteRepository =
                    new com.example.securedigitalvault.data.repository.SecureNoteRepository(
                            getApplicationContext()
                    );

            noteRepository.getNoteCountLive().observe(this, count -> {
                updateCategoryCount(
                        "notes",
                        "Secure Notes",
                        R.drawable.ic_note,
                        R.drawable.circle_coral,
                        count
                );
            });

            com.example.securedigitalvault.data.repository.DocumentIdRepository documentRepository =
                    new com.example.securedigitalvault.data.repository.DocumentIdRepository(
                            getApplicationContext()
                    );

            documentRepository.getDocumentCountLive().observe(this, count -> {
                updateCategoryCount(
                        "ids",
                        "IDs & Documents",
                        R.drawable.ic_id,
                        R.drawable.circle_mint,
                        count
                );
            });

            com.example.securedigitalvault.data.repository.CardRepository cardRepository =
                    new com.example.securedigitalvault.data.repository.CardRepository(
                            getApplicationContext()
                    );

            cardRepository.getCardCountLive().observe(this, count -> {
                updateCategoryCount(
                        "cards",
                        "Cards",
                        R.drawable.ic_card,
                        R.drawable.circle_indigo,
                        count
                );
            });

            com.example.securedigitalvault.data.repository.RecoveryCodeRepository recoveryRepository =
                    new com.example.securedigitalvault.data.repository.RecoveryCodeRepository(
                            getApplicationContext()
                    );

            recoveryRepository.getEntryCountLive().observe(this, count -> {
                updateCategoryCount(
                        "recovery",
                        "Recovery Codes",
                        R.drawable.ic_recovery,
                        R.drawable.circle_mint,
                        count
                );
            });

            com.example.securedigitalvault.data.repository.PhotoRepository photoRepository =
                    new com.example.securedigitalvault.data.repository.PhotoRepository(
                            getApplicationContext()
                    );

            photoRepository.getPhotoCountLive().observe(this, count -> {
                updateCategoryCount(
                        "photos",
                        "Private Photos",
                        R.drawable.ic_photo,
                        R.drawable.circle_mustard,
                        count
                );
            });

            com.example.securedigitalvault.data.repository.DocumentFileRepository docFileRepository =
                    new com.example.securedigitalvault.data.repository.DocumentFileRepository(
                            getApplicationContext()
                    );

            docFileRepository.getDocumentCountLive().observe(this, count -> {
                updateCategoryCount(
                        "pdfs",
                        "PDFs",
                        R.drawable.ic_pdf,
                        R.drawable.circle_coral,
                        count
                );
            });

        } catch (Throwable t) {
            showCrashDialog(t);
        }
    }

    private void updateCategoryCount(
            String id,
            String title,
            int iconResource,
            int backgroundResource,
            Integer count
    ) {
        if (categories == null || adapter == null) {
            return;
        }

        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId().equals(id)) {
                categories.set(
                        i,
                        new CategoryItem(
                                id,
                                title,
                                iconResource,
                                backgroundResource,
                                count == null ? 0 : count
                        )
                );

                adapter.notifyItemChanged(i);
                return;
            }
        }
    }

    private List<CategoryItem> buildCategoryList() {
        List<CategoryItem> list = new ArrayList<>();

        list.add(new CategoryItem(
                "passwords", "Passwords",
                R.drawable.ic_password, R.drawable.circle_mustard, 0
        ));

        list.add(new CategoryItem(
                "notes", "Secure Notes",
                R.drawable.ic_note, R.drawable.circle_coral, 0
        ));

        list.add(new CategoryItem(
                "ids", "IDs & Documents",
                R.drawable.ic_id, R.drawable.circle_mint, 0
        ));

        list.add(new CategoryItem(
                "cards", "Cards",
                R.drawable.ic_card, R.drawable.circle_indigo, 0
        ));

        list.add(new CategoryItem(
                "photos", "Private Photos",
                R.drawable.ic_photo, R.drawable.circle_mustard, 0
        ));

        list.add(new CategoryItem(
                "pdfs", "PDFs",
                R.drawable.ic_pdf, R.drawable.circle_coral, 0
        ));

        list.add(new CategoryItem(
                "recovery", "Recovery Codes",
                R.drawable.ic_recovery, R.drawable.circle_mint, 0
        ));

        return list;
    }

    private void setupHeaderActions() {
        lockButton.setOnClickListener(v -> lockVaultNow());

        findViewById(R.id.searchButton).setOnClickListener(v ->
                safeStartActivity(
                        com.example.securedigitalvault.ui.search.GlobalSearchActivity.class
                )
        );

        settingsButton.setOnClickListener(v ->
                safeStartActivity(
                        com.example.securedigitalvault.ui.settings.SettingsActivity.class
                )
        );
    }

    private void setupQuickActions() {
        findViewById(R.id.addPasswordButton).setOnClickListener(v ->
                safeStartActivity(
                        com.example.securedigitalvault.ui.passwords.AddEditPasswordActivity.class
                )
        );

        findViewById(R.id.addNoteButton).setOnClickListener(v ->
                safeStartActivity(
                        com.example.securedigitalvault.ui.notes.AddEditNoteActivity.class
                )
        );

        findViewById(R.id.addDocumentButton).setOnClickListener(v ->
                safeStartActivity(
                        com.example.securedigitalvault.ui.documents.AddEditDocumentIdActivity.class
                )
        );

        findViewById(R.id.addCardButton).setOnClickListener(v ->
                safeStartActivity(
                        com.example.securedigitalvault.ui.cards.AddEditCardActivity.class
                )
        );

        findViewById(R.id.addRecoveryButton).setOnClickListener(v ->
                safeStartActivity(
                        com.example.securedigitalvault.ui.recovery.AddEditRecoveryCodeActivity.class
                )
        );

        // Open the photo screen and automatically launch the image picker.
        findViewById(R.id.addPhotoButton).setOnClickListener(v ->
                openImportScreen(
                        com.example.securedigitalvault.ui.photos.PhotoListActivity.class,
                        "OPEN_PHOTO_PICKER"
                )
        );

        // Open the PDF screen and automatically launch the PDF picker.
        findViewById(R.id.addPdfButton).setOnClickListener(v ->
                openImportScreen(
                        com.example.securedigitalvault.ui.documents.DocumentFileListActivity.class,
                        "OPEN_DOCUMENT_PICKER"
                )
        );
    }

    private void openImportScreen(Class<?> activityClass, String extraKey) {
        try {
            Intent intent = new Intent(this, activityClass);
            intent.putExtra(extraKey, true);
            startActivity(intent);
        } catch (Throwable t) {
            showCrashDialog(t);
        }
    }

    private void lockVaultNow() {
        Intent intent = new Intent(
                DashboardActivity.this,
                LockScreenActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    public void onCategoryClick(CategoryItem item) {
        if (item == null) {
            return;
        }

        if (item.getId().equals("passwords")) {
            safeStartActivity(
                    com.example.securedigitalvault.ui.passwords.PasswordListActivity.class
            );

        } else if (item.getId().equals("notes")) {
            safeStartActivity(
                    com.example.securedigitalvault.ui.notes.NoteListActivity.class
            );

        } else if (item.getId().equals("ids")) {
            safeStartActivity(
                    com.example.securedigitalvault.ui.documents.DocumentIdListActivity.class
            );

        } else if (item.getId().equals("cards")) {
            safeStartActivity(
                    com.example.securedigitalvault.ui.cards.CardListActivity.class
            );

        } else if (item.getId().equals("recovery")) {
            safeStartActivity(
                    com.example.securedigitalvault.ui.recovery.RecoveryCodeListActivity.class
            );

        } else if (item.getId().equals("photos")) {
            safeStartActivity(
                    com.example.securedigitalvault.ui.photos.PhotoListActivity.class
            );

        } else if (item.getId().equals("pdfs")) {
            safeStartActivity(
                    com.example.securedigitalvault.ui.documents.DocumentFileListActivity.class
            );

        } else {
            Toast.makeText(
                    this,
                    item.getTitle() + " — coming soon",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void safeStartActivity(Class<?> activityClass) {
        try {
            startActivity(new Intent(this, activityClass));
        } catch (Throwable t) {
            showCrashDialog(t);
        }
    }

    private void showCrashDialog(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));

        new android.app.AlertDialog.Builder(this)
                .setTitle("Error: " + t.getClass().getSimpleName())
                .setMessage(sw.toString())
                .setPositiveButton("OK", null)
                .show();
    }
}
