package com.bbrustol.core.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScrollModifierNode
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow


@Composable
@ExperimentalMaterial3Api
fun InversePullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    contentAlignment: Alignment = Alignment.TopStart,
    indicator: @Composable BoxScope.() -> Unit = {
        Indicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .rotate(180f),
            isRefreshing = isRefreshing,
            state = state
        )
    },
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier.pullToRefresh(state = state, isRefreshing = isRefreshing, onRefresh = onRefresh),
        contentAlignment = contentAlignment
    ) {
        content()
        indicator()
    }
}

@ExperimentalMaterial3Api
fun Modifier.pullToRefresh(
    isRefreshing: Boolean,
    state: PullToRefreshState,
    enabled: Boolean = true,
    threshold: Dp = PullToRefreshDefaults.PositionalThreshold,
    onRefresh: () -> Unit,
): Modifier =
    this then
            PullToRefreshElement(
                state = state,
                isRefreshing = isRefreshing,
                enabled = enabled,
                onRefresh = onRefresh,
                threshold = threshold
            )

@OptIn(ExperimentalMaterial3Api::class)
internal data class PullToRefreshElement(
    val isRefreshing: Boolean,
    val onRefresh: () -> Unit,
    val enabled: Boolean,
    val state: PullToRefreshState,
    val threshold: Dp,
) : ModifierNodeElement<PullToRefreshModifierNode>() {
    override fun create() =
        PullToRefreshModifierNode(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            enabled = enabled,
            state = state,
            threshold = threshold
        )

    override fun update(node: PullToRefreshModifierNode) {
        node.onRefresh = onRefresh
        node.enabled = enabled
        node.state = state
        node.threshold = threshold
        if (node.isRefreshing != isRefreshing) {
            node.isRefreshing = isRefreshing
            node.update()
        }
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "PullToRefreshModifierNode"
        properties["isRefreshing"] = isRefreshing
        properties["onRefresh"] = onRefresh
        properties["enabled"] = enabled
        properties["state"] = state
        properties["threshold"] = threshold
    }
}

@OptIn(ExperimentalMaterial3Api::class)
internal class PullToRefreshModifierNode(
    var isRefreshing: Boolean,
    var onRefresh: () -> Unit,
    var enabled: Boolean,
    var state: PullToRefreshState,
    var threshold: Dp,
) : DelegatingNode(), CompositionLocalConsumerModifierNode, NestedScrollConnection {

    private var nestedScrollNode: DelegatableNode =
        nestedScrollModifierNode(
            connection = this,
            dispatcher = null,
        )

    private var verticalOffset by mutableFloatStateOf(0f)
    private var distancePulled by mutableFloatStateOf(0f)

    private val adjustedDistancePulled: Float
        get() = distancePulled * DragMultiplier

    private val thresholdPx
        get() = with(currentValueOf(LocalDensity)) { threshold.roundToPx() }

    private val progress
        get() = adjustedDistancePulled / thresholdPx

    override fun onAttach() {
        delegate(nestedScrollNode)
        coroutineScope.launch {
            if (isRefreshing) {
                state.snapTo(1f)
            } else {
                state.snapTo(0f)
            }
        }
    }

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset =
        when {
            state.isAnimating -> Offset.Zero
            !enabled -> Offset.Zero
            // Swiping up
            source == NestedScrollSource.UserInput && available.y > 0 -> {
                consumeAvailableOffset(available)
            }
            else -> Offset.Zero
        }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset =
        when {
            state.isAnimating -> Offset.Zero
            !enabled -> Offset.Zero
            // Swiping down
            source == NestedScrollSource.UserInput -> {
                val newOffset = consumeAvailableOffset(available)
                coroutineScope.launch { state.snapTo(verticalOffset / thresholdPx) }

                newOffset
            }
            else -> Offset.Zero
        }

    override suspend fun onPreFling(available: Velocity): Velocity {
        return Velocity(0f, onRelease(available.y))
    }

    fun update() {
        coroutineScope.launch {
            if (!isRefreshing) {
                animateToHidden()
            } else {
                animateToThreshold()
            }
        }
    }

    /** Helper method for nested scroll connection */
    private fun consumeAvailableOffset(available: Offset): Offset {
        val y =
            if (isRefreshing) 0f
            else {
                val newOffset = (distancePulled + available.y).coerceAtMost(0f)
                val dragConsumed = newOffset - distancePulled
                distancePulled = newOffset
                verticalOffset = abs(calculateVerticalOffset())
                dragConsumed
            }
        return Offset(0f, y)
    }

    /** Helper method for nested scroll connection. Calls onRefresh callback when triggered */
    private suspend fun onRelease(velocity: Float): Float {
        if (isRefreshing) return 0f // Already refreshing, do nothing
        // Trigger refresh
        if (abs(adjustedDistancePulled) > thresholdPx) {
            animateToThreshold()
            onRefresh()
        } else {
            animateToHidden()
        }

        val consumed =
            when {
                // We are flinging without having dragged the pull refresh (for example a fling
                // inside
                // a list) - don't consume
                distancePulled == 0f -> 0f
                // If the velocity is negative, the fling is upwards, and we don't want to prevent
                // the
                // the list from scrolling
                velocity < 0f -> 0f
                // We are showing the indicator, and the fling is downwards - consume everything
                else -> velocity
            }
        distancePulled = 0f
        return consumed
    }

    private fun calculateVerticalOffset(): Float =
        when {
            // If drag hasn't gone past the threshold, the position is the adjustedDistancePulled.
            adjustedDistancePulled <= thresholdPx -> adjustedDistancePulled
            else -> {
                // How far beyond the threshold pull has gone, as a percentage of the threshold.
                val overshootPercent = abs(progress) - 1.0f
                // Limit the overshoot to 200%. Linear between 0 and 200.
                val linearTension = overshootPercent.coerceIn(0f, 2f)
                // Non-linear tension. Increases with linearTension, but at a decreasing rate.
                val tensionPercent = linearTension - linearTension.pow(2) / 4
                // The additional offset beyond the threshold.
                val extraOffset = thresholdPx * tensionPercent
                thresholdPx + extraOffset
            }
        }

    private suspend fun animateToThreshold() {
        state.animateToThreshold()
        distancePulled = thresholdPx.toFloat()
        verticalOffset = thresholdPx.toFloat()
    }

    private suspend fun animateToHidden() {
        state.animateToHidden()
        distancePulled = 0f
        verticalOffset = 0f
    }
}

/**
 * The distance pulled is multiplied by this value to give us the adjusted distance pulled, which is
 * used in calculating the indicator position (when the adjusted distance pulled is less than the
 * refresh threshold, it is the indicator position, otherwise the indicator position is derived from
 * the progress).
 */
private const val DragMultiplier = 0.5f