import time
import requests

class BackendClient:
    """
    Communicates securely with the Spring Boot backend API using the device agent token.
    """
    def __init__(self, backend_url: str, agent_token: str, device_uuid: str = None):
        self.backend_url = backend_url.rstrip("/")
        self.agent_token = agent_token
        self.device_uuid = device_uuid
        self.session = requests.Session()
        self.session.headers.update({
            "Content-Type": "application/json",
            "X-Agent-Token": self.agent_token,
            "X-Device-Token": self.agent_token
        })

    def send_handshake(self, hw_info: dict, device_uuid: str = None) -> dict:
        """
        Transmits initial hardware and operating system profile along with device UUID.
        """
        url = f"{self.backend_url}/api/agent/handshake"
        payload = dict(hw_info) if hw_info else {}
        target_uuid = device_uuid or payload.get("deviceUuid") or self.device_uuid
        if target_uuid:
            payload["deviceUuid"] = target_uuid

        try:
            resp = self.session.post(url, json=payload, timeout=10)
            if resp.status_code == 200:
                return resp.json()
            else:
                print(f"[BackendClient] Handshake rejected with status {resp.status_code}: {resp.text}")
                return None
        except Exception as e:
            print(f"[BackendClient] Handshake connection error: {e}")
            return None

    def send_heartbeat(self, device_uuid: str = None, metrics: dict = None) -> bool:
        """
        Emits periodic heartbeat with live metric snapshot to update last_seen_at timestamp and telemetry cache.
        """
        target_uuid = device_uuid or self.device_uuid
        if not target_uuid:
            print("[BackendClient] Heartbeat error: Missing device UUID")
            return False

        url = f"{self.backend_url}/api/agent/heartbeat"
        payload = {
            "deviceUuid": target_uuid,
            "timestamp": int(time.time() * 1000)
        }
        if metrics and isinstance(metrics, dict):
            payload.update({
                "cpuPercent": metrics.get("cpuPercent"),
                "ramPercent": metrics.get("ramPercent"),
                "ramUsedBytes": metrics.get("ramUsedBytes"),
                "ramTotalBytes": metrics.get("ramTotalBytes"),
                "diskPercent": metrics.get("diskPercent"),
                "diskUsedBytes": metrics.get("diskUsedBytes"),
                "diskTotalBytes": metrics.get("diskTotalBytes"),
                "networkRxRateBps": metrics.get("networkRxRateBps"),
                "networkTxRateBps": metrics.get("networkTxRateBps"),
                "uptimeSeconds": metrics.get("uptimeSeconds"),
                "temperatureC": metrics.get("temperatureC")
            })

        try:
            resp = self.session.post(url, json=payload, timeout=5)
            return resp.status_code == 200
        except Exception as e:
            print(f"[BackendClient] Heartbeat error: {e}")
            return False

    def send_logs(self, log_records: list) -> bool:
        """
        Transmits sanitized real-time log batches.
        """
        if not log_records:
            return True
        url = f"{self.backend_url}/api/agent/logs"
        try:
            resp = self.session.post(url, json=log_records, timeout=10)
            return resp.status_code == 200
        except Exception as e:
            print(f"[BackendClient] Log transmission error: {e}")
            return False