#!/bin/bash
#
# Deploy ARC web app to production server
#
# Usage: ./deploy/deploy.sh [user@host]
# Default: reads DEPLOY_HOST from secrets.properties
#

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
SECRETS_FILE="$PROJECT_DIR/secrets.properties"

# Read DEPLOY_HOST from secrets.properties if no argument provided
if [ -n "$1" ]; then
    SERVER="$1"
elif [ -f "$SECRETS_FILE" ]; then
    SERVER=$(grep '^DEPLOY_HOST=' "$SECRETS_FILE" | cut -d'=' -f2-)
fi

if [ -z "$SERVER" ]; then
    echo "Error: No deploy host specified."
    echo "Either pass it as an argument: ./deploy/deploy.sh user@host"
    echo "Or set DEPLOY_HOST in secrets.properties"
    exit 1
fi

REMOTE_DIR="/var/www/arcvgc"
BUILD_DIR="webApp/build/dist/wasmJs/productionExecutable"

cd "$PROJECT_DIR"

echo "=== Step 1: Building webapp ==="
./gradlew :webApp:wasmJsBrowserDistribution
echo ""

echo "=== Step 2: Creating remote directory ==="
ssh "$SERVER" "mkdir -p $REMOTE_DIR"
echo ""

echo "=== Step 3: Uploading webapp files ==="
rsync -avz --delete "$BUILD_DIR/" "$SERVER:$REMOTE_DIR/"
echo ""

echo "=== Step 4: Uploading legal pages ==="
scp legal/privacy-policy.html "$SERVER:$REMOTE_DIR/privacy-policy.html"
scp legal/terms-of-service.html "$SERVER:$REMOTE_DIR/terms-of-service.html"
echo ""

echo "=== Done! ==="
echo "Files uploaded to $SERVER:$REMOTE_DIR"
echo ""
echo "If this is the first deploy, you still need to set up nginx on the server."
echo "See deploy/SETUP.md for instructions."
