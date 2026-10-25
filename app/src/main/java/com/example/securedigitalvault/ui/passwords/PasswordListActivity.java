
package com.example.securedigitalvault.ui.passwords;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.repository.PasswordRepository;
import com.example.securedigitalvault.util.ClipboardHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PasswordListActivity extends AppCompatActivity
        implements PasswordListAdapter.OnPasswordActionListener {

    private RecyclerView recyclerView;
    private EditText searchInput;
    private TextView emptyStateText;
    private ImageButton backButton;

    private PasswordRepository passwordRepository;
    private PasswordListAdapter adapter;

    private List<DecryptedPassword> fullList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_list);

        passwordRepository = new PasswordRepository(getApplicationContext());

        recyclerView = findViewById(R.id.passwordRecyclerView);
        searchInput = findViewById(R.id.searchInput);
        emptyStateText = findViewById(R.id.emptyStateText);
        backButton = findViewById(R.id.backButton);

        adapter = new PasswordListAdapter(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());

        setupSearch();
        observePasswords();
    }

    private void observePasswords() {
        passwordRepository.getAllPasswords().observe(this, passwords -> {
            fullList = passwords == null
                    ? new ArrayList<>()
                    : new ArrayList<>(passwords);

            applyFilter(
                    searchInput.getText() == null
                            ? ""
                            : searchInput.getText().toString()
            );
        });
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
                // No action required before text changes.
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                applyFilter(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Filtering is handled in onTextChanged().
            }
        });
    }

    private void applyFilter(String query) {
        if (query == null || query.trim().isEmpty()) {
            renderList(fullList);
            return;
        }

        String lowerQuery = query.trim().toLowerCase(Locale.ROOT);
        List<DecryptedPassword> filtered = new ArrayList<>();

        for (DecryptedPassword password : fullList) {
            if (password == null) {
                continue;
            }

            String siteName = password.siteName == null
                    ? ""
                    : password.siteName;

            if (siteName.toLowerCase(Locale.ROOT).contains(lowerQuery)) {
                filtered.add(password);
            }
        }

        renderList(filtered);
    }

    private void renderList(List<DecryptedPassword> list) {
        List<DecryptedPassword> safeList = list == null
                ? new ArrayList<>()
                : new ArrayList<>(list);

        adapter.submitList(safeList);

        boolean isEmpty = safeList.isEmpty();

        emptyStateText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            String query = searchInput.getText() == null
                    ? ""
                    : searchInput.getText().toString().trim();

            emptyStateText.setText(
                    query.isEmpty()
                            ? "No passwords saved yet."
                            : "No passwords match your search."
            );
        }
    }

    @Override
    public void onRowClick(DecryptedPassword password) {
        if (password == null) {
            return;
        }

        Intent intent = new Intent(
                this,
                AddEditPasswordActivity.class
        );

        intent.putExtra(
                AddEditPasswordActivity.EXTRA_PASSWORD_ID,
                password.id
        );

        startActivity(intent);
    }

    @Override
    public void onCopyClick(DecryptedPassword password) {
        if (password == null || password.password == null) {
            return;
        }

        ClipboardHelper.copyWithAutoClear(
                this,
                "Password",
                password.password
        );
    }

    @Override
    public void onFavoriteToggle(DecryptedPassword password) {
        if (password == null) {
            return;
        }

        passwordRepository.toggleFavorite(
                password.id,
                !password.isFavorite
        );
    }

    @Override
    public void onDeleteClick(DecryptedPassword password) {
        if (password == null) {
            return;
        }

        String siteName = password.siteName == null
                || password.siteName.trim().isEmpty()
                ? "this entry"
                : password.siteName;

        new AlertDialog.Builder(this)
                .setTitle("Delete password?")
                .setMessage(
                        "This will permanently delete the entry for \""
                                + siteName
                                + "\". This cannot be undone."
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                passwordRepository.deletePassword(password.id)
                )
                .setNegativeButton("Cancel", null)
                .show();
    }
}
