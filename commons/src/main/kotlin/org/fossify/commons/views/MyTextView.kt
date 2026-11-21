package org.fossify.commons.views

import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import org.fossify.commons.extensions.applyFontToTextView

open class MyTextView : TextView {
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

    // shiroikuma (sk18): the colours come from the role store; a text in a dialog reads the dialog's role.
    fun applyRoles(inDialog: Boolean = false) {
        setTextColor(context.uiColor(if (inDialog) UiRole.DIALOG_TEXT else UiRole.TEXT))
        setLinkTextColor(context.uiColor(UiRole.TEXT_LINK))
    }

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        setTextColor(textColor)
        setLinkTextColor(accentColor)
    }
}
