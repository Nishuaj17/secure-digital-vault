package com.example.securedigitalvault.ui.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.local.VaultDatabase;
import com.example.securedigitalvault.data.local.dao.CategoryDao;
import com.example.securedigitalvault.data.local.entity.CategoryEntity;
import com.example.securedigitalvault.util.SecurityUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CategoryManagerActivity
 *
 * Lets the user create and delete categories (e.g. "Work", "Personal",
 * "Banking"). Categories are non-sensitive, so no encryption is
 * involved here — this Activity talks to CategoryDao directly rather
 * than going through a dedicated repository, since the entity is
 * simple enough not to need one.
 *
 * NOTE ON DELETING A CATEGORY: deleting a category here does NOT
 * delete or reassign items that were using it — their categoryId
 * simply becomes a "dangling" reference. This is a known simplification;
 * a more complete implementation would prompt the user to reassign
 * those items first or automatically fall back to "No category."
 */
public class CategoryManagerActivity extends AppCompatActivity implements CategoryManageAdapter.OnCategoryDeleteListener {

    private final String[] colorOptions = {"#F4B942", "#F26A5B", "#5FD6C2", "#2B2A4C"};

    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private CategoryDao categoryDao;
    private CategoryManageAdapter adapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecurityUtils.applySecureFlag(this);
        setContentView(R.layout.activity_category_manager);

        categoryDao = VaultDatabase.getInstance(getApplicationContext()).categoryDao();

        recyclerView = findViewById(R.id.categoryRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        adapter = new CategoryManageAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.addButton).setOnClickListener(v -> showAddCategoryDialog());

        categoryDao.getAllCategories().observe(this, categories -> {
            adapter.submitList(categories);
            emptyStateText.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(categories.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    private void showAddCategoryDialog() {
        EditText input = new EditText(this);
        input.setHint("Category name");
        input.setPadding(40, 40, 40, 40);

        new AlertDialog.Builder(this)
                .setTitle("New category")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Please enter a name.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Cycle through our palette colors for variety.
                    String color = colorOptions[Math.abs(name.hashCode()) % colorOptions.length];
                    executorService.execute(() -> categoryDao.insert(new CategoryEntity(name, color)));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeleteClick(CategoryEntity category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete category?")
                .setMessage("Items using \"" + category.name + "\" will keep showing it as their category until you edit them individually.")
                .setPositiveButton("Delete", (dialog, which) ->
                        executorService.execute(() -> categoryDao.delete(category)))
                .setNegativeButton("Cancel", null)
                .show();
    }
}


