package org.fossify.commons.compose.extensions

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput

internal fun Modifier.listDragHandlerLongKey(
    lazyListState: LazyListState,
    haptics: HapticFeedback,
    selectedIds: MutableState<Set<Long>>,
    autoScrollSpeed: MutableState<Float>,
    autoScrollThreshold: Float,
    dragUpdate: (Boolean) -> Unit,
    ids: List<Long>,
    isScrollingUp: Boolean,
    // Fork (7.1.3-sk3): a list whose rows hold several entries says by itself which one is under the finger
    keyAt: ((Offset) -> Long?)? = null,
) = pointerInput(Unit) {
    val hit: (Offset) -> Long? = { point -> if (keyAt != null) keyAt(point) else lazyListState.itemKeyAtPosition(point) }

    var initialKey: Long? = null
    var currentKey: Long? = null
    val onDragCancelAndEnd = {
        initialKey = null
        autoScrollSpeed.value = 0f
        dragUpdate(false)
    }
    detectDragGesturesAfterLongPress(
        onDragStart = { offset ->
            dragUpdate(true)
            hit(offset)?.let { key ->
                if (!selectedIds.value.contains(key)) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    initialKey = key
                    currentKey = key
                    selectedIds.value += key
                }
            }
        },
        onDragCancel = onDragCancelAndEnd,
        onDragEnd = onDragCancelAndEnd,
        onDrag = { change, _ ->
            if (initialKey != null) {
                val distFromBottom = lazyListState.layoutInfo.viewportSize.height - change.position.y
                val distFromTop = change.position.y
                autoScrollSpeed.value = when {
                    distFromBottom < autoScrollThreshold -> autoScrollThreshold - distFromBottom
                    distFromTop < autoScrollThreshold -> -(autoScrollThreshold - distFromTop)
                    else -> 0f
                }

                hit(change.position)?.let { key ->
                    if (currentKey != key) {
                        val toSelect = if (selectedIds.value.contains(key) && ids.isNotEmpty()) {
                            val successor = ids.indexOf(key) + if (isScrollingUp) +1 else -1
                            selectedIds.value
                                .minus(ids[successor])
                        } else {
                            selectedIds.value + setOf(currentKey!!, key)
                        }

                        selectedIds.value = toSelect
                        currentKey = key
                    }
                }
            }
        }
    )
}

/**
 * Returns whether the lazy list is currently scrolling up.
 */
@Composable
internal fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

internal fun LazyListState.itemKeyAtPosition(hitPoint: Offset): Long? =
    layoutInfo.visibleItemsInfo
        .firstOrNull { lazyListItemInfo ->
            hitPoint.y.toInt() in lazyListItemInfo.offset..lazyListItemInfo.offset + lazyListItemInfo.size
        }
        ?.key as? Long
