package org.fossify.commons.views

import org.fossify.commons.extensions.applyFieldCursorRoles
import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import org.fossify.commons.extensions.adjustAlpha
import org.fossify.commons.extensions.value
import org.fossify.commons.helpers.FontHelper
import org.fossify.commons.helpers.HIGHER_ALPHA
import org.fossify.commons.helpers.MEDIUM_ALPHA

open class MyTextInputLayout : TextInputLayout {
    constructor(context: Context) : super(context) {
        applyCustomFont()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        applyCustomFont()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        applyCustomFont()
    }

    private fun applyCustomFont() {
        if (isInEditMode) return
        val customTypeface = FontHelper.getTypeface(context)
        typeface = customTypeface
    }

    // shiroikuma (sk18): text, hint, label, box line (idle and focused) and cursor each from its own role.
    fun applyRoles() {
        val text = context.uiColor(UiRole.FIELD_TEXT)
        val hint = context.uiColor(UiRole.FIELD_HINT)
        val label = context.uiColor(UiRole.FIELD_LABEL)
        val line = context.uiColor(UiRole.FIELD_LINE)
        val idle = context.uiColor(UiRole.FIELD_LINE_IDLE)
        editText?.apply {
            setTextColor(text)
            setHintTextColor(hint)
            backgroundTintList = ColorStateList.valueOf(line)
            applyFieldCursorRoles()
        }
        defaultHintTextColor = ColorStateList.valueOf(hint)
        hintTextColor = ColorStateList.valueOf(label)
        setBoxStrokeColorStateList(
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(line, idle),
            )
        )
        setEndIconTintList(ColorStateList.valueOf(hint))
        setStartIconTintList(ColorStateList.valueOf(hint))
        setHelperTextColor(ColorStateList.valueOf(text))
        placeholderTextColor = ColorStateList.valueOf(hint)
        prefixTextView.setTextColor(text)
        suffixTextView.setTextColor(text)
    }

    // we need to use reflection to make some colors work well
    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        try {
            editText!!.setTextColor(textColor)
            editText!!.backgroundTintList = ColorStateList.valueOf(accentColor)

            val hintColor = if (editText!!.value.isEmpty()) textColor.adjustAlpha(HIGHER_ALPHA) else textColor
            val defaultTextColor = TextInputLayout::class.java.getDeclaredField("defaultHintTextColor")
            defaultTextColor.isAccessible = true
            defaultTextColor.set(this, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(hintColor)))

            val focusedTextColor = TextInputLayout::class.java.getDeclaredField("focusedTextColor")
            focusedTextColor.isAccessible = true
            focusedTextColor.set(this, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(accentColor)))

            val defaultHintTextColor = textColor.adjustAlpha(MEDIUM_ALPHA)
            val boxColorState = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_active),
                    intArrayOf(android.R.attr.state_focused)
                ),
                intArrayOf(
                    defaultHintTextColor,
                    accentColor
                )
            )

            setEndIconTintList(ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(hintColor)))
            setBoxStrokeColorStateList(boxColorState)
            defaultTextColor.set(this, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(defaultHintTextColor)))
            setHelperTextColor(ColorStateList.valueOf(textColor))
        } catch (e: Exception) {
        }
    }
}
