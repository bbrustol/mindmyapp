package com.bbrustol.feature.organizations.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class OrganizationsItemUiModel(
    val avatarUrl: String,
    val description: String,
    val eventsUrl: String,
    val hooksUrl: String,
    val id: Int,
    val issuesUrl: String,
    val login: String,
    val membersUrl: String,
    val nodeId: String,
    val publicMembersUrl: String,
    val reposUrl: String,
    val url: String,
    val isFavorite: Boolean,
    val index: Int,
    val isVisible: Boolean = true,
)