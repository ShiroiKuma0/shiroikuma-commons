package org.fossify.commons.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.widget.TextView
import org.fossify.commons.helpers.UiRole
import org.fossify.commons.helpers.isQPlus
import org.fossify.commons.helpers.uiColor

// shiroikuma (sk18): the drawables and view states the commons used to inflate from XML with a colour
// written into them. Each is built here from the role store, so nothing in res/ names a colour.

private const val CURSOR_WIDTH_DP = 2f
private const val DISABLED_ALPHA = 0.38f

/** Checked / unchecked tint for a selection mark or a switch part. */
fun checkedStates(off: Int, on: Int) = ColorStateList(
    arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
    intArrayOf(off, on),
)

/** The cursor, the selection ground and the selection handles of a text field, from their roles. */
fun TextView.applyFieldCursorRoles() {
    val cursor = context.uiColor(UiRole.FIELD_CURSOR)
    highlightColor = context.uiColor(UiRole.FIELD_SELECTION)
    if (isQPlus()) {
        textCursorDrawable = GradientDrawable().apply {
            setColor(cursor)
            setSize((CURSOR_WIDTH_DP * resources.displayMetrics.density).toInt().coerceAtLeast(1), 0)
        }
        textSelectHandle?.mutate()?.let { it.setTint(cursor); setTextSelectHandle(it) }
        textSelectHandleLeft?.mutate()?.let { it.setTint(cursor); setTextSelectHandleLeft(it) }
        textSelectHandleRight?.mutate()?.let { it.setTint(cursor); setTextSelectHandleRight(it) }
    }
}

/** A row's ground: nothing at rest, the selected-row role when activated or selected, a ripple when pressed. */
fun Context.rowSelectorDrawable(): Drawable {
    val selected = ColorDrawable(uiColor(UiRole.LIST_SELECTED))
    val states = StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_activated), selected)
        addState(intArrayOf(android.R.attr.state_selected), selected)
    }
    return RippleDrawable(ColorStateList.valueOf(uiColor(UiRole.LIST_PRESSED)), states, ColorDrawable(uiColor(UiRole.ACCENT)))
}

/** A plain pressed-state ripple over nothing, bounded by a rectangle of [cornerRadiusDp]. */
fun Context.rippleDrawable(cornerRadiusDp: Float = 0f, ground: Int? = null): Drawable {
    val radius = cornerRadiusDp * resources.displayMetrics.density
    val mask = GradientDrawable().apply {
        cornerRadius = radius
        setColor(uiColor(UiRole.ACCENT))
    }
    val content = ground?.let {
        GradientDrawable().apply {
            cornerRadius = radius
            setColor(it)
        }
    }
    return RippleDrawable(ColorStateList.valueOf(uiColor(UiRole.LIST_PRESSED)), content, mask)
}

/** A round pressed-state ripple (icon buttons). */
fun Context.circleRippleDrawable(): Drawable {
    val mask = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(uiColor(UiRole.ACCENT))
    }
    return RippleDrawable(ColorStateList.valueOf(uiColor(UiRole.LIST_PRESSED)), null, mask)
}

/** A filled circle of [color] (a swatch, a badge ground). */
fun circleDrawable(color: Int): Drawable = GradientDrawable().apply {
    shape = GradientDrawable.OVAL
    setColor(color)
}

/** The line between two list rows, [heightPx] high, in the list-divider role. */
fun Context.listDividerDrawable(heightPx: Int = 1): Drawable = GradientDrawable().apply {
    setColor(uiColor(UiRole.LIST_DIVIDER))
    setSize(0, heightPx.coerceAtLeast(1))
}

/** A popup menu's panel: the menu ground inside the menu frame. */
fun Context.menuPanelDrawable(): Drawable {
    val density = resources.displayMetrics.density
    return GradientDrawable().apply {
        cornerRadius = 8 * density
        setColor(uiColor(UiRole.MENU_GROUND))
        setStroke((2 * density).toInt().coerceAtLeast(1), uiColor(UiRole.MENU_FRAME))
    }
}

/** [color] as a disabled control shows it. */
fun Int.disabledLook() = adjustAlpha(DISABLED_ALPHA)

/**
 * The ring that marks the chosen point in a colour picker: the background role inside the accent role, so
 * one of the two shows on whatever hue lies beneath it.
 */
fun colorPickerRing(context: Context): Drawable {
    fun ring(color: Int, widthPx: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(android.graphics.Color.TRANSPARENT)
        setStroke(widthPx, color)
    }
    return android.graphics.drawable.LayerDrawable(
        arrayOf(ring(context.uiColor(UiRole.ACCENT), 4), ring(context.uiColor(UiRole.BACKGROUND), 2))
    )
}

/** The thin frame around a holder (an attachment, a list item that stands apart): the button-frame role. */
fun Context.sectionFrameDrawable(cornerRadiusDp: Float = 8f): Drawable {
    val density = resources.displayMetrics.density
    return GradientDrawable().apply {
        cornerRadius = cornerRadiusDp * density
        setColor(android.graphics.Color.TRANSPARENT)
        setStroke(density.toInt().coerceAtLeast(1), uiColor(UiRole.BUTTON_FRAME))
    }
}

/**
 * A ripple the layout took from the framework (`?attr/selectableItemBackground` and its borderless twin)
 * has the theme's highlight colour; this gives it the pressed-row role. The attribute names a shape, not
 * a colour, so it stays in the layouts and every tree that is themed passes through here.
 */
fun android.view.View.applyRippleRole() {
    val pressed = ColorStateList.valueOf(context.uiColor(UiRole.LIST_PRESSED))
    (background as? RippleDrawable)?.setColor(pressed)
    (foreground as? RippleDrawable)?.setColor(pressed)
}

fun android.view.View.applyRippleRoles() {
    applyRippleRole()
    if (this is android.view.ViewGroup) {
        for (i in 0 until childCount) getChildAt(i).applyRippleRoles()
    }
}

/**
 * The platform's date picker takes its colours from the theme, which names none. What code can reach is
 * painted here: the header's ground and its two texts, and the wheels of the spinner form. The day grid of
 * the calendar form (day numbers, the circle on the chosen day) is drawn by the platform from theme
 * attributes that have no setter.
 */
fun android.widget.DatePicker.applyRoles() {
    fun id(name: String) = android.content.res.Resources.getSystem().getIdentifier(name, "id", "android")
    findViewById<android.view.View>(id("date_picker_header"))?.setBackgroundColor(context.uiColor(UiRole.DIALOG_GROUND))
    listOf("date_picker_header_year", "date_picker_header_date").forEach { name ->
        findViewById<TextView>(id(name))?.setTextColor(context.uiColor(UiRole.DIALOG_TITLE))
    }
    if (isQPlus()) {
        listOf("day", "month", "year").forEach { name ->
            findViewById<android.widget.NumberPicker>(id(name))?.textColor = context.uiColor(UiRole.DIALOG_TEXT)
        }
    }
}
