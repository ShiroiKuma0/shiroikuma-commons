package org.fossify.commons.compose.settings

import org.fossify.commons.compose.components.familyLook
import org.fossify.commons.compose.components.OutlinedPillSwitch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.PreviewParameter
import org.fossify.commons.compose.extensions.BooleanPreviewParameterProvider
import org.fossify.commons.compose.extensions.MyDevices
import org.fossify.commons.compose.extensions.rememberMutableInteractionSource
import org.fossify.commons.compose.theme.AppThemeSurface
import org.fossify.commons.compose.theme.SimpleTheme
import org.fossify.commons.compose.theme.preferenceLabelColor
import org.fossify.commons.compose.theme.preferenceValueColor

@Composable
fun SettingsSwitchComponent(
    modifier: Modifier = Modifier,
    label: String,
    value: String? = null,
    initialValue: Boolean = false,
    isPreferenceEnabled: Boolean = true,
    showCheckmark: Boolean,
    onChange: ((Boolean) -> Unit)? = null,
    switchColors: SwitchColors = SwitchDefaults.colors(),
    // Fork (7.1.3-sk3): a row icon, coloured by what the row does
    icon: ImageVector? = null,
    iconColor: Color = Color.Unspecified,
) {
    val interactionSource = rememberMutableInteractionSource()
    val indication = LocalIndication.current

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = isPreferenceEnabled,
                onClick = { onChange?.invoke(!initialValue) },
                interactionSource = interactionSource,
                indication = indication
            ),
        colors = ListItemDefaults.colors(
            headlineColor = preferenceLabelColor(isEnabled = isPreferenceEnabled),
            supportingColor = preferenceValueColor(isEnabled = isPreferenceEnabled)
        ),
        headlineContent = {
            Text(text = label)
        },
        leadingContent = if (icon != null) {
            { Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp)) }
        } else null,
        supportingContent = {
            AnimatedVisibility(visible = !value.isNullOrBlank()) {
                Text(text = value.toString())
            }
        },
        trailingContent = {
            // Fork (sk15): the outlined switch under the family's look
            if (familyLook()) {
                OutlinedPillSwitch(checked = initialValue, onCheckedChange = { onChange?.invoke(it) }, enabled = isPreferenceEnabled)
            } else CompositionLocalProvider(LocalRippleConfiguration provides null) {
                Switch(
                    checked = initialValue,
                    onCheckedChange = { onChange?.invoke(it) },
                    thumbContent = if (showCheckmark && initialValue) {
                        {
                            Icon(
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SimpleTheme.colorScheme.primary
                            )
                        }
                    } else null,
                    enabled = isPreferenceEnabled,
                    colors = switchColors,
                    interactionSource = interactionSource,
                )
            }
        }
    )
}

@MyDevices
@Composable
private fun SettingsSwitchComponentPreview(@PreviewParameter(BooleanPreviewParameterProvider::class) isChecked: Boolean) {
    var checked by remember { mutableStateOf(isChecked) }
    AppThemeSurface {
        SettingsSwitchComponent(
            label = "Some label",
            value = "Some value",
            initialValue = checked,
            showCheckmark = false,
            onChange = {
                checked = it
            }
        )
    }
}
