package com.example.pemutarmusicoffline.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteSongDao {
    @Query("SELECT * FROM favorite_songs ORDER BY dateAdded DESC")
    fun getAllFavoriteSongs(): Flow<List<FavoriteSong>>
    
    @Query("SELECT * FROM favorite_songs WHERE songId = :songId")
    suspend fun getFavoriteSongById(songId: Long): FavoriteSong?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteSong(favoriteSong: FavoriteSong)
    
    @Delete
    suspend fun deleteFavoriteSong(favoriteSong: FavoriteSong)
    
    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun deleteFavoriteSongById(songId: Long)
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE songId = :songId)")
    suspend fun isSongFavorite(songId: Long): Boolean
}
