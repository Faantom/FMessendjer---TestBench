package com.example.fantom.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.fantom.model.FavoriteMessage;

import java.util.List;

@Dao
public interface FavoriteMessageDao {
    @Query("SELECT * FROM favorite_messages ORDER BY timestamp DESC")
    LiveData<List<FavoriteMessage>> getAllFavorites();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteMessage favoriteMessage);

    @Delete
    void delete(FavoriteMessage favoriteMessage);

    @Query("DELETE FROM favorite_messages WHERE messageId = :messageId")
    void deleteByMessageId(String messageId);

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_messages WHERE messageId = :messageId)")
    boolean isFavorite(String messageId);
}
