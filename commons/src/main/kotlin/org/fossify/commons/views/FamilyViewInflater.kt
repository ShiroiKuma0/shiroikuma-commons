package org.fossify.commons.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.android.material.theme.MaterialComponentsViewInflater
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.applyFieldCursorRoles
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.extensions.checkedStates
import org.fossify.commons.extensions.menuPanelDrawable
import org.fossify.commons.extensions.pillDrawable
import org.fossify.commons.helpers.UiRole
import org.fossify.commons.helpers.uiColor

/**
 * shiroikuma (sk18): the theme's `viewInflaterClass` — the one place every stock widget of a layout passes
 * through. No layout, style or theme names a colour any more, so a widget nothing else paints would show
 * the Material parent theme's own colours; here each gets its roles as it is inflated. Code that knows the
 * item better (a list adapter, a dialog, a screen) paints over this afterwards.
 *
 * The commons' own My* views are not created here (they are custom tags) and read their roles themselves.
 *
 * An icon keeps a neutral placeholder fill in its vector file and is tinted here from the text role;
 * pictures with colours of their own (brand marks, flags, file types, launcher icons) are left alone.
 */
@Keep
class FamilyViewInflater : MaterialComponentsViewInflater() {
    override fun createTextView(context: Context, attrs: AttributeSet?): AppCompatTextView =
        super.createTextView(context, attrs).apply {
            setTextColor(context.uiColor(UiRole.TEXT))
            setLinkTextColor(context.uiColor(UiRole.TEXT_LINK))
            highlightColor = context.uiColor(UiRole.FIELD_SELECTION)
        }

    override fun createButton(context: Context, attrs: AttributeSet): AppCompatButton =
        super.createButton(context, attrs).apply {
            setTextColor(context.uiColor(UiRole.BUTTON_TEXT))
            if (context.baseConfig.pillDialogButtons && attrs.getAttributeValue(ANDROID_NS, "background") == null) {
                (this as? com.google.android.material.button.MaterialButton)?.backgroundTintList = null
                background = context.pillDrawable(context.uiColor(UiRole.BUTTON_FRAME), context.uiColor(UiRole.BUTTON_GROUND), false)
            }
        }

    override fun createEditText(context: Context, attrs: AttributeSet?): AppCompatEditText =
        super.createEditText(context, attrs).apply { styleField() }

    override fun createAutoCompleteTextView(context: Context, attrs: AttributeSet?): AppCompatAutoCompleteTextView =
        super.createAutoCompleteTextView(context, attrs).apply { styleField() }

    private fun android.widget.EditText.styleField() {
        setTextColor(context.uiColor(UiRole.FIELD_TEXT))
        setHintTextColor(context.uiColor(UiRole.FIELD_HINT))
        backgroundTintList = ColorStateList.valueOf(context.uiColor(UiRole.FIELD_LINE))
        applyFieldCursorRoles()
    }

    override fun createCheckBox(context: Context, attrs: AttributeSet?): AppCompatCheckBox =
        super.createCheckBox(context, attrs).apply {
            setTextColor(context.uiColor(UiRole.CHECK_LABEL))
            supportButtonTintList = checkedStates(context.uiColor(UiRole.CHECK_OFF), context.uiColor(UiRole.CHECK_ON))
        }

    override fun createRadioButton(context: Context, attrs: AttributeSet?): AppCompatRadioButton =
        super.createRadioButton(context, attrs).apply {
            setTextColor(context.uiColor(UiRole.RADIO_LABEL))
            supportButtonTintList = checkedStates(context.uiColor(UiRole.RADIO_OFF), context.uiColor(UiRole.RADIO_ON))
        }

    override fun createCheckedTextView(context: Context, attrs: AttributeSet?): AppCompatCheckedTextView =
        super.createCheckedTextView(context, attrs).apply {
            setTextColor(context.uiColor(UiRole.CHECK_LABEL))
            checkMarkTintList = checkedStates(context.uiColor(UiRole.CHECK_OFF), context.uiColor(UiRole.CHECK_ON))
        }

    override fun createSeekBar(context: Context, attrs: AttributeSet?): AppCompatSeekBar =
        super.createSeekBar(context, attrs).apply {
            progressTintList = ColorStateList.valueOf(context.uiColor(UiRole.SLIDER_TRACK))
            progressBackgroundTintList = ColorStateList.valueOf(context.uiColor(UiRole.SWITCH_OFF_OUTLINE))
            thumbTintList = ColorStateList.valueOf(context.uiColor(UiRole.SLIDER_THUMB))
        }

    override fun createSpinner(context: Context, attrs: AttributeSet?): AppCompatSpinner =
        super.createSpinner(context, attrs).apply {
            background?.mutate()?.applyColorFilter(context.uiColor(UiRole.SPINNER_ARROW))
            setPopupBackgroundDrawable(context.menuPanelDrawable())
        }

    override fun createImageView(context: Context, attrs: AttributeSet?): AppCompatImageView =
        super.createImageView(context, attrs).apply { tintIcon(attrs) }

    override fun createImageButton(context: Context, attrs: AttributeSet?): AppCompatImageButton =
        super.createImageButton(context, attrs).apply { tintIcon(attrs) }

    // A single-colour icon set in the layout: its placeholder fill takes the text role. The tint is set on
    // the icon itself, never on the view — a photo loaded into the same view later must stay untouched —
    // and a colour filter a screen applies afterwards (its own role for that icon) outranks it.
    private fun android.widget.ImageView.tintIcon(attrs: AttributeSet?) {
        val icon = drawable ?: return
        if (icon !is VectorDrawable && icon !is VectorDrawableCompat) return
        val src = attrs?.getAttributeResourceValue(ANDROID_NS, "src", 0)?.takeIf { it != 0 }
            ?: attrs?.getAttributeResourceValue(APP_NS, "srcCompat", 0)?.takeIf { it != 0 }
            ?: return
        val name = try {
            resources.getResourceEntryName(src)
        } catch (ignored: Exception) {
            return
        }
        if (PICTURE_MARKS.any { name.contains(it) }) return
        setImageDrawable(icon.mutate().apply { setTint(context.uiColor(UiRole.TEXT)) })
    }

    // Stock views AppCompat has no hook for come through here by tag name.
    override fun createView(context: Context, name: String, attrs: AttributeSet): View? = when (name) {
        "ProgressBar" -> ProgressBar(context, attrs).apply {
            val accent = ColorStateList.valueOf(context.uiColor(UiRole.ACCENT))
            indeterminateTintList = accent
            progressTintList = accent
            progressBackgroundTintList = ColorStateList.valueOf(context.uiColor(UiRole.SWITCH_OFF_OUTLINE))
        }

        else -> null
    }

    private companion object {
        const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
        const val APP_NS = "http://schemas.android.com/apk/res-auto"

        // Pictures that carry their own colours, told by their resource names.
        val PICTURE_MARKS = listOf("_rect_", "ic_launcher", "ic_flag_", "ic_file_", "_tinted_", "_color")
    }
}
