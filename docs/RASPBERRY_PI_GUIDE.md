# Raspberry Pi 4 Real Node Integration Guide

## 1. Zero-Simulation Policy
The Raspberry Pi 4 is a real physical edge node. It will only appear on dashboards once physically connected, registered, and executing the monitoring agent.

## 2. Hardware & OS Prerequisites
- Board: Raspberry Pi 4 Model B (2GB, 4GB, or 8GB RAM).
- Operating System: Raspberry Pi OS 64-bit (Debian Bullseye / Bookworm).
- Network: Connected via Ethernet (eth0) or Wi-Fi (wlan0) with network access to the Spring Boot server host.
- Python: Python 3.9+ with `python3-pip` and `python3-venv`.

## 3. Step-by-Step Device Registration

### Step A: Register in Web UI
1. Log in to your Cloud Infrastructure Monitoring account.
2. Click **"Register New Device"** on the dashboard.
3. Choose Name (e.g., `Edge-Pi4-Lab01`) and Device Type: `RASPBERRY_PI_4`.
4. Copy the generated Agent Token and one-line setup command.

### Step B: Run the Agent on the Raspberry Pi
Open SSH or terminal on the Pi and execute:
```bash
curl -sSL http://<SERVER_HOST_IP>:8080/install.sh | bash -s -- --token=<YOUR_PI_AGENT_TOKEN> --server=http://<SERVER_HOST_IP>:8080
```

### Step C: Verification
- The agent registers device hardware specs (Broadcom BCM2711 ARM64 CPU, total RAM, SD card disk capacity, hostname).
- The device status on the web dashboard automatically transitions from `PENDING` to `ONLINE`.
- Real-time CPU, RAM, Disk, and CPU Thermal readings begin streaming into Prometheus and MySQL logs.

## 4. Hardware Sensor Details
The Python agent specifically monitors Raspberry Pi hardware metrics:
- **CPU Thermal Zone**: Read from `/sys/class/thermal/thermal_zone0/temp` (converted to °C).
- **System Journal**: Tail logs via `journalctl` filtered for system services and sanitized against private directories.