package org.fossify.commons.compose.settings

import androidx.compose.ui.platform.LocalContext
import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import org.fossify.commons.compose.extensions.MyDevices
import org.fossify.commons.compose.theme.AppThemeSurface

@Composable
fun SettingsHorizontalDivider(
    modifier: Modifier = Modifier,
    color: Color = Color(LocalContext.current.uiColor(UiRole.LIST_DIVIDER)),
    thickness: Dp = DividerDefaults.Thickness,
) {
    HorizontalDivider(modifier = modifier, color = color, thickness = thickness)
}


@Composable
@MyDevices
private fun SettingsHorizontalDividerPreview() {
    AppThemeSurface {
        SettingsHorizontalDivider()
    }
}
