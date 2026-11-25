package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_photos")
data class SavedPhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val savedUri: String,
    val originalUri: String?,
    val timestampMillis: Long,
    val formattedTimestamp: String,
    val locationText: String?,
    val gpsCoordsText: String?,
    val activityName: String?,
    val officerName: String?,
    val notes: String?,
    val width: Int,
    val height: Int,
    val createdAt: Long = System.currentTimeMillis()
)
