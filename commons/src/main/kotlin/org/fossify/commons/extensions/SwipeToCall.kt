package org.fossify.commons.extensions

import android.view.MotionEvent
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.fossify.commons.R
import org.fossify.commons.helpers.SwipeToCallCallback

// shiroikuma (sk13): swipe to call, shared by denwa and renrakusaki — see SwipeToCallCallback.

// What setupSwipeToCall attaches to a list, kept on the list itself: the adapter is rebuilt on every
// column / layout / settings change, and each rebuild must replace these rather than pile up a second
// ItemTouchHelper (two helpers would both answer one swipe and place two calls).
private class SwipeToCallParts(
    val helper: ItemTouchHelper,
    val decoration: RecyclerView.ItemDecoration,
    val touchListener: RecyclerView.OnItemTouchListener,
)

/** Remove the swipe-to-call parts from this list, if any are attached. */
fun RecyclerView.removeSwipeToCall() {
    val parts = getTag(R.id.swipe_to_call_tag) as? SwipeToCallParts ?: return
    parts.helper.attachToRecyclerView(null)
    removeItemDecoration(parts.decoration)
    removeOnItemTouchListener(parts.touchListener)
    setTag(R.id.swipe_to_call_tag, null)
}

/**
 * Attach [callback] to this list: its ItemTouchHelper, its "Dialing…" decoration, and a touch listener
 * that lets a row swipe win over a surrounding ViewPager. Whatever an earlier call attached is removed
 * first, so an adapter rebuilt on every settings change never stacks a second helper (two helpers would
 * both answer one swipe and place two calls).
 */
fun RecyclerView.setupSwipeToCall(callback: SwipeToCallCallback) {
    removeSwipeToCall()
    val itemTouchHelper = ItemTouchHelper(callback)
    itemTouchHelper.attachToRecyclerView(this)
    // Its own decoration, so resetting the helper never takes the "Dialing…" row away with it.
    addItemDecoration(callback.dialingDecoration)

    // Detaching forces ItemTouchHelper to clearView() every pending recover animation, dropping the
    // per-frame off-screen translation it would otherwise keep re-applying to a swiped row; the
    // callback triggers this right after a committed swipe, before the call screen pauses the list.
    callback.resetItemTouchHelper = {
        itemTouchHelper.attachToRecyclerView(null)
        itemTouchHelper.attachToRecyclerView(this)
    }

    // The list lives inside a horizontally-paging ViewPager. On touch-down over a swipeable row
    // we tell the parent not to intercept, so the row swipe wins instead of switching tabs.
    val touchListener = object : RecyclerView.SimpleOnItemTouchListener() {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            if (e.actionMasked == MotionEvent.ACTION_DOWN) {
                val child = rv.findChildViewUnder(e.x, e.y)
                val position = child?.let { rv.getChildAdapterPosition(it) } ?: RecyclerView.NO_POSITION
                if (position != RecyclerView.NO_POSITION && callback.canSwipe(position)) {
                    rv.parent?.requestDisallowInterceptTouchEvent(true)
                }
            }
            return false
        }
    }
    addOnItemTouchListener(touchListener)
    setTag(R.id.swipe_to_call_tag, SwipeToCallParts(itemTouchHelper, callback.dialingDecoration, touchListener))
}
