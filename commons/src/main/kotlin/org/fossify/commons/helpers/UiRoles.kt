package org.fossify.commons.helpers

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.graphics.ColorUtils
import org.fossify.commons.R
import org.fossify.commons.extensions.baseConfig

/**
 * shiroikuma (sk18): the colour role table of the commons. **Every colour the commons draws comes from
 * here** — no layout, drawable, style or theme of the commons names one. A role is one thing 白い熊 can
 * see as a separate item (a button's text, its frame and its ground are three roles), settable on the
 * consuming app's UI page, in the section of its item.
 *
 * The two primaries below are the family's defaults and are written here ONCE; every other default is
 * derived from the three foundation roles, so a fresh install is black and yellow and an app that only
 * changes the foundation still repaints everything that was not set by itself.
 *
 * Stored in the app's own preferences (the file BaseConfig wraps) under `ui_role_<key>`, so the roles
 * travel in the app's settings export. An absent key means "the default".
 */
const val FAMILY_YELLOW = 0xFFFFFF00.toInt()
const val FAMILY_BLACK = 0xFF000000.toInt()

/** Colours that carry meaning keep their settled defaults. */
private const val FAMILY_RED = 0xFFFF0000.toInt()
private const val FAMILY_ORANGE = 0xFFFF8000.toInt()
private const val FAMILY_ROSE = 0xFFFF3D7F.toInt()

private const val SWITCH_OFF_BLEND = 0.55f
private const val DIVIDER_BLEND = 0.7f
private const val HINT_BLEND = 0.5f
private const val MARK_OFF_BLEND = 0.6f
private const val SELECTED_BLEND = 0.27f
private const val ROW_ALT_BLEND = 0.07f
private const val SECONDARY_BLEND = 0.7f
private const val SELECTION_ALPHA = 0x66

enum class UiRoleSection(@StringRes val labelRes: Int) {
    FOUNDATION(R.string.ui_roles_foundation),
    TEXT(R.string.ui_roles_text),
    FIELDS(R.string.ui_roles_fields),
    BUTTONS(R.string.ui_roles_buttons),
    DIALOGS(R.string.ui_roles_dialogs),
    SWITCHES(R.string.ui_roles_switches),
    MARKS(R.string.ui_roles_marks),
    MENUS(R.string.ui_roles_menus),
    LISTS(R.string.ui_roles_lists),
    BARS(R.string.ui_roles_bars),
    TOASTS(R.string.ui_roles_toasts),
}

/** What a default is worked out from: the three foundation roles and any other role. */
class UiRoleDefaults internal constructor(private val context: Context) {
    val background: Int get() = context.uiColor(UiRole.BACKGROUND)
    val accent: Int get() = context.uiColor(UiRole.ACCENT)
    val text: Int get() = context.uiColor(UiRole.TEXT)
    val config: BaseConfig get() = context.baseConfig

    fun of(role: UiRole) = context.uiColor(role)

    /** An opaque colour [ratio] of the way from the background to [color] — never a faded one. */
    fun toward(color: Int, ratio: Float) = ColorUtils.blendARGB(background, color, ratio)
}

enum class UiRole(
    val key: String,
    val section: UiRoleSection,
    @StringRes val labelRes: Int,
    private val fallback: UiRoleDefaults.() -> Int,
) {
    // Foundation — what every other default is derived from. Kept in the stock preference keys.
    BACKGROUND("background", UiRoleSection.FOUNDATION, R.string.ui_role_background, { FAMILY_BLACK }),
    ACCENT("accent", UiRoleSection.FOUNDATION, R.string.ui_role_accent, { FAMILY_YELLOW }),
    TEXT("text", UiRoleSection.FOUNDATION, R.string.ui_role_text, { FAMILY_YELLOW }),

    // Text that is only read
    TEXT_LINK("text_link", UiRoleSection.TEXT, R.string.ui_role_text_link, { accent }),

    // Text fields
    FIELD_TEXT("field_text", UiRoleSection.FIELDS, R.string.ui_role_field_text, { text }),
    FIELD_HINT("field_hint", UiRoleSection.FIELDS, R.string.ui_role_field_hint, { toward(text, HINT_BLEND) }),
    FIELD_LINE("field_line", UiRoleSection.FIELDS, R.string.ui_role_field_line, { accent }),
    FIELD_LINE_IDLE("field_line_idle", UiRoleSection.FIELDS, R.string.ui_role_field_line_idle, { toward(text, HINT_BLEND) }),
    FIELD_LABEL("field_label", UiRoleSection.FIELDS, R.string.ui_role_field_label, { accent }),
    FIELD_CURSOR("field_cursor", UiRoleSection.FIELDS, R.string.ui_role_field_cursor, { accent }),
    FIELD_SELECTION(
        "field_selection", UiRoleSection.FIELDS, R.string.ui_role_field_selection,
        { ColorUtils.setAlphaComponent(accent, SELECTION_ALPHA) },
    ),
    SPINNER_TEXT("spinner_text", UiRoleSection.FIELDS, R.string.ui_role_spinner_text, { text }),
    SPINNER_ARROW("spinner_arrow", UiRoleSection.FIELDS, R.string.ui_role_spinner_arrow, { text }),
    SPINNER_GROUND("spinner_ground", UiRoleSection.FIELDS, R.string.ui_role_spinner_ground, { background }),
    SLIDER_TRACK("slider_track", UiRoleSection.FIELDS, R.string.ui_role_slider_track, { accent }),
    SLIDER_THUMB("slider_thumb", UiRoleSection.FIELDS, R.string.ui_role_slider_thumb, { accent }),

    // Buttons — always outlined pills
    BUTTON_TEXT("button_text", UiRoleSection.BUTTONS, R.string.ui_role_button_text, { accent }),
    BUTTON_FRAME("button_frame", UiRoleSection.BUTTONS, R.string.ui_role_button_frame, { accent }),
    BUTTON_GROUND("button_ground", UiRoleSection.BUTTONS, R.string.ui_role_button_ground, { background }),
    BUTTON_DESTRUCTIVE("button_destructive", UiRoleSection.BUTTONS, R.string.ui_role_button_destructive, { FAMILY_RED }),
    FAB_ICON("fab_icon", UiRoleSection.BUTTONS, R.string.ui_role_fab_icon, { accent }),
    FAB_FRAME("fab_frame", UiRoleSection.BUTTONS, R.string.ui_role_fab_frame, { accent }),
    FAB_GROUND("fab_ground", UiRoleSection.BUTTONS, R.string.ui_role_fab_ground, { background }),

    // Dialogs
    DIALOG_GROUND("dialog_ground", UiRoleSection.DIALOGS, R.string.ui_role_dialog_ground, { background }),
    DIALOG_FRAME("dialog_frame", UiRoleSection.DIALOGS, R.string.ui_role_dialog_frame, { accent }),
    DIALOG_TITLE("dialog_title", UiRoleSection.DIALOGS, R.string.ui_role_dialog_title, { text }),
    DIALOG_TEXT("dialog_text", UiRoleSection.DIALOGS, R.string.ui_role_dialog_text, { text }),
    DIALOG_BUTTON_TEXT("dialog_button_text", UiRoleSection.DIALOGS, R.string.ui_role_dialog_button_text, { of(BUTTON_TEXT) }),
    DIALOG_BUTTON_FRAME("dialog_button_frame", UiRoleSection.DIALOGS, R.string.ui_role_dialog_button_frame, { of(BUTTON_FRAME) }),
    DIALOG_BUTTON_GROUND("dialog_button_ground", UiRoleSection.DIALOGS, R.string.ui_role_dialog_button_ground, { of(DIALOG_GROUND) }),

    // The date picker drawn by the commons (views/FamilyDatePicker)
    PICKER_DAY("picker_day", UiRoleSection.DIALOGS, R.string.ui_role_picker_day, { of(DIALOG_TEXT) }),
    PICKER_DAY_CHOSEN("picker_day_chosen", UiRoleSection.DIALOGS, R.string.ui_role_picker_day_chosen, { accent }),
    PICKER_TODAY("picker_today", UiRoleSection.DIALOGS, R.string.ui_role_picker_today, { toward(accent, DIVIDER_BLEND) }),
    PICKER_WEEKDAY("picker_weekday", UiRoleSection.DIALOGS, R.string.ui_role_picker_weekday, { toward(text, MARK_OFF_BLEND) }),
    PICKER_HEADING("picker_heading", UiRoleSection.DIALOGS, R.string.ui_role_picker_heading, { of(DIALOG_TITLE) }),
    PICKER_ARROW("picker_arrow", UiRoleSection.DIALOGS, R.string.ui_role_picker_arrow, { of(BUTTON_TEXT) }),
    PICKER_ARROW_FRAME("picker_arrow_frame", UiRoleSection.DIALOGS, R.string.ui_role_picker_arrow_frame, { of(BUTTON_FRAME) }),
    // …and the time picker (views/FamilyTimePicker): its digits, the colon between them, the AM / PM mark.
    // Its arrows are the date picker's.
    PICKER_TIME_DIGITS("picker_time_digits", UiRoleSection.DIALOGS, R.string.ui_role_picker_time_digits, { of(DIALOG_TITLE) }),
    PICKER_TIME_SEPARATOR(
        "picker_time_separator", UiRoleSection.DIALOGS, R.string.ui_role_picker_time_separator,
        { toward(text, MARK_OFF_BLEND) },
    ),
    PICKER_AM_PM("picker_am_pm", UiRoleSection.DIALOGS, R.string.ui_role_picker_am_pm, { of(BUTTON_TEXT) }),

    // Switches — an outline and a dot, on and off
    SWITCH_ON_OUTLINE("switch_on_outline", UiRoleSection.SWITCHES, R.string.ui_role_switch_on_outline, { accent }),
    SWITCH_ON_DOT("switch_on_dot", UiRoleSection.SWITCHES, R.string.ui_role_switch_on_dot, { accent }),
    SWITCH_OFF_OUTLINE(
        "switch_off_outline", UiRoleSection.SWITCHES, R.string.ui_role_switch_off_outline,
        { toward(accent, SWITCH_OFF_BLEND) },
    ),
    SWITCH_OFF_DOT("switch_off_dot", UiRoleSection.SWITCHES, R.string.ui_role_switch_off_dot, { of(SWITCH_OFF_OUTLINE) }),
    SWITCH_LABEL("switch_label", UiRoleSection.SWITCHES, R.string.ui_role_switch_label, { text }),

    // Selection marks — check boxes and radio buttons
    CHECK_ON("check_on", UiRoleSection.MARKS, R.string.ui_role_check_on, { accent }),
    CHECK_OFF("check_off", UiRoleSection.MARKS, R.string.ui_role_check_off, { toward(text, MARK_OFF_BLEND) }),
    CHECK_LABEL("check_label", UiRoleSection.MARKS, R.string.ui_role_check_label, { text }),
    RADIO_ON("radio_on", UiRoleSection.MARKS, R.string.ui_role_radio_on, { accent }),
    RADIO_OFF("radio_off", UiRoleSection.MARKS, R.string.ui_role_radio_off, { toward(text, MARK_OFF_BLEND) }),
    RADIO_LABEL("radio_label", UiRoleSection.MARKS, R.string.ui_role_radio_label, { text }),

    // Menus — the popup itself; an app's own row view adds its icon roles
    MENU_GROUND("menu_ground", UiRoleSection.MENUS, R.string.ui_role_menu_ground, { background }),
    MENU_FRAME("menu_frame", UiRoleSection.MENUS, R.string.ui_role_menu_frame, { accent }),
    MENU_LABEL("menu_label", UiRoleSection.MENUS, R.string.ui_role_menu_label, { text }),
    MENU_ROW_FRAME("menu_row_frame", UiRoleSection.MENUS, R.string.ui_role_menu_row_frame, { accent }),
    MENU_DIVIDER("menu_divider", UiRoleSection.MENUS, R.string.ui_role_menu_divider, { toward(accent, DIVIDER_BLEND) }),
    // 7.1.3-sk3: the icon roles of the commons' own Compose menus — an icon is coloured by what its row does
    MENU_ICON_VIEW("menu_icon_view", UiRoleSection.MENUS, R.string.ui_role_menu_icon_view, { accent }),
    MENU_ICON_SHARE("menu_icon_share", UiRoleSection.MENUS, R.string.ui_role_menu_icon_share, { FAMILY_ORANGE }),
    MENU_ICON_BLOCK("menu_icon_block", UiRoleSection.MENUS, R.string.ui_role_menu_icon_block, { FAMILY_ROSE }),
    MENU_DESTRUCTIVE("menu_destructive", UiRoleSection.MENUS, R.string.ui_role_menu_destructive, { of(BUTTON_DESTRUCTIVE) }),

    // Lists
    LIST_DIVIDER("list_divider", UiRoleSection.LISTS, R.string.ui_role_list_divider, { toward(accent, DIVIDER_BLEND) }),
    LIST_SELECTED("list_selected", UiRoleSection.LISTS, R.string.ui_role_list_selected, { toward(accent, SELECTED_BLEND) }),
    LIST_PRESSED("list_pressed", UiRoleSection.LISTS, R.string.ui_role_list_pressed, { toward(accent, SELECTED_BLEND) }),
    // 7.1.3-sk3: a list the commons draws by itself (the blocked numbers)
    LIST_TEXT("list_text", UiRoleSection.LISTS, R.string.ui_role_list_text, { text }),
    LIST_TEXT_SECONDARY("list_text_secondary", UiRoleSection.LISTS, R.string.ui_role_list_text_secondary, { toward(text, SECONDARY_BLEND) }),
    LIST_ROW_ALT("list_row_alt", UiRoleSection.LISTS, R.string.ui_role_list_row_alt, { toward(accent, ROW_ALT_BLEND) }),
    LIST_BOX_FRAME("list_box_frame", UiRoleSection.LISTS, R.string.ui_role_list_box_frame, { accent }),
    LIST_BOX_HEADING("list_box_heading", UiRoleSection.LISTS, R.string.ui_role_list_box_heading, { text }),
    SCROLL_THUMB("scroll_thumb", UiRoleSection.LISTS, R.string.ui_role_scroll_thumb, { accent }),
    SCROLL_BUBBLE("scroll_bubble", UiRoleSection.LISTS, R.string.ui_role_scroll_bubble, { accent }),
    SCROLL_BUBBLE_TEXT("scroll_bubble_text", UiRoleSection.LISTS, R.string.ui_role_scroll_bubble_text, { background }),

    // Bars
    STATUS_BAR("status_bar", UiRoleSection.BARS, R.string.ui_role_status_bar, { background }),
    NAVIGATION_BAR("navigation_bar", UiRoleSection.BARS, R.string.ui_role_navigation_bar, { background }),
    TOOLBAR_GROUND("toolbar_ground", UiRoleSection.BARS, R.string.ui_role_toolbar_ground, { background }),
    TOOLBAR_TITLE("toolbar_title", UiRoleSection.BARS, R.string.ui_role_toolbar_title, { text }),
    TOOLBAR_ICON("toolbar_icon", UiRoleSection.BARS, R.string.ui_role_toolbar_icon, { text }),
    ACTION_BAR_GROUND("action_bar_ground", UiRoleSection.BARS, R.string.ui_role_action_bar_ground, { background }),
    ACTION_BAR_TEXT("action_bar_text", UiRoleSection.BARS, R.string.ui_role_action_bar_text, { accent }),
    ACTION_BAR_ICON("action_bar_icon", UiRoleSection.BARS, R.string.ui_role_action_bar_icon, { accent }),
    BOTTOM_BAR_GROUND("bottom_bar_ground", UiRoleSection.BARS, R.string.ui_role_bottom_bar_ground, { background }),
    BOTTOM_TAB_ACTIVE("bottom_tab_active", UiRoleSection.BARS, R.string.ui_role_bottom_tab_active, { accent }),
    BOTTOM_TAB_IDLE("bottom_tab_idle", UiRoleSection.BARS, R.string.ui_role_bottom_tab_idle, { toward(text, MARK_OFF_BLEND) }),
    SEARCH_GROUND("search_ground", UiRoleSection.BARS, R.string.ui_role_search_ground, { background }),
    SEARCH_FRAME("search_frame", UiRoleSection.BARS, R.string.ui_role_search_frame, { accent }),
    SEARCH_TEXT("search_text", UiRoleSection.BARS, R.string.ui_role_search_text, { text }),
    SEARCH_HINT("search_hint", UiRoleSection.BARS, R.string.ui_role_search_hint, { toward(text, HINT_BLEND) }),
    SEARCH_ICON("search_icon", UiRoleSection.BARS, R.string.ui_role_search_icon, { text }),

    // Toasts
    TOAST_GROUND("toast_ground", UiRoleSection.TOASTS, R.string.ui_role_toast_ground, { background }),
    TOAST_FRAME("toast_frame", UiRoleSection.TOASTS, R.string.ui_role_toast_frame, { accent }),
    TOAST_TEXT("toast_text", UiRoleSection.TOASTS, R.string.ui_role_toast_text, { accent }),
    ;

    val prefKey: String get() = UI_ROLE_PREFIX + key

    /** True for the three roles kept in the stock keys (text / background / primary colour). */
    val isFoundation: Boolean get() = section == UiRoleSection.FOUNDATION

    internal fun default(context: Context): Int = UiRoleDefaults(context).fallback()
}

const val UI_ROLE_PREFIX = "ui_role_"

/** The colour of [role]: what was set on the UI page, else its default. */
fun Context.uiColor(role: UiRole): Int = baseConfig.uiRoleOverride(role) ?: role.default(this)

/** The default of [role], whatever is stored — for a reset row and its swatch. */
fun Context.uiDefaultColor(role: UiRole): Int = role.default(this)

fun Context.isUiColorSet(role: UiRole): Boolean = baseConfig.uiRoleOverride(role) != null

fun Context.setUiColor(role: UiRole, color: Int) = baseConfig.setUiRole(role, color)

fun Context.resetUiColor(role: UiRole) = baseConfig.clearUiRole(role)

fun Context.resetAllUiColors() = baseConfig.clearAllUiRoles()
