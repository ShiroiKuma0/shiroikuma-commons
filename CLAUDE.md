# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`shiroikuma-commons` is a **patched fork of [Fossify Commons](https://github.com/FossifyOrg/Commons)**,
the shared library behind the Fossify app family. It exists so the `shiroikuma-*` app forks (which
install under `shiroikuma.*` ids rather than `org.fossify.*`) work correctly. Two kinds of patch:

1. **Remove Commons' anti-tamper checks** — upstream Commons 6.1.x shows a "You are using a fake version
   of the app…" dialog, and breaks the "Customize colors" screen, whenever `packageName` does not start
   with `org.fossify.`. This fork deletes that machinery (see "The anti-tamper patch" below).
2. **Fork-package compatibility fixes** — other spots where Commons hard-codes the `org.fossify.*` package
   and silently misbehaves for our renamed ids (see "Fork-package fixes" below).

## Remotes & branches

- `origin` → `git@github.com:ShiroiKuma0/shiroikuma-commons` — our fork (push here).
- `upstream` → `https://github.com/FossifyOrg/Commons.git` — the original (read-only, for rebasing).
- `custom` — our working branch, based on an upstream **tag** (currently `7.1.3`). All our changes live here.
- `custom-6.1.6` — the previous line, frozen at `6.1.6-sk19`, kept for the apps whose upstream has not
  moved to Commons 7 yet. A fix such an app needs goes there as `6.1.6-sk20`, … **and** on `custom`.

## The anti-tamper patch (branch `custom`)

Removed entirely (not stubbed):

- `extensions/Activity.kt` — `showModdedAppWarning()`; `checkAppSideloading()` reduced to
  `appSideloadingStatus = SIDELOADING_FALSE; return false`.
- `compose/extensions/ActivityExtensions.kt` — `FAKE_VERSION_APP_LABEL`, `fakeVersionCheck()`.
- `compose/extensions/ComposeActivityExtensions.kt` — the `FakeVersionCheck()` composable.
- `compose/theme/AppTheme.kt` — the `OnContentDisplayed()` call site.
- `activities/BaseSimpleActivity.kt` — the `onCreate` modded-app block and the `startCustomizationActivity`
  guard.
- `activities/CustomizationActivity.kt` — the `pickPrimaryColor()` guard.

`DEVELOPER_PLAY_STORE_URL` is kept (used by legitimate features).

## Fork-package fixes (branch `custom`)

Commons assumes in several places that it runs as the real `org.fossify.*` app; these broke for our
renamed `shiroikuma.*` ids. **Re-apply these when rebasing onto a new Commons tag:**

- `helpers/MyContactsContentProvider.kt` — `getSimpleContacts`/`getContacts` returned an empty list
  unless the consumer package was exactly `org.fossify.{phone,messages,calendar}`, hiding the Contacts
  app's shared private/local contacts from our renamed forks. **Dropped the client-side allowlist.**
- `activities/ManageBlockedNumbersActivity.kt` — the dialer label (`isDialer`) and the call-screening
  role request (`maybeSetDefaultCallerIdApp`) gated on `appId.startsWith("org.fossify.phone")`. **Now
  also recognise our Phone fork's id (`shiroikuma.denwa`)**, while staying `false` for Messages.

Related, but **not** patched here: `extensions/Activity.kt`'s `launchCallIntent` pins outgoing calls to
the hard-coded `org.fossify.phone` package (broke calls with "No valid app found"). That one is fixed
**app-side** in `shiroikuma-denwa` (`extensions/CallExt.kt`), so there is nothing to re-apply here for it.

## Dialog accent border + boxed buttons (branch `custom`, `sk3`)

A fork **addition** (not a fix): on a black-on-black theme a dialog is invisible against the app
background, because `setupDialogStuff` paints the dialog window with a plain colored background and no
visible border. Three opt-in, off-by-default `BaseConfig` settings drive a configurable look:

- `dialogBorderColor` / `dialogBorderWidth` (dp, `0` = off) — `extensions/Activity.kt`'s
  `setupDialogStuff` wraps the window background via the new `Context.withDialogBorder()` helper
  (a `LayerDrawable` adding an inset accent stroke). No-op at width 0, so stock consumers are unaffected.
- `styledDialogButtons` — when on, `setupDialogStuff` gives each dialog button a boxed look
  (theme-background fill + accent stroke + accent text) instead of borderless same-colored text.

Keys live in `helpers/Constants.kt` (`DIALOG_BORDER_COLOR`, `DIALOG_BORDER_WIDTH`,
`STYLED_DIALOG_BUTTONS`); accessors in `helpers/BaseConfig.kt`. Consuming apps opt in by writing the
prefs (e.g. shiroikuma-yotehyo seeds yellow / 2 dp / on) and can expose them in their own UI. **Re-apply
when rebasing onto a new Commons tag.**

## Black/yellow contextual action bar + menus (branch `custom`, `sk4`–`sk5`)

A fork addition: make the contextual action bar (CAB) and dropdown/overflow menus match the
black-on-yellow theme in **every** theme mode — including system/dynamic, which the fork actually uses
(`isSystemThemeEnabled` true, with granular black/yellow color overrides), so the `*You` style variants
apply, **not** the `*_dark` ones.

- **CAB** (`adapters/MyRecyclerViewAdapter.kt`, `onCreateActionMode`): colored in CODE so it is
  independent of the action-mode style — bar background = `backgroundColor`, and title + menu icons +
  back arrow + overflow = `properPrimaryColor` (yellow). A `tintActionModeImageViews` traversal recolors
  the non-menu-item icons (back arrow, overflow). Relies on a new `iconColor` override added to
  `activities/BaseSimpleActivity.kt`'s `updateMenuItemColors`.
- **Menus**: pure black + 2 dp yellow border. `drawable/popup_menu_bg_you.xml` (used by the system/You
  overflow once `TopPopupMenuYou` is repointed to it in `res/values/styles.xml`) and
  `drawable/top_popup_menu_bg_dark.xml`; `drawable/actionmenu_background.xml` → black for the
  non-dynamic CAB.

**Re-apply when rebasing onto a new Commons tag.** Gotcha: because the fork runs the system/dynamic
theme, the CAB must be colored in code (not via the `*_dark` styles) and the menu border must go on the
`*You` drawable — editing only the `*_dark` variants has **no visible effect** (the mistake `sk4` made;
`sk5` fixed it by doing the CAB in code and bordering `popup_menu_bg_you`).

`sk8` applied the same in-code CAB colouring to **`MyRecyclerViewListAdapter`** — the `ListAdapter`
twin of `MyRecyclerViewAdapter` that `sk4`–`sk5` missed, so apps built on it (e.g. Messages'
conversation list) still showed the grey bar with white title and grey back arrow. Patch both
adapters when rebasing.

## Theme-styled toasts (branch `custom`, `sk6`)

A fork addition: toasts raised through `Context.toast()` with a foreground **activity** context use a
custom view — theme background fill, `getProperPrimaryColor()` text and a 2 dp frame of the same color,
8 dp corner radius — instead of the system's white bubble (`extensions/Context.kt`, `doToast` →
`showThemedToast`). Non-activity contexts (receivers, services) keep the plain system toast, because
custom toast views from a backgrounded app are silently dropped on API 30+ and the message would be
lost. Theme-driven, so on the black/yellow fork theme this renders black with yellow text + frame.

`sk9`: a non-activity context (receiver, service) borrows the **resumed activity** when the app is on
screen — `helpers/ForegroundActivity.kt`, an `ActivityLifecycleCallbacks` registered by `FossifyApp` —
so e.g. an MMS-send failure toast is themed too. With nothing resumed it still falls back to the
system toast.
**Re-apply when rebasing onto a new Commons tag.**

## Build / publish

The apps consume this via **mavenLocal**. A composite `includeBuild` does **not** work — AGP won't expose
a library's generated ViewBinding classes (e.g. `SearchBarBinding` from `search_bar.xml`, which apps
`<include>`) across the composite boundary, so consumers fail to compile. Publish with:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew :commons:publishToMavenLocal -PVERSION=6.1.6-sk5
```

→ `~/.m2/repository/org/fossify/commons/6.1.6-sk5/`. Requires a gitignored `local.properties`
(`sdk.dir=/home/shiroikuma/android-sdk`). The consuming apps pin `commons = "6.1.6-sk5"`.

The `-skN` suffix is **our** patch revision (independent of the upstream Commons version): bump it
(`sk1` → `sk2` → …) whenever you change the patch, so consumers opt in by bumping their pin. Current: `sk19`
(`sk1` = anti-tamper only; `sk2` added the fork-package fixes; `sk3` the dialog accent border + boxed
buttons; `sk4`–`sk5` the black/yellow contextual action bar + menus above; `sk6` the theme-styled toasts;
`sk7` the `ContactsHelper` raw-contact insert falling back to the local account when a stale source has
no account type — published 2026-07-23, committed 2026-11-02; `sk8` = `sk7` + the list-adapter CAB; `sk9` themed toasts from non-activity contexts while on screen; `sk10` `getMyContactsCursor()` reads our Contacts fork's private contacts — see below; `sk11` `Contact.extra` — see below; `sk12` pill-shaped dialog buttons — see below; `sk13` swipe to call and the call-log formats shared by denwa and renrakusaki — see below).

## Private contacts come from our Contacts fork (`sk10`)

Stock `getMyContactsCursor()` asked only `org.fossify.commons.contactsprovider`, which only stock Fossify
Contacts serves; shiroikuma-renrakusaki serves `<applicationId>.contactsprovider`, so every consumer saw
**no** private ("Phone storage (not visible by other apps)") contact — no names in the dialer, Messages
or Calendar, and the dialer's "block unknown numbers" rejected them. `sk10` tries
`MyContactsContentProvider.CONTACTS_CONTENT_URIS` in order (`shiroikuma.renrakusaki`, its `.debug`, then
the stock authority), and this library's manifest adds the renrakusaki `<queries>` packages and
`uses-permission` for `shiroikuma.renrakusaki[.debug].permission.READ_PRIVATE_DATA`. That permission is
**signature-level** on renrakusaki's side: it is granted only to an app signed with renrakusaki's key
(`shiroikuma-denwa.jks` — denwa, yotehyo), and silently denied to any other (messeji signs with
`shiroikuma-messages.jks`). renrakusaki also only answers while its "Show private contacts…" setting is on.

## No CI / GitHub Actions

We **deleted** all of upstream's `.github/workflows/*` and `.github/dependabot.yml`. This is a
locally-built fork (published to mavenLocal, consumed on-device), so none of Fossify's CI applies.
The scheduled `no-response` and `update-lint-baselines` workflows in particular were *failing daily and
emailing*, because they call reusable workflows in `FossifyOrg/.github` that need org-level secrets this
fork doesn't have. Keep `.github/` absent — don't re-add any of it.

Note: GitHub runs `on: schedule` workflows **only from the default branch**, which on `origin` is `custom`
(not `main`). So the deletion only takes effect once it's committed to `custom` and pushed.

## Rebasing onto a new Commons release

When an app's upstream bumps Commons: `git fetch upstream --tags`, check out the new tag onto `custom`
(re-applying **both** patch sets above — the anti-tamper removal *and* the fork-package fixes),
republish with `-PVERSION=<newtag>-sk1` (the `-skN` counter restarts at `sk1` for a new Commons base),
and bump each app's `commons` pin.

A new upstream tag will **reintroduce** the `.github/` directory (workflows + dependabot). Delete it
again as part of the rebase — see "No CI / GitHub Actions" above.

## Commons 7 (`7.1.3-sk1`, 2026-11-22)

Re-cut for shiroikuma-messeji's move to upstream Messages 1.10.3: every patch of `6.1.6-sk1`–`sk19`
replayed onto the `7.1.3` tag, published as `7.1.3-sk1`. What the new base changed for us:

- **Upstream removed the "thank you" feature locks** — `FeatureLockedDialog`, `UpgradeToProDialog`,
  `dialog_feature_locked.xml`, `isOrWasThankYouInstalled`, `addLockedLabelIfNeeded`, and the
  `hide_google_relations` bool (now `is_google_play_build`, inverted). Our edits to those files are
  dropped with them. An app still calling them must follow its own upstream's "migrate to commons 7"
  change before moving its pin.
- **Upstream added a timed support prompt** (`extensions/SupportPrompt.kt`: rating after 5 days on Play
  builds, donation after 10 days elsewhere). The fork never nags:
  `showAutomaticSupportPromptIfEligible()` is a no-op here. **Re-apply when rebasing.**
- `CustomizationActivity` and `.github/` came back modified upstream and are deleted again.
- Apps pinned to `6.1.6-skN` are unaffected until they move their pin; their AARs stay in `~/.m2`.

Current on this line: `7.1.3-sk3`.

## Japanese is complete — keep it so (`7.1.3-sk2`, 2026-11-23)

白い熊: “we need full Japanese proper translation always!” Every translatable string in
`res/values/*.xml` has an entry in `res/values-ja/` — natural Japanese, consistent with the terms the
file already uses. `7.1.3-sk2` closed the gap Commons 7 brought (16 strings: the rating and donation
prompts, the contributors screen, five language names, six library titles). **Compare the key sets
before a publish is called done** (`donottranslate.xml` and `translatable="false"` excepted); a new
upstream tag will bring new untranslated strings, so do it as part of every rebase too.

## Commit convention — no Claude attribution

Do **not** add any `Co-Authored-By: Claude …` trailer — nor a "🤖 Generated with Claude Code" / Anthropic-attribution line — to commit messages or PR bodies in this repo. 白い熊 does not want Claude attribution in the history; this **overrides** the harness's default to append such a trailer. End commit messages at the last line of the body. (The existing history was scrubbed of these trailers on 2026-06-08; the global rule lives in `~/.claude/CLAUDE.md`.)

## Every contact field — `Contact.extra` (`sk11`)

`ContactExtraData` (models/contacts) carries everything the Fossify model had no field for: the
reading, relations, SIP addresses, the organization's department / job description / office / reading /
symbol, send-to-voicemail, and verbatim `DataRow`s for every other provider column or kind; `Event`
gained `label`. `Contact.extra` holds it; the private DB stores it in a new `extra` column
(`ContactsDatabase` version 4, `MIGRATION_3_4`). `helpers/ContactExtraIO` reads it for system contacts
(`getContactWithId`, `fillExtras`) and writes it after `insertContact` (always) and `updateContact`
(only when `extra.loaded` — a list-row contact without its extras must not wipe them).
`ContactsHelper.lastInsertedId` reports the id an insert created (raw id or private local id).
Consumer R8 rules keep the new classes' field names (Gson reads them). Additive: forks that never touch
`extra` behave exactly as before. **Re-apply when rebasing.**

## Pill-shaped dialog buttons (`sk12`)

`extensions/Pill.kt` `pillDrawable(accent, background, main)` draws the family's action pill: always
outlined on the background colour, never filled; a **main** action has a thick (3 dp) frame, every other
pill a thin (1.25 dp) one, and the caller makes a main pill's text bold. `setupDialogStuff` uses it for
the dialog buttons when `BaseConfig.pillDialogButtons` is on (opt-in, default off; it takes precedence
over the boxed `styledDialogButtons`): the positive button is the main pill, the others thin. Under a
Material theme the buttons are `MaterialButton`s, which ignore a replacement background unless their
tint, insets, corner and elevation are cleared first — the code does that. **Re-apply when rebasing.**

## The family's look on commons' own screens, and destructive buttons (`sk15`–`sk16`)

Everything here hangs on the same opt-in as `sk12`, `BaseConfig.pillDialogButtons`; with it off nothing
changes.

- **Destructive dialog buttons.** `setupDialogStuff(…, destructiveButton = AlertDialog.BUTTON_*)` draws
  that button's frame and text in `BaseConfig.dialogWarningColor` (default pure red; the app syncs it to
  its own warning role). `ConfirmationDialog(…, destructive = true)` passes the positive button.
- **Compose** (`compose/components/FamilyPills.kt`): `PillButton` (outlined, never filled; 3 dp + bold
  when main), `DialogButton` (a pill under the look, the stock `TextButton` otherwise — every Compose
  dialog's buttons go through it, the confirm one as main), `OutlinedPillSwitch` (the outlined switch;
  `SettingsSwitchComponent` and `SettingsCheckBoxComponent` use it), and `familyLook()`.
- **Rows that open something** — `SettingsListItem(text: String, click = …)`, which is every row of the
  About screen — are a pill around the label with a ›. Manage blocked numbers' two buttons are main pills.

`sk16` adds the same to `CustomizationActivity` (Views): `tightenRowsForFamilyLook()` at the end of its
`onResume` gives every row 2 dp above and below and no minimum height, makes the headings bold and
underlined and takes the alpha dimming off the value texts. **Re-apply when rebasing.**

## Swipe to call and the call-log formats, shared by denwa and renrakusaki (`sk13`)

Both of our telephony forks used to carry a hand-kept copy of these; they now live here, purely added:

- `helpers/SwipeToCallCallback.kt` — denwa's swipe-only `ItemTouchHelper.SimpleCallback`: left = SIM 1,
  right = SIM 2, the row slides out at the finger's speed, then shows a filled “Dialing…” row until the
  activity stops. The app passes `sim1Color`, `sim2Color`, `canSwipe`, `onSwipe`, `simTextColor` (the 1/2
  glyph and the text) and `dialingText` (its own string). Placing the call stays in each app.
- `extensions/SwipeToCall.kt` — `RecyclerView.setupSwipeToCall(callback)` attaches the helper, the
  “Dialing…” decoration and a touch listener that wins over a surrounding ViewPager, replacing whatever an
  earlier call attached (tag `R.id.swipe_to_call_tag`); `removeSwipeToCall()` detaches it.
- `extensions/CallFormats.kt` — `toJapaneseClockString`, `toJapaneseDurationString`,
  `toImperialDateString`, `toKanjiNumeral`, and `formatCallTime(context, format)` /
  `formatCallDuration(context, format)` over the ordinals `CALL_TIME_JAPANESE/SYSTEM/24H/12H` and
  `CALL_DURATION_JAPANESE/DIGITAL`. **Those ordinals are stored in denwa's settings and served to
  renrakusaki by denwa's call-log style provider — never reorder or re-mean them.**

Consumers: denwa from `6.1.6-sk13`; renrakusaki once its chat moves its pin. **Re-apply when rebasing.**

### Tiles swipe inside their own cell (`sk17`)

ItemTouchHelper measures every swipe against the whole list: the threshold, and a committed item's flight
to the list's edge. In a grid that sent a cell across its neighbours. `SwipeToCallCallback` now treats an
item narrower than 75 % of the list as a **tile**: it commits after 40 % of its own width (or a flick),
travels one width at most, is clipped to its cell while it moves (`View.clipBounds`, cleared in
`clearView` and `onSwiped`), keeps its mark 10 dp from the cell's edge and shrinks “Dialing…” to fit. A
full-width row behaves exactly as before, so denwa is unchanged. Consumer: renrakusaki's Favorites grid.

## The outlined switch and floating button under the family's look (`sk17`)

Under `BaseConfig.pillDialogButtons` (nothing changes with it off):

- `MyMaterialSwitch` is the family's switch — a 2 dp round outline with a solid dot (46 % of the height),
  right and in the accent when on, left and in the off colour when not; never filled, no checkmark icon.
  Its parts are `views/FamilySwitchDrawable`, tinted by the switch's own track and thumb tint lists. The off
  colour is `BaseConfig.switchOffColor`, or, at 0, 55 % of the way from the background to the accent; an
  app with its own role for it writes that role there.
- `MyFloatingActionButton` is never a filled slab: the background colour inside, the accent for its icon
  and for a 3 dp frame in the button's own shape (a `MaterialShapeDrawable` as the foreground).

**Re-apply when rebasing.**

## Every colour a role, none in XML (`sk18`)

白い熊, 2026-11-21: `#FFFF00` and `#000000` are defaults only, and every item's colour is settable in the
consuming app's UI. Owner of this rebuild: the renrakusaki chat.

- **`helpers/UiRoles.kt`** is the one table: `UiRole` (key, section `UiRoleSection`, label, default) for
  everything the commons draws — foundation (background, accent, text), text, fields, buttons, dialogs,
  pickers, switches, check boxes and radio buttons, menus, lists, bars, toasts. The two primaries are
  written there once (`FAMILY_YELLOW`, `FAMILY_BLACK`); every other default is derived from the three
  foundation roles. Stored under `ui_role_<key>`; read with `uiColor(role)`, written with `setUiColor` /
  `resetUiColor` / `resetAllUiColors`. **A role's key is stored in every app's settings — never rename one.**
  Labels: `res/values/ui_roles.xml` (+ `values-ja`).
- **No colour in XML.** `colors.xml`, the `md_` palette, the per-colour / white / system-dynamic themes,
  the coloured shape drawables, `CustomizationActivity` and the line colour picker are deleted. One theme,
  `AppTheme.Base`, whose only colour is the black `android:windowBackground` (drawn before code runs).
  Shapes are built in code: `extensions/UiRoleViews.kt` (`rowSelectorDrawable`, `rippleDrawable`,
  `circleDrawable`, `listDividerDrawable`, `menuPanelDrawable`, `sectionFrameDrawable`, …); `MyDivider`
  paints `@layout/divider`. `widget_round_background` keeps a literal (a home-screen widget's shape).
- **`views/FamilyViewInflater`** is the theme's `viewInflaterClass` (kept by `consumer-rules.pro`): stock
  widgets get their roles as they are inflated, and a single-colour vector icon is tinted on its drawable,
  never on the view (a view tint would colour a photo loaded later). The `My*` views carry `applyRoles()`,
  called from `updateTextColors(viewGroup, inDialog)`.
- The old theme predicates (`isDynamicTheme()`, `isWhiteTheme()`, …) always answer false and
  `getThemeId()` is always `AppTheme_Base`; `dialogBorderColor`, `switchOffColor` and `dialogWarningColor`
  are aliases of their roles. The Compose theme is one scheme built from the roles.
- **`views/FamilyDatePicker`**: a month grid drawn in code — a ring on the chosen day, a short line under
  today, arrow pills, the year stepping by 1 and by 10 — every part from a `UiRole.PICKER_*` role. The
  platform `DatePicker` draws its grid from theme attributes with no setter; do not put one in a layout.

Consumers: renrakusaki and denwa from `sk18`. **This is the bulk of the fork now — a rebase onto a new
Commons tag re-applies it as a whole.**

## A time picker of our own, and a settable week start (`sk19`)

- **`views/FamilyTimePicker`** (Messēji asked): hour and minute columns, an arrow pill above and below each;
  a held arrow keeps stepping, the minutes by five once it runs; a tap on the minutes rounds them to the
  next five; an AM / PM pill in the 12-hour form. API as the platform's: `hour` (0–23), `minute`,
  `setTime`, `is24HourView`, `onTimeChanged`. Roles `PICKER_TIME_DIGITS`, `PICKER_TIME_SEPARATOR`,
  `PICKER_AM_PM`; its arrows share `PICKER_ARROW` / `PICKER_ARROW_FRAME` with the date picker.
- **`FamilyDatePicker`** (Yotēhyō asked): `firstDayOfWeek` (a `Calendar` day; default the locale's),
  `showDays` (off = a month-and-year picker) and `showMonth` (off = a year picker).

Consumers: yotehyo and messeji from `sk19`.

## The blocked-numbers screen and the Compose pill menu (`7.1.3-sk3`, 2026-11-26)

白い熊, on 電話's screen: “very ugly page, remove the line padding”, and both dropdowns “must respect
black-yellow skill”. Everything here is under `BaseConfig.pillDialogButtons`; off, the screen is stock.

- **`compose/menus/FamilyMenu.kt`**: `FamilyDropdownMenu` (panel from `MENU_GROUND` / `MENU_FRAME`) and
  `FamilyMenuRow` (a full-width 1.25 dp pill, icon inside at the left coloured by `MenuIconRole` —
  `VIEW`, `SHARE`, `BLOCK` — a › when `opens`, the `MENU_DESTRUCTIVE` red when `destructive`).
  `ActionItem` gained `role`, `opens`, `destructive` and `overflowIcon`, and `ActionMenu` draws its
  overflow with these rows. **A new Compose menu in the commons uses these, never `DropdownMenu`.**
- **`compose/screens/ManageBlockedNumbersScreen.kt`**: `FamilyBlockedNumbers` — the outlined box with
  “Blocked numbers (N)” on its top edge, rows chunked into one or two columns (two from 520 dp of screen
  width), every second row in `LIST_ROW_ALT`, a 1 px `LIST_DIVIDER` between rows. A row is as tall as
  its text plus 2 dp above and below. Numbers are shown through `formatBlockedNumber`
  (`+420-222-292-055`, hyphens by 白い熊's word); only a plain number is formatted, and copying copies
  the stored one. The drag-to-select handler (`compose/extensions/DragHandler.kt`) takes a `keyAt`
  callback, because a list item is now a row of up to two numbers.
- **Roles added** (keys are stored — never rename): `menu_icon_view`, `menu_icon_share`,
  `menu_icon_block`, `menu_destructive`, `list_text`, `list_text_secondary`, `list_row_alt`,
  `list_box_frame`, `list_box_heading`. An app that has menu icon slots of its own for the same
  meanings maps them onto these, so one colour is set in one place.
- `SettingsSwitchComponent` takes an optional `icon` / `iconColor`.

Not checked on a device. **Re-apply when rebasing.**
