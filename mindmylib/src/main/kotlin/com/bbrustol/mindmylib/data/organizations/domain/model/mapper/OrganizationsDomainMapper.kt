package com.bbrustol.mindmylib.data.organizations.domain.model.mapper

import com.bbrustol.mindmylib.data.organizations.domain.model.OrganizationsItemDomainModel
import com.bbrustol.mindmylib.data.organizations.data.response.OrganizationsItemResponse

fun List<OrganizationsItemResponse>.toDomainModel() = this.map { it.toDomainModel() }

fun OrganizationsItemResponse.toDomainModel() = OrganizationsItemDomainModel(
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

