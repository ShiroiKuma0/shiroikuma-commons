package org.fossify.commons.views

import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import org.fossify.commons.extensions.applyColorFilter

open class MySeekBar : SeekBar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    // shiroikuma (sk18): track and thumb from their roles.
    fun applyRoles() {
        progressDrawable.applyColorFilter(context.uiColor(UiRole.SLIDER_TRACK))
        thumb?.applyColorFilter(context.uiColor(UiRole.SLIDER_THUMB))
    }

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        progressDrawable.applyColorFilter(accentColor)
        thumb?.applyColorFilter(accentColor)
    }
}
