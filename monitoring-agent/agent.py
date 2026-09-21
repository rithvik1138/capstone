import argparse
import os
import signal
import sys
import time

from collectors.system_collector import SystemCollector
from collectors.log_collector import LogCollector
from publisher.prometheus_exporter import PrometheusExporter
from publisher.backend_client import BackendClient

class CloudMonitoringAgent:
    """
    Real infrastructure monitoring agent daemon.
    Collects genuine system metrics and permitted logs from host machine.
    """

    def __init__(self, server_url: str, agent_token: str, device_uuid: str = None, metrics_port: int = 9100):
        self.server_url = server_url
        self.agent_token = agent_token
        self.device_uuid = device_uuid
        self.metrics_port = metrics_port
        
        self.system_collector = SystemCollector()
        self.log_collector = LogCollector()
        self.backend_client = BackendClient(server_url, agent_token, device_uuid=device_uuid)
        self.exporter = None
        self.running = False

    def start(self):
        print("==================================================")
        print("   Cloud Monitoring & Logging Agent v1.0.0        ")
        print("==================================================")
        print(f"[*] Target Server: {self.server_url}")
        print(f"[*] Metrics Port:  {self.metrics_port}")
        if self.device_uuid:
            print(f"[*] Device UUID:   {self.device_uuid}")
        
        # Step 1: Gather authentic hardware info and perform registration handshake
        print("[*] Performing hardware inspection...")
        hw_info = self.system_collector.get_hardware_info()
        if self.device_uuid:
            hw_info["deviceUuid"] = self.device_uuid

        print(f"    - Hostname: {hw_info['hostname']}")
        print(f"    - OS:       {hw_info['osName']} {hw_info['osVersion']}")
        print(f"    - CPU:      {hw_info['cpuModel']} ({hw_info['totalCores']} cores)")
        print(f"    - RAM:      {round(hw_info['totalMemoryBytes'] / (1024**3), 2)} GB")

        print("[*] Connecting to Cloud Monitoring backend...")
        handshake_resp = self.backend_client.send_handshake(hw_info, device_uuid=self.device_uuid)
        if not handshake_resp or not handshake_resp.get("success"):
            print("[!] FATAL: Handshake failed. Please check your X-Agent-Token, Device UUID, and server URL.")
            sys.exit(1)

        device_data = handshake_resp.get("data", {})
        self.device_uuid = device_data.get("deviceUuid") or self.device_uuid
        device_name = device_data.get("deviceName", "Unknown")
        print(f"[+] Successfully authenticated device: '{device_name}' (UUID: {self.device_uuid})")

        # Step 2: Start Prometheus Metrics Exporter
        print(f"[*] Starting local Prometheus exporter on 0.0.0.0:{self.metrics_port}/metrics...")
        self.exporter = PrometheusExporter(self.system_collector, self.device_uuid, self.metrics_port)
        self.exporter.start()

        # Step 3: Start telemetry and heartbeat loop
        self.running = True
        print("[+] Monitoring agent is now ACTIVE. Press Ctrl+C to terminate.")

        last_heartbeat = 0
        last_log_check = 0
        heartbeat_interval = 10  # Seconds
        log_interval = 15        # Seconds

        while self.running:
            now = time.time()
            
            # Periodic Heartbeat & Live Telemetry Push
            if now - last_heartbeat >= heartbeat_interval:
                live_metrics = self.system_collector.collect_live_metrics()
                success = self.backend_client.send_heartbeat(self.device_uuid, live_metrics)
                if success:
                    print(f"[{time.strftime('%H:%M:%S')}] Heartbeat acknowledged (ONLINE) - CPU: {live_metrics['cpuPercent']}%, RAM: {live_metrics['ramPercent']}%")
                else:
                    print(f"[{time.strftime('%H:%M:%S')}] Heartbeat warning: server unreachable")
                last_heartbeat = now

            # Periodic Log Collection & Ingestion
            if now - last_log_check >= log_interval:
                logs = self.log_collector.collect_recent_logs(max_lines=25)
                if logs:
                    self.backend_client.send_logs(logs)
                last_log_check = now

            time.sleep(1)

    def stop(self):
        print("\n[*] Stopping Cloud Monitoring agent...")
        self.running = False
        if self.exporter:
            self.exporter.stop()
        print("[+] Agent gracefully stopped.")

def main():
    parser = argparse.ArgumentParser(description="Cloud Infrastructure Monitoring and Logging Agent")
    parser.add_argument("--server", type=str, default=os.getenv("MONITORING_SERVER_URL", "http://localhost:8080"),
                        help="Backend server URL (e.g. http://localhost:8080)")
    parser.add_argument("--token", type=str, default=os.getenv("MONITORING_AGENT_TOKEN"),
                        help="Device authorization agent token")
    parser.add_argument("--uuid", "--device-uuid", "--deviceUuid", dest="uuid", type=str,
                        default=os.getenv("MONITORING_DEVICE_UUID"),
                        help="Device UUID generated during registration")
    parser.add_argument("--port", type=int, default=int(os.getenv("MONITORING_METRICS_PORT", "9100")),
                        help="Prometheus exporter HTTP port (default: 9100)")

    args = parser.parse_args()

    if not args.token:
        print("[!] Error: --token (or MONITORING_AGENT_TOKEN environment variable) is required.")
        print("    Example: python agent.py --server http://localhost:8080 --token cmd_YOUR_TOKEN --uuid YOUR_DEVICE_UUID")
        sys.exit(1)

    if not args.uuid:
        print("[!] Error: --uuid (or MONITORING_DEVICE_UUID environment variable) is required.")
        print("    Example: python agent.py --server http://localhost:8080 --token cmd_YOUR_TOKEN --uuid YOUR_DEVICE_UUID")
        sys.exit(1)

    agent = CloudMonitoringAgent(args.server, args.token, args.uuid, args.port)

    def signal_handler(sig, frame):
        agent.stop()
        sys.exit(0)

    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)

    agent.start()

if __name__ == "__main__":
    main()