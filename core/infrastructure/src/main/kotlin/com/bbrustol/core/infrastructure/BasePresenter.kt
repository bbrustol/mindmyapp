package com.bbrustol.core.infrastructure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
/**
 * BasePresenter is an abstract class that serves as a base for implementing the MVI (Model-View-Intent) pattern in the project.
 * It utilizes the ViewModel architecture component to manage UI-related data in a lifecycle-conscious way. The presenter handles
 * events, updates UI state, and manages side effects using Kotlin Coroutines and Flow.
 *
 * @param <Event> Type representing user actions or events that trigger changes in the UI state.
 * @param <UiState> Type representing the state of the UI, often modeled as either data class, sealed class
 * or sealed interface, depending on the UI and logic specs.
 * @param <SideEffect> Type representing one-time actions or effects, such as navigation or showing
 * a toast. Once the receiver consumes the event, it is gone.
 */
abstract class BasePresenter<in Event, UiState, SideEffect> : ViewModel() {

    /**
     * Backing property for the StateFlow which holds the current UI state.
     */
    private val _uiState by lazy { MutableStateFlow(setInitialState()) }

    /*
     * StateFlow that exposes the current UI state to the observers.
     * It is updated by calling the protected method `updateState`.
     */
    val uiState: StateFlow<UiState> = _uiState

    /**
     * MutableSharedFlow that collects events usually dispatched from the view. Events can also be
     * dispatched by the Presenter itself.
     */
    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()

    /**
     * Channel used to handle side effects. It supports buffered transmission of side effects.
     */
    private val _sideEffect: Channel<SideEffect> = Channel(Channel.BUFFERED)

    /**
     * Flow that allows observers to receive side effects emitted by the presenter.
     */
    val sideEffect = _sideEffect.receiveAsFlow()

    /**
     * Initializes the presenter by calling the abstract method `handleEvents` which starts collecting events.
     */
    init {
        handleEvents()
    }

    /**
     * Implement this method to define the initial state of the UI. This state will be set when the presenter is created.
     *
     * @return the initial state of type [UiState].
     */
    protected abstract fun setInitialState(): UiState

    /**
     * Implement this method to handle and process events that are dispatched to the presenter.
     * Each event should trigger corresponding state updates or side effect.
     *
     * @param event the event to process.
     */
    protected abstract fun process(event: Event)

    /**
     * Collects dispatched events and processes them using the implemented `process` method.
     * This method is automatically invoked during initialization.
     */
    private fun handleEvents() {
        viewModelScope.launch {
            _event.collect {
                process(it)
            }
        }
    }

    /**
     * Dispatches one or more events to be processed by the presenter.
     * All events are emitted to the `_event` flow.
     *
     * @param event vararg parameter of events to be dispatched.
     */
    fun dispatch(vararg event: Event) {
        viewModelScope.launch {
            event.forEach { _event.emit(it) }
        }
    }

    /**
     * Updates the current UI state based on the provided reducer function.
     *
     * @param reducer a lambda that takes the current UI state and returns a new modified state.
     */
    fun updateState(reducer: UiState.() -> UiState) {
        _uiState.update(reducer)
    }

    /**
     * Sends a side effect to be consumed by the observers.
     * This method utilizes the `_sideEffect` channel to send the side effects.
     *
     * @param builder a lambda that returns a new side effect instance to be sent.
     */
    protected fun sendSideEffect(builder: () -> SideEffect) {
        viewModelScope.launch {
            _sideEffect.send(builder())
        }
    }
}