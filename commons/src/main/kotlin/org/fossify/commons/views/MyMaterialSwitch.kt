package org.fossify.commons.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.ColorUtils
import com.google.android.material.materialswitch.MaterialSwitch
import org.fossify.commons.R
import org.fossify.commons.extensions.adjustAlpha
import org.fossify.commons.extensions.applyFontToTextView
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.extensions.getContrastColor
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.extensions.getProperPrimaryColor

open class MyMaterialSwitch : MaterialSwitch {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    // shiroikuma (sk17): under the family's look (BaseConfig.pillDialogButtons) every switch is the outlined
    // pill with a solid dot — right and in the accent when on, left and in the darker off colour when not.
    private val familyLook = !isInEditMode && context.baseConfig.pillDialogButtons

    init {
        setShowCheckmark(context.baseConfig.showCheckmarksOnSwitches)
        if (!isInEditMode) context.applyFontToTextView(this)
        if (familyLook) applyFamilyLook(context.getProperPrimaryColor(), context.getProperBackgroundColor())
    }

    private fun applyFamilyLook(accentColor: Int, backgroundColor: Int) {
        val density = resources.displayMetrics.density
        val height = (FAMILY_HEIGHT_DP * density).toInt()
        val stroke = FAMILY_STROKE_DP * density
        if (trackDrawable !is FamilySwitchDrawable) {
            // the thumb is as wide as the track is high, so the dot keeps the same gap to the frame all round
            trackDrawable = FamilySwitchDrawable(true, (FAMILY_WIDTH_DP * density).toInt(), height, stroke, 0f)
            thumbDrawable = FamilySwitchDrawable(false, height, height, stroke, height * FAMILY_DOT_RADIUS)
            trackDecorationDrawable = null
            thumbIconDrawable = null
        }
        val off = context.baseConfig.switchOffColor.takeIf { it != 0 }
            ?: ColorUtils.blendARGB(backgroundColor, accentColor, FAMILY_OFF_BLEND)
        val states = arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked))
        val colors = ColorStateList(states, intArrayOf(off, accentColor))
        trackTintList = colors
        thumbTintList = colors
    }

    fun setShowCheckmark(showCheckmark: Boolean) {
        if (showCheckmark && !familyLook) {
            setOnCheckedChangeListener { _, isChecked ->
                thumbIconDrawable = if (isChecked) {
                    AppCompatResources.getDrawable(context, R.drawable.ic_check_vector)
                } else {
                    null
                }
            }
        } else {
            setOnCheckedChangeListener(null)
        }
    }

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        if (familyLook) {
            setTextColor(textColor)
            applyFamilyLook(accentColor, backgroundColor)
            return
        }
        val onPrimary = accentColor.getContrastColor()
        val outlineColor = textColor.adjustAlpha(0.4f)
        val trackColor = textColor.adjustAlpha(0.2f)

        setTextColor(textColor)
        trackTintList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ),
            intArrayOf(trackColor, accentColor)
        )

        thumbTintList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ),
            intArrayOf(outlineColor, onPrimary)
        )

        thumbIconTintList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ),
            intArrayOf(trackColor, accentColor)
        )

        trackDecorationTintList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ),
            intArrayOf(outlineColor, accentColor)
        )
    }

    private companion object {
        const val FAMILY_WIDTH_DP = 52f
        const val FAMILY_HEIGHT_DP = 28f
        const val FAMILY_STROKE_DP = 2f
        const val FAMILY_DOT_RADIUS = 0.23f // the dot is 46 % of the track's height
        const val FAMILY_OFF_BLEND = 0.55f
    }
}
