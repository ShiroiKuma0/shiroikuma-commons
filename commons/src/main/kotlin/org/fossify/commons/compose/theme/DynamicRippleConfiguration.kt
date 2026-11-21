package org.fossify.commons.compose.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable

@Composable
fun dynamicRippleConfiguration() =
    RippleConfiguration(
        color = Color(LocalContext.current.uiColor(UiRole.LIST_PRESSED)),
    )
