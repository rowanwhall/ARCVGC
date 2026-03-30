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

## Step 2: Bump versions

Update both files:
- `composeApp/build.gradle.kts`: set `versionCode` and `versionName`
- `iosApp/Configuration/Config.xcconfig`: set `CURRENT_PROJECT_VERSION` and `MARKETING_VERSION`

## Step 3: Build Android AAB

Run `./gradlew :composeApp:bundleRelease` and confirm success. Report the output path of the AAB.

## Step 4: iOS instructions

Remind the user:
- Archive and distribute the iOS app from Xcode (Product > Archive > Distribute App)
- The `ITSAppUsesNonExemptEncryption` key in Info.plist should be set to `false` (standard HTTPS only) to avoid the "Missing Compliance" warning in App Store Connect
