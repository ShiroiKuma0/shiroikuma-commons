<div align="center">

# 白い熊 Commons

**The patched Fossify Commons that powers the `shiroikuma.*` app forks.**

A fork of [Fossify Commons](https://github.com/FossifyOrg/Commons) with **major additions**: the anti-tamper “fake version” machinery removed entirely, fork-package compatibility fixes for renamed app ids, and black/yellow fork chrome — dialog accent borders, a themed contextual action bar and popup menus, and theme-styled toasts.

Consumed from **mavenLocal** by the 白い熊 forks (denwa, messeji, renrakusaki, yotehyo, …) as `org.fossify:commons:<upstream>-skN`.

**📥 Latest release: [`6.1.6-sk6`](https://github.com/ShiroiKuma0/shiroikuma-commons/releases/latest)** — [all releases »](https://github.com/ShiroiKuma0/shiroikuma-commons/releases)

</div>

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

## 🍞 Theme-styled toasts (sk6)

Toasts raised through `Context.toast()` with a foreground activity context use a custom view — theme background fill, primary-color text, and a 2 dp frame of the same color — instead of the system’s white bubble. Receiver/service toasts keep the system style, since Android 11+ silently drops custom toast views from backgrounded apps.

---

## Versioning & publishing

The `-skN` suffix is the fork’s patch revision on top of an upstream tag (currently based on `6.1.6`). Publish to the local Maven repo with:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew :commons:publishToMavenLocal -PVERSION=6.1.6-sk6
```

Consuming apps pin `commons = "6.1.6-sk6"` in their `gradle/libs.versions.toml` (`mavenLocal()` must be in their repositories). See `CLAUDE.md` for the full patch documentation and the rebase procedure for new upstream tags.

## Built on Fossify Commons

A fork of [Fossify Commons](https://github.com/FossifyOrg/Commons), the shared library behind the Fossify app family — all credit for the underlying components goes to them. The code remains under the [GPL-3.0 license](LICENSE).
