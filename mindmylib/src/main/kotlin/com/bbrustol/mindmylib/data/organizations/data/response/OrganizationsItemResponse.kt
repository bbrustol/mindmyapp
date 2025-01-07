package com.bbrustol.mindmylib.data.organizations.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganizationsItemResponse(
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("events_url")
    val eventsUrl: String? = null,
    @SerialName("hooks_url")
    val hooksUrl: String? = null,
    @SerialName("id")
    val id: Int,
    @SerialName("issues_url")
    val issuesUrl: String? = null,
    @SerialName("login")
    val login: String,
    @SerialName("members_url")
    val membersUrl: String? = null,
    @SerialName("node_id")
    val nodeId: String,
    @SerialName("public_members_url")
    val publicMembersUrl: String? = null,
    @SerialName("repos_url")
    val reposUrl: String? = null,
    @SerialName("url")
    val url: String? = null
)