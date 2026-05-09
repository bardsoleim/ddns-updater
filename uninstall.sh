#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/opt/ddns-updater"
DATA_DIR="/var/lib/ddns-updater"
SERVICE_USER="ddns-updater"
SERVICE_FILE="/etc/systemd/system/ddns-updater.service"

if [[ $EUID -ne 0 ]]; then
    echo "This script must be run as root (use sudo)." >&2
    exit 1
fi

echo "==> Stopping and disabling service..."
systemctl stop ddns-updater 2>/dev/null || true
systemctl disable ddns-updater 2>/dev/null || true
rm -f "$SERVICE_FILE"
systemctl daemon-reload

echo "==> Removing application files..."
rm -rf "$APP_DIR"

read -r -p "Delete all data (domain database at $DATA_DIR)? [y/N] " confirm
if [[ "${confirm,,}" == "y" ]]; then
    rm -rf "$DATA_DIR"
    echo "  Data deleted."
else
    echo "  Data kept at $DATA_DIR."
fi

echo "==> Removing system user..."
userdel "$SERVICE_USER" 2>/dev/null || true

echo ""
echo "✓ ddns-updater has been uninstalled."
