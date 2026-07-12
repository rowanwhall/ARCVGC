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

## WebGL Context-Loss Recovery

Browsers evict GPU contexts from long-backgrounded tabs, and Skiko (Compose-web's renderer) has no context-loss recovery — the wasm runtime keeps handling input (clicks still update the URL via `pushState`) but nothing repaints, leaving the app frozen until a manual refresh. An inline script in `webApp/src/wasmJsMain/resources/index.html` recovers automatically: it watches for the Compose canvas via `MutationObserver`, listens for `webglcontextlost`, reports to Sentry (flushed before navigating so the event isn't lost), and reloads the page — immediately when the tab is visible, otherwise deferred to the next `visibilitychange` (reloading a hidden tab risks immediate re-eviction). A 30s `sessionStorage` timestamp paces reloads: a repeat loss inside the window schedules a retry for when the window expires, so a pathological GPU reloads at most twice a minute instead of looping. The guard is best-effort — if storage access is blocked, recovery still reloads. Deep-link URL mirroring means the reload restores the user's location.

Invariants:
- The script must **never call `canvas.getContext()`** — creating the `webgl2` context before Skiko would fix the wrong context attributes; it relies on the `webglcontextlost` event only.
- It must stay in `index.html` (plain JS, before `webApp.js`) so it works even when the render loop is dead.

To test manually in DevTools on the running app:
```js
document.querySelector('canvas').getContext('webgl2').getExtension('WEBGL_lose_context').loseContext()
```
The page should reload in place (once — repeating it within 30s schedules the next reload for when the guard window expires).

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
