package org.fossify.commons.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import org.fossify.commons.helpers.UiRole
import org.fossify.commons.helpers.uiColor

/**
 * shiroikuma (sk18): the thin line between two groups of rows. It paints itself in the list-divider role
 * each time it is attached, so no layout has to name a colour for it.
 */
class MyDivider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) setBackgroundColor(context.uiColor(UiRole.LIST_DIVIDER))
    }
}
