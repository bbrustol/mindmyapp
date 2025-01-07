package com.bbrustol.feature.organizations.presentation

import androidx.lifecycle.viewModelScope
import com.bbrustol.core.infrastructure.BasePresenter
import com.bbrustol.core.infrastructure.network.ApiError
import com.bbrustol.core.infrastructure.network.ApiException
import com.bbrustol.core.infrastructure.network.ApiSuccess
import com.bbrustol.core.infrastructure.network.ServerStatusType
import com.bbrustol.feature.organizations.model.OrganizationsItemsUiModel
import com.bbrustol.feature.organizations.model.mapper.toUiModel
import com.bbrustol.mindmylib.data.organizations.domain.model.OrganizationsItemDomainModel
import com.bbrustol.feature.organizations.presentation.OrganizationsEvent.*
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.*
import com.bbrustol.feature.organizations.presentation.OrganizationsSideEffect.GotoDetails
import com.bbrustol.mindmylib.data.organizations.data.repository.OrganizationsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

internal sealed interface OrganizationsUiState {
    data object Idle : OrganizationsUiState
    data class OrganizationsList(
        val list: List<OrganizationsItemsUiModel>,
        val lastId: Int = 0,
        val isLoading: Boolean = false
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
    data class GetDetails(val uiModel: OrganizationsItemsUiModel) : OrganizationsEvent
}

internal sealed interface OrganizationsSideEffect {
    data class GotoDetails(val organizationsUiModel: OrganizationsItemsUiModel) :
        OrganizationsSideEffect
}

internal class OrganizationsPresenter(private val organizationsRepository: OrganizationsRepository) :
    BasePresenter<OrganizationsEvent, OrganizationsUiState, OrganizationsSideEffect>() {

    override fun setInitialState(): OrganizationsUiState = Idle

    override fun process(event: OrganizationsEvent) {
        when (event) {
            is GetDetails -> sendSideEffect { GotoDetails(event.uiModel) }
            is GetList -> viewModelScope.launch {
                val lastId = (uiState.value as? OrganizationsList)?.lastId ?: 0
                organizationsRepository.getOrganizationsList(lastId)
                    .onStart {
                        val currentState = (uiState.value as? OrganizationsList)
                        if (currentState != null) {
                            updateState {
                                OrganizationsList(
                                    list = currentState.list,
                                    lastId = currentState.lastId,
                                    isLoading = true,
                                )
                            }
                        }
                    }
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
    }

    private fun initList(result: List<OrganizationsItemDomainModel>) {
        updateState {
            OrganizationsList(
                list = result.toUiModel(),
                lastId = result.toUiModel().last().id,
                isLoading = false,
            )
        }
    }

    private fun updateList(result: List<OrganizationsItemDomainModel>) {
        val currentState = (uiState.value as? OrganizationsList)

        if (currentState != null) {
            updateState {
                OrganizationsList(
                    list = currentState.list + result.toUiModel(),
                    lastId = result.toUiModel().last().id,
                    isLoading = false,

                    )
            }
        }
    }
}