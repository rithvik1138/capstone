import threading
from http.server import HTTPServer, BaseHTTPRequestHandler

class PrometheusMetricsHandler(BaseHTTPRequestHandler):
    collector = None
    device_uuid = "unknown"

    def do_GET(self):
        if self.path == "/metrics" or self.path == "/":
            self.send_response(200)
            self.send_header("Content-Type", "text/plain; version=0.0.4; charset=utf-8")
            self.end_headers()

            if PrometheusMetricsHandler.collector:
                metrics = PrometheusMetricsHandler.collector.collect_live_metrics()
                uuid = PrometheusMetricsHandler.device_uuid
                
                output = []
                # CPU Metrics
                output.append(f"# HELP node_cpu_usage_percent Real CPU utilization percentage")
                output.append(f"# TYPE node_cpu_usage_percent gauge")
                output.append(f'node_cpu_usage_percent{{device_uuid="{uuid}"}} {metrics["cpuPercent"]}')
                output.append(f'system_cpu_usage_percent{{device_uuid="{uuid}"}} {metrics["cpuPercent"]}')

                # Memory Metrics
                output.append(f"# HELP node_memory_usage_percent Real RAM utilization percentage")
                output.append(f"# TYPE node_memory_usage_percent gauge")
                output.append(f'node_memory_usage_percent{{device_uuid="{uuid}"}} {metrics["ramPercent"]}')
                output.append(f'system_memory_usage_percent{{device_uuid="{uuid}"}} {metrics["ramPercent"]}')

                output.append(f"# HELP node_memory_used_bytes Real RAM used in bytes")
                output.append(f"# TYPE node_memory_used_bytes gauge")
                output.append(f'node_memory_used_bytes{{device_uuid="{uuid}"}} {metrics["ramUsedBytes"]}')
                output.append(f'system_memory_used_bytes{{device_uuid="{uuid}"}} {metrics["ramUsedBytes"]}')

                output.append(f"# HELP node_memory_total_bytes Real RAM total capacity in bytes")
                output.append(f"# TYPE node_memory_total_bytes gauge")
                output.append(f'node_memory_total_bytes{{device_uuid="{uuid}"}} {metrics["ramTotalBytes"]}')
                output.append(f'system_memory_total_bytes{{device_uuid="{uuid}"}} {metrics["ramTotalBytes"]}')

                # Disk Metrics
                output.append(f"# HELP node_disk_usage_percent Real root disk utilization percentage")
                output.append(f"# TYPE node_disk_usage_percent gauge")
                output.append(f'node_disk_usage_percent{{device_uuid="{uuid}"}} {metrics["diskPercent"]}')
                output.append(f'system_disk_usage_percent{{device_uuid="{uuid}"}} {metrics["diskPercent"]}')

                output.append(f"# HELP node_disk_used_bytes Real root disk used space in bytes")
                output.append(f"# TYPE node_disk_used_bytes gauge")
                output.append(f'node_disk_used_bytes{{device_uuid="{uuid}"}} {metrics["diskUsedBytes"]}')
                output.append(f'system_disk_used_bytes{{device_uuid="{uuid}"}} {metrics["diskUsedBytes"]}')

                output.append(f"# HELP node_disk_total_bytes Real root disk total capacity in bytes")
                output.append(f"# TYPE node_disk_total_bytes gauge")
                output.append(f'node_disk_total_bytes{{device_uuid="{uuid}"}} {metrics["diskTotalBytes"]}')
                output.append(f'system_disk_total_bytes{{device_uuid="{uuid}"}} {metrics["diskTotalBytes"]}')

                # Network Metrics
                output.append(f"# HELP node_network_receive_rate_bps Real network ingress bytes per second")
                output.append(f"# TYPE node_network_receive_rate_bps gauge")
                output.append(f'node_network_receive_rate_bps{{device_uuid="{uuid}"}} {metrics["networkRxRateBps"]}')
                output.append(f'system_network_receive_rate_bps{{device_uuid="{uuid}"}} {metrics["networkRxRateBps"]}')

                output.append(f"# HELP node_network_transmit_rate_bps Real network egress bytes per second")
                output.append(f"# TYPE node_network_transmit_rate_bps gauge")
                output.append(f'node_network_transmit_rate_bps{{device_uuid="{uuid}"}} {metrics["networkTxRateBps"]}')
                output.append(f'system_network_transmit_rate_bps{{device_uuid="{uuid}"}} {metrics["networkTxRateBps"]}')

                # Uptime & Thermal
                output.append(f"# HELP node_uptime_seconds Real system uptime in seconds")
                output.append(f"# TYPE node_uptime_seconds gauge")
                output.append(f'node_uptime_seconds{{device_uuid="{uuid}"}} {metrics["uptimeSeconds"]}')
                output.append(f'system_uptime_seconds{{device_uuid="{uuid}"}} {metrics["uptimeSeconds"]}')

                if metrics.get("temperatureC") is not None:
                    output.append(f"# HELP node_temperature_celsius Real hardware temperature in Celsius")
                    output.append(f"# TYPE node_temperature_celsius gauge")
                    output.append(f'node_temperature_celsius{{device_uuid="{uuid}"}} {metrics["temperatureC"]}')
                    output.append(f'system_temperature_celsius{{device_uuid="{uuid}"}} {metrics["temperatureC"]}')

                self.wfile.write("\n".join(output).encode("utf-8") + b"\n")
            else:
                self.wfile.write(b"# Collector unavailable\n")
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        # Suppress noisy HTTP access logs
        return

class PrometheusExporter:
    """
    Spawns a lightweight local HTTP server exposing /metrics for Prometheus scraping.
    """
    def __init__(self, collector, device_uuid: str, port: int = 9100):
        self.collector = collector
        self.device_uuid = device_uuid
        self.port = port
        self.server = None
        self.thread = None

    def start(self):
        PrometheusMetricsHandler.collector = self.collector
        PrometheusMetricsHandler.device_uuid = self.device_uuid

        try:
            self.server = HTTPServer(("0.0.0.0", self.port), PrometheusMetricsHandler)
            self.thread = threading.Thread(target=self.server.serve_forever, daemon=True)
            self.thread.start()
            print(f"[Exporter] Prometheus exporter running on port {self.port} at /metrics")
        except Exception as e:
            print(f"[Exporter] Warning: Could not bind port {self.port}: {e}")

    def stop(self):
        if self.server:
            self.server.shutdown()