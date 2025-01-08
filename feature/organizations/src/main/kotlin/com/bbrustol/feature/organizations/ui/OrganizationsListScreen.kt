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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.bbrustol.feature.organizations.model.OrganizationsItemUiModel
import com.bbrustol.feature.organizations.presentation.OrganizationsEvent
import com.bbrustol.feature.organizations.presentation.OrganizationsEvent.*
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.Idle
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.OrganizationList
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

        is OrganizationList -> CreateOrganizationsList(
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
    val uiState = (organizationsUiState as OrganizationList)

    Scaffold(
        modifier = modifier.padding(0.dp),
        topBar = {
            Column(modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues())) {
                SearchBar(
                    searchTerm = uiState.searchTerm,
                    onSearchTermChanged = { search ->
                        onEvent(FilterList(search))

                        CoroutineScope(Dispatchers.Main).launch {
                            listState.firstVisibleItemScrollOffset
                        }
                    },
                    message = if (uiState.list.firstOrNull { !it.isVisible }?.isVisible == true) {
                        stringResource(CoreUiR.string.search_not_found)
                    } else {
                        ""
                    },
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
                            with(uiState) {
                                val filteredList = list.filter { it.isVisible }
                                items(
                                    filteredList.size,
                                    key = { filteredList[it].id }) {
                                    CardOrganization(filteredList[it], onEvent)
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
    itemUiModel: OrganizationsItemUiModel,
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
                    contentScale = ContentScale.Fit
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

            IconButton(onClick = { onEvent(ToggleFavorite(itemUiModel)) }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(if (isFavorite) CoreUiR.string.speech_remove_from_favorites else CoreUiR.string.speech_add_to_favorites)
                )
            }
        }
    }
}

@Preview
@Composable
fun CardOrganizationPreview() {
    val mock = OrganizationsItemUiModel(
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
        url = "url",
        isFavorite = false,
        index = 0
    )

    CardOrganization(mock) {}
}
