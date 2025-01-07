package com.bbrustol.feature.organizations.model.mapper

import com.bbrustol.feature.organizations.model.OrganizationsItemsUiModel
import com.bbrustol.mindmylib.data.organizations.domain.model.OrganizationsItemDomainModel

fun List<OrganizationsItemDomainModel>.toUiModel() = this.map { it.toUiModel() }

fun OrganizationsItemDomainModel.toUiModel() = OrganizationsItemsUiModel(
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
    url = url
)

