
package com.example.securedigitalvault.ui.photos;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.repository.PhotoRepository;
import com.example.securedigitalvault.util.SecurityUtils;

public class PhotoViewerActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO_ID = "photo_id";

    private static final String TAG = "PhotoViewerActivity";

    private PhotoRepository photoRepository;
    private ImageView fullPhotoView;
    private int photoId = -1;
    private boolean activityClosed = false;
    private Bitmap displayedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SecurityUtils.applySecureFlag(this);
        setContentView(R.layout.activity_photo_viewer);

        photoRepository = new PhotoRepository(getApplicationContext());

        fullPhotoView = findViewById(R.id.fullPhotoView);
        photoId = getIntent().getIntExtra(EXTRA_PHOTO_ID, -1);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.deleteButton).setOnClickListener(v -> confirmDelete());

        if (photoId < 0) {
            Toast.makeText(
                    this,
                    "Invalid photo selected.",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        loadPhoto();
    }

    private void loadPhoto() {
        photoRepository.decryptPhotoByIdAsync(photoId, decrypted -> {
            if (activityClosed || isFinishing() || isDestroyed()) {
                return;
            }

            if (decrypted == null
                    || decrypted.imageBytes == null
                    || decrypted.imageBytes.length == 0) {
                Toast.makeText(
                        PhotoViewerActivity.this,
                        "Could not decrypt or load this photo.",
                        Toast.LENGTH_SHORT
                ).show();
                finish();
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    decrypted.imageBytes,
                    0,
                    decrypted.imageBytes.length
            );

            if (bitmap == null) {
                Toast.makeText(
                        PhotoViewerActivity.this,
                        "The photo format could not be displayed.",
                        Toast.LENGTH_SHORT
                ).show();
                finish();
                return;
            }

            displayedBitmap = bitmap;
            fullPhotoView.setImageBitmap(displayedBitmap);
            fullPhotoView.setVisibility(View.VISIBLE);
        });
    }

    private void confirmDelete() {
        if (photoId < 0) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete this photo?")
                .setMessage(
                        "This will permanently delete the photo. This cannot be undone."
                )
                .setPositiveButton("Delete", (dialog, which) -> {
                    photoRepository.deletePhoto(photoId);

                    Toast.makeText(
                            this,
                            "Photo deletion requested.",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        activityClosed = true;

        if (fullPhotoView != null) {
            fullPhotoView.setImageDrawable(null);
        }

        if (displayedBitmap != null && !displayedBitmap.isRecycled()) {
            displayedBitmap.recycle();
            displayedBitmap = null;
        }

        super.onDestroy();
    }
}
