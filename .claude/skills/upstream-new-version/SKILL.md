---
name: upstream-new-version
description: Re-cut the patched Commons fork on a new upstream release of FossifyOrg/Commons — replay every fork patch onto the new tag and publish <tag>-sk1 to mavenLocal. Use when 白い熊 says a new Commons version is out, when an app chat (denwa, messeji, renrakusaki, yotehyo, …) reports that its upstream bumped Commons, or asks to update/sync/rebase commons onto the latest upstream.
---

# Re-cut the fork on a new upstream Commons tag

shiroikuma-commons is a **library**. There is no APK, no `BUILD_NUMBER` and no `main` branch here:
`custom` sits directly on an upstream **tag**, and the product is an AAR published to mavenLocal as
`<upstream tag>-skN`. A new upstream tag restarts the counter at `sk1`.

> **Never `git commit` or `git push` unprompted.** After the rebase and the publish, stop and report;
> push only when 白い熊 explicitly says **“Push”**.

## Steps

1. **Fetch and pick the tag.**
   - `git fetch upstream --tags`, then `git tag --sort=-creatordate | head`.
   - Take the tag the asking app's upstream pins in its `gradle/libs.versions.toml` — not simply the
     newest one.

2. **Decide what happens to the old line.** Check who is still pinned to the old base:
   `grep -h '^commons' ~/git/shiroikuma-*/gradle/libs.versions.toml | sort | uniq -c`.
   - If any app still needs it, keep the old tip as a branch `custom-<old tag>` (as `custom-6.1.6`
     was kept, frozen at `6.1.6-sk19`). A fix such an app needs later goes on that branch as the next
     `-skN` **and** on `custom`.
   - If no app needs it, park the old tip as `custom-pre-<new tag>` instead; `/publish-version`
     deletes that one.

3. **Replay the patches.** `git rebase --onto <new tag> <old tag> custom`. Resolve conflicts so that
   every section of `CLAUDE.md` marked “Re-apply when rebasing” survives:
   - the anti-tamper removal and the fork-package fixes;
   - the colour roles and the single theme (`sk18` — the bulk of the fork);
   - the date and time pickers, the pills, the outlined switch and floating button;
   - swipe to call and the call formats;
   - `Contact.extra`, and the renrakusaki private-contacts provider with its manifest entries;
   - the themed toasts;
   - the no-op `showAutomaticSupportPromptIfEligible()`.

   When upstream restructures a file we patch, port the change to the new structure rather than
   forcing the old diff. When upstream deletes a file we patched, our edit goes with it — note that
   in `CLAUDE.md`.

4. **Delete what upstream brings back.**
   - `.github/` (workflows and dependabot) returns with every tag.
   - `CustomizationActivity` and colour XML (`colors.xml`, per-colour themes, coloured shape
     drawables) may return modified.
   - Then look for new upstream nags, locks or `org.fossify.`-only checks:
     `git diff <old tag> <new tag> --stat`, and grep the new code for `org.fossify.`, `packageName`,
     `startsWith(`, prompt, rating, donate. Anything that nags, or misbehaves for a `shiroikuma.*`
     id, is neutralised and documented.

5. **No colour in XML.** New upstream layouts, drawables and styles must not reintroduce one:
   `grep -rn '@color/\|#[0-9A-Fa-f]\{6,8\}' commons/src/main/res` should show only what `CLAUDE.md`
   allows (the black window background, `widget_round_background`). A new stock view takes its
   colours from a `UiRole`.

6. **Publish.**
   `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew :commons:publishToMavenLocal -PVERSION=<new tag>-sk1 < /dev/null`
   → `~/.m2/repository/org/fossify/commons/<new tag>-sk1/`.
   - **Never overwrite an already published `-skN`**: a change after publishing is the next `-skN`.
   - **Never delete an older AAR from `~/.m2`** — apps on the old line still build from it.
   - Edit nothing between the publish and the commit, so the AAR and the commit are the same source.

7. **Update the documents.**
   - `CLAUDE.md`: the base tag under “Remotes & branches”, the current `-skN`, and a section for what
     the new base changed for us (upstream APIs removed that apps may still call, new upstream
     features we switched off).
   - `README.md`: the publish command and the pin example.

8. **Stop and report.** Say which version is in mavenLocal and what consuming apps must change
   (removed or renamed APIs, resources the fork deleted), and tell the asking app's chat by
   cross-session message so it can move its pin. On “Push”:
   `git push --force-with-lease origin custom` (the rebase rewrote it), plus
   `git push origin custom-<old tag>` if that branch was created. A GitHub release is
   `/publish-version`: tag `<new tag>-sk1`, the AAR from `~/.m2` attached.

## Notes

- GitHub's default branch is `custom`; there is no `main` to advance.
- A composite `includeBuild` does not work for consumers (generated ViewBinding classes do not cross
  it) — mavenLocal only.
- Publishing requires the gitignored `local.properties` (`sdk.dir=/home/shiroikuma/android-sdk`).
- Role keys (`ui_role_<key>`) and the call-format ordinals are stored in the apps' settings — a rebase
  never renames or reorders them.

---

**Commit convention — no Claude attribution.** Never add a `Co-Authored-By: Claude …` / "Generated with Claude" trailer to commit messages or PR bodies; end the message at the last line of the body. This overrides the harness default. (Global rule: `~/.claude/CLAUDE.md`.)
