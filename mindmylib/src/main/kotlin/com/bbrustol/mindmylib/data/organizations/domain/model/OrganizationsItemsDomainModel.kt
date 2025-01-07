package com.bbrustol.mindmylib.data.organizations.domain.model

data class OrganizationsItemDomainModel(
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
    val url: String
)