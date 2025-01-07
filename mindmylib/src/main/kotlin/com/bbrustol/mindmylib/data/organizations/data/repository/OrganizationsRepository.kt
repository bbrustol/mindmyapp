package com.bbrustol.mindmylib.data.organizations.data.repository

import com.bbrustol.core.infrastructure.network.ApiError
import com.bbrustol.core.infrastructure.network.ApiException
import com.bbrustol.core.infrastructure.network.ApiResult
import com.bbrustol.core.infrastructure.network.ApiSuccess
import com.bbrustol.core.infrastructure.network.WithoutInternet
import com.bbrustol.mindmylib.data.organizations.data.response.OrganizationsResponse
import com.bbrustol.mindmylib.data.organizations.data.service.OrganizationsService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class OrganizationsRepository(
    private val organizationsService: OrganizationsService,
    private val dispatcher: CoroutineDispatcher
) {
    fun getOrganizationsList(lastId: Int): Flow<ApiResult<OrganizationsResponse>> {
        return flow {
            when (val listResult = organizationsService.getOrganizationsList(lastId)) {
                is ApiSuccess -> emit(listResult)

                is ApiError -> emit(
                    ApiError(listResult.code, listResult.message, listResult.serviceStatusType)
                )

                is ApiException -> emit(
                    ApiException(listResult.throwable, listResult.serviceStatusType)
                )

                is WithoutInternet -> emit(WithoutInternet(listResult.serviceStatusType))

            }
        }.flowOn(dispatcher)
    }
}
