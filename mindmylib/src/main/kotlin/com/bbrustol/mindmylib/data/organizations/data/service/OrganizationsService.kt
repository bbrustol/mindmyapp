package com.bbrustol.mindmylib.data.organizations.data.service

import com.bbrustol.core.infrastructure.network.ApiHandler.handleApi
import com.bbrustol.core.infrastructure.network.ApiResult
import com.bbrustol.mindmylib.data.organizations.data.response.OrganizationsResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.path

class OrganizationsService(
    private val httpClient: HttpClient
) {
    suspend fun getOrganizationsList(lastId: Int): ApiResult<OrganizationsResponse> =
        handleApi {
            httpClient.get {
                url {
                    path(ORGANIZATIONS_ENDPOINT)
                    parameters.append(PARAM_PER_PAGE, PER_PAGE_SIZE.toString())
                    parameters.append(PARAM_SINCE, lastId.toString())
                }
            }
        }

    companion object {
        private const val PARAM_PER_PAGE = "per_page"
        private const val PARAM_SINCE = "since"
        private const val ORGANIZATIONS_ENDPOINT = "organizations/"
        private const val PER_PAGE_SIZE = 50
    }
}
