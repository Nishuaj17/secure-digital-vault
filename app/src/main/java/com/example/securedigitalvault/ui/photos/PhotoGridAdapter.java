
package com.example.securedigitalvault.ui.photos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.local.entity.PhotoEntity;
import com.example.securedigitalvault.data.repository.PhotoRepository;

import java.util.ArrayList;
import java.util.List;

public class PhotoGridAdapter
        extends RecyclerView.Adapter<PhotoGridAdapter.PhotoViewHolder> {

    private static final String TAG = "PhotoGridAdapter";

    public interface OnPhotoClickListener {
        void onPhotoClick(PhotoEntity photo);
    }

    private final PhotoRepository photoRepository;
    private final OnPhotoClickListener clickListener;

    private List<PhotoEntity> photoList = new ArrayList<>();

    public PhotoGridAdapter(
            PhotoRepository photoRepository,
            OnPhotoClickListener clickListener
    ) {
        this.photoRepository = photoRepository;
        this.clickListener = clickListener;
        setHasStableIds(true);
    }

    public void submitList(List<PhotoEntity> newList) {
        photoList = newList == null
                ? new ArrayList<>()
                : new ArrayList<>(newList);

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return photoList.get(position).id;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_grid, parent, false);

        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PhotoViewHolder holder,
            int position
    ) {
        PhotoEntity photo = photoList.get(position);
        int photoId = photo.id;

        // Mark which photo this ImageView is currently displaying.
        holder.boundPhotoId = photoId;

        // Clear any image left by a previously recycled item.
        holder.thumbnail.setImageDrawable(null);
        holder.thumbnail.setContentDescription(photo.displayName);

        holder.itemView.setOnClickListener(
                view -> clickListener.onPhotoClick(photo)
        );

        // Decrypt the selected photo asynchronously.
        photoRepository.decryptPhotoByIdAsync(photoId, decrypted -> {
            // Ignore the result if this view now belongs to another photo.
            if (holder.boundPhotoId != photoId) {
                return;
            }

            if (decrypted == null
                    || decrypted.imageBytes == null
                    || decrypted.imageBytes.length == 0) {
                Log.w(TAG, "Could not load thumbnail for photo ID " + photoId);
                return;
            }

            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        decrypted.imageBytes,
                        0,
                        decrypted.imageBytes.length
                );

                // Check again before displaying the decoded bitmap.
                if (holder.boundPhotoId != photoId) {
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                    return;
                }

                if (bitmap != null) {
                    holder.thumbnail.setImageBitmap(bitmap);
                } else {
                    Log.w(TAG, "Unable to decode photo ID " + photoId);
                }

            } catch (OutOfMemoryError error) {
                // Avoid crashing the app if a very large image cannot fit
                // into available memory.
                Log.e(TAG, "Not enough memory to display photo " + photoId, error);
                holder.thumbnail.setImageDrawable(null);
            } catch (Exception error) {
                Log.e(TAG, "Failed to display photo " + photoId, error);
                holder.thumbnail.setImageDrawable(null);
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull PhotoViewHolder holder) {
        holder.boundPhotoId = RecyclerView.NO_ID;
        holder.thumbnail.setImageDrawable(null);
        holder.thumbnail.setContentDescription(null);

        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {

        final ImageView thumbnail;
        long boundPhotoId = RecyclerView.NO_ID;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.photoThumbnail);
        }
    }
}
