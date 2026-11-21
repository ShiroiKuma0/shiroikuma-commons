package org.fossify.commons.views

import org.fossify.commons.extensions.checkedStates
import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import org.fossify.commons.extensions.adjustAlpha
import org.fossify.commons.extensions.applyFontToTextView

open class MyAppCompatCheckbox : AppCompatCheckBox {
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

    // shiroikuma (sk18): the mark on and off and its label, each from its own role.
    fun applyRoles() {
        setTextColor(context.uiColor(UiRole.CHECK_LABEL))
        supportButtonTintList = checkedStates(context.uiColor(UiRole.CHECK_OFF), context.uiColor(UiRole.CHECK_ON))
    }

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        setTextColor(textColor)
        supportButtonTintList = checkedStates(textColor.adjustAlpha(0.6f), accentColor)
    }
}
