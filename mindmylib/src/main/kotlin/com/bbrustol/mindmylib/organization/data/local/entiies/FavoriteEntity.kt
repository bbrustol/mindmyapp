package com.bbrustol.mindmylib.organization.data.local.entiies

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: Int,
    val login: String,
    val avatarUrl: String
)