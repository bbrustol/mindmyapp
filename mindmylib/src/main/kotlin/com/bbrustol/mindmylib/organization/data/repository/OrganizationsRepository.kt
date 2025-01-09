package com.bbrustol.mindmylib.organization.data.repository

import com.bbrustol.core.infrastructure.network.ApiError
import com.bbrustol.core.infrastructure.network.ApiException
import com.bbrustol.core.infrastructure.network.ApiResult
import com.bbrustol.core.infrastructure.network.ApiSuccess
import com.bbrustol.mindmylib.organization.data.local.dao.FavoriteDao
import com.bbrustol.mindmylib.organization.data.local.entiies.FavoriteEntity
import com.bbrustol.mindmylib.organization.data.remote.service.OrganizationsService
import com.bbrustol.mindmylib.organization.domain.model.OrganizationsItemDomainModel
import com.bbrustol.mindmylib.organization.domain.model.mapper.toDomainModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class OrganizationsRepository(
    private val organizationsService: OrganizationsService,
    private val favoriteDao: FavoriteDao,
    private val dispatcher: CoroutineDispatcher
) {
    fun getOrganizationsList(lastId: Int): Flow<ApiResult<List<OrganizationsItemDomainModel>>> {
        return flow {
            when (val listResult = organizationsService.getOrganizationsList(lastId)) {
                is ApiSuccess -> {
                    emit(ApiSuccess(data = listResult.data.toDomainModel(getFavorites())))
                }

                is ApiError -> emit(
                    ApiError(listResult.code, listResult.message, listResult.serviceStatusType)
                )

                is ApiException -> emit(
                    ApiException(listResult.throwable, listResult.serviceStatusType)
                )
            }
        }.flowOn(dispatcher)
    }

    //region favorites
    suspend fun addFavorite(item: OrganizationsItemDomainModel) {
        withContext(dispatcher) {
            favoriteDao.insertFavorite(FavoriteEntity(item.id, item.login, item.avatarUrl))
        }
    }

    suspend fun removeFavorite(id: Int) {
        withContext(dispatcher) {
            favoriteDao.getFavoriteById(id).let { entity ->
                entity?.let { favoriteDao.deleteFavorite(it) }
            }
        }
    }

    private fun getFavorites(): List<FavoriteEntity> {
        return favoriteDao.getAllFavorites()
    }

    fun getFavoritesDomainModel(): Flow<List<OrganizationsItemDomainModel>> {
        return flow { emit(getFavorites().toDomainModel()) }.flowOn(dispatcher)
    }
    //endregion
}
