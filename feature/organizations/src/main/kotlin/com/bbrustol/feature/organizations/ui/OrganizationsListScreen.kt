package com.bbrustol.feature.organizations.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbrustol.core.ui.compose.ErrorScreen
import com.bbrustol.core.ui.compose.ExceptionScreen
import com.bbrustol.core.ui.compose.SearchBar
import com.bbrustol.core.ui.compose.WithoutInternetScreen
import com.bbrustol.core.ui.utils.InversePullToRefreshBox
import com.bbrustol.core.ui.utils.LoadImage
import com.bbrustol.feature.organizations.model.OrganizationsItemsUiModel
import com.bbrustol.feature.organizations.presentation.OrganizationsEvent
import com.bbrustol.feature.organizations.presentation.OrganizationsEvent.GetDetails
import com.bbrustol.feature.organizations.presentation.OrganizationsEvent.GetList
import com.bbrustol.feature.organizations.presentation.OrganizationsEvent.SortListBy
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.Idle
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.OrganizationsList
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.ShowError
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.ShowException
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.ShowMissingToken
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.ShowNoInternet
import com.bbrustol.feature.organizations.presentation.SortType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.bbrustol.core.ui.R as CoreUiR

@Composable
internal fun OrganizationsListScreen(
    uiState: OrganizationsUiState,
    onEvent: (OrganizationsEvent) -> Unit,
) {


    when (uiState) {
        Idle -> { /*Do nothing*/
        }

        is OrganizationsList -> CreateOrganizationsList(
            organizationsUiState = uiState,
            onEvent = onEvent
        )

        is ShowError -> ErrorScreen(
            code = uiState.code,
            errorMessage = uiState.message ?: ""
        ) { onEvent(GetList) }

        is ShowException -> ExceptionScreen(
            errorMessage = uiState.throwable?.message ?: ""
        ) { onEvent(GetList) }

        ShowMissingToken -> ExceptionScreen(
            errorMessage = stringResource(CoreUiR.string.missing_token),
            isShowButton = false
        ) { }

        ShowNoInternet -> WithoutInternetScreen { onEvent(GetList) }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateOrganizationsList(
    modifier: Modifier = Modifier,
    organizationsUiState: OrganizationsUiState,
    onEvent: (OrganizationsEvent) -> Unit,
) {
    val refreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()
    val uiState = (organizationsUiState as OrganizationsList)

    var searchTerm by remember { mutableStateOf("") }
    var filteredItems by remember { mutableStateOf(emptyList<OrganizationsItemsUiModel>()) }

    Scaffold(
        modifier = modifier.padding(0.dp),
        topBar = {
            Column(modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues())) {
                SearchBar(
                    searchTerm = searchTerm,
                    onSearchTermChanged = { search ->
                        searchTerm = search

                        CoroutineScope(Dispatchers.Main).launch {
                            listState.scrollToItem(0, 0)
                        }

                        filteredItems = uiState.list.filter { item ->
                            item.login.contains(
                                search,
                                ignoreCase = true
                            ) || item.id.toString()
                                .contains(searchTerm)
                        }
                    },
                    message = if (filteredItems.isEmpty()) {
                        stringResource(CoreUiR.string.search_not_found)
                    } else {
                        ""
                    }
                )
            }
        },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                with(uiState) {
                    InversePullToRefreshBox(
                        modifier = Modifier.fillMaxSize(),
                        state = refreshState,
                        isRefreshing = isLoading,
                        onRefresh = {
                            if (!isLoading) {
                                onEvent(GetList)
                            }
                        }
                    ) {
                        LazyColumn(state = listState) {
                            if (filteredItems.isEmpty()) {
                                items(list.size, key = { list[it].id }) {
                                    CardOrganization(list[it], onEvent)
                                }
                            } else {
                                items(filteredItems.size, key = { filteredItems[it].id }) {
                                    CardOrganization(filteredItems[it], onEvent)
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(
                        top = 4.dp,
                        bottom = WindowInsets.statusBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                    )

            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp, end = 1.dp),
                    onClick = { /*TODO*/ },
                    shape = RoundedCornerShape(size = 4.dp),
                ) {
                    Text(text = stringResource(CoreUiR.string.button_try_again))
                }
                var sortedByStr by remember { mutableIntStateOf(CoreUiR.string.button_sort_id) }
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp, start = 1.dp),
                    shape = RoundedCornerShape(size = 4.dp),
                    onClick = {

                        when (uiState.sortType) {
                            SortType.Id -> {
                                sortedByStr = CoreUiR.string.button_sort_login
                                onEvent(SortListBy(SortType.Login))
                            }
                            SortType.Login -> {
                                sortedByStr = CoreUiR.string.button_sort_id
                                onEvent(SortListBy(SortType.Id))
                            }
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            listState.firstVisibleItemScrollOffset
                        }

                    }
                ) {
                    Text(
                        text = stringResource(
                            CoreUiR.string.button_sort_by,
                            stringResource(sortedByStr)
                        )
                    )
                }
            }
        }
    )
}

@Composable
private fun CardOrganization(
    itemUiModel: OrganizationsItemsUiModel,
    onEvent: (OrganizationsEvent) -> Unit
) {

    Card(
        modifier = Modifier
            .padding(8.dp, 4.dp, 8.dp, 0.dp)
            .clickable {
                onEvent(GetDetails(itemUiModel))
            },
        shape = RoundedCornerShape(size = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        with(itemUiModel) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                LoadImage(
                    imageUrl = avatarUrl,
                    contentDescription = null,
                    placeholder = CoreUiR.drawable.octocat,
                    modifier = Modifier
                        .size(80.dp)
                        .weight(.3f),
                    contentScale = ContentScale.FillBounds
                )
                Column(
                    modifier = Modifier
                        .padding(4.dp, 0.dp)
                        .align(Alignment.CenterVertically)
                        .weight(.7f)
                ) {
                    Text(
                        modifier = Modifier,
                        text = id.toString(),
                        color = colorScheme.onPrimaryContainer
                    )

                    Text(
                        modifier = Modifier,
                        text = login,
                        color = colorScheme.onPrimaryContainer
                    )
                }

            }
        }


    }


}

@Preview
@Composable
fun CardOrganizationPreview() {
    val mock = OrganizationsItemsUiModel(
        avatarUrl = "avatarUrl",
        description = "description",
        eventsUrl = "eventsUrl",
        hooksUrl = "hooksUrl",
        id = 0,
        issuesUrl = "issuesUrl",
        login = "login",
        membersUrl = "membersUrl",
        nodeId = "nodeId",
        publicMembersUrl = "publicMembersUrl",
        reposUrl = "reposUrl",
        url = "url"
    )

    CardOrganization(mock) {}
}
