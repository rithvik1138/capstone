#!/usr/bin/env bash
# ==============================================================================
# Cloud Infrastructure Monitoring & Logging Agent - Linux/Raspberry Pi Installer
# ==============================================================================
set -e

SERVER_URL="http://localhost:8080"
AGENT_TOKEN=""
DEVICE_UUID=""
METRICS_PORT=9100
INSTALL_DIR="/opt/cloud-monitoring-agent"

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --server|--server-url) SERVER_URL="$2"; shift ;;
        --token) AGENT_TOKEN="$2"; shift ;;
        --uuid|--device-uuid) DEVICE_UUID="$2"; shift ;;
        --port) METRICS_PORT="$2"; shift ;;
        --dir) INSTALL_DIR="$2"; shift ;;
        *) echo "Unknown parameter: $1"; exit 1 ;;
    esac
    shift
done

if [ -z "$AGENT_TOKEN" ]; then
    echo "[!] Error: --token is required."
    echo "    Usage: curl -sSL <URL>/install.sh | bash -s -- --token <TOKEN> --uuid <UUID> --server <SERVER>"
    exit 1
fi

if [ -z "$DEVICE_UUID" ]; then
    echo "[!] Error: --uuid is required."
    echo "    Usage: curl -sSL <URL>/install.sh | bash -s -- --token <TOKEN> --uuid <UUID> --server <SERVER>"
    exit 1
fi

echo "=================================================="
echo " Installing Cloud Monitoring Agent (Linux/ARM64)  "
echo "=================================================="
echo "[*] Server URL:   $SERVER_URL"
echo "[*] Metrics Port: $METRICS_PORT"
echo "[*] Install Path: $INSTALL_DIR"

# Check Python3, Pip, and Unzip
if ! command -v python3 &> /dev/null || ! command -v unzip &> /dev/null; then
    echo "[*] Installing python3, python3-pip, python3-venv, unzip..."
    sudo apt-get update -y && sudo apt-get install -y python3 python3-pip python3-venv unzip curl
fi

# Create install directory
echo "[*] Setting up install directory at $INSTALL_DIR..."
sudo mkdir -p "$INSTALL_DIR"
sudo chown -R "$USER:$USER" "$INSTALL_DIR"

# Copy or download agent files
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
if [ -f "$SCRIPT_DIR/agent.py" ]; then
    cp -r "$SCRIPT_DIR"/* "$INSTALL_DIR/"
elif [ ! -f "$INSTALL_DIR/agent.py" ]; then
    echo "[*] Downloading agent package from $SERVER_URL/agent.zip..."
    curl -sSL "$SERVER_URL/agent.zip" -o "$INSTALL_DIR/agent.zip"
    unzip -q -o "$INSTALL_DIR/agent.zip" -d "$INSTALL_DIR/temp"
    if [ -d "$INSTALL_DIR/temp/monitoring-agent" ]; then
        cp -r "$INSTALL_DIR/temp/monitoring-agent"/* "$INSTALL_DIR/"
    else
        cp -r "$INSTALL_DIR/temp"/* "$INSTALL_DIR/"
    fi
    rm -rf "$INSTALL_DIR/temp" "$INSTALL_DIR/agent.zip"
fi

cd "$INSTALL_DIR"
if [ ! -d "venv" ]; then
    python3 -m venv venv
fi
./venv/bin/pip install --upgrade pip --quiet
./venv/bin/pip install -r requirements.txt --quiet

# Create systemd service
SERVICE_FILE="/etc/systemd/system/cloud-monitoring-agent.service"
echo "[*] Registering systemd service at $SERVICE_FILE..."

sudo bash -c "cat > $SERVICE_FILE" <<EOF
[Unit]
Description=Cloud Infrastructure Monitoring and Logging Agent
After=network.target

[Service]
Type=simple
User=$USER
WorkingDirectory=$INSTALL_DIR
ExecStart=$INSTALL_DIR/venv/bin/python agent.py --server $SERVER_URL --token $AGENT_TOKEN --uuid $DEVICE_UUID --port $METRICS_PORT
Restart=always
RestartSec=5
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable cloud-monitoring-agent
sudo systemctl restart cloud-monitoring-agent

echo "=================================================="
echo "[+] Cloud Monitoring Agent successfully installed & started!"
echo "    Status check: sudo systemctl status cloud-monitoring-agent"
echo "    Live logs:    journalctl -u cloud-monitoring-agent -f"
echo "=================================================="