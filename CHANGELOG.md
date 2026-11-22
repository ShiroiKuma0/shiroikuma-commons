# Changelog

This file carries the history of the 白い熊 Commons fork. Upstream Fossify Commons keeps no changelog file;
its releases are listed at https://github.com/FossifyOrg/Commons/releases.

## 白い熊 Commons 7.1.3-sk1 — 2026-11-22

Built on Fossify Commons `7.1.3`. Everything since `6.1.6-sk19`; the `-skN` counter restarts with the new base.

### The new base

- **Every patch of `6.1.6-sk1`–`sk19` replayed onto the upstream `7.1.3` tag** — the anti-tamper removal, the fork-package fixes, the colour roles and the single theme, the date and time pickers, the pills, switches and floating button, swipe to call and the call formats, `Contact.extra`, private contacts from the 白い熊 連絡先 provider, the themed toasts. Nothing of the fork’s own behaviour changes.
- Published as `org.fossify:commons:7.1.3-sk1`. First consumer: 白い熊 メッセージ, for its move to upstream Messages 1.10.3.
- The `6.1.6` line stays alive as the branch `custom-6.1.6`, frozen at `6.1.6-sk19`, for the apps whose upstream has not moved to Commons 7. Their AARs stay in `~/.m2`, and an app pinned to `6.1.6-skN` is unaffected until it moves its pin.

### No timed support prompt

- Commons 7 added `extensions/SupportPrompt.kt`: a rating prompt after 5 days on Play builds, a donation prompt after 10 days elsewhere. **`showAutomaticSupportPromptIfEligible()` is a no-op in this fork** — both call sites (`Activity.kt`, the Compose `ActivityExtensions.kt`) still call it and nothing is shown.

### What Commons 7 brings and takes away

- **The “thank you” feature locks are gone upstream**: `FeatureLockedDialog`, `UpgradeToProDialog`, `dialog_feature_locked.xml`, `isOrWasThankYouInstalled`, `addLockedLabelIfNeeded`. The fork’s edits to those files (the pill buttons and roles of sk12–sk18 there) are dropped with them. **An app that still calls them must follow its own upstream’s “migrate to commons 7” change before moving its pin.**
- The `hide_google_relations` bool is replaced by `is_google_play_build`, with the opposite meaning.
- Upstream’s simplified support prompts and app flags, the Compose “thank you” row, the storage-unit customization, more library licence metadata, the purchase and translation help links corrected, an unsafe `List`-to-`ArrayList` cast removed, the Glide Compose library dropped, and the translations of 6.1.6 → 7.1.3.

### Removed again after the rebase

- `CustomizationActivity` (*Customize colors*), which came back modified upstream — the colour roles replace it.
- `.github/` (workflows and dependabot), which every upstream tag reintroduces — this fork is built locally and runs no CI.

## 白い熊 Commons 6.1.6-sk19 — 2026-11-21

Built on Fossify Commons `6.1.6`. Everything since `6.1.6-sk6`.

### Colours (sk18)

- **Every colour is a role, and none is written in XML.** `helpers/UiRoles.kt` holds `UiRole` — foundation (background, accent, text), text and links, text fields, spinners and sliders, buttons and the floating button, dialogs, pickers, switches, check boxes and radio buttons, menus, lists and the scroll thumb, system bars, toolbar, action bar, bottom bar, search bar, toasts. Black `#000000` and yellow `#FFFF00` are written once as the defaults; every other default is derived from the three foundation roles. API: `uiColor`, `uiDefaultColor`, `isUiColorSet`, `setUiColor`, `resetUiColor`, `resetAllUiColors`; stored under `ui_role_<key>`; labels in English and Japanese.
- Removed: `colors.xml`, the `md_` palette arrays, the per-colour, white and system-dynamic themes (about 200 styles), 34 coloured shape drawables, `CustomizationActivity` (*Customize colors*), the line colour picker, the app-icon colour switching. One theme remains, `AppTheme.Base`; its only colour is the black window background the platform draws before any code runs.
- Shapes are built in code (`extensions/UiRoleViews.kt`): row selector, ripples, circles, list divider, menu panel, section frame, colour-picker ring. `MyDivider` paints `@layout/divider`.
- `views/FamilyViewInflater`, the theme’s view inflater, gives stock widgets their roles as they are inflated and tints single-colour vector icons on the drawable. Every `My*` view has `applyRoles()`, called by `updateTextColors(viewGroup, inDialog)`.
- Dialogs (`setupDialogStuff`) take ground, frame, title, text and button colours from the dialog roles; toasts, the contextual action bar, the search bar, breadcrumbs, the bottom sheet, the file picker and the security tabs from theirs. The status and navigation bars are painted once their role is set.
- The Compose theme is a single scheme built from the roles.
- `dialogBorderColor`, `switchOffColor` and `dialogWarningColor` are aliases of their roles; the old theme predicates always answer false.

### Pickers (sk18–sk19)

- `views/FamilyDatePicker`: a month grid drawn in code — a ring on the chosen day, a short line under today, arrow pills, the year stepping by 1 and by 10 — each part from its own `PICKER_*` role. sk19: `firstDayOfWeek`, and `showDays` / `showMonth` for month-only and year-only forms.
- `views/FamilyTimePicker` (sk19): hour and minute columns with arrow pills that keep stepping while held (minutes by five), a tap on the minutes rounds to the next five, an AM / PM pill in the 12-hour form; roles for digits, separator and AM / PM.

### The family’s look (sk12, sk15–sk17)

- Pill-shaped dialog buttons, opt-in through `pillDialogButtons` (sk12).
- Destructive confirmations drawn in their own colour; the pill look on the library’s own Compose screens (sk15) and tight rows with bold underlined headings (sk16).
- The outlined switch (`FamilySwitchDrawable`: a round outline with a solid dot) and the outlined floating button (sk17).

### Swipe to call and call formats (sk13–sk14, sk17)

- `SwipeToCallCallback`, `RecyclerView.setupSwipeToCall` / `removeSwipeToCall`, and the call-log formats (`CallFormats.kt`: time, duration, imperial date, kanji numerals), shared by the dialer and Contacts (sk13).
- Each side can be switched off and given its own mark, colour, glyph and label (sk14).
- A grid tile swipes inside its own cell (sk17).

### Contacts (sk7, sk10–sk11)

- A contact with a stale source is imported into local storage (sk7).
- `getMyContactsCursor()` reads private contacts from the 白い熊 連絡先 provider before the stock authority (sk10).
- `Contact.extra`: reading, relations, SIP addresses, organization details, event labels, send-to-voicemail and every unknown data row, kept in the contact itself in both stores; private database migration 3 → 4 (sk11).

### Chrome (sk8–sk9)

- The black / yellow contextual action bar for the list adapter too (sk8).
- Theme-styled toasts from receivers and services while the app is on screen (sk9).

### Packaging

- Published to mavenLocal as `org.fossify:commons:6.1.6-sk19`. Consumers: 連絡先 and 電話 (sk18), 予定表 and メッセージ (sk19).

## 白い熊 Commons 6.1.6-sk6 — 2026-07-16

First published release, built on Fossify Commons `6.1.6`.

- **sk1** — the anti-tamper “fake version” / sideloading machinery removed entirely.
- **sk2** — fork-package fixes: `MyContactsContentProvider` no longer returns an empty list to renamed app ids; `ManageBlockedNumbersActivity` recognizes `shiroikuma.denwa` as a dialer.
- **sk3** — opt-in dialog accent border and boxed dialog buttons.
- **sk4–sk5** — the contextual action bar and popup menus in black with the accent.
- **sk6** — theme-styled toasts.
- Upstream GitHub Actions and Dependabot removed.
