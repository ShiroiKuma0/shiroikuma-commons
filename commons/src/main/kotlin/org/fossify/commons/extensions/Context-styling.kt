package org.fossify.commons.extensions

import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.loader.content.CursorLoader
import com.google.android.material.color.MaterialColors
import org.fossify.commons.R
import org.fossify.commons.helpers.DARK_GREY
import org.fossify.commons.helpers.FONT_TYPE_CUSTOM
import org.fossify.commons.helpers.FONT_TYPE_SYSTEM_DEFAULT
import org.fossify.commons.helpers.FontHelper
import org.fossify.commons.helpers.MyContentProvider
import org.fossify.commons.helpers.MyContentProvider.GLOBAL_THEME_SYSTEM
import org.fossify.commons.helpers.appIconColorStrings
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.commons.helpers.isSPlus
import org.fossify.commons.models.GlobalConfig
import org.fossify.commons.models.isGlobalThemingEnabled
import org.fossify.commons.views.MyAppCompatCheckbox
import org.fossify.commons.views.MyAppCompatSpinner
import org.fossify.commons.views.MyAutoCompleteTextView
import org.fossify.commons.views.MyButton
import org.fossify.commons.views.MyCompatRadioButton
import org.fossify.commons.views.MyEditText
import org.fossify.commons.views.MyFloatingActionButton
import org.fossify.commons.views.MyMaterialSwitch
import org.fossify.commons.views.MySeekBar
import org.fossify.commons.views.MyTextInputLayout
import org.fossify.commons.views.MyTextView
import java.io.File

// shiroikuma (sk18): the stock theme modes (system / dynamic, white, black-and-white, auto) are gone — there
// is one theme, coloured from the role store. The predicates stay for their callers and are always false.
fun Context.isDynamicTheme() = false

fun Context.isBlackAndWhiteTheme() = false

fun Context.isWhiteTheme() = false

fun Context.isSystemInDarkMode() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES != 0

fun Context.isAutoTheme() = false

// The three foundation roles under their stock names.
fun Context.getProperTextColor() = uiColor(UiRole.TEXT)

fun Context.getProperBackgroundColor() = uiColor(UiRole.BACKGROUND)

fun Context.getProperPrimaryColor() = uiColor(UiRole.ACCENT)

fun Context.getProperStatusBarColor() = uiColor(UiRole.STATUS_BAR)

// get the color of the status bar with material activity, if the layout is scrolled down a bit
fun Context.getColoredMaterialStatusBarColor(): Int {
    // sk18: the top bar keeps its own ground when the list beneath it is scrolled — no accent slab
    return uiColor(UiRole.TOOLBAR_GROUND)
}

// shiroikuma (sk18): every themed view of a tree takes its colours from its own roles. [inDialog] makes
// plain texts read the dialog's text role instead of the screen's.
fun Context.updateTextColors(viewGroup: ViewGroup, inDialog: Boolean = false) {
    val cnt = viewGroup.childCount
    viewGroup.applyRippleRole()
    (0 until cnt).map { viewGroup.getChildAt(it) }.forEach {
        it.applyRippleRole()
        when (it) {
            is MyTextView -> it.applyRoles(inDialog)
            is MyAppCompatSpinner -> it.applyRoles()
            is MyCompatRadioButton -> it.applyRoles()
            is MyAppCompatCheckbox -> it.applyRoles()
            is MyMaterialSwitch -> it.applyRoles()
            is MyEditText -> it.applyRoles()
            is MyAutoCompleteTextView -> it.applyRoles()
            is MyFloatingActionButton -> it.applyRoles()
            is MySeekBar -> it.applyRoles()
            is MyButton -> it.applyRoles()
            is MyTextInputLayout -> it.applyRoles()
            is ViewGroup -> updateTextColors(it, inDialog)
        }
    }
}

fun Context.getTimePickerDialogTheme() = R.style.MyDialogTheme

fun Context.getDatePickerDialogTheme() = R.style.MyDialogTheme

fun Context.getPopupMenuTheme(): Int = R.style.AppTheme_PopupMenuDarkStyle

fun Context.syncGlobalConfig(callback: (() -> Unit)? = null) {
    if (canAccessGlobalConfig()) {
        withGlobalConfig {
            if (it != null) {
                baseConfig.apply {
                    showCheckmarksOnSwitches = it.showCheckmarksOnSwitches
                    // sk18: the shared theme of the stock "Thank You" app no longer sets colours; fonts still follow it
                    if (it.isGlobalThemingEnabled()) {
                        if (it.fontType >= 0 && (fontType != it.fontType || fontName != it.fontName)) {
                            if (it.fontType != FONT_TYPE_CUSTOM || ensureFontPresentLocally(it.fontName)) {
                                fontType = it.fontType
                                fontName = it.fontName
                            }
                            FontHelper.clearCache()
                        }
                    }
                }
            }

            validateFontSettings()
            callback?.invoke()
        }
    } else {
        baseConfig.showCheckmarksOnSwitches = false
        validateFontSettings()
        callback?.invoke()
    }
}

private fun Context.validateFontSettings() {
    if (baseConfig.fontType == FONT_TYPE_CUSTOM) {
        val typeface = FontHelper.getTypeface(this)
        if (typeface == Typeface.DEFAULT) {
            baseConfig.fontType = FONT_TYPE_SYSTEM_DEFAULT
            baseConfig.fontName = ""
            FontHelper.clearCache()
        }
    }
}

fun Context.withGlobalConfig(callback: (globalConfig: GlobalConfig?) -> Unit) {
    if (!isThankYouInstalled()) {
        callback(null)
    } else {
        val cursorLoader = getMyContentProviderCursorLoader()
        ensureBackgroundThread {
            callback(getGlobalConfig(cursorLoader))
        }
    }
}

fun Context.getGlobalConfig(cursorLoader: CursorLoader): GlobalConfig? {
    val cursor = cursorLoader.loadInBackground()
    cursor?.use {
        if (cursor.moveToFirst()) {
            try {
                return GlobalConfig(
                    themeType = cursor.getIntValue(MyContentProvider.COL_THEME_TYPE),
                    textColor = cursor.getIntValue(MyContentProvider.COL_TEXT_COLOR),
                    backgroundColor = cursor.getIntValue(MyContentProvider.COL_BACKGROUND_COLOR),
                    primaryColor = cursor.getIntValue(MyContentProvider.COL_PRIMARY_COLOR),
                    accentColor = cursor.getIntValue(MyContentProvider.COL_ACCENT_COLOR),
                    appIconColor = cursor.getIntValue(MyContentProvider.COL_APP_ICON_COLOR),
                    showCheckmarksOnSwitches = cursor.getIntValue(MyContentProvider.COL_SHOW_CHECKMARKS_ON_SWITCHES) != 0,
                    lastUpdatedTS = cursor.getIntValue(MyContentProvider.COL_LAST_UPDATED_TS),
                    fontType = cursor.getIntValueOr(MyContentProvider.COL_FONT_TYPE, -1),
                    fontName = cursor.getStringValueOr(MyContentProvider.COL_FONT_NAME, "")
                )
            } catch (_: Exception) {
            }
        }
    }
    return null
}

fun Context.ensureFontPresentLocally(fontName: String): Boolean {
    if (fontName.isEmpty()) return false
    val localFile = File(FontHelper.getFontsDir(this), fontName)
    if (localFile.exists()) return true

    val fontUri = MyContentProvider.FONTS_URI.buildUpon()
        .appendPath(fontName)
        .build()

    return try {
        contentResolver.openInputStream(fontUri)?.use { input ->
            localFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } != null
    } catch (_: Exception) {
        false
    }
}

// shiroikuma (sk18): the launcher icon's colour was chosen on the removed Customize colors screen. The
// aliases an app declares keep whatever state they have; nothing switches them any more.
fun Context.checkAppIconColor() {}

fun Context.getAppIconColors() = ArrayList<Int>()

@SuppressLint("NewApi")
fun Context.getBottomNavigationBackgroundColor(): Int {
    return uiColor(UiRole.BOTTOM_BAR_GROUND)
}

fun Context.getDialogBackgroundColor(): Int = uiColor(UiRole.DIALOG_GROUND)
