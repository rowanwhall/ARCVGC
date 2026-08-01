# Web App Deployment

The web app is hosted at **https://arcvgc.com** on a DigitalOcean droplet that also runs the Django API. nginx serves the static webapp files and reverse-proxies `/api/` and `/static/` to gunicorn. Server connection details are in `secrets.properties` (see `secrets.properties.example`).

## How to deploy

From the project root on the local machine:

```bash
./deploy/deploy.sh
```

This builds the webapp (`./gradlew :webApp:wasmJsBrowserDistribution`), uploads the production files to `/var/www/arcvgc/` on the server via rsync, uploads the legal HTML pages, and uploads `.well-known/` files (Apple AASA + Android assetlinks). The nginx config is also uploaded and reloaded.

The script reads `DEPLOY_HOST` from `secrets.properties`. You can also pass the host as an argument: `./deploy/deploy.sh user@host`.

## When to deploy

Deploy the web app after any changes to:
- `webApp/` (web UI code)
- `shared/` (shared code used by web)
- `legal/*.html` (privacy policy, terms of service)
- `deploy/.well-known/` (Apple AASA, Android assetlinks)
- `deploy/arcvgc.conf` (nginx config)

## Deployment files

- `deploy/deploy.sh` — Build + upload script (run from local machine)
- `deploy/arcvgc.conf` — nginx server config (installed on server at `/etc/nginx/sites-available/arcvgc.conf`). Includes `/.well-known/` location for deep link verification files.
- `deploy/.well-known/apple-app-site-association` — iOS Universal Links config
- `deploy/.well-known/assetlinks.json` — Android App Links config (currently debug key only)
- `deploy/SETUP.md` — One-time server setup guide (DNS, nginx, HTTPS)

## Server details

- **nginx config**: `/etc/nginx/sites-available/arcvgc.conf` (symlinked to `sites-enabled`)
- **Webapp files**: `/var/www/arcvgc/`
- **Django API**: gunicorn proxied by nginx
- **SSL**: Let's Encrypt via certbot (auto-renews)
- **Domain**: `arcvgc.com` — DNS A records point to the droplet

## If nginx config changes are needed

After editing `deploy/arcvgc.conf` locally, upload and reload:
```bash
scp deploy/arcvgc.conf $DEPLOY_HOST:/etc/nginx/sites-available/arcvgc.conf
# Then on the server:
sudo nginx -t && sudo systemctl reload nginx
```

## Render-Death Recovery (WebGL context loss & frozen render loop)

Browsers evict GPU contexts from long-backgrounded tabs, and Skiko (Compose-web's renderer) has no context-loss recovery — the wasm runtime keeps handling input (clicks still update the URL via `pushState`) but nothing repaints, leaving the app frozen until a manual refresh. A related mode: an exception escaping a Compose frame callback kills the `requestAnimationFrame` chain with the same symptom.

**Critical DOM fact:** `ComposeViewport` renders into a canvas inside an **open shadow root on `document.body`** — `document.querySelector('canvas')` finds nothing. Every canvas lookup must also check `document.body.shadowRoot`. (The first version of this fix missed that and never armed in production.)

Recovery is two-part:

1. **Watchdog script** in `webApp/src/wasmJsMain/resources/index.html` with three detectors sharing one reload path:
   - `webglcontextlost` on the shadow-root canvas (fastest; browsers don't always fire it; ignored for detached canvases — Compose orphans the old canvas when recreating its context),
   - Compose canvas removed from the DOM (incl. shadow root) and not replaced for 8s,
   - frame heartbeat stale for 15s while the tab is visible and a canvas is present (canvas-gone-and-heartbeat-dead is detector 2's territory).
2. **`FrameHeartbeat.kt`** (`webApp/.../com/arcvgc/app/FrameHeartbeat.kt`, called at the top of `WebApp()`): awaits `withFrameNanos` then stamps `globalThis.__arcFrameTs`, every ~3s. If Compose's frame clock dies, the stamp stops and detector 3 fires.

Shared reload path: reports the detector-specific message to Sentry (flushed before navigating), then reloads — immediately when visible, deferred to the next `visibilitychange` otherwise (reloading a hidden tab risks immediate re-eviction). A 30s `sessionStorage` timestamp paces reloads (repeat failure schedules a retry at window expiry; at most ~2 reloads/min). Deep-link URL mirroring restores the user's location after reload.

Invariants:
- The script must **never call `canvas.getContext()`** — creating the `webgl2` context before Skiko would fix the wrong attributes.
- It must stay in `index.html` (plain JS, before `webApp.js`) so it works even when the wasm render loop is dead.
- Polled detectors only arm after the app demonstrably booted (canvas seen / heartbeat present) and only judge time the tab spent visible; a large inter-tick gap (system sleep/page freeze) resets the grace window. These gates are what design out false-positive reloads during normal operation — a stale `webApp.js` (no heartbeat) simply leaves detector 3 inert.
- Sentry messages distinguish the detectors ("WebGL context lost" / "Compose canvas disappeared" / "Render loop stalled") — use them to attribute freezes in the wild.

To test manually in DevTools on the running app (note the shadow root):
```js
document.body.shadowRoot.querySelector('canvas').getContext('webgl2').getExtension('WEBGL_lose_context').loseContext()
```
The page should reload in place almost immediately — the event path is not tied to the 5s poll; the ~2s upper bound is the Sentry flush (repeating it within 30s schedules the next reload for when the guard window expires). To check the heartbeat: `globalThis.__arcFrameTs` should be at most ~3s old while the page renders.

## CORS & Image URL Handling

The API and web app are served from the same origin (`https://arcvgc.com`) via nginx reverse proxy, so CORS is not an issue in production. Two mechanisms handle dev and image URLs:

### API requests
- `getPlatformBaseUrl()` returns `""` on wasmJs, so all API calls use relative paths (`/api/v0/...`)
- `webApp/webpack.config.d/devServer.js` configures a webpack dev server proxy that forwards `/api` and `/static` to `https://arcvgc.com`
- In production, nginx reverse-proxies `/api/` and `/static/` to gunicorn on the same server

### Image URLs
- The API returns absolute image URLs (`https://arcvgc.com/static/images/...`) in all responses
- `normalizeImageUrl()` (`expect`/`actual` in `shared/.../network/`) rewrites these at the DTO-to-domain mapping layer:
  - **Android/iOS**: No-op (returns URL unchanged — direct HTTPS works fine)
  - **wasmJs**: Replaces the API host with `window.location.origin` (e.g., `http://localhost:8082/static/images/...` in dev), so requests go through the webpack proxy
- Applied in all 4 DTO-to-domain mappers: `MatchDetailMapper`, `MatchPreviewMapper`, `PokemonListMapper`, `ItemListMapper`
- This approach works in both dev (webpack proxy) and production (same-origin), since `window.location.origin` always resolves to the correct host
- Coil's `KtorNetworkFetcherFactory` requires full URLs — relative paths (like `/static/...`) will not work
