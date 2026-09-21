import os
import platform
import socket
import sys
import time
import psutil

class SystemCollector:
    """
    Collects genuine, real hardware and OS metrics directly from the host machine.
    No simulated or mock values are used.
    """

    def __init__(self):
        self._prev_net_io = psutil.net_io_counters()
        self._prev_net_time = time.time()
        # Initialize CPU baseline so interval=None returns non-zero utilization immediately
        psutil.cpu_percent(interval=None)
        psutil.cpu_percent(interval=None, percpu=True)

    def get_hardware_info(self):
        """
        Gathers static hardware & OS identification info for registration handshake.
        """
        try:
            hostname = socket.gethostname()
        except Exception:
            hostname = "unknown-host"

        try:
            ip_address = socket.gethostbyname(hostname)
        except Exception:
            ip_address = "127.0.0.1"

        try:
            import uuid
            mac_num = uuid.getnode()
            mac_address = ':'.join(['{:02x}'.format((mac_num >> elements) & 0xff) for elements in range(0, 2 * 6, 2)][::-1])
        except Exception:
            mac_address = "00:00:00:00:00:00"

        os_name = platform.system()
        os_version = f"{platform.release()} ({platform.version()})"
        cpu_model = platform.processor() or "Generic CPU"
        total_cores = psutil.cpu_count(logical=True) or 1
        
        mem = psutil.virtual_memory()
        total_memory_bytes = mem.total

        disk_path = 'C:\\' if platform.system() == 'Windows' else '/'
        try:
            disk = psutil.disk_usage(disk_path)
            total_disk_bytes = disk.total
        except Exception:
            total_disk_bytes = 0

        return {
            "hostname": hostname,
            "ipAddress": ip_address,
            "macAddress": mac_address,
            "osName": os_name,
            "osVersion": os_version,
            "cpuModel": cpu_model,
            "totalCores": total_cores,
            "totalMemoryBytes": total_memory_bytes,
            "totalDiskBytes": total_disk_bytes,
            "agentVersion": "1.0.0"
        }

    def collect_live_metrics(self):
        """
        Polls current real-time metric readings from the operating system.
        """
        cpu_percent = psutil.cpu_percent(interval=None)
        cpu_per_core = psutil.cpu_percent(interval=None, percpu=True)
        
        mem = psutil.virtual_memory()
        ram_percent = mem.percent
        ram_used = mem.used
        ram_total = mem.total

        disk_path = 'C:\\' if platform.system() == 'Windows' else '/'
        try:
            disk = psutil.disk_usage(disk_path)
            disk_percent = disk.percent
            disk_used = disk.used
            disk_total = disk.total
        except Exception:
            disk_percent = 0.0
            disk_used = 0
            disk_total = 0

        current_time = time.time()
        current_net_io = psutil.net_io_counters()
        time_delta = current_time - self._prev_net_time

        if time_delta > 0:
            rx_rate = (current_net_io.bytes_recv - self._prev_net_io.bytes_recv) / time_delta
            tx_rate = (current_net_io.bytes_sent - self._prev_net_io.bytes_sent) / time_delta
        else:
            rx_rate = 0.0
            tx_rate = 0.0

        self._prev_net_io = current_net_io
        self._prev_net_time = current_time

        uptime_seconds = int(time.time() - psutil.boot_time())

        temperature_c = self._get_temperature()

        return {
            "cpuPercent": round(cpu_percent, 2),
            "cpuPerCore": [round(c, 2) for c in cpu_per_core],
            "ramPercent": round(ram_percent, 2),
            "ramUsedBytes": ram_used,
            "ramTotalBytes": ram_total,
            "diskPercent": round(disk_percent, 2),
            "diskUsedBytes": disk_used,
            "diskTotalBytes": disk_total,
            "networkRxRateBps": round(rx_rate, 2),
            "networkTxRateBps": round(tx_rate, 2),
            "uptimeSeconds": uptime_seconds,
            "temperatureC": temperature_c
        }

    def _get_temperature(self):
        """
        Extracts hardware thermal sensor readings on Linux and Raspberry Pi.
        Returns None if not supported by hardware/OS.
        """
        try:
            if hasattr(psutil, "sensors_temperatures"):
                temps = psutil.sensors_temperatures()
                if temps:
                    for name, entries in temps.items():
                        for entry in entries:
                            if entry.current:
                                return round(entry.current, 1)

            # Raspberry Pi specific thermal path check
            thermal_path = "/sys/class/thermal/thermal_zone0/temp"
            if os.path.exists(thermal_path):
                with open(thermal_path, "r") as f:
                    temp_raw = f.read().strip()
                    return round(float(temp_raw) / 1000.0, 1)
        except Exception:
            pass
        return None