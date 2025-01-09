package com.bbrustol.feature.organizations

import com.bbrustol.core.infrastructure.network.ApiError
import com.bbrustol.core.infrastructure.network.ApiException
import com.bbrustol.core.infrastructure.network.ApiSuccess
import com.bbrustol.core.infrastructure.network.ResourceUtils
import com.bbrustol.core.infrastructure.network.ServerStatusType
import com.bbrustol.core.infrastructure.network.ServerStatusType.ServerError
import com.bbrustol.feature.organizations.model.mapper.toDomainModel
import com.bbrustol.feature.organizations.model.mapper.toUiModel
import com.bbrustol.feature.organizations.presentation.OrganizationsEvent.FilterList
import com.bbrustol.feature.organizations.presentation.OrganizationsEvent.GetFavoriteList
import com.bbrustol.feature.organizations.presentation.OrganizationsEvent.GetList
import com.bbrustol.feature.organizations.presentation.OrganizationsEvent.ToggleFavorite
import com.bbrustol.feature.organizations.presentation.OrganizationsPresenter
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.FavoriteList
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.OrganizationList
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.ShowError
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.ShowException
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.ShowMissingToken
import com.bbrustol.feature.organizations.presentation.OrganizationsUiState.ShowNoInternet
import com.bbrustol.mindmylib.organization.data.remote.response.OrganizationsResponse
import com.bbrustol.mindmylib.organization.data.repository.OrganizationsRepository
import com.bbrustol.mindmylib.organization.domain.model.mapper.toDomainModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrganizationsPresenterTest {

    private lateinit var presenter: OrganizationsPresenter
    private val mockRepository: OrganizationsRepository = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    private val jsonParser = Json { ignoreUnknownKeys = true }


    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        presenter = spyk(OrganizationsPresenter(mockRepository))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN GetList event, WHEN repository returns data, THEN state should be OrganizationList`() =
        runTest {
            // GIVEN
            val organizations = getOrganizationsMock().toDomainModel()
            coEvery { mockRepository.getOrganizationsList(any()) } returns flowOf(
                ApiSuccess(
                    organizations
                )
            )

            // WHEN
            presenter.dispatch(GetList)
            advanceUntilIdle()

            // THEN
            val expectedState =
                OrganizationList(list = organizations.map { it.toUiModel(0) }, lastId = 1)
            assertEquals(expectedState, presenter.uiState.value)
        }

    @Test
    fun `GIVEN GetFavoriteList event, WHEN repository returns favorites, THEN state should be FavoriteList`() =
        runTest {
            // GIVEN
            val favorites = getOrganizationsMock().toDomainModel()
            coEvery { mockRepository.getFavoritesDomainModel() } returns flowOf(favorites)

            // WHEN
            presenter.dispatch(GetFavoriteList)
            advanceUntilIdle()

            // THEN
            val expectedState = FavoriteList(list = favorites.map { it.toUiModel(0) })
            assertEquals(expectedState, presenter.uiState.value)
        }

    @Test
    fun `GIVEN ToggleFavorite event, WHEN item is toggled, THEN repository should update favorites`() =
        runTest {
            // GIVEN
            val items = getOrganizationsMock().toDomainModel().toUiModel(0)

            presenter.updateState { OrganizationList(list = items) }

            // WHEN
            presenter.dispatch(ToggleFavorite(items.first()))
            advanceUntilIdle()

            // THEN
            coVerify { mockRepository.addFavorite(items.first().toDomainModel()) }
        }

    @Test
    fun `GIVEN ToggleFavorite event, WHEN item is toggled off, THEN repository should remove favorite`() =
        runTest {
            // GIVEN
            val items = getOrganizationsMock().toDomainModel().toUiModel(0)

            presenter.updateState { OrganizationList(list = items) }

            // WHEN
            presenter.dispatch(ToggleFavorite(items.first())) // add favorite
            presenter.dispatch(ToggleFavorite(items.first())) // remove favorite
            advanceUntilIdle()

            // THEN
            coVerify { mockRepository.removeFavorite(items.first().id) }
        }

    @Test
    fun `GIVEN FilterList event, WHEN search term is provided, THEN state should update with filtered list`() =
        runTest {
            // GIVEN
            val items = getOrganizationsMock().toDomainModel().toUiModel(0)

            presenter.updateState { OrganizationList(list = items) }

            // WHEN
            presenter.dispatch(FilterList("github"))
            advanceUntilIdle()

            // THEN
            val expectedState = OrganizationList(
                list = listOf(items[0].copy(isVisible = true)),
                searchTerm = "github",
                isLoading = false,
                lastId = 0
            )

            assertEquals(expectedState, presenter.uiState.value)
        }

    @Test
    fun `GIVEN repository returns ApiError, WHEN GetList is processed, THEN state is updated with ShowError`() =
        runTest {
            // GIVEN
            coEvery { mockRepository.getOrganizationsList(any()) } returns flow {
                emit(
                    ApiError(
                        code = 404,
                        message = "Not Found",
                        serviceStatusType = ServerError
                    )
                )
            }

            // WHEN
            presenter.dispatch(GetList)
            advanceUntilIdle()

            // THEN
            val expectedState = ShowError(404, "Not Found")
            assertEquals(expectedState, presenter.uiState.value)
        }

    @Test
    fun `GIVEN repository throws exception, WHEN GetList is processed, THEN state is updated with ShowException`() =
        runTest {
            // GIVEN
            val exception = RuntimeException("Unexpected error")
            coEvery { mockRepository.getOrganizationsList(any()) } returns flow {
                emit(
                    ApiException(
                        throwable = exception,
                        serviceStatusType = ServerStatusType.UnknownError
                    )
                )
            }
            // WHEN
            presenter.dispatch(GetList)
            advanceUntilIdle()

            // THEN
            val expectedState = ShowException(exception)
            assertEquals(expectedState, presenter.uiState.value)
        }

    @Test
    fun `GIVEN repository returns ApiException with ServiceUnavailable, WHEN GetList is processed, THEN state is updated with ShowNoInternet`() =
        runTest {
            // GIVEN
            coEvery { mockRepository.getOrganizationsList(any()) } returns flow {
                emit(
                    ApiException(
                        throwable = IOException(),
                        serviceStatusType = ServerStatusType.ServiceUnavailable
                    )
                )
            }

            // WHEN
            presenter.dispatch(GetList)
            advanceUntilIdle()

            // THEN
            assertEquals(ShowNoInternet, presenter.uiState.value)
        }

    @Test
    fun `GIVEN repository returns ApiException with NoToken, WHEN GetList is processed, THEN state is updated with ShowMissingToken`() =
        runTest {
            // GIVEN
            coEvery { mockRepository.getOrganizationsList(any()) } returns flow {
                emit(ApiException(throwable = null, serviceStatusType = ServerStatusType.NoToken))
            }

            // WHEN
            presenter.dispatch(GetList)
            advanceUntilIdle()

            // THEN
            assertEquals(ShowMissingToken, presenter.uiState.value)
        }

    // A utility method for accessing private fields through reflection
    private inline fun <reified T> Any.getPrivateField(fieldName: String): T {
        val field = this::class.java.getDeclaredField(fieldName).apply { isAccessible = true }
        return field.get(this) as T
    }

    private fun getOrganizationsMock(): OrganizationsResponse {
        val response = ResourceUtils().openFile("organizations_200.json")
        return jsonParser.decodeFromString(response)
    }
}
