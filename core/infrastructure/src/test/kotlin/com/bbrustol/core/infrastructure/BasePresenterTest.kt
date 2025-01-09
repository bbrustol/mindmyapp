package com.bbrustol.core.infrastructure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class BasePresenterTest {

    private sealed interface Event {
        data object SomeEvent : Event
        data object SomeOtherEvent : Event
        data object YetAnotherEvent : Event
    }

    private sealed interface UiState {
        data object InitialState : UiState
        data object SomeUiState : UiState
        data object SomeOtherUiState : UiState
    }

    private sealed interface UiSideEffect {
        data object SomeSideEffect : UiSideEffect
    }

    private lateinit var presenter: BasePresenter<Event, UiState, UiSideEffect>
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun `set up`() {
        Dispatchers.setMain(testDispatcher)
        presenter = object : BasePresenter<Event, UiState, UiSideEffect>() {
            override fun setInitialState(): UiState = UiState.InitialState
            override fun process(event: Event) {
                when (event) {
                    Event.SomeEvent -> updateState { UiState.SomeUiState }
                    Event.SomeOtherEvent -> updateState { UiState.SomeOtherUiState }
                    Event.YetAnotherEvent -> sendSideEffect { UiSideEffect.SomeSideEffect }
                }
            }
        }
    }

    @After
    fun `tear down`() {
        Dispatchers.resetMain()
    }

    private fun getUiState() = presenter.uiState.value

    @Test
    fun `initialization should default to result of setInitialState`() = runTest {
        assertThat(getUiState(), equalTo(UiState.InitialState))
    }

    @Test
    fun `dispatching SomeEvent should update UI state to SomeUiState`() = runTest {
        presenter.dispatch(Event.SomeEvent)
        advanceUntilIdle()

        assertThat(getUiState(), equalTo(UiState.SomeUiState))
    }

    @Test
    fun `dispatching SomeOtherEvent should update UI state to SomeOtherUiState`() = runTest {
        presenter.dispatch(Event.SomeOtherEvent)
        advanceUntilIdle()

        assertThat(getUiState(), equalTo(UiState.SomeOtherUiState))
    }

    @Test
    fun `dispatching YetAnotherEvent should keep current state and send SomeSideEffect`() =
        runTest {
            presenter.dispatch(Event.SomeOtherEvent)
            presenter.dispatch(Event.YetAnotherEvent)
            advanceUntilIdle()
            val sideEffect = presenter.sideEffect.first()

            assertThat(getUiState(), equalTo(UiState.SomeOtherUiState))
            assertThat(sideEffect, equalTo(UiSideEffect.SomeSideEffect))
        }

    @Test
    fun `dispatching multiple event should result in sequential emission`() = runTest {
        presenter.dispatch(Event.SomeEvent, Event.SomeOtherEvent, Event.SomeEvent)

        val result = mutableListOf<UiState>()
        val expected = listOf(
            UiState.InitialState,
            UiState.SomeUiState,
            UiState.SomeOtherUiState,
            UiState.SomeUiState,
        )

        presenter.uiState
            .take(4)
            .collect { result.add(it) }

        assertThat(result, equalTo(expected))
    }
}