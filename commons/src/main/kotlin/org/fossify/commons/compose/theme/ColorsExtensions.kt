package org.fossify.commons.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// sk18: derived from the scheme's text role, never from white, black or grey
@get:ReadOnlyComposable
val disabledTextColor @Composable get() = SimpleTheme.colorScheme.onSurface.copy(alpha = 0.38f)

@get:ReadOnlyComposable
val textSubTitleColor @Composable get() = SimpleTheme.colorScheme.onSurface.copy(alpha = 0.6f)

@get:ReadOnlyComposable
val iconsColor @Composable get() = SimpleTheme.colorScheme.onSurface

@Composable
@ReadOnlyComposable
fun preferenceValueColor(isEnabled: Boolean) =
    if (isEnabled) SimpleTheme.colorScheme.onSurface.copy(alpha = 0.6f) else disabledTextColor

@Composable
@ReadOnlyComposable
fun preferenceLabelColor(isEnabled: Boolean) = if (isEnabled) SimpleTheme.colorScheme.onSurface else disabledTextColor

fun Color.isLitWell(threshold: Float = LUMINANCE_THRESHOLD) = luminance() > threshold

fun Color.isNotLitWell(threshold: Float = LUMINANCE_THRESHOLD) = luminance() < threshold
