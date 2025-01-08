package com.bbrustol.feature.organizations.model.mapper

import com.bbrustol.feature.organizations.model.OrganizationsItemUiModel
import com.bbrustol.mindmylib.organization.domain.model.OrganizationsItemDomainModel

fun List<OrganizationsItemDomainModel>.toUiModel(lastIndex: Int) = this.mapIndexed { index, organizationsItemDomainModel ->
    organizationsItemDomainModel.toUiModel(index + lastIndex)
}

fun OrganizationsItemDomainModel.toUiModel(index: Int) = OrganizationsItemUiModel(
    avatarUrl = avatarUrl,
    description = description,
    eventsUrl = eventsUrl,
    hooksUrl = hooksUrl,
    id = id,
    issuesUrl = issuesUrl,
    login = login,
    membersUrl = membersUrl,
    nodeId = nodeId,
    publicMembersUrl = publicMembersUrl,
    reposUrl = reposUrl,
    url = url,
    isFavorite = isFavorite,
    index = index
)

fun OrganizationsItemUiModel.toDomainModel() = OrganizationsItemDomainModel(
    avatarUrl = avatarUrl,
    description = description,
    eventsUrl = eventsUrl,
    hooksUrl = hooksUrl,
    id = id,
    issuesUrl = issuesUrl,
    login = login,
    membersUrl = membersUrl,
    nodeId = nodeId,
    publicMembersUrl = publicMembersUrl,
    reposUrl = reposUrl,
    url = url,
    isFavorite = isFavorite
)
