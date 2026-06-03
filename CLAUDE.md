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
- `custom` — our working branch, based on an upstream **tag** (currently `6.1.6`). All our changes live here.

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

## Build / publish

The apps consume this via **mavenLocal**. A composite `includeBuild` does **not** work — AGP won't expose
a library's generated ViewBinding classes (e.g. `SearchBarBinding` from `search_bar.xml`, which apps
`<include>`) across the composite boundary, so consumers fail to compile. Publish with:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew :commons:publishToMavenLocal -PVERSION=6.1.6-sk2
```

→ `~/.m2/repository/org/fossify/commons/6.1.6-sk2/`. Requires a gitignored `local.properties`
(`sdk.dir=/home/shiroikuma/android-sdk`). The consuming apps pin `commons = "6.1.6-sk2"`.

The `-skN` suffix is **our** patch revision (independent of the upstream Commons version): bump it
(`sk1` → `sk2` → …) whenever you change the patch, so consumers opt in by bumping their pin. Current: `sk2`
(`sk1` = anti-tamper only; `sk2` added the fork-package fixes above).

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
