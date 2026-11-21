package org.fossify.commons.views

import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.InsetDrawable
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.shape.MaterialShapeDrawable
import org.fossify.commons.R
import org.fossify.commons.extensions.adjustAlpha
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.extensions.getContrastColor
import org.fossify.commons.extensions.updateMarginWithBase

open class MyFloatingActionButton : FloatingActionButton {
    private var applyWindowInsets = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        context.withStyledAttributes(attrs, R.styleable.MyFloatingActionButton) {
            applyWindowInsets = getBoolean(R.styleable.MyFloatingActionButton_applyWindowInsets, false)
        }

        if (applyWindowInsets) {
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                val system = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars())
                updateMarginWithBase(bottom = system.bottom)
                insets
            }
        }
    }

    fun applyRoles() = setColors(context.uiColor(UiRole.TEXT), context.uiColor(UiRole.ACCENT), context.uiColor(UiRole.BACKGROUND))

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        if (!isInEditMode && context.baseConfig.pillDialogButtons) {
            applyFamilyLook(accentColor, backgroundColor)
            return
        }
        backgroundTintList = ColorStateList.valueOf(accentColor)
        applyColorFilter(accentColor.getContrastColor())
    }

    // shiroikuma (sk17): under the family's look a floating button is never a filled slab — the background
    // shows through, and the accent draws its icon and a thick frame in the button's own shape (it is the
    // main action of its screen, hence the main pill's 3 dp).
    private fun applyFamilyLook(accentColor: Int, backgroundColor: Int) {
        // sk18: icon, frame and ground are three roles; the arguments are kept for the callers of setColors
        val frameColor = context.uiColor(UiRole.FAB_FRAME)
        backgroundTintList = ColorStateList.valueOf(context.uiColor(UiRole.FAB_GROUND))
        rippleColor = frameColor.adjustAlpha(FAMILY_RIPPLE_ALPHA)
        applyColorFilter(context.uiColor(UiRole.FAB_ICON))
        val stroke = FAMILY_STROKE_DP * resources.displayMetrics.density
        val frame = MaterialShapeDrawable(shapeAppearanceModel).apply {
            fillColor = ColorStateList.valueOf(Color.TRANSPARENT)
            strokeColor = ColorStateList.valueOf(frameColor)
            strokeWidth = stroke
        }
        // the stroke straddles the outline; half of it would be cut off by the button's own edge
        val inset = (stroke / 2f).toInt()
        foreground = InsetDrawable(frame, inset)
    }

    private companion object {
        const val FAMILY_STROKE_DP = 3f
        const val FAMILY_RIPPLE_ALPHA = 0.3f
    }
}
