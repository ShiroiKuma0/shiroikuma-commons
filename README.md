<div align="center">

# 白い熊 Commons

**The patched Fossify Commons that powers the `shiroikuma.*` app forks.**

A fork of [Fossify Commons](https://github.com/FossifyOrg/Commons) with **major additions**: the anti-tamper “fake version” machinery removed entirely, fork-package compatibility fixes for renamed app ids, **every colour a role an app can set — none written in XML**, date and time pickers drawn in code, swipe to call, pill buttons and outlined switches, complete contact fields, and theme-styled toasts.

Consumed from **mavenLocal** by the 白い熊 forks (denwa, messeji, renrakusaki, yotehyo, …) as `org.fossify:commons:<upstream>-skN`.

**📥 Latest release: [`6.1.6-sk19`](https://github.com/ShiroiKuma0/shiroikuma-commons/releases/latest)** — [all releases »](https://github.com/ShiroiKuma0/shiroikuma-commons/releases)

</div>

---

## 🎨 Every colour a role, none in XML (sk18)

Black `#000000` and yellow `#FFFF00` are only defaults. `UiRole` names every item the library draws — text, fields, buttons, dialogs, pickers, switches, check boxes, menus, lists, bars, toasts — and an app reads and stores each one separately (`uiColor(role)`, `setUiColor`), so its settings page can offer them all. No layout, drawable or style carries a colour: shapes are built in code, the stock per-colour / white / dynamic themes and *Customize colors* are gone, and `FamilyViewInflater` paints stock widgets as they are inflated.

---

## 📅 Date and time pickers drawn in code (sk18–sk19)

The platform pickers take their colours from theme attributes with no setter. `FamilyDatePicker` is a month grid of our own — a ring on the chosen day, a line under today, arrow pills, a settable first day of the week, and month-only and year-only forms. `FamilyTimePicker` is two columns of digits with arrow pills that keep stepping while held, and an AM / PM pill. Every part has its own role.

---

## 💊 Pills, outlined switches, red for what deletes (sk12, sk15–sk17)

Opt-in through `pillDialogButtons`: dialog buttons are outlined pills (thick frame for the main action), the switch is a round outline with a solid dot, the floating button is never a filled slab, and a confirmation that deletes is drawn in the destructive role. The library’s own Compose screens wear the same look.

---

## 📞 Swipe to call, shared (sk13–sk14, sk17)

`SwipeToCallCallback` and `RecyclerView.setupSwipeToCall` serve the dialer and Contacts alike: each side can be switched off and given its own mark, colour and label, and a grid tile swipes inside its own cell. The call-log time, duration and imperial-date formats live here too.

---

## 👤 Every contact field, private contacts everywhere (sk7, sk10–sk11)

`Contact.extra` keeps reading, relations, SIP addresses, organization details, event labels and every unknown data row in the contact itself — in the system store and the private database alike. `getMyContactsCursor()` reads private contacts from the 白い熊 連絡先 provider, and an import with a stale source falls back to local storage.

---

## 🔓 Anti-tamper checks removed (sk1)

Upstream Commons 6.1.x shows a blocking “You are using a fake version of the app…” dialog — and silently breaks the “Customize colors” screen — whenever the installed package id is not `org.fossify.*`, which is always the case for a renamed fork. This fork deletes that machinery entirely (dialog, compose `FakeVersionCheck`, the `BaseSimpleActivity` and `CustomizationActivity` guards), so forks run clean with no in-app workarounds.

---

## 🧩 Fork-package fixes (sk2)

Commons hard-codes `org.fossify.*` in places and silently misbehaves for renamed ids:

- `MyContactsContentProvider` returned an **empty contact list** unless the consumer was exactly `org.fossify.{phone,messages,calendar}` — the client-side allowlist is dropped, so forks see the Contacts app’s shared private/local contacts.
- `ManageBlockedNumbersActivity` only recognized `org.fossify.phone` as a dialer — it now also recognizes the Phone fork’s id (`shiroikuma.denwa`).

---

## 🖼️ Dialog accent border + boxed buttons (sk3, opt-in)

On a black-on-black theme a dialog is invisible against the app background. Three off-by-default `BaseConfig` settings (`dialogBorderColor`, `dialogBorderWidth`, `styledDialogButtons`) add a configurable accent frame around dialogs and boxed, accent-stroked dialog buttons. No-ops unless a consumer opts in.

---

## 🖤 Black/yellow contextual action bar & menus (sk4–sk5)

The selection CAB is colored in code — theme background bar with primary-color title, menu icons, back arrow, and overflow — and dropdown/overflow menus render pure black with a 2 dp yellow border, in **every** theme mode including system/dynamic (the mode the forks actually run).

---

## 🍞 Theme-styled toasts (sk6, sk9)

Toasts raised through `Context.toast()` use a custom view — ground, text and a 2 dp frame, each from its own role — instead of the system’s white bubble, from receivers and services too while the app is on screen.

---

## Versioning & publishing

The `-skN` suffix is the fork’s patch revision on top of an upstream tag (currently based on `6.1.6`). Publish to the local Maven repo with:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew :commons:publishToMavenLocal -PVERSION=6.1.6-sk19
```

Consuming apps pin `commons = "6.1.6-sk19"` in their `gradle/libs.versions.toml` (`mavenLocal()` must be in their repositories). See `CLAUDE.md` for the full patch documentation and the rebase procedure for new upstream tags.

## Built on Fossify Commons

A fork of [Fossify Commons](https://github.com/FossifyOrg/Commons), the shared library behind the Fossify app family — all credit for the underlying components goes to them. The code remains under the [GPL-3.0 license](LICENSE).
