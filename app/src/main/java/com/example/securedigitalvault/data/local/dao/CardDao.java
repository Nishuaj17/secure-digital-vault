package com.example.securedigitalvault.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.securedigitalvault.data.local.entity.CardEntity;

import java.util.List;

@Dao
public interface CardDao {

    @Insert
    long insert(CardEntity card);

    @Update
    void update(CardEntity card);

    @Delete
    void delete(CardEntity card);

    @Query("SELECT * FROM cards ORDER BY updated_at DESC")
    LiveData<List<CardEntity>> getAllCards();

    @Query("SELECT * FROM cards WHERE id = :id LIMIT 1")
    CardEntity getCardById(int id);

    @Query("SELECT COUNT(*) FROM cards")
    LiveData<Integer> getCardCountLive();

    @Query("UPDATE cards SET is_favorite = :isFavorite WHERE id = :id")
    void updateFavoriteStatus(int id, boolean isFavorite);
}


