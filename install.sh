#!/usr/bin/env bash
set -euo pipefail

REPO="bardsoleim/ddns-updater"
JAR_URL="https://github.com/$REPO/releases/latest/download/ddns-updater.jar"
APP_DIR="/opt/ddns-updater"
DATA_DIR="/var/lib/ddns-updater"
SERVICE_USER="ddns-updater"
SERVICE_FILE="/etc/systemd/system/ddns-updater.service"
PORT=1337

if [[ $EUID -ne 0 ]]; then
    echo "This script must be run as root (use sudo)." >&2
    exit 1
fi

echo "==> Installing dependencies..."
apt-get update -qq
apt-get install -y -qq openjdk-21-jre-headless curl

echo "==> Setting up user and directories..."
useradd --system --no-create-home --shell /bin/false "$SERVICE_USER" 2>/dev/null || true
mkdir -p "$APP_DIR" "$DATA_DIR"
chown "$SERVICE_USER:$SERVICE_USER" "$DATA_DIR"

echo "==> Downloading latest release..."
curl -fsSL "$JAR_URL" -o "$APP_DIR/ddns-updater.jar"

echo "==> Installing systemd service..."
cat > "$SERVICE_FILE" <<EOF
[Unit]
Description=DDNS Updater
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=$SERVICE_USER
WorkingDirectory=$DATA_DIR
ExecStart=/usr/bin/java -jar $APP_DIR/ddns-updater.jar
Restart=on-failure
RestartSec=10
Environment=DDNS_DB_PATH=$DATA_DIR/db

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable ddns-updater
systemctl restart ddns-updater

echo ""
echo "✓ ddns-updater is running on port $PORT"
echo "  Manage with: systemctl {start|stop|restart|status} ddns-updater"
echo "  Logs:        journalctl -u ddns-updater -f"
echo "  Update:      curl -sSL https://raw.githubusercontent.com/$REPO/master/install.sh | sudo bash"
