package com.example.securedigitalvault.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.securedigitalvault.data.local.entity.PhotoEntity;

import java.util.List;

@Dao
public interface PhotoDao {

    @Insert
    long insert(PhotoEntity photo);

    @Delete
    void delete(PhotoEntity photo);

    @Query("SELECT * FROM photos ORDER BY date_added DESC")
    LiveData<List<PhotoEntity>> getAllPhotos();

    @Query("SELECT * FROM photos WHERE id = :id LIMIT 1")
    PhotoEntity getPhotoById(int id);

    @Query("SELECT COUNT(*) FROM photos")
    LiveData<Integer> getPhotoCountLive();
}


