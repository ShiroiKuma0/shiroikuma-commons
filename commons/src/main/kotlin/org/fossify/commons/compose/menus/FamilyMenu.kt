package org.fossify.commons.compose.menus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.fossify.commons.helpers.UiRole
import org.fossify.commons.helpers.uiColor

// Fork (7.1.3-sk3): the family's popup menu for the commons' own Compose screens. Every action row is a
// full-width outlined pill (1.25 dp) with its icon inside at the left, the icon coloured by what the row
// does, a › where the row opens something and red for a destructive row; the panel takes its ground and
// frame from the menu roles. No colour is written here — every one is a UiRole.

private const val PRESSED_ALPHA = 0.3f
private val PANEL_SHAPE = RoundedCornerShape(16.dp)

/** What a menu row does — it decides the colour of the row's icon. */
enum class MenuIconRole(internal val role: UiRole) {
    VIEW(UiRole.MENU_ICON_VIEW),
    SHARE(UiRole.MENU_ICON_SHARE),
    BLOCK(UiRole.MENU_ICON_BLOCK),
}

@Composable
@ReadOnlyComposable
internal fun roleColor(role: UiRole): Color = Color(LocalContext.current.uiColor(role))

/** A dropdown whose panel is drawn from the menu roles: the menu ground, a 2 dp menu frame, no shadow tint. */
@Composable
fun FamilyDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = PANEL_SHAPE,
        containerColor = roleColor(UiRole.MENU_GROUND),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(2.dp, roleColor(UiRole.MENU_FRAME)),
        content = content,
    )
}

/**
 * One action row of a family menu: a pill as wide as the menu. [opens] adds the › of a row that opens a
 * screen, dialog or picker; a [destructive] row lends the warning colour to frame, icon and label.
 */
@Composable
fun FamilyMenuRow(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconRole: MenuIconRole = MenuIconRole.VIEW,
    opens: Boolean = false,
    destructive: Boolean = false,
) {
    val warning = roleColor(UiRole.MENU_DESTRUCTIVE)
    val frame = if (destructive) warning else roleColor(UiRole.MENU_ROW_FRAME)
    val label = if (destructive) warning else roleColor(UiRole.MENU_LABEL)
    val iconColor = if (destructive) warning else roleColor(iconRole.role)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .clip(CircleShape)
            .border(1.25.dp, frame, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = frame.copy(alpha = PRESSED_ALPHA)),
                role = Role.Button,
                onClick = onClick,
            )
            .heightIn(min = 42.dp)
            .padding(start = 14.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text(text = text, color = label, fontSize = 16.sp, maxLines = 1, modifier = Modifier.weight(1f, fill = true))
        if (opens) {
            Spacer(Modifier.width(12.dp))
            Text(text = "›", color = label, fontSize = 20.sp)
        }
    }
}
