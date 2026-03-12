# ARC Web App - Server Setup Guide

One-time setup for hosting the web app. After this, future deploys are just `./deploy/deploy.sh`.

---

## Prerequisites

Set `DEPLOY_HOST` in `secrets.properties` (see `secrets.properties.example`):
```
DEPLOY_HOST=user@your-server-ip
```

---

## Step 1: Investigate the server

SSH into your server and run these commands. Copy-paste the output back so we can finalize the nginx config if needed.

```bash
ssh $DEPLOY_HOST
```

Then run:

```bash
# What's listening on port 80?
sudo ss -tlnp | grep ':80'

# Is nginx already installed?
which nginx && nginx -v

# Is gunicorn/uwsgi running? What port?
ps aux | grep -E 'gunicorn|uwsgi'

# Existing nginx config (if nginx exists)
ls /etc/nginx/sites-enabled/ 2>/dev/null
cat /etc/nginx/sites-enabled/default 2>/dev/null
```

---

## Step 2: Install nginx (if not already installed)

```bash
sudo apt update
sudo apt install -y nginx
```

---

## Step 3: Upload the nginx config

From your **local machine** (not the server):

```bash
scp deploy/arcvgc.conf $DEPLOY_HOST:/etc/nginx/sites-available/arcvgc.conf
```

Then on the **server**:

```bash
# Enable the site
sudo ln -sf /etc/nginx/sites-available/arcvgc.conf /etc/nginx/sites-enabled/arcvgc.conf

# Remove the default site (optional, only if arcvgc.com is the only site)
# sudo rm /etc/nginx/sites-enabled/default

# Test the config
sudo nginx -t

# Reload nginx
sudo systemctl reload nginx
```

---

## Step 4: Deploy the webapp files

From your **local machine**:

```bash
./deploy/deploy.sh
```

This builds the webapp and uploads all files (including legal pages) to `/var/www/arcvgc/` on the server.

---

## Step 5: Update DNS

1. Go to your domain registrar's DNS settings
2. Add these records:

| Type | Host | Value |
|------|------|-------|
| A | @ | `<your-server-ip>` |
| A | www | `<your-server-ip>` |

3. Wait for propagation (usually 5-30 minutes, up to 48 hours)

You can check propagation with:
```bash
dig arcvgc.com +short
# Should return your server IP
```

---

## Step 6: Set up HTTPS

Once DNS is pointing to the server (Step 5 complete):

```bash
ssh $DEPLOY_HOST

# Install certbot
sudo apt install -y certbot python3-certbot-nginx

# Get certificate + auto-configure nginx for HTTPS
sudo certbot --nginx -d arcvgc.com -d www.arcvgc.com

# Follow the prompts:
# - Enter your email for renewal notices
# - Agree to terms
# - Choose whether to redirect HTTP → HTTPS (recommended: yes)
```

Certbot automatically:
- Gets a free SSL certificate from Let's Encrypt
- Modifies your nginx config to serve HTTPS
- Sets up auto-renewal (cron job)

---

## Step 7: Verify

```bash
# Should return webapp HTML
curl https://arcvgc.com

# Should return API JSON
curl https://arcvgc.com/api/v0/matches/?limit=1

# Should return privacy policy HTML
curl https://arcvgc.com/privacy-policy
```

Then open https://arcvgc.com in a browser and verify:
- The app loads and displays battles
- Pokemon images/sprites load correctly
- Search works
- Settings → Privacy Policy / Terms of Service links open correctly

---

## Future Deploys

After the initial setup, deploying updates is one command:

```bash
./deploy/deploy.sh
```

No server config changes needed — it just rebuilds and re-uploads the static files.

---

## Troubleshooting

**"502 Bad Gateway"** — Django/gunicorn isn't running or is on a different port. Check:
```bash
sudo ss -tlnp | grep '8000'
ps aux | grep gunicorn
```
If gunicorn is on a different port, update the `proxy_pass` line in `/etc/nginx/sites-available/arcvgc.conf`.

**Images not loading** — Check browser dev tools (F12 → Network). If `/static/` requests 404, Django may serve static files from a different path. Check:
```bash
# On the server, find where Django static files live
find / -path "*/static/images" -type d 2>/dev/null
```

**WASM not loading** — Some nginx versions don't recognize `.wasm` files. The config includes a MIME type block, but if it still fails:
```bash
# Check if wasm MIME type is in nginx's mime.types
grep wasm /etc/nginx/mime.types
```

**DNS not resolving** — Check propagation:
```bash
dig arcvgc.com +short
# Should show your server IP
```

**Certbot fails** — DNS must be pointing to the server first. Certbot validates by hitting the domain.
