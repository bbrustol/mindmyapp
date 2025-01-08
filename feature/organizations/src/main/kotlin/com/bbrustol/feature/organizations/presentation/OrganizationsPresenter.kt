package com.bbrustol.feature.organizations.presentation

import androidx.lifecycle.viewModelScope
import com.bbrustol.core.infrastructure.BasePresenter
import com.bbrustol.core.infrastructure.network.ApiError
import com.bbrustol.core.infrastructure.network.ApiException
import com.bbrustol.core.infrastructure.network.ApiSuccess
import com.bbrustol.core.infrastructure.network.ServerStatusType
import com.bbrustol.feature.organizations.model.OrganizationsItemUiModel
import com.bbrustol.feature.organizations.model.mapper.toDomainModel
import com.bbrustol.feature.organizations.model.mapper.toUiModel
import com.bbrustol.mindmylib.organization.domain.model.OrganizationsItemDomainModel
import com.bbrustol.feature.organizations.presentation.OrganizationsEvent.*
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.*
import com.bbrustol.feature.organizations.presentation.OrganizationsSideEffect.GotoDetails
import com.bbrustol.mindmylib.organization.data.repository.OrganizationsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

internal sealed interface OrganizationsUiState {
    data object Idle : OrganizationsUiState
    data class OrganizationList(
        val list: List<OrganizationsItemUiModel>,
        val lastId: Int = 0,
        val isLoading: Boolean = false,
        val sortType: SortType = SortType.Id,
    ) : OrganizationsUiState

    data class ShowError(
        val code: Int,
        val message: String?,
    ) : OrganizationsUiState

    data class ShowException(
        val throwable: Throwable?,
    ) : OrganizationsUiState

    data object ShowNoInternet : OrganizationsUiState
    data object ShowMissingToken : OrganizationsUiState
}

internal sealed interface OrganizationsEvent {
    data object GetList : OrganizationsEvent
    data class GetDetails(val uiModel: OrganizationsItemUiModel) : OrganizationsEvent
    data class UpdateSearchTerm(val uiModel: List<OrganizationsItemUiModel>) : OrganizationsEvent
    data class SortListBy(val sortType: SortType) : OrganizationsEvent
    data class ToggleFavorite(val item: OrganizationsItemUiModel) :
        OrganizationsEvent
}

internal sealed interface OrganizationsSideEffect {
    data class GotoDetails(val organizationsUiModel: OrganizationsItemUiModel) :
        OrganizationsSideEffect
}

internal class OrganizationsPresenter(private val organizationsRepository: OrganizationsRepository) :
    BasePresenter<OrganizationsEvent, OrganizationsUiState, OrganizationsSideEffect>() {

    override fun setInitialState(): OrganizationsUiState = Idle

    override fun process(event: OrganizationsEvent) {
        when (event) {
            is GetDetails -> sendSideEffect { GotoDetails(event.uiModel) }
            is GetList -> getOrganizationList()
            is UpdateSearchTerm -> updateSearchTerm(event.uiModel)
            is SortListBy -> sortList(
                event.sortType,
                OrganizationList((uiState.value as? OrganizationList)?.list ?: emptyList())
            )

            is ToggleFavorite -> toggleFavorite(event.item)

        }
    }

    private fun getOrganizationList() {
        viewModelScope.launch {
            val lastId = (uiState.value as? OrganizationList)?.lastId ?: 0
            organizationsRepository.getOrganizationsList(lastId)
                .onStart { setLoading() }
                .catch {
                    updateState {
                        ShowException(
                            throwable = it.cause
                        )
                    }
                }
                .collect { result ->
                    when (result) {
                        is ApiError -> updateState {
                            ShowError(
                                result.code,
                                result.message
                            )
                        }

                        is ApiException -> updateState {
                            when (result.serviceStatusType) {
                                ServerStatusType.ServiceUnavailable -> ShowNoInternet
                                ServerStatusType.NoToken -> ShowMissingToken
                                else -> ShowException(
                                    result.throwable
                                )
                            }
                        }

                        is ApiSuccess -> if (lastId == 0) initList(
                            result.data
                        ) else updateList(result.data)
                    }
                }
        }
    }

    private fun setLoading() {
        val currentState = (uiState.value as? OrganizationList)
        currentState?.let {
            sortList(
                organizationList = OrganizationList(
                    list = it.list,
                    isLoading = true
                )
            )
        }
    }

    private fun initList(result: List<OrganizationsItemDomainModel>) {
        sortList(
            organizationList = OrganizationList(
                list = result.toUiModel(0),
                lastId = result.toUiModel(0).last().id,
                isLoading = false
            )
        )
    }

    private fun updateList(result: List<OrganizationsItemDomainModel>) {
        val currentState = (uiState.value as? OrganizationList)

        currentState?.let {
            sortList(
                organizationList = OrganizationList(
                    list = it.list + result.toUiModel(currentState.list.size),
                    lastId = result.toUiModel(currentState.list.size).last().id,
                    isLoading = false,
                )
            )
        }
    }

    private fun updateSearchTerm(filteredItems: List<OrganizationsItemUiModel>) {
        sortList(organizationList = OrganizationList(list = filteredItems))
    }

    private fun sortList(sortType: SortType? = null, organizationList: OrganizationList) {
        updateState {
            if (sortType == null) {
                organizationList
            } else {
                organizationList.copy(
                    list = when (sortType) {
                        SortType.Login -> organizationList.list.sortedBy { it.login }
                        SortType.Id -> organizationList.list.sortedBy { it.id }
                    },
                    sortType = sortType
                )
            }
        }
    }

    private fun toggleFavorite(item: OrganizationsItemUiModel) {
        viewModelScope.launch {
            val currentState = (uiState.value as OrganizationList)
            val listUpdated = currentState.let { orgList ->
                orgList.list.map { uiModel ->
                    if (uiModel.id == item.id) {
                        uiModel.copy(isFavorite = !uiModel.isFavorite)
                    } else uiModel
                }
            }

            if (listUpdated[item.index].isFavorite) {
                organizationsRepository.addFavorite(item.toDomainModel())
            } else {
                organizationsRepository.removeFavorite(item.id)
            }

            sortList(organizationList = OrganizationList(list = listUpdated))
        }
    }
}