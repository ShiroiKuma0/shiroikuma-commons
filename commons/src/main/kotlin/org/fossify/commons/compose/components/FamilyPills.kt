package org.fossify.commons.compose.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.fossify.commons.compose.theme.SimpleTheme
import org.fossify.commons.extensions.baseConfig

// Fork (sk15): the family's look for commons' own Compose screens and dialogs — pills instead of
// text / filled buttons, the outlined switch instead of Material's filled one. Opt-in through the same
// BaseConfig.pillDialogButtons switch as the Views dialogs (sk12); off, everything is stock.

private const val DISABLED_ALPHA = 0.38f
private const val SWITCH_OFF_BLEND = 0.55f
private const val SWITCH_DOT_RADIUS = 0.23f
private const val SWITCH_TRAVEL_MS = 180

/** Whether the consuming app asked for the family's look. */
@Composable
fun familyLook(): Boolean = LocalContext.current.baseConfig.pillDialogButtons

/** The colour of a destructive action: BaseConfig.dialogWarningColor. */
@Composable
fun warningColor(): Color = Color(LocalContext.current.baseConfig.dialogWarningColor)

/** An outlined pill, never filled: a 3 dp frame and bold text for a [main] action, 1.25 dp otherwise. */
@Composable
fun PillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    main: Boolean = false,
    color: Color = SimpleTheme.colorScheme.primary,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val shown = if (enabled) color else color.copy(alpha = DISABLED_ALPHA)
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = CircleShape,
        border = BorderStroke(if (main) 3.dp else 1.25.dp, shown),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = color,
            disabledContentColor = shown,
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 9.dp),
    ) {
        ProvideTextStyle(LocalTextStyle.current.copy(fontWeight = if (main) FontWeight.Bold else FontWeight.Normal)) {
            content()
        }
    }
}

/**
 * A dialog's button: a pill under the family's look (thick and bold when [main], in the warning colour
 * when [destructive]), the stock text button otherwise.
 */
@Composable
fun DialogButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    main: Boolean = false,
    destructive: Boolean = false,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    if (familyLook()) {
        PillButton(
            onClick = onClick,
            modifier = modifier.padding(start = 10.dp),
            main = main,
            color = if (destructive) warningColor() else SimpleTheme.colorScheme.primary,
            enabled = enabled,
            content = content,
        )
    } else {
        TextButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
    }
}

/**
 * The family's switch: an outlined pill with the background showing through and a solid dot inside it —
 * right and in the accent when on, left and in a darker, opaque accent when off. Nothing is ever filled.
 */
@Composable
fun OutlinedPillSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val on = SimpleTheme.colorScheme.primary
    val off = lerp(SimpleTheme.colorScheme.background, on, SWITCH_OFF_BLEND)
    val progress by animateFloatAsState(if (checked) 1f else 0f, tween(SWITCH_TRAVEL_MS), label = "switch")
    val color = lerp(off, on, progress).let { if (enabled) it else it.copy(alpha = DISABLED_ALPHA) }
    val toggle = if (onCheckedChange != null) {
        Modifier.toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
    } else {
        Modifier
    }
    Canvas(modifier = modifier.size(width = 52.dp, height = 28.dp).then(toggle)) {
        val stroke = 2.dp.toPx()
        val h = size.height
        drawRoundRect(
            color = color,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, h - stroke),
            cornerRadius = CornerRadius(h / 2, h / 2),
            style = Stroke(stroke),
        )
        drawCircle(color = color, radius = h * SWITCH_DOT_RADIUS, center = Offset(h / 2 + (size.width - h) * progress, h / 2))
    }
}
