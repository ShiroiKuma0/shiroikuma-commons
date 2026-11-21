package org.fossify.commons.extensions

import android.app.Activity
import org.fossify.commons.R

/**
 * shiroikuma (sk18): there is one theme. The stock per-colour, white, black-and-white and system themes
 * pinned their colours in XML and are gone; every colour is applied in code from the role store
 * (helpers/UiRoles.kt). The parameters are kept for the callers.
 */
@Suppress("UNUSED_PARAMETER")
fun Activity.getThemeId(color: Int = 0, showTransparentTop: Boolean = false) = R.style.AppTheme_Base
