package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPhotoDao {
    @Query("SELECT * FROM saved_photos ORDER BY createdAt DESC")
    fun getAllSavedPhotos(): Flow<List<SavedPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPhoto(photo: SavedPhotoEntity): Long

    @Query("DELETE FROM saved_photos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saved_photos")
    suspend fun deleteAll()
}
