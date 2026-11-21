package org.fossify.commons.views

import org.fossify.commons.extensions.pillDrawable
import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import org.fossify.commons.extensions.applyFontToTextView

open class MyButton : Button {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        if (!isInEditMode) context.applyFontToTextView(this)
    }

    // shiroikuma (sk18): an outlined pill from the button roles — text, frame and ground.
    fun applyRoles(main: Boolean = false) {
        setTextColor(context.uiColor(UiRole.BUTTON_TEXT))
        background = context.pillDrawable(context.uiColor(UiRole.BUTTON_FRAME), context.uiColor(UiRole.BUTTON_GROUND), main)
        setTypeface(typeface, if (main) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
    }

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        setTextColor(textColor)
    }
}
