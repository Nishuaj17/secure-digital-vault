package com.example.securedigitalvault.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.securedigitalvault.data.local.entity.CategoryEntity;

import java.util.List;

/**
 * CategoryDao
 *
 * Handles all database operations for categories. Categories contain
 * no sensitive data, so these queries operate on plain columns only.
 */
@Dao
public interface CategoryDao {

    @Insert
    long insert(CategoryEntity category);

    @Delete
    void delete(CategoryEntity category);

    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<CategoryEntity>> getAllCategories();

    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCount();
}

