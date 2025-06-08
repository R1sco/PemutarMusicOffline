package com.example.pemutarmusicoffline.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_songs")
data class FavoriteSong(
    @PrimaryKey
    val songId: Long,
    val title: String,
    val artist: String,
    val filePath: String,
    val dateAdded: Long = System.currentTimeMillis()
)
