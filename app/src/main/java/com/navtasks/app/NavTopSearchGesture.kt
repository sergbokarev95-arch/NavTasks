package com.navtasks.app

import android.os.SystemClock
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/** Only screens with an existing magnifier use this passive gesture. */
internal object NavTopSearchGesture {
    const val minRestMs = 200L
    const val minGestureMs = 150L
    const val minPullDp = 90f
    const val directionSlopDp = 10f
}

/** Pure predicate for tests; visual overscroll and flings never request a search. */
internal fun shouldRevealSearchOnPull(
    beganAtTop: Boolean,
    restedBeforeDown: Boolean,
    firstMoveWasDown: Boolean,
    endedAtTop: Boolean,
    dxPx: Float,
    dyPx: Float,
    elapsedMs: Long,
    multiTouch: Boolean,
    minPullPx: Float,
): Boolean =
    beganAtTop && restedBeforeDown && firstMoveWasDown && endedAtTop &&
        !multiTouch && elapsedMs >= NavTopSearchGesture.minGestureMs &&
        minPullPx > 0f && dyPx >= minPullPx && abs(dxPx) <= dyPx * 0.45f

/**
 * Non-consuming touch observer on the list surface.
 *
 * A gesture must BEGIN while the list is already idle at item zero, after
 * a 200 ms rest. The original lazy list continues receiving every event;
 * reaching the top mid-swipe or canceling an ongoing fling cannot arm search.
 * The action fires only after the user's finger goes up, not during overscroll.
 */
@Composable
internal fun Modifier.revealSearchOnSettledTopPull(
    listState: LazyListState,
    searchOpen: Boolean,
    onReveal: () -> Unit,
): Modifier {
    val restedAt = remember(listState) { mutableLongStateOf(SystemClock.uptimeMillis()) }
    val reveal by rememberUpdatedState(onReveal)
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect { scrolling ->
            restedAt.longValue =
                if (scrolling) Long.MAX_VALUE else SystemClock.uptimeMillis()
        }
    }
    if (searchOpen) return this
    return this.pointerInput(listState) {
        val minPull = NavTopSearchGesture.minPullDp.dp.toPx()
        val directionSlop = NavTopSearchGesture.directionSlopDp.dp.toPx()
        awaitEachGesture {
            val down = awaitFirstDown(
                requireUnconsumed = false, pass = PointerEventPass.Initial)
            val startsAtTop = listState.firstVisibleItemIndex == 0 &&
                listState.firstVisibleItemScrollOffset == 0 &&
                !listState.isScrollInProgress
            val idleFor = SystemClock.uptimeMillis() - restedAt.longValue
            if (!startsAtTop || idleFor < NavTopSearchGesture.minRestMs) {
                return@awaitEachGesture
            }

            var firstMoveDown: Boolean? = null
            var multiplePointers = false
            while (true) {
                // Observe BEFORE the LazyColumn consumes movement, but do not consume it.
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.any { it.id != down.id && it.pressed }) {
                    multiplePointers = true
                }
                val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                val dx = pointer.position.x - down.position.x
                val dy = pointer.position.y - down.position.y
                if (firstMoveDown == null &&
                    (abs(dx) >= directionSlop || abs(dy) >= directionSlop)
                ) {
                    firstMoveDown = dy > directionSlop && dy > abs(dx) * 1.25f
                }
                if (!pointer.pressed) {
                    val endsAtTop = listState.firstVisibleItemIndex == 0 &&
                        listState.firstVisibleItemScrollOffset == 0
                    if (shouldRevealSearchOnPull(
                        beganAtTop = startsAtTop,
                        restedBeforeDown = true,
                        firstMoveWasDown = firstMoveDown == true,
                        endedAtTop = endsAtTop,
                        dxPx = dx,
                        dyPx = dy,
                        elapsedMs = pointer.uptimeMillis - down.uptimeMillis,
                        multiTouch = multiplePointers,
                        minPullPx = minPull,
                    )) {
                        reveal()
                    }
                    break
                }
            }
        }
    }
}
