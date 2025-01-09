package com.bbrustol.mindmylib.organization.domain.model.mapper

import com.bbrustol.mindmylib.organization.data.local.entiies.FavoriteEntity
import com.bbrustol.mindmylib.organization.domain.model.OrganizationsItemDomainModel
import com.bbrustol.mindmylib.organization.data.remote.response.OrganizationsItemResponse

fun List<OrganizationsItemResponse>.toDomainModel(favorites: List<FavoriteEntity>) = this.map { it.toDomainModel(favorites) }

fun OrganizationsItemResponse.toDomainModel(favorites: List<FavoriteEntity>) = OrganizationsItemDomainModel(
    avatarUrl = avatarUrl ?: "",
    description = description ?: "",
    eventsUrl = eventsUrl ?: "",
    hooksUrl = hooksUrl ?: "",
    id = id,
    issuesUrl = issuesUrl ?: "",
    login = login,
    membersUrl = membersUrl ?: "",
    nodeId = nodeId,
    publicMembersUrl = publicMembersUrl ?: "",
    reposUrl = reposUrl ?: "",
    url = url ?: "",
    isFavorite = favorites.any { it.id == id }
)


fun List<FavoriteEntity>.toDomainModel() = this.map { it.toDomainModel() }

fun FavoriteEntity.toDomainModel() = OrganizationsItemDomainModel (
    avatarUrl = avatarUrl,
    description = "",
    eventsUrl = "",
    hooksUrl = "",
    id = id,
    issuesUrl = "",
    login = login,
    membersUrl = "",
    nodeId = "",
    publicMembersUrl = "",
    reposUrl = "",
    url = "",
    isFavorite = true
)
