package com.bbrustol.mindmylib.data.organizations.data.repository

import com.bbrustol.core.infrastructure.network.ApiError
import com.bbrustol.core.infrastructure.network.ApiException
import com.bbrustol.core.infrastructure.network.ApiResult
import com.bbrustol.core.infrastructure.network.ApiSuccess
import com.bbrustol.mindmylib.data.organizations.data.service.OrganizationsService
import com.bbrustol.mindmylib.data.organizations.domain.model.OrganizationsItemDomainModel
import com.bbrustol.mindmylib.data.organizations.domain.model.mapper.toDomainModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class OrganizationsRepository(
    private val organizationsService: OrganizationsService,
    private val dispatcher: CoroutineDispatcher
) {
    fun getOrganizationsList(lastId: Int): Flow<ApiResult<List<OrganizationsItemDomainModel>>> {
        return flow {
            when (val listResult = organizationsService.getOrganizationsList(lastId)) {
                is ApiSuccess -> emit(ApiSuccess(listResult.data.toDomainModel()))

                is ApiError -> emit(
                    ApiError(listResult.code, listResult.message, listResult.serviceStatusType)
                )

                is ApiException -> emit(
                    ApiException(listResult.throwable, listResult.serviceStatusType)
                )
            }
        }.flowOn(dispatcher)
    }
}
