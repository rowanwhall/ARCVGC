---
name: release-mobile
description: Prepare a mobile app release — bump version codes/names, build Android AAB, and provide iOS archive instructions. Use when releasing Android and iOS apps for beta testing or production.
disable-model-invocation: true
argument-hint: [version-name]
---

# Mobile Release Workflow

## Step 1: Determine version

Read the current version name and version code from:
- `composeApp/build.gradle.kts` (`versionCode` and `versionName`)
- `iosApp/Configuration/Config.xcconfig` (`CURRENT_PROJECT_VERSION` and `MARKETING_VERSION`)

Verify they are in sync (both should have the same version code and version name). If not, warn the user.

Report the current version name and version code to the user.

If `$ARGUMENTS` was provided, use it as the new version name. Otherwise, ask the user what the new version name should be (e.g. "1.2", "2.0").

The new version code is always the current version code + 1 (auto-incremented).

## Step 2: Generate release notes

Find the commit where the current version was set by searching git history for the last change to `versionName` in `composeApp/build.gradle.kts`:

```bash
git log --oneline -S 'versionName' -- composeApp/build.gradle.kts | head -1
```

This gives the commit that bumped to the current version. List all commits since that commit, excluding non-user-facing changes (e.g., skill/tooling updates, doc-only changes). Summarize the user-facing changes as bullet-point release notes and present them to the user for review before proceeding.

## Step 3: Bump versions

Update both files:
- `composeApp/build.gradle.kts`: set `versionCode` and `versionName`
- `iosApp/Configuration/Config.xcconfig`: set `CURRENT_PROJECT_VERSION` and `MARKETING_VERSION`

## Step 4: Build Android AAB

Run `./gradlew :composeApp:bundleRelease` and confirm success. Report the output path of the AAB.

## Step 5: iOS instructions

Remind the user:
- Archive and distribute the iOS app from Xcode (Product > Archive > Distribute App)
