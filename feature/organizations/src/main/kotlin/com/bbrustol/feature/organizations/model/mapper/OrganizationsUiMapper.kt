package com.bbrustol.feature.organizations.model.mapper

import com.bbrustol.feature.organizations.model.OrganizationsItemUiModel
import com.bbrustol.mindmylib.organization.domain.model.OrganizationsItemDomainModel

fun List<OrganizationsItemDomainModel>.toUiModel(lastIndex: Int) = this.mapIndexed { index, organizationsItemDomainModel ->
    organizationsItemDomainModel.toUiModel(index + lastIndex)
}

fun OrganizationsItemDomainModel.toUiModel(index: Int) = OrganizationsItemUiModel(
    avatarUrl = avatarUrl,
    eventsUrl = eventsUrl,
    id = id,
    login = login,
    isFavorite = isFavorite,
    index = index
)

fun OrganizationsItemUiModel.toDomainModel() = OrganizationsItemDomainModel(
    avatarUrl = avatarUrl,
    description = "",
    eventsUrl = eventsUrl,
    hooksUrl = "",
    id = id,
    issuesUrl = "",
    login = login,
    membersUrl = "",
    nodeId = "",
    publicMembersUrl = "",
    reposUrl = "",
    url = "",
    isFavorite = isFavorite
)
