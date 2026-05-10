---
name: local-network-development
description: Temporarily point all three platforms (Android, iOS, Web) at a LAN backend URL for development. Handles cleartext-traffic config (Android network_security_config, iOS ATS exception) and webpack dev-server proxy so the web build stays same-origin and avoids CORS. Use when testing against a backend running on your LAN instead of production.
disable-model-invocation: true
argument-hint: <lan-base-url> | off
---

# Local Network Development

This skill swaps the app's network base URL from production (`https://arcvgc.com`) to a LAN host, and applies the platform-specific configuration each platform needs to actually talk to a non-HTTPS LAN server.

## Argument

`$ARGUMENTS` is one of:

- **A LAN URL** — e.g., `http://192.168.86.250:5000` or `http://192.168.86.250:5000/api/v1/`. The skill normalizes this to `<scheme>://<host>[:<port>]` (any path is stripped — the codebase already appends `/api/v1/...` endpoints).
- **`off`** (or empty) — revert all changes back to production.

If `$ARGUMENTS` is empty, ask the user whether to set up (and for a URL) or tear down before doing anything.

## Setup mode (URL provided)

Apply **all** of the following changes in one pass:

### 1. Shared base URL

In `shared/src/commonMain/kotlin/com/arcvgc/app/network/ApiConstants.kt`:
- Set `const val API_HOST = "<normalized LAN URL>"`.

In `shared/src/androidMain/kotlin/com/arcvgc/app/network/ApiConstants.android.kt`:
- Set `actual fun getPlatformBaseUrl(): String = "<normalized LAN URL>"`.

In `shared/src/iosMain/kotlin/com/arcvgc/app/network/ApiConstants.ios.kt`:
- Set `actual fun getPlatformBaseUrl(): String = "<normalized LAN URL>"`.

**Leave `shared/src/wasmJsMain/.../ApiConstants.wasmJs.kt` untouched.** The web build stays same-origin (`""`) and reaches the LAN via the webpack proxy below. This matches the prod nginx setup and avoids CORS.

Update `normalizeImageUrl` in `ApiConstants.wasmJs.kt` to also rewrite production image URLs to same-origin, so images proxied through `/static` keep working:

```kotlin
private const val PROD_IMAGE_HOST = "https://arcvgc.com"

actual fun normalizeImageUrl(url: String?): String? {
    if (url == null) return null
    if (url.startsWith(API_HOST)) {
        return getWindowOrigin() + url.removePrefix(API_HOST)
    }
    if (url.startsWith(PROD_IMAGE_HOST)) {
        return getWindowOrigin() + url.removePrefix(PROD_IMAGE_HOST)
    }
    return url
}
```

### 2. Android cleartext (only if the URL is `http://`)

Create `composeApp/src/androidMain/res/xml/network_security_config.xml` (the `xml/` directory likely does not exist — create it):

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true"><LAN HOST></domain>
    </domain-config>
</network-security-config>
```

Replace `<LAN HOST>` with just the hostname (no scheme/port), e.g., `192.168.86.250`.

In `composeApp/src/androidMain/AndroidManifest.xml`, add `android:networkSecurityConfig="@xml/network_security_config"` to the `<application>` element (insert alphabetically between `android:label` and `android:supportsRtl`).

### 3. iOS ATS exception (only if the URL is `http://`)

In `iosApp/iosApp/Info.plist`, add the following dict after the existing `ITSAppUsesNonExemptEncryption` key (keep alphabetical-ish ordering of top-level keys is not required — just put it directly after the encryption key):

```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSExceptionDomains</key>
    <dict>
        <key><LAN HOST></key>
        <dict>
            <key>NSExceptionAllowsInsecureHTTPLoads</key>
            <true/>
            <key>NSIncludesSubdomains</key>
            <true/>
        </dict>
    </dict>
</dict>
```

### 4. Web dev-server proxy

Create `webApp/webpack.config.d/proxy.js` (Kotlin's wasmJs Gradle plugin auto-merges `*.js` files in `webpack.config.d/` into the dev-server config):

```javascript
// Temporary LAN dev proxy — forwards /api/* to the LAN backend and
// /static/* to production, so the browser stays same-origin and avoids
// CORS. Revert (delete this file) when pointing back at https://arcvgc.com.
config.devServer = config.devServer || {};
config.devServer.proxy = [
    {
        context: ["/api"],
        target: "<normalized LAN URL>",
        changeOrigin: true,
        secure: false,
    },
    {
        context: ["/static"],
        target: "https://arcvgc.com",
        changeOrigin: true,
        secure: true,
    },
];
```

The `/static` rule is required because the LAN backend tends to hand back production image URLs (`https://arcvgc.com/static/images/...`); `normalizeImageUrl` rewrites them to same-origin, then the proxy forwards them to production.

### 5. Save a memory marker

Save a project memory entry at `local_network_dev_active.md` so future sessions know this is in effect. Use this format and add a corresponding line in `MEMORY.md`:

```markdown
---
name: Local network dev active
description: Network base URL temporarily pointed at LAN — must be reverted before commit/release
type: project
---

LAN base URL `<normalized LAN URL>` is currently configured across all three platforms.

**Why:** active development session against a local backend.

**How to apply:** Before any commit/PR/release, run `/local-network-development off` to restore production config (`https://arcvgc.com`). If the user asks to commit or release while this memory exists, warn them first.
```

### 6. Confirm + remind

After applying, report the diff summary and remind the user **clearly**:

> Local network dev is now active against `<LAN URL>`. Remember to run `/local-network-development off` before committing or releasing — the changes above must not land in main.

If the dev server is currently running, mention they need to restart it (and run `./gradlew :webApp:clean :webApp:wasmJsBrowserDevelopmentRun` if they hit stale-wasm errors).

## Teardown mode (`off`)

Revert every change made during setup:

1. `shared/.../ApiConstants.kt` — `API_HOST` back to `"https://arcvgc.com"`.
2. `shared/.../ApiConstants.android.kt` — `getPlatformBaseUrl()` back to `"https://arcvgc.com"`.
3. `shared/.../ApiConstants.ios.kt` — `getPlatformBaseUrl()` back to `"https://arcvgc.com"`.
4. `shared/.../ApiConstants.wasmJs.kt` — remove the `PROD_IMAGE_HOST` branch from `normalizeImageUrl` (it becomes redundant once `API_HOST` is back to `https://arcvgc.com`).
5. Delete `composeApp/src/androidMain/res/xml/network_security_config.xml` (and the empty `xml/` directory if nothing else lives in it).
6. Remove `android:networkSecurityConfig="@xml/network_security_config"` from `composeApp/src/androidMain/AndroidManifest.xml`.
7. Remove the `NSAppTransportSecurity` dict from `iosApp/iosApp/Info.plist`.
8. Delete `webApp/webpack.config.d/proxy.js`.
9. Remove the project memory entry: delete `local_network_dev_active.md` from the memory directory and remove its line from `MEMORY.md`.

After reverting, confirm with `git diff` that all four platforms' network config is back to production state. Report the result to the user.

## Notes

- The `disable-model-invocation: true` frontmatter means this skill only runs when the user explicitly types `/local-network-development`. Don't try to auto-invoke it.
- If any of the target files already differ from the expected "production" state at setup time, surface the discrepancy to the user before overwriting — they may have other in-flight changes.
- The webpack dev-server proxy is dev-only. Production web builds (`./gradlew :webApp:wasmJsBrowserDistribution`) ignore `webpack.config.d/proxy.js` for the dev-server but webpack still loads the config — leaving the file in place can cause confusing prod-build behavior, so always delete it on teardown.
