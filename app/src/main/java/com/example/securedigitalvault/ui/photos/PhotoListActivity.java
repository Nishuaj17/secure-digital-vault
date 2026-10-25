
package com.example.securedigitalvault.ui.photos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.local.entity.PhotoEntity;
import com.example.securedigitalvault.data.repository.PhotoRepository;
import com.example.securedigitalvault.util.SecurityUtils;

import java.util.Collections;
import java.util.List;

public class PhotoListActivity extends AppCompatActivity
        implements PhotoGridAdapter.OnPhotoClickListener {

    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private ImageButton importButton;

    private PhotoRepository photoRepository;
    private PhotoGridAdapter adapter;

    private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;

    private boolean isImporting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SecurityUtils.applySecureFlag(this);
        setContentView(R.layout.activity_photo_list);

        photoRepository = new PhotoRepository(getApplicationContext());

        recyclerView = findViewById(R.id.photoRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        importButton = findViewById(R.id.importButton);

        adapter = new PhotoGridAdapter(photoRepository, this);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);

        registerPhotoPicker();

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Import button on the photo screen.
        importButton.setOnClickListener(v -> openPhotoPicker());

        // Show saved photos and update the empty state.
        photoRepository.getAllPhotoMetadata().observe(this, photos -> {
            List<PhotoEntity> safePhotos = photos == null
                    ? Collections.emptyList()
                    : photos;

            adapter.submitList(safePhotos);

            boolean isEmpty = safePhotos.isEmpty();

            emptyStateText.setVisibility(
                    isEmpty ? View.VISIBLE : View.GONE
            );

            recyclerView.setVisibility(
                    isEmpty ? View.GONE : View.VISIBLE
            );
        });

        // If launched from the dashboard's + Private Photo button,
        // open the image picker immediately.
        if (getIntent().getBooleanExtra("OPEN_PHOTO_PICKER", false)) {
            recyclerView.post(this::openPhotoPicker);
        }
    }

    private void registerPhotoPicker() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                this::onPhotoSelected
        );
    }

    private void openPhotoPicker() {
        if (isImporting || isFinishing() || isDestroyed()) {
            return;
        }

        photoPickerLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(
                                ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE
                        )
                        .build()
        );
    }

    private void onPhotoSelected(@Nullable Uri selectedUri) {
        // The user may cancel the picker.
        if (selectedUri == null || isFinishing() || isDestroyed()) {
            return;
        }

        isImporting = true;
        importButton.setEnabled(false);

        Toast.makeText(
                this,
                "Encrypting and saving photo...",
                Toast.LENGTH_SHORT
        ).show();

        photoRepository.insertPhoto(selectedUri, success ->
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    isImporting = false;
                    importButton.setEnabled(true);

                    Toast.makeText(
                            PhotoListActivity.this,
                            success
                                    ? "Photo saved securely."
                                    : "Could not save the photo. Please try again.",
                            Toast.LENGTH_LONG
                    ).show();
                })
        );
    }

    @Override
    public void onPhotoClick(PhotoEntity photo) {
        if (photo == null) {
            return;
        }

        Intent intent = new Intent(
                PhotoListActivity.this,
                PhotoViewerActivity.class
        );

        intent.putExtra(PhotoViewerActivity.EXTRA_PHOTO_ID, photo.id);
        startActivity(intent);
    }
}
