package com.bbrustol.mindmylib.data.organizations.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganizationsItemResponse(
    @SerialName("avatar_url")
    val avatarUrl: String,
    @SerialName("description")
    val description: String,
    @SerialName("events_url")
    val eventsUrl: String,
    @SerialName("hooks_url")
    val hooksUrl: String,
    @SerialName("id")
    val id: Int,
    @SerialName("issues_url")
    val issuesUrl: String,
    @SerialName("login")
    val login: String,
    @SerialName("members_url")
    val membersUrl: String,
    @SerialName("node_id")
    val nodeId: String,
    @SerialName("public_members_url")
    val publicMembersUrl: String,
    @SerialName("repos_url")
    val reposUrl: String,
    @SerialName("url")
    val url: String
)