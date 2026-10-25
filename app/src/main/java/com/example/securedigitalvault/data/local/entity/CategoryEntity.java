package com.example.securedigitalvault.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * CategoryEntity
 *
 * Represents a category that vault items can be organized under
 * (e.g. "Work", "Personal", "Banking"). Category names and color tags
 * are considered non-sensitive metadata — they are never encrypted,
 * since they're needed for filtering/sorting without decrypting
 * anything.
 */
@Entity(tableName = "categories")
public class CategoryEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    // A hex color string (e.g. "#F4B942") used to tint the category
    // chip/label in the UI.
    public String colorTag;

    public CategoryEntity(String name, String colorTag) {
        this.name = name;
        this.colorTag = colorTag;
    }
}


