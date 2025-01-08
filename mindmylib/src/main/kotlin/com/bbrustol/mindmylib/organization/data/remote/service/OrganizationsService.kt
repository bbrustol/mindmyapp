package com.bbrustol.mindmylib.organization.data.remote.service

import com.bbrustol.core.infrastructure.BuildConfig
import com.bbrustol.core.infrastructure.network.ApiHandler.handleApi
import com.bbrustol.core.infrastructure.network.ApiResult
import com.bbrustol.mindmylib.organization.data.remote.response.OrganizationsResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.path

class OrganizationsService(private val httpClient: HttpClient) {
    suspend fun getOrganizationsList(lastId: Int): ApiResult<OrganizationsResponse> =
        handleApi {
            httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = BuildConfig.BASE_URL
                    path(ORGANIZATIONS_ENDPOINT)
                    parameters.append(PARAM_PER_PAGE, PER_PAGE_SIZE.toString())
                    parameters.append(PARAM_SINCE, lastId.toString())
                }

                header(HttpHeaders.Authorization, "Bearer ${BuildConfig.API_TOKEN}")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }

    companion object {
        private const val PARAM_PER_PAGE = "per_page"
        private const val PARAM_SINCE = "since"
        private const val ORGANIZATIONS_ENDPOINT = "organizations"
        private const val PER_PAGE_SIZE = 50
    }
}