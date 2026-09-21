import os
import platform
import re
import subprocess
import time
from datetime import datetime

class LogCollector:
    """
    Collects real logs from system event streams while enforcing strict privacy
    and sanitization guards. Never accesses personal files or private credentials.
    """

    # Sensitive patterns to scrub
    REDACT_PATTERNS = [
        (re.compile(r'(?i)(password|passwd|pwd|secret|token|api[_-]?key|auth|bearer)\s*[:=]\s*["\']?([^"\'\s,;]+)["\']?'), r'\1=[REDACTED]'),
        (re.compile(r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,7}\b'), r'[REDACTED_EMAIL]'),
        (re.compile(r'\b(?:\d{4}[-\s]?){3}\d{4}\b'), r'[REDACTED_CARD]'),
        (re.compile(r'cmd_[a-f0-9]{64}'), r'[REDACTED_AGENT_TOKEN]')
    ]

    def __init__(self):
        self.os_type = platform.system()
        self._last_log_time = datetime.now()

    def sanitize_message(self, message: str) -> str:
        """
        Strips private tokens, passwords, and sensitive keys from log lines.
        """
        if not message:
            return ""
        sanitized = message.strip()
        for pattern, replacement in self.REDACT_PATTERNS:
            sanitized = pattern.sub(replacement, sanitized)
        return sanitized

    def collect_recent_logs(self, max_lines=50):
        """
        Retrieves authentic system/application log entries from the operating system.
        """
        logs = []
        try:
            if self.os_type == "Linux":
                logs = self._collect_linux_journal(max_lines)
            elif self.os_type == "Windows":
                logs = self._collect_windows_events(max_lines)
            else:
                logs = self._collect_generic_syslog(max_lines)
        except Exception as e:
            logs.append({
                "timestamp": int(time.time() * 1000),
                "level": "WARNING",
                "service": "agent-log-collector",
                "message": f"Log collection fallback active: {str(e)}",
                "eventType": "SYSTEM_DIAGNOSTIC"
            })
        return logs

    def _collect_linux_journal(self, max_lines):
        logs = []
        try:
            # Uses journalctl to read system-level logs without personal directories
            cmd = ["journalctl", "-n", str(max_lines), "-o", "short-iso", "--no-pager"]
            output = subprocess.check_output(cmd, stderr=subprocess.DEVNULL, timeout=3).decode("utf-8", errors="ignore")
            for line in output.strip().splitlines():
                parsed = self._parse_syslog_line(line)
                if parsed:
                    logs.append(parsed)
        except Exception:
            # Fallback to /var/log/syslog if accessible
            if os.path.exists("/var/log/syslog"):
                try:
                    with open("/var/log/syslog", "r") as f:
                        lines = f.readlines()[-max_lines:]
                        for line in lines:
                            parsed = self._parse_syslog_line(line)
                            if parsed:
                                logs.append(parsed)
                except Exception:
                    pass
        return logs

    def _collect_windows_events(self, max_lines):
        logs = []
        try:
            # Uses PowerShell Get-WinEvent to query System and Application event logs
            ps_script = f"Get-WinEvent -LogName 'System', 'Application' -MaxEvents {max_lines} | Select-Object TimeCreated, LevelDisplayName, ProviderName, Message | ConvertTo-Json -Compress"
            cmd = ["powershell", "-NoProfile", "-NonInteractive", "-Command", ps_script]
            output = subprocess.check_output(cmd, stderr=subprocess.DEVNULL, timeout=5).decode("utf-8", errors="ignore")
            
            import json
            data = json.loads(output)
            items = data if isinstance(data, list) else [data]
            
            for item in items:
                raw_msg = item.get("Message") or ""
                level_str = (item.get("LevelDisplayName") or "Information").upper()
                
                level = "INFO"
                if "ERROR" in level_str or "CRITICAL" in level_str:
                    level = "ERROR"
                elif "WARN" in level_str:
                    level = "WARNING"

                sanitized_msg = self.sanitize_message(raw_msg)
                
                # Parse Windows time string /Date(1234567890)/ or ISO
                ts = int(time.time() * 1000)
                time_val = item.get("TimeCreated")
                if time_val and "/Date(" in str(time_val):
                    try:
                        ts = int(re.search(r'\d+', time_val).group())
                    except Exception:
                        pass

                logs.append({
                    "timestamp": ts,
                    "level": level,
                    "service": item.get("ProviderName") or "Windows-EventLog",
                    "message": sanitized_msg[:1000],  # Keep reasonable size
                    "eventType": "SYSTEM_EVENT"
                })
        except Exception:
            pass
        return logs

    def _collect_generic_syslog(self, max_lines):
        return []

    def _parse_syslog_line(self, line: str):
        if not line:
            return None
        level = "INFO"
        lower = line.lower()
        if "err" in lower or "fail" in lower or "fatal" in lower:
            level = "ERROR"
        elif "warn" in lower:
            level = "WARNING"
        elif "crit" in lower or "panic" in lower:
            level = "CRITICAL"

        sanitized = self.sanitize_message(line)
        return {
            "timestamp": int(time.time() * 1000),
            "level": level,
            "service": "system-journal",
            "message": sanitized[:1000],
            "eventType": "SYSTEM_JOURNAL"
        }