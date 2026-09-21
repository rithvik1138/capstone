package com.cloudmonitoring.service;

import com.cloudmonitoring.dto.response.DeviceMetricsDto;
import com.cloudmonitoring.dto.response.MetricHistoryPointDto;
import com.cloudmonitoring.entity.Device;
import com.cloudmonitoring.entity.DeviceStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PrometheusQueryService {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusQueryService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${prometheus.base-url:${prometheus.url:http://localhost:9090}}")
    private String prometheusUrl;

    // In-memory cache for live telemetry snapshots
    private final ConcurrentHashMap<String, DeviceMetricsDto> liveMetricsCache = new ConcurrentHashMap<>();
    
    // In-memory sliding time-series history for immediate chart rendering
    private final ConcurrentHashMap<String, LinkedList<MetricHistoryPointDto>> metricHistoryCache = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY_POINTS = 120;

    public PrometheusQueryService(@Qualifier("prometheusWebClient") WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Query instant metric value from Prometheus TSDB using PromQL.
     */
    public Double queryInstantScalar(String query) {
        try {
            String uri = prometheusUrl + "/api/v1/query?query=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
            String response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(2))
                    .block();

            if (response == null) return null;

            JsonNode root = objectMapper.readTree(response);
            if ("success".equalsIgnoreCase(root.path("status").asText())) {
                JsonNode result = root.path("data").path("result");
                if (result.isArray() && !result.isEmpty()) {
                    JsonNode valNode = result.get(0).path("value");
                    if (valNode.isArray() && valNode.size() >= 2) {
                        return Double.parseDouble(valNode.get(1).asText());
                    }
                }
            }
        } catch (Exception ex) {
            logger.debug("Prometheus query failed for query [{}]: {}", query, ex.getMessage());
        }
        return null;
    }

    /**
     * Updates live telemetry cache and appends to historical ring buffer.
     */
    public void updateLiveMetrics(String deviceUuid, DeviceMetricsDto metrics) {
        if (deviceUuid == null || metrics == null) return;
        
        metrics.setDeviceUuid(deviceUuid);
        metrics.setTimestamp(LocalDateTime.now());
        liveMetricsCache.put(deviceUuid, metrics);

        metricHistoryCache.compute(deviceUuid, (uuid, list) -> {
            if (list == null) list = new LinkedList<>();
            MetricHistoryPointDto pt = new MetricHistoryPointDto(
                    Instant.now(),
                    metrics.getCpuPercent(),
                    metrics.getRamPercent(),
                    metrics.getDiskPercent(),
                    metrics.getNetworkRxRateBytesPerSec(),
                    metrics.getNetworkTxRateBytesPerSec()
            );
            list.addLast(pt);
            while (list.size() > MAX_HISTORY_POINTS) {
                list.removeFirst();
            }
            return list;
        });
    }

    /**
     * Get real-time metric snapshot for a device.
     */
    public DeviceMetricsDto getLiveMetrics(Device device) {
        return getCurrentDeviceMetrics(device);
    }

    public DeviceMetricsDto getCurrentMetrics(String deviceUuid) {
        DeviceMetricsDto metrics = fetchFromPrometheus(deviceUuid);
        if (hasAnyTelemetry(metrics)) {
            updateLiveMetrics(deviceUuid, metrics);
            return metrics;
        }

        // Fallback: Direct scrape localhost/127.0.0.1 on port 9100
        DeviceMetricsDto scraped = directScrapeAgent("127.0.0.1", 9100, deviceUuid);
        if (hasAnyTelemetry(scraped)) {
            updateLiveMetrics(deviceUuid, scraped);
            return scraped;
        }

        // Fallback: Check cached live metrics
        DeviceMetricsDto cached = liveMetricsCache.get(deviceUuid);
        if (cached != null) {
            return cached;
        }

        DeviceMetricsDto empty = new DeviceMetricsDto();
        empty.setDeviceUuid(deviceUuid);
        empty.setStatus("ONLINE");
        return empty;
    }


    public DeviceMetricsDto getCurrentDeviceMetrics(Device device) {
        if (device == null) {
            return new DeviceMetricsDto();
        }

        String uuid = device.getDeviceUuid();
        DeviceMetricsDto metrics = fetchFromPrometheus(uuid);
        metrics.setStatus(device.getStatus() != null ? device.getStatus().name() : "ONLINE");

        if (hasAnyTelemetry(metrics)) {
            updateLiveMetrics(uuid, metrics);
            return metrics;
        }

        // Fallback: Direct scrape using device IP address or localhost
        String ip = (device.getIpAddress() != null && !device.getIpAddress().isBlank() && !"unknown".equalsIgnoreCase(device.getIpAddress()))
                ? device.getIpAddress() : "127.0.0.1";
        
        DeviceMetricsDto scraped = directScrapeAgent(ip, 9100, uuid);
        if (!hasAnyTelemetry(scraped) && !ip.equals("127.0.0.1")) {
            scraped = directScrapeAgent("127.0.0.1", 9100, uuid);
        }

        if (hasAnyTelemetry(scraped)) {
            scraped.setStatus(device.getStatus() != null ? device.getStatus().name() : "ONLINE");
            updateLiveMetrics(uuid, scraped);
            return scraped;
        }

        // Fallback: Check cached live metrics
        DeviceMetricsDto cached = liveMetricsCache.get(uuid);
        if (cached != null) {
            cached.setStatus(device.getStatus() != null ? device.getStatus().name() : "ONLINE");
            return cached;
        }

        return metrics;
    }

    private DeviceMetricsDto fetchFromPrometheus(String deviceUuid) {
        DeviceMetricsDto metrics = new DeviceMetricsDto();
        metrics.setDeviceUuid(deviceUuid);

        // PromQL queries matching Python Prometheus exporter metrics
        String cpuQuery = "node_cpu_usage_percent{device_uuid=\"" + deviceUuid + "\"}";
        String memUsedQuery = "node_memory_used_bytes{device_uuid=\"" + deviceUuid + "\"}";
        String memTotalQuery = "node_memory_total_bytes{device_uuid=\"" + deviceUuid + "\"}";
        String memPercentQuery = "node_memory_usage_percent{device_uuid=\"" + deviceUuid + "\"}";
        String diskUsedQuery = "node_disk_used_bytes{device_uuid=\"" + deviceUuid + "\"}";
        String diskTotalQuery = "node_disk_total_bytes{device_uuid=\"" + deviceUuid + "\"}";
        String diskPercentQuery = "node_disk_usage_percent{device_uuid=\"" + deviceUuid + "\"}";
        String netRxQuery = "node_network_receive_rate_bps{device_uuid=\"" + deviceUuid + "\"}";
        String netTxQuery = "node_network_transmit_rate_bps{device_uuid=\"" + deviceUuid + "\"}";
        String uptimeQuery = "node_uptime_seconds{device_uuid=\"" + deviceUuid + "\"}";
        String tempQuery = "node_temperature_celsius{device_uuid=\"" + deviceUuid + "\"}";

        Double cpu = queryInstantScalar(cpuQuery);
        Double memUsed = queryInstantScalar(memUsedQuery);
        Double memTotal = queryInstantScalar(memTotalQuery);
        Double memPercent = queryInstantScalar(memPercentQuery);
        Double diskUsed = queryInstantScalar(diskUsedQuery);
        Double diskTotal = queryInstantScalar(diskTotalQuery);
        Double diskPercent = queryInstantScalar(diskPercentQuery);
        Double netRx = queryInstantScalar(netRxQuery);
        Double netTx = queryInstantScalar(netTxQuery);
        Double uptime = queryInstantScalar(uptimeQuery);
        Double temp = queryInstantScalar(tempQuery);

        if (cpu != null) metrics.setCpuUsagePercent(Math.round(cpu * 10.0) / 10.0);
        
        if (memPercent != null) {
            metrics.setMemoryUsagePercent(Math.round(memPercent * 10.0) / 10.0);
        } else if (memUsed != null && memTotal != null && memTotal > 0) {
            metrics.setMemoryUsagePercent(Math.round((memUsed / memTotal * 100.0) * 10.0) / 10.0);
        }
        if (memUsed != null) metrics.setMemoryUsedBytes(memUsed.longValue());
        if (memTotal != null) metrics.setMemoryTotalBytes(memTotal.longValue());

        if (diskPercent != null) {
            metrics.setDiskUsagePercent(Math.round(diskPercent * 10.0) / 10.0);
        } else if (diskUsed != null && diskTotal != null && diskTotal > 0) {
            metrics.setDiskUsagePercent(Math.round((diskUsed / diskTotal * 100.0) * 10.0) / 10.0);
        }
        if (diskUsed != null) metrics.setDiskUsedBytes(diskUsed.longValue());
        if (diskTotal != null) metrics.setDiskTotalBytes(diskTotal.longValue());

        if (netRx != null) metrics.setNetworkRxRateBytesPerSec(Math.round(netRx * 10.0) / 10.0);
        if (netTx != null) metrics.setNetworkTxRateBytesPerSec(Math.round(netTx * 10.0) / 10.0);
        if (uptime != null) metrics.setUptimeSeconds(uptime.longValue());
        if (temp != null) metrics.setTemperatureC(temp);

        return metrics;
    }

    /**
     * Direct scraping of agent /metrics endpoint on port 9100.
     */
    public DeviceMetricsDto directScrapeAgent(String host, int port, String deviceUuid) {
        DeviceMetricsDto metrics = new DeviceMetricsDto();
        metrics.setDeviceUuid(deviceUuid);

        try {
            String url = "http://" + host + ":" + port + "/metrics";
            String body = WebClient.create().get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(1500))
                    .block();

            if (body != null && !body.isBlank()) {
                parsePrometheusMetricsText(body, metrics, deviceUuid);
            }
        } catch (Exception ex) {
            logger.trace("Direct scrape to http://{}:{}/metrics skipped: {}", host, port, ex.getMessage());
        }
        return metrics;
    }

    private void parsePrometheusMetricsText(String body, DeviceMetricsDto metrics, String deviceUuid) {
        try (BufferedReader reader = new BufferedReader(new StringReader(body))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length < 2) continue;

                String metricWithLabels = parts[0];
                double val;
                try {
                    val = Double.parseDouble(parts[1]);
                } catch (NumberFormatException e) {
                    continue;
                }

                if (deviceUuid != null && metricWithLabels.contains("device_uuid=") && !metricWithLabels.contains(deviceUuid)) {
                    continue;
                }

                if (metricWithLabels.startsWith("node_cpu_usage_percent") || metricWithLabels.startsWith("system_cpu_usage_percent")) {
                    metrics.setCpuUsagePercent(Math.round(val * 10.0) / 10.0);
                } else if (metricWithLabels.startsWith("node_memory_usage_percent") || metricWithLabels.startsWith("system_memory_usage_percent")) {
                    metrics.setMemoryUsagePercent(Math.round(val * 10.0) / 10.0);
                } else if (metricWithLabels.startsWith("node_memory_used_bytes") || metricWithLabels.startsWith("system_memory_used_bytes")) {
                    metrics.setMemoryUsedBytes((long) val);
                } else if (metricWithLabels.startsWith("node_memory_total_bytes") || metricWithLabels.startsWith("system_memory_total_bytes")) {
                    metrics.setMemoryTotalBytes((long) val);
                } else if (metricWithLabels.startsWith("node_disk_usage_percent") || metricWithLabels.startsWith("system_disk_usage_percent")) {
                    metrics.setDiskUsagePercent(Math.round(val * 10.0) / 10.0);
                } else if (metricWithLabels.startsWith("node_disk_used_bytes") || metricWithLabels.startsWith("system_disk_used_bytes")) {
                    metrics.setDiskUsedBytes((long) val);
                } else if (metricWithLabels.startsWith("node_disk_total_bytes") || metricWithLabels.startsWith("system_disk_total_bytes")) {
                    metrics.setDiskTotalBytes((long) val);
                } else if (metricWithLabels.startsWith("node_network_receive_rate_bps") || metricWithLabels.startsWith("system_network_receive_rate_bps")) {
                    metrics.setNetworkRxRateBytesPerSec(Math.round(val * 10.0) / 10.0);
                } else if (metricWithLabels.startsWith("node_network_transmit_rate_bps") || metricWithLabels.startsWith("system_network_transmit_rate_bps")) {
                    metrics.setNetworkTxRateBytesPerSec(Math.round(val * 10.0) / 10.0);
                } else if (metricWithLabels.startsWith("node_uptime_seconds") || metricWithLabels.startsWith("system_uptime_seconds")) {
                    metrics.setUptimeSeconds((long) val);
                } else if (metricWithLabels.startsWith("node_temperature_celsius") || metricWithLabels.startsWith("system_temperature_celsius")) {
                    metrics.setTemperatureC(val);
                }
            }

            if (metrics.getMemoryUsagePercent() == null && metrics.getMemoryUsedBytes() != null && metrics.getMemoryTotalBytes() != null && metrics.getMemoryTotalBytes() > 0) {
                metrics.setMemoryUsagePercent(Math.round(((double) metrics.getMemoryUsedBytes() / metrics.getMemoryTotalBytes() * 100.0) * 10.0) / 10.0);
            }
            if (metrics.getDiskUsagePercent() == null && metrics.getDiskUsedBytes() != null && metrics.getDiskTotalBytes() != null && metrics.getDiskTotalBytes() > 0) {
                metrics.setDiskUsagePercent(Math.round(((double) metrics.getDiskUsedBytes() / metrics.getDiskTotalBytes() * 100.0) * 10.0) / 10.0);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse metrics text: {}", e.getMessage());
        }
    }

    private boolean hasAnyTelemetry(DeviceMetricsDto dto) {
        return dto != null && (dto.getCpuPercent() != null || dto.getRamPercent() != null || dto.getDiskPercent() != null || dto.getUptimeSeconds() != null);
    }

    /**
     * Fetch time series range history from Prometheus for real graphing.
     */
    public List<MetricHistoryPointDto> getMetricHistory(Device device, String range, String step) {
        if (device == null || device.getStatus() == DeviceStatus.PENDING) {
            return Collections.emptyList();
        }

        String uuid = device.getDeviceUuid();
        long endSeconds = Instant.now().getEpochSecond();
        long startSeconds = endSeconds - parseDurationToSeconds(range);

        String cpuRangeQuery = "node_cpu_usage_percent{device_uuid=\"" + uuid + "\"}";
        
        List<MetricHistoryPointDto> points = new ArrayList<>();
        try {
            String uri = prometheusUrl + "/api/v1/query_range?query=" + java.net.URLEncoder.encode(cpuRangeQuery, java.nio.charset.StandardCharsets.UTF_8)
                    + "&start=" + startSeconds + "&end=" + endSeconds + "&step=" + step;

            String response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode result = root.path("data").path("result");
                if (result.isArray() && !result.isEmpty()) {
                    JsonNode values = result.get(0).path("values");
                    for (JsonNode item : values) {
                        if (item.isArray() && item.size() >= 2) {
                            long ts = item.get(0).asLong();
                            double val = Double.parseDouble(item.get(1).asText());
                            MetricHistoryPointDto pt = new MetricHistoryPointDto();
                            pt.setTimestamp(Instant.ofEpochSecond(ts));
                            pt.setCpuPercent(Math.round(val * 10.0) / 10.0);
                            points.add(pt);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.debug("Prometheus query_range failed: {}", ex.getMessage());
        }

        if (!points.isEmpty()) {
            return points;
        }

        // Fallback to in-memory history cache
        List<MetricHistoryPointDto> cached = metricHistoryCache.get(uuid);
        if (cached != null && !cached.isEmpty()) {
            return new ArrayList<>(cached);
        }

        return Collections.emptyList();
    }

    public void removeDeviceFromCache(String deviceUuid) {
        if (deviceUuid != null) {
            liveMetricsCache.remove(deviceUuid);
            metricHistoryCache.remove(deviceUuid);
        }
    }

    private long parseDurationToSeconds(String duration) {
        if (duration == null || duration.isEmpty()) return 3600; // 1 hour default
        if (duration.endsWith("h")) {
            return Long.parseLong(duration.replace("h", "")) * 3600;
        } else if (duration.endsWith("m")) {
            return Long.parseLong(duration.replace("m", "")) * 60;
        } else if (duration.endsWith("d")) {
            return Long.parseLong(duration.replace("d", "")) * 86400;
        }
        return 3600;
    }
}