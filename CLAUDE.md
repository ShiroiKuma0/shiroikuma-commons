# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`shiroikuma-commons` is a **patched fork of [Fossify Commons](https://github.com/FossifyOrg/Commons)**,
the shared library behind the Fossify app family. It exists for one reason: to **remove Commons'
anti-tamper checks** so the `shiroikuma-*` app forks (which install under `shiroikuma.*` ids rather than
`org.fossify.*`) are not nagged.

Upstream Commons 6.1.x shows a "You are using a fake version of the app…" dialog, and breaks the
"Customize colors" screen, whenever `packageName` does not start with `org.fossify.`. This fork deletes
that machinery.

## Remotes & branches

- `origin` → `git@github.com:ShiroiKuma0/shiroikuma-commons` — our fork (push here).
- `upstream` → `https://github.com/FossifyOrg/Commons.git` — the original (read-only, for rebasing).
- `custom` — our working branch, based on an upstream **tag** (currently `6.1.6`). All our changes live here.

## The patch (branch `custom`)

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

## Build / publish

The apps consume this via **mavenLocal**. A composite `includeBuild` does **not** work — AGP won't expose
a library's generated ViewBinding classes (e.g. `SearchBarBinding` from `search_bar.xml`, which apps
`<include>`) across the composite boundary, so consumers fail to compile. Publish with:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew :commons:publishToMavenLocal -PVERSION=6.1.6-sk1
```

→ `~/.m2/repository/org/fossify/commons/6.1.6-sk1/`. Requires a gitignored `local.properties`
(`sdk.dir=/home/shiroikuma/android-sdk`). The consuming apps pin `commons = "6.1.6-sk1"`.

## Rebasing onto a new Commons release

When an app's upstream bumps Commons: `git fetch upstream --tags`, check out the new tag onto `custom`
(re-applying the patch above), republish with `-PVERSION=<newtag>-sk1`, and bump each app's `commons` pin.
