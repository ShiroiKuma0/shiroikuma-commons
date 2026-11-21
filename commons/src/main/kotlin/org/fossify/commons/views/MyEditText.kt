package org.fossify.commons.views

import org.fossify.commons.extensions.applyFieldCursorRoles
import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import org.fossify.commons.extensions.adjustAlpha
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.applyFontToTextView
import org.fossify.commons.helpers.MEDIUM_ALPHA

open class MyEditText : AppCompatEditText {
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

    // shiroikuma (sk18): text, hint, line, cursor and selection each from its own role.
    fun applyRoles() {
        background?.mutate()?.applyColorFilter(context.uiColor(UiRole.FIELD_LINE))
        setTextColor(context.uiColor(UiRole.FIELD_TEXT))
        setHintTextColor(context.uiColor(UiRole.FIELD_HINT))
        setLinkTextColor(context.uiColor(UiRole.TEXT_LINK))
        applyFieldCursorRoles()
    }

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        background?.mutate()?.applyColorFilter(accentColor)
        setTextColor(textColor)
        setHintTextColor(textColor.adjustAlpha(MEDIUM_ALPHA))
        setLinkTextColor(accentColor)
    }
}
