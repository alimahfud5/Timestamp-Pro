package com.example.data.local

import kotlinx.coroutines.flow.Flow

class SavedPhotoRepository(private val dao: SavedPhotoDao) {
    val allPhotos: Flow<List<SavedPhotoEntity>> = dao.getAllSavedPhotos()

    suspend fun insert(photo: SavedPhotoEntity): Long = dao.insertSavedPhoto(photo)

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.deleteAll()
}
