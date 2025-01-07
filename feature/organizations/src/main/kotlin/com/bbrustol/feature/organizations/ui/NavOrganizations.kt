package com.bbrustol.feature.organizations.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bbrustol.feature.organizations.presentation.OrganizationsEvent.GetList
import com.bbrustol.feature.organizations.presentation.OrganizationsPresenter
import com.bbrustol.feature.organizations.presentation.OrganizationsSideEffect.GotoDetails
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavOrganizations() {

    val navController = rememberNavController()

    val organizationsPresenter: OrganizationsPresenter = koinViewModel()
    val uiState: OrganizationsUiState by organizationsPresenter.uiState.collectAsState()

    organizationsPresenter.dispatch(GetList)

    NavHost(
        navController = navController,
        startDestination = NavOrganizationsList
    ) {
        composable<NavOrganizationsList> {
            OrganizationsListScreen(
                uiState = uiState,
                onEvent = { event -> organizationsPresenter.dispatch(event) })
        }

//        composable<NavOrganizationDetails> {
//            val args = it.toRoute<NavOrganizationDetails>()
//        }
    }

    LaunchedEffect(Unit) {
        organizationsPresenter.sideEffect.collect {
            when (it) {
                is GotoDetails -> { } //navController.navigate(NavOrganizationDetails(it.organizationsUiModel))
            }
        }
    }
}

@Serializable
data object NavOrganizationsList

//@Serializable
//data class NavOrganizationDetails(private val organizationItemUiModel: OrganizationsItemsUiModel)