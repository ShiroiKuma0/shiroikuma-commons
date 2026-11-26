package org.fossify.commons.compose.screens

import androidx.compose.ui.platform.LocalContext
import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import org.fossify.commons.compose.components.familyLook
import org.fossify.commons.compose.components.PillButton
import android.content.Context
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.collections.immutable.persistentListOf
import java.util.Locale
import org.fossify.commons.compose.menus.roleColor
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.fossify.commons.R
import org.fossify.commons.compose.components.SimpleDropDownMenuItem
import org.fossify.commons.compose.extensions.*
import org.fossify.commons.compose.lists.*
import org.fossify.commons.compose.menus.ActionItem
import org.fossify.commons.compose.menus.ActionMenu
import org.fossify.commons.compose.menus.FamilyDropdownMenu
import org.fossify.commons.compose.menus.FamilyMenuRow
import org.fossify.commons.compose.menus.MenuIconRole
import org.fossify.commons.compose.menus.OverflowMode
import org.fossify.commons.compose.settings.SettingsHorizontalDivider
import org.fossify.commons.compose.settings.SettingsSwitchComponent
import org.fossify.commons.compose.system_ui_controller.rememberSystemUiController
import org.fossify.commons.compose.theme.*
import org.fossify.commons.compose.theme.model.Theme
import org.fossify.commons.extensions.darkenColor
import org.fossify.commons.extensions.getContrastColor
import org.fossify.commons.models.BlockedNumber

private const val CLICK_RESET_TIME = 250L
private const val RESET_IMMEDIATELY = 1L
private const val RESET_IDLE = -1L
private const val BETWEEN_CLICKS_TIME = 200 //time between a click which is slightly lower than the reset time
private const val ON_LONG_CLICK_LABEL = "select"

@Composable
internal fun ManageBlockedNumbersScreen(
    goBack: () -> Unit,
    onAdd: () -> Unit,
    onImportBlockedNumbers: () -> Unit,
    onExportBlockedNumbers: () -> Unit,
    setAsDefault: () -> Unit,
    isDialer: Boolean,
    hasGivenPermissionToBlock: Boolean,
    isBlockUnknownSelected: Boolean,
    showCheckmarksOnSwitches: Boolean,
    onBlockUnknownSelectedChange: (Boolean) -> Unit,
    isHiddenSelected: Boolean,
    onHiddenSelectedChange: (Boolean) -> Unit,
    blockedNumbers: ImmutableList<BlockedNumber>?,
    onDelete: (Set<Long>) -> Unit,
    onEdit: (BlockedNumber) -> Unit,
    onCopy: (BlockedNumber) -> Unit,
) {
    val selectedIds: MutableState<Set<Long>> = rememberSaveable { mutableStateOf(emptySet()) }
    val hapticFeedback = LocalHapticFeedback.current
    val isInActionMode by remember { derivedStateOf { selectedIds.value.isNotEmpty() } }
    val clearSelection = remember {
        { selectedIds.value = emptySet() }
    }
    BackHandler(isInActionMode) {
        clearSelection()
    }

    SimpleScaffold(
        darkStatusBarIcons = !isInActionMode,
        customTopBar = {
                scrolledColor: Color,
                navigationInteractionSource: MutableInteractionSource,
                scrollBehavior: TopAppBarScrollBehavior,
                statusBarColor: Int,
                colorTransitionFraction: Float,
                contrastColor: Color,
            ->
            Column {
                Crossfade(
                    targetState = isInActionMode,
                    label = "toolbar-anim",
                    animationSpec = tween(easing = FastOutLinearInEasing)
                ) { actionMode ->
                    if (actionMode && blockedNumbers != null) {
                        ActionModeToolbar(
                            selectedIdsCount = selectedIds.value.count(),
                            blockedNumbersCount = blockedNumbers.count(),
                            onBackClick = clearSelection,
                            onCopy = {
                                onCopy(blockedNumbers.first { blockedNumber -> blockedNumber.id == selectedIds.value.first() })
                                clearSelection()
                            },
                            onDelete = {
                                onDelete(selectedIds.value)
                                clearSelection()
                            },
                            onSelectAll = {
                                selectedIds.value = blockedNumbers.map { it.id }.toSet()
                            }
                        )
                    } else {
                        NonActionModeToolbar(
                            scrolledColor = scrolledColor,
                            navigationInteractionSource = navigationInteractionSource,
                            goBack = goBack,
                            scrollBehavior = scrollBehavior,
                            statusBarColor = statusBarColor,
                            colorTransitionFraction = colorTransitionFraction,
                            contrastColor = contrastColor,
                            onAdd = onAdd,
                            onImportBlockedNumbers = onImportBlockedNumbers,
                            onExportBlockedNumbers = onExportBlockedNumbers
                        )
                    }
                }

                SettingsSwitchComponent(
                    label = if (isDialer) stringResource(id = R.string.block_unknown_calls) else stringResource(id = R.string.block_unknown_messages),
                    initialValue = isBlockUnknownSelected,
                    onChange = onBlockUnknownSelectedChange,
                    modifier = Modifier.topAppBarPaddings(),
                    showCheckmark = showCheckmarksOnSwitches,
                    icon = if (familyLook()) Icons.Default.PersonOff else null,
                    iconColor = roleColor(UiRole.MENU_ICON_BLOCK),
                )
                SettingsSwitchComponent(
                    label = if (isDialer) stringResource(id = R.string.block_hidden_calls) else stringResource(id = R.string.block_hidden_messages),
                    initialValue = isHiddenSelected,
                    onChange = onHiddenSelectedChange,
                    modifier = Modifier.topAppBarPaddings(),
                    showCheckmark = showCheckmarksOnSwitches,
                    icon = if (familyLook()) Icons.Default.VisibilityOff else null,
                    iconColor = roleColor(UiRole.MENU_ICON_BLOCK),
                )
                SettingsHorizontalDivider(modifier = Modifier.topAppBarPaddings())
            }
        },
    ) { paddingValues ->
        val state = rememberLazyListState()
        val autoScrollSpeed = remember { mutableFloatStateOf(0f) }
        LaunchedEffect(autoScrollSpeed.floatValue) {
            if (autoScrollSpeed.floatValue != 0f) {
                while (isActive) {
                    state.scrollBy(autoScrollSpeed.floatValue)
                    delay(10)
                }
            }
        }
        var hasDraggingStarted by remember { mutableStateOf(false) }
        var lastClickedValue by remember { mutableStateOf<Pair<Long, BlockedNumber?>>(Pair(RESET_IDLE, null)) }
        var triggerReset by remember { mutableLongStateOf(RESET_IDLE) }
        LaunchedEffect(triggerReset) {
            if (triggerReset != RESET_IDLE) {
                delay(triggerReset)
                lastClickedValue = Pair(RESET_IDLE, null)
                triggerReset = RESET_IDLE
            }
        }
        val numbers = blockedNumbers ?: persistentListOf()
        // Fork (7.1.3-sk3): one entry of the list, drawn the same in the stock column and in the family's box
        val cell: @Composable (Int, BlockedNumber, Modifier) -> Unit = { index, blockedNumber, base ->
            val isSelected = selectedIds.value.contains(blockedNumber.id)
            BlockedNumber(
                modifier = base
                    .semantics {
                        if (!isInActionMode) {
                            onLongClick(ON_LONG_CLICK_LABEL) {
                                selectedIds.value += blockedNumber.id
                                true
                            }
                        }
                    }
                    .ifTrue(!isInActionMode) {
                        combinedClickable(onLongClick = {
                            val selectable = longPressSelectableValue(lastClickedValue, blockedNumber, triggerReset) { bNumber1, bNumber2 ->
                                updateSelectedIndices(numbers, bNumber1, bNumber2, selectedIds)
                            }
                            lastClickedValue = selectable.first
                            triggerReset = selectable.second
                        }, onClick = {
                            onEdit(blockedNumber)
                        })
                    }
                    .ifTrue(isInActionMode) {
                        combinedClickable(
                            interactionSource = rememberMutableInteractionSource(),
                            indication = null,
                            enabled = !hasDraggingStarted,
                            onLongClick = {
                                val indexOfLastValueInSelection = numbers.indexOfFirst { selectedIds.value.last() == it.id }
                                when {
                                    indexOfLastValueInSelection == index -> {}
                                    indexOfLastValueInSelection < index -> {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedIds.value += numbers
                                            .subList(indexOfLastValueInSelection, index)
                                            .map { number -> number.id }
                                    }

                                    else -> {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedIds.value += numbers
                                            .subList(index, indexOfLastValueInSelection)
                                            .map { number -> number.id }
                                    }
                                }
                            },
                            onClick = {
                                if (isSelected) {
                                    selectedIds.value -= blockedNumber.id
                                } else {
                                    selectedIds.value += blockedNumber.id
                                }
                            }
                        )
                    },
                blockedNumber = blockedNumber,
                onDelete = onDelete,
                onCopy = onCopy,
                isSelected = isSelected
            )
        }
        if (familyLook()) {
            FamilyBlockedNumbers(
                state = state,
                paddingValues = paddingValues,
                numbers = numbers,
                loaded = blockedNumbers != null,
                hasGivenPermissionToBlock = hasGivenPermissionToBlock,
                setAsDefault = setAsDefault,
                onAdd = onAdd,
                dragHandler = { keyAt ->
                    listDragHandlerLongKey(
                        isScrollingUp = state.isScrollingUp(),
                        lazyListState = state,
                        haptics = hapticFeedback,
                        selectedIds = selectedIds,
                        autoScrollSpeed = autoScrollSpeed,
                        autoScrollThreshold = with(LocalDensity.current) { 40.dp.toPx() },
                        dragUpdate = { isDraggingStarted ->
                            hasDraggingStarted = isDraggingStarted
                            triggerReset = RESET_IMMEDIATELY
                        },
                        ids = numbers.map { blockedNumber -> blockedNumber.id },
                        keyAt = keyAt,
                    )
                },
                cell = cell,
            )
        } else
        LazyColumn(
            state = state,
            modifier = Modifier.ifFalse(blockedNumbers.isNullOrEmpty()) {
                listDragHandlerLongKey(
                    isScrollingUp = state.isScrollingUp(),
                    lazyListState = state,
                    haptics = hapticFeedback,
                    selectedIds = selectedIds,
                    autoScrollSpeed = autoScrollSpeed,
                    autoScrollThreshold = with(LocalDensity.current) { 40.dp.toPx() },
                    dragUpdate = { isDraggingStarted ->
                        hasDraggingStarted = isDraggingStarted
                        triggerReset = RESET_IMMEDIATELY
                    },
                    ids = blockedNumbers?.map { blockedNumber -> blockedNumber.id }.orEmpty()
                )
            },
            // Fork (7.1.3-sk3): no space between the rows under the family's look
            verticalArrangement = Arrangement.spacedBy(if (familyLook()) 0.dp else SimpleTheme.dimens.padding.extraSmall),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding())
        ) {
            when {
                !hasGivenPermissionToBlock -> {
                    noPermissionToBlock(setAsDefault = setAsDefault)
                }

                blockedNumbers == null -> {}

                blockedNumbers.isEmpty() -> {
                    emptyBlockedNumbers(addABlockedNumber = onAdd)
                }

                hasGivenPermissionToBlock && blockedNumbers.isNotEmpty() -> {
                    itemsIndexed(blockedNumbers, key = { _, blockedNumber -> blockedNumber.id }) { index, blockedNumber ->
                        cell(index, blockedNumber, Modifier.animateItem())
                    }
                }
            }
        }
    }
}

private const val TWO_COLUMNS_FROM_DP = 520

/**
 * Fork (7.1.3-sk3): the family's list of blocked numbers — an outlined box that fills the rest of the
 * screen, its heading "Blocked numbers (N)" on the top edge, the numbers scrolling inside it; every second
 * row faintly tinted, a hairline between rows, and two columns on a wide screen. Every colour is a UiRole.
 */
@Composable
private fun FamilyBlockedNumbers(
    state: LazyListState,
    paddingValues: PaddingValues,
    numbers: ImmutableList<BlockedNumber>,
    loaded: Boolean,
    hasGivenPermissionToBlock: Boolean,
    setAsDefault: () -> Unit,
    onAdd: () -> Unit,
    dragHandler: @Composable Modifier.(keyAt: (Offset) -> Long?) -> Modifier,
    cell: @Composable (Int, BlockedNumber, Modifier) -> Unit,
) {
    val columns = if (LocalConfiguration.current.screenWidthDp >= TWO_COLUMNS_FROM_DP) 2 else 1
    val rows = remember(numbers, columns) { numbers.chunked(columns) }
    val currentRows by rememberUpdatedState(rows)
    val currentColumns by rememberUpdatedState(columns)
    val showsNumbers = hasGivenPermissionToBlock && numbers.isNotEmpty()
    val frame = roleColor(UiRole.LIST_BOX_FRAME)
    val divider = roleColor(UiRole.LIST_DIVIDER)
    val alternate = roleColor(UiRole.LIST_ROW_ALT)
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = paddingValues.calculateBottomPadding() + 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)
                .border(1.25.dp, frame, shape)
                .clip(shape)
        ) {
            LazyColumn(
                state = state,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 12.dp, bottom = 2.dp)
                    .ifTrue(showsNumbers) {
                        dragHandler { point ->
                            state.layoutInfo.visibleItemsInfo
                                .firstOrNull { point.y.toInt() in it.offset..it.offset + it.size }
                                ?.let { item ->
                                    val column = (point.x / (state.layoutInfo.viewportSize.width / currentColumns)).toInt()
                                    currentRows.getOrNull(item.index)?.getOrNull(column.coerceIn(0, currentColumns - 1))?.id
                                }
                        }
                    },
            ) {
                when {
                    !hasGivenPermissionToBlock -> noPermissionToBlock(setAsDefault = setAsDefault)
                    !loaded -> {}
                    numbers.isEmpty() -> emptyBlockedNumbers(addABlockedNumber = onAdd)
                    else -> itemsIndexed(rows, key = { _, row -> row.first().id }) { rowIndex, row ->
                        Row(
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .background(if (rowIndex % 2 == 1) alternate else Color.Transparent)
                                .drawBehind {
                                    if (rowIndex < rows.lastIndex) {
                                        drawLine(divider, Offset(0f, size.height - 0.5f), Offset(size.width, size.height - 0.5f), strokeWidth = 1f)
                                    }
                                    if (columns > 1) {
                                        drawLine(divider, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 1f)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            row.forEachIndexed { column, blockedNumber ->
                                cell(rowIndex * columns + column, blockedNumber, Modifier.weight(1f))
                            }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
        if (hasGivenPermissionToBlock && loaded) {
            Text(
                text = stringResource(id = R.string.blocked_numbers_count, numbers.size),
                color = roleColor(UiRole.LIST_BOX_HEADING),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier
                    .padding(start = 14.dp)
                    .background(roleColor(UiRole.BACKGROUND))
                    .padding(horizontal = 6.dp),
            )
        }
    }
}

/**
 * A stored number spaced for reading, the groups joined by hyphens: +420222292055 → +420-222-292-055.
 * Anything that is not a plain number (a pattern with a wildcard, a name) is shown as it is stored.
 */
internal fun String.formatBlockedNumber(region: String): String {
    if (!PLAIN_NUMBER.matches(this)) return this
    val formatted = PhoneNumberUtils.formatNumber(this, region) ?: return this
    return formatted.replace("(", "").replace(")", "").trim().replace(GROUP_GAP, "-")
}

private val PLAIN_NUMBER = Regex("^\\+?[0-9]{5,}$")
private val GROUP_GAP = Regex("[\\s-]+")

private fun Context.numberRegion(): String {
    val telephony = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    val iso = telephony?.networkCountryIso?.takeIf { it.isNotEmpty() } ?: telephony?.simCountryIso?.takeIf { it.isNotEmpty() }
    return (iso ?: Locale.getDefault().country).uppercase(Locale.ROOT)
}

private fun updateSelectedIndices(
    blockedNumbers: ImmutableList<BlockedNumber>,
    bNumber1: BlockedNumber,
    bNumber2: BlockedNumber,
    selectedIds: MutableState<Set<Long>>,
) {
    val indices = listOf(blockedNumbers.indexOf(bNumber1), blockedNumbers.indexOf(bNumber2))
    selectedIds.value += blockedNumbers
        .subList(indices.minOrNull()!!, indices.maxOrNull()!! + 1)
        .map { number -> number.id }
}

private fun longPressSelectableValue(
    lastClickedValue: Pair<Long, BlockedNumber?>,
    blockedNumber: BlockedNumber,
    triggerReset: Long,
    select: (BlockedNumber, BlockedNumber) -> Unit,
): Pair<Pair<Long, BlockedNumber?>, Long> {
    var lastClickedValueTemp = lastClickedValue
    var triggerResetTemp = triggerReset
    if (lastClickedValueTemp.first == RESET_IDLE) {
        lastClickedValueTemp = Pair(System.currentTimeMillis(), blockedNumber)
        triggerResetTemp = CLICK_RESET_TIME
    } else {
        if (lastClickedValueTemp.first + BETWEEN_CLICKS_TIME > System.currentTimeMillis()) {
            val firstValue = lastClickedValueTemp
            select(blockedNumber, firstValue.second!!)
            lastClickedValueTemp = Pair(RESET_IDLE, null)
        }
    }
    return lastClickedValueTemp to triggerResetTemp
}

@Composable
private fun BlockedNumber(
    modifier: Modifier = Modifier,
    blockedNumber: BlockedNumber,
    onDelete: (Set<Long>) -> Unit,
    onCopy: (BlockedNumber) -> Unit,
    isSelected: Boolean,
) {
    val hasContactName = blockedNumber.contactName != null
    val contactNameContent = remember {
        movableContentOf {
            Text(
                text = blockedNumber.contactName.toString(),
            )
        }
    }
    val blockedNumberContent = remember {
        movableContentOf {
            BlockedNumberHeadlineContent(blockedNumber = blockedNumber, hasContactName = hasContactName)
        }
    }
    // Fork (7.1.3-sk3): under the family's look a row is as tall as its text — no 56 dp list item, no
    // 48 dp touch box round the ⋮ — with 2 dp above and below.
    if (familyLook()) {
        val ground = if (isSelected) roleColor(UiRole.LIST_SELECTED) else Color.Transparent
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(ground)
                .padding(start = 16.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val context = LocalContext.current
            val shownNumber = remember(blockedNumber.number) { blockedNumber.number.formatBlockedNumber(context.numberRegion()) }
            Column(modifier = Modifier.weight(1f)) {
                if (hasContactName) {
                    Text(
                        text = blockedNumber.contactName.toString(),
                        color = roleColor(UiRole.LIST_TEXT),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Text(
                    text = shownNumber,
                    color = roleColor(if (hasContactName) UiRole.LIST_TEXT_SECONDARY else UiRole.LIST_TEXT),
                    style = if (hasContactName) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                )
            }
            BlockedNumberTrailingContent(onDelete = {
                onDelete(setOf(blockedNumber.id))
            }, onCopy = {
                onCopy(blockedNumber)
            })
        }
    } else ListItem(
        modifier = modifier,
        headlineContent = {
            if (hasContactName) {
                contactNameContent()
            } else {
                blockedNumberContent()
            }
        },
        supportingContent = {
            if (hasContactName) {
                blockedNumberContent()
            }
        },
        trailingContent = {
            BlockedNumberTrailingContent(onDelete = {
                onDelete(setOf(blockedNumber.id))
            }, onCopy = {
                onCopy(blockedNumber)
            })
        },
        colors = blockedNumberListItemColors(
            isSelected = isSelected
        )
    )
}

@Composable
private fun blockedNumberListItemColors(
    isSelected: Boolean,
) = ListItemDefaults.colors(
    containerColor = if (isSelected) {
        if (LocalTheme.current is Theme.SystemDefaultMaterialYou) {
            Color(SimpleTheme.colorScheme.primaryContainer.toArgb().darkenColor()).copy(alpha = 0.8f)
        } else {
            SimpleTheme.colorScheme.primary.copy(alpha = 0.3f)
        }
    } else {
        SimpleTheme.colorScheme.surface
    },
    trailingIconColor = iconsColor
)


@Composable
private fun BlockedNumberHeadlineContent(modifier: Modifier = Modifier, blockedNumber: BlockedNumber, hasContactName: Boolean) {
    Text(
        text = blockedNumber.number,
        modifier = modifier,
        color = if (hasContactName) LocalContentColor.current.copy(alpha = 0.7f) else LocalContentColor.current
    )
}

@Composable
private fun BlockedNumberTrailingContent(modifier: Modifier = Modifier, onDelete: () -> Unit, onCopy: () -> Unit) {
    var isMenuVisible by remember { mutableStateOf(false) }
    val dismissMenu = remember {
        {
            isMenuVisible = false
        }
    }
    // Fork (7.1.3-sk3): the family's menu — full-width icon pills, the icon coloured by what the row does
    if (familyLook()) {
        Box(modifier = modifier) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = stringResource(id = R.string.more_options),
                tint = iconsColor,
                modifier = Modifier
                    .clickable(
                        interactionSource = rememberMutableInteractionSource(),
                        indication = ripple(bounded = false),
                        onClick = { isMenuVisible = true },
                    )
                    .padding(horizontal = 16.dp)
                    .size(24.dp),
            )
            FamilyDropdownMenu(expanded = isMenuVisible, onDismissRequest = dismissMenu) {
                FamilyMenuRow(
                    text = stringResource(id = R.string.copy_number_to_clipboard),
                    icon = Icons.Default.ContentCopy,
                    iconRole = MenuIconRole.SHARE,
                    onClick = {
                        onCopy()
                        dismissMenu()
                    },
                )
                FamilyMenuRow(
                    text = stringResource(id = R.string.unblock),
                    icon = Icons.Default.Block,
                    iconRole = MenuIconRole.BLOCK,
                    onClick = {
                        onDelete()
                        dismissMenu()
                    },
                )
            }
        }
        return
    }
    DropdownMenu(
        //https://github.com/JetBrains/compose-multiplatform/issues/1878 same in M3, remove the top and bottom margin blocker
        expanded = isMenuVisible,
        onDismissRequest = dismissMenu,
        modifier = modifier
    ) {
        SimpleDropDownMenuItem(onClick = {
            onCopy()
            dismissMenu()
        }, text = {
            Text(
                text = stringResource(id = R.string.copy_number_to_clipboard),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        })
        SimpleDropDownMenuItem(onClick = {
            onDelete()
            dismissMenu()
        }, text = {
            Text(
                text = stringResource(id = R.string.unblock),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        })
    }
    IconButton(onClick = {
        isMenuVisible = true
    }) {
        Icon(Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.more_options), tint = iconsColor)
    }
}

@Composable
private fun ActionModeToolbar(
    modifier: Modifier = Modifier,
    selectedIdsCount: Int,
    blockedNumbersCount: Int,
    onBackClick: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onSelectAll: () -> Unit,
) {
    val systemUiController = rememberSystemUiController()
    val navigationIconInteractionSource = rememberMutableInteractionSource()
    val bgColor = actionModeBgColor()
    val textColor by remember {
        derivedStateOf { Color(bgColor.toArgb().getContrastColor()) }
    }
    DisposableEffect(systemUiController, bgColor) {
        systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = bgColor.isLitWell())
        onDispose {}
    }
    TopAppBar(
        title = {
            Box(
                modifier = Modifier
                    .clickable {
                        if (selectedIdsCount == blockedNumbersCount) {
                            onBackClick()
                        } else {
                            onSelectAll()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedIdsCount != 0) {
                    Text(
                        text = "$selectedIdsCount / $blockedNumbersCount",
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

        },
        navigationIcon = {
            SimpleNavigationIcon(navigationIconInteractionSource = navigationIconInteractionSource, goBack = onBackClick, iconColor = textColor)
        },
        actions = {
            BlockedNumberActionMenu(selectedIdsCount = selectedIdsCount, onDelete = onDelete, onCopy = onCopy, iconColor = textColor)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = bgColor
        ),
        modifier = modifier.topAppBarPaddings(),
        windowInsets = topAppBarInsets()
    )
}


@Composable
@ReadOnlyComposable
private fun actionModeBgColor(): Color =
    Color(LocalContext.current.uiColor(UiRole.ACTION_BAR_GROUND))


@Composable
private fun BlockedNumberActionMenu(
    selectedIdsCount: Int,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    iconColor: Color? = null,
) {
    val actionMenus = remember(selectedIdsCount) {
        val delete =
            ActionItem(
                nameRes = R.string.unblock,
                doAction = onDelete,
                overflowMode = OverflowMode.ALWAYS_OVERFLOW,
                role = MenuIconRole.BLOCK,
                overflowIcon = Icons.Default.Block,
            )

        val list = if (selectedIdsCount == 1) {
            listOf(
                ActionItem(
                    nameRes = R.string.copy,
                    icon = Icons.Default.ContentCopy,
                    doAction = onCopy,
                    overflowMode = OverflowMode.NEVER_OVERFLOW,
                    iconColor = iconColor
                ),
                delete
            )
        } else {
            listOf(delete)
        }
        list.toImmutableList()
    }
    var isMenuVisible by remember { mutableStateOf(false) }
    ActionMenu(
        items = actionMenus,
        numIcons = if (selectedIdsCount == 1) 2 else 1,
        isMenuVisible = isMenuVisible,
        onMenuToggle = { isMenuVisible = it },
        iconsColor = iconColor
    )
}

@Composable
private fun NonActionModeToolbar(
    scrolledColor: Color,
    navigationInteractionSource: MutableInteractionSource,
    goBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    statusBarColor: Int,
    colorTransitionFraction: Float,
    contrastColor: Color,
    onAdd: () -> Unit,
    onImportBlockedNumbers: () -> Unit,
    onExportBlockedNumbers: () -> Unit,
) {
    SimpleScaffoldTopBar(
        title = { scrolledTextColor ->
            Text(
                text = stringResource(id = R.string.manage_blocked_numbers),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = scrolledTextColor
            )
        },
        scrolledColor = scrolledColor,
        navigationIconInteractionSource = navigationInteractionSource,
        goBack = goBack,
        scrollBehavior = scrollBehavior,
        statusBarColor = statusBarColor,
        colorTransitionFraction = colorTransitionFraction,
        contrastColor = contrastColor,
        actions = {
            val actionMenus = remember {
                listOf(
                    ActionItem(
                        R.string.add_a_blocked_number, icon = Icons.Filled.Add, doAction = onAdd, overflowMode = OverflowMode.ALWAYS_OVERFLOW,
                        role = MenuIconRole.BLOCK, opens = true,
                    ),
                    ActionItem(
                        R.string.import_blocked_numbers, doAction = onImportBlockedNumbers, overflowMode = OverflowMode.ALWAYS_OVERFLOW,
                        role = MenuIconRole.SHARE, opens = true, overflowIcon = Icons.Filled.FileDownload,
                    ),
                    ActionItem(
                        R.string.export_blocked_numbers, doAction = onExportBlockedNumbers, overflowMode = OverflowMode.ALWAYS_OVERFLOW,
                        role = MenuIconRole.SHARE, opens = true, overflowIcon = Icons.Filled.FileUpload,
                    ),
                ).toImmutableList()
            }
            var isMenuVisible by remember { mutableStateOf(false) }
            ActionMenu(items = actionMenus, numIcons = 2, isMenuVisible = isMenuVisible, onMenuToggle = { isMenuVisible = it }, iconsColor = scrolledColor)
        }
    )
}

private fun LazyListScope.emptyBlockedNumbers(
    addABlockedNumber: () -> Unit,
) {
    item {
        Text(
            text = stringResource(id = R.string.not_blocking_anyone),
            style = TextStyle(fontStyle = FontStyle.Italic, textAlign = TextAlign.Center, color = SimpleTheme.colorScheme.onSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = SimpleTheme.dimens.padding.extraLarge,
                    bottom = SimpleTheme.dimens.padding.medium
                )
                .padding(horizontal = SimpleTheme.dimens.padding.extraLarge)
        )
    }
    item {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Fork (sk15): the screen's one action, as the family's main pill
            if (familyLook()) PillButton(onClick = addABlockedNumber, main = true) {
                Text(
                    text = stringResource(id = R.string.add_a_blocked_number),
                )
            } else OutlinedButton(onClick = addABlockedNumber) {
                Text(
                    text = stringResource(id = R.string.add_a_blocked_number),
                )
            }
        }
    }
}

private fun LazyListScope.noPermissionToBlock(
    setAsDefault: () -> Unit,
) {
    item {
        Text(
            text = stringResource(id = R.string.must_make_default_dialer),
            style = TextStyle(fontStyle = FontStyle.Italic, textAlign = TextAlign.Center),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = SimpleTheme.dimens.padding.extraLarge,
                    bottom = SimpleTheme.dimens.padding.medium
                )
                .padding(horizontal = SimpleTheme.dimens.padding.extraLarge)
        )
    }
    item {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Fork (sk15): never a filled button under the family's look
            if (familyLook()) PillButton(onClick = setAsDefault, main = true) {
                Text(
                    text = stringResource(id = R.string.set_as_default),
                )
            } else Button(onClick = setAsDefault) {
                Text(
                    text = stringResource(id = R.string.set_as_default),
                )
            }
        }
    }
}

@MyDevices
@Composable
private fun ManageBlockedNumbersScreenPreview(@PreviewParameter(BooleanPreviewParameterProvider::class) isDialer: Boolean) {
    AppThemeSurface {
        ManageBlockedNumbersScreen(
            goBack = {},
            onAdd = {},
            onImportBlockedNumbers = {},
            onExportBlockedNumbers = {},
            setAsDefault = {},
            isDialer = isDialer,
            hasGivenPermissionToBlock = !isDialer,
            isBlockUnknownSelected = false,
            showCheckmarksOnSwitches = false,
            onBlockUnknownSelectedChange = {},
            isHiddenSelected = false,
            onHiddenSelectedChange = {},
            blockedNumbers = listOf(
                BlockedNumber(id = 1, number = "000000000", normalizedNumber = "000000000", numberToCompare = "000000000", contactName = "Test"),
                BlockedNumber(id = 2, number = "111111111", normalizedNumber = "111111111", numberToCompare = "111111111"),
                BlockedNumber(id = 3, number = "5555555555", normalizedNumber = "5555555555", numberToCompare = "5555555555"),
                BlockedNumber(id = 4, number = "1234567890", normalizedNumber = "1234567890", numberToCompare = "1234567890"),
                BlockedNumber(id = 5, number = "9876543210", normalizedNumber = "9876543210", numberToCompare = "9876543210", contactName = "Test"),
                BlockedNumber(id = 6, number = "9998887777", normalizedNumber = "9998887777", numberToCompare = "9998887777"),
                BlockedNumber(id = 7, number = "2223334444", normalizedNumber = "2223334444", numberToCompare = "2223334444"),
                BlockedNumber(id = 8, number = "5552221111", normalizedNumber = "5552221111", numberToCompare = "5552221111")
            ).toImmutableList(),
            onDelete = {},
            onEdit = {}
        ) {}
    }
}
