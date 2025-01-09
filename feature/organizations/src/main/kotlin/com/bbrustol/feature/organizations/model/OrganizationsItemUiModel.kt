package com.bbrustol.feature.organizations.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class OrganizationsItemUiModel(
    val avatarUrl: String,
    val eventsUrl: String,
    val id: Int,
    val login: String,
    val isFavorite: Boolean,
    val index: Int,
    val isVisible: Boolean = true,
)