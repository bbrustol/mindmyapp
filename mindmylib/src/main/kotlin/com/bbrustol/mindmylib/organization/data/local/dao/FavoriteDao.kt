package com.bbrustol.mindmylib.organization.data.local.dao

import androidx.room.*
import com.bbrustol.mindmylib.organization.data.local.entiies.FavoriteEntity

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): List<FavoriteEntity>

    @Query("SELECT * FROM favorites WHERE id = :id")
    fun getFavoriteById(id: Int): FavoriteEntity?
}