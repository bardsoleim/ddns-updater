# ddns-updater

A lightweight Dynamic DNS updater service. It periodically checks your public IP and notifies your DNS provider when it changes, keeping your domain pointed at your home server.

Built with Kotlin + Ktor, runs as a systemd service on Ubuntu.

## Install

```bash
curl -sSL https://raw.githubusercontent.com/bardsoleim/ddns-updater/master/install.sh | sudo bash
```

Requires Ubuntu with `curl` available. The script installs Java 21, downloads the latest pre-built JAR from GitHub Releases, and sets up a systemd service.

Re-running the same command will update the app to the latest version.

## Uninstall

```bash
curl -sSL https://raw.githubusercontent.com/bardsoleim/ddns-updater/master/uninstall.sh | sudo bash
```

You will be asked whether to also delete the domain database.

## Managing the service

```bash
sudo systemctl status ddns-updater
sudo systemctl restart ddns-updater
sudo journalctl -u ddns-updater -f   # live logs
```

## API

The service runs on port **1337** and is intended to be accessible only from your local network.

### Add a domain

**Namecheap** (root domain):
```bash
curl -X POST http://localhost:1337/domain \
  -H "Content-Type: application/json" \
  -d '{
    "dnsProvider": "https://dynamicdns.park-your-domain.com/update",
    "domain": "example.com",
    "password": "your-namecheap-ddns-password"
  }'
```

**Namecheap** (subdomain, e.g. `www`):
```bash
curl -X POST http://localhost:1337/domain \
  -H "Content-Type: application/json" \
  -d '{
    "dnsProvider": "https://dynamicdns.park-your-domain.com/update",
    "domain": "example.com",
    "password": "your-namecheap-ddns-password",
    "host": "www"
  }'
```

`host` defaults to `"@"` (root domain) when omitted. Leave out `ip` to have the service auto-detect your public IP, or provide it explicitly:

```bash
  "ip": "1.2.3.4"
```

### List domains

```bash
curl http://localhost:1337/domain
```

### Remove a domain

```bash
curl -X DELETE http://localhost:1337/domain \
  -d "domain=example.com"
```

## How it works

Every 5 minutes the service:
1. Fetches your current public IP (via [api.ipify.org](https://api.ipify.org))
2. For each registered domain, calls the configured `dnsProvider` URL with `?host=<subdomain>&domain=<domain>&password=<password>&ip=<ip>`
3. Only makes the call if the IP has changed since the last update

## Data

The H2 database is stored at `/var/lib/ddns-updater/db`. Domain records (including passwords) are stored in plaintext — keep the service off the public internet.

## Development

Requires Java 21.

```bash
./gradlew run        # run locally
./gradlew build      # compile + test
./gradlew shadowJar  # build fat JAR → build/libs/ddns-updater-*-all.jar
```

The database path can be overridden with the `DDNS_DB_PATH` environment variable.
