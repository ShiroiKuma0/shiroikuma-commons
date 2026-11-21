package org.fossify.commons.compose.theme

import android.content.Context
import org.fossify.commons.compose.theme.model.Theme
import org.fossify.commons.helpers.UiRole
import org.fossify.commons.helpers.uiColor

/**
 * shiroikuma (sk18): the Compose screens have one theme, made of the three foundation roles. The stock
 * material-you, white and black-and-white themes and the md_ palette table are gone. [materialYouTheme]
 * is kept for the callers.
 */
@Suppress("UNUSED_PARAMETER")
fun getTheme(context: Context, materialYouTheme: Theme.SystemDefaultMaterialYou): Theme = Theme.Custom(
    primaryColorInt = context.uiColor(UiRole.ACCENT),
    backgroundColorInt = context.uiColor(UiRole.BACKGROUND),
    appIconColorInt = context.uiColor(UiRole.ACCENT),
    textColorInt = context.uiColor(UiRole.TEXT),
)
