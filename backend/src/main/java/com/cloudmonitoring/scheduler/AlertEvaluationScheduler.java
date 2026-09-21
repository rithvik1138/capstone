package com.cloudmonitoring.scheduler;

import com.cloudmonitoring.entity.*;
import com.cloudmonitoring.repository.DeviceRepository;
import com.cloudmonitoring.service.AlertService;
import com.cloudmonitoring.service.PrometheusQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AlertEvaluationScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertEvaluationScheduler.class);

    private final DeviceRepository deviceRepository;
    private final AlertService alertService;
    private final PrometheusQueryService prometheusQueryService;

    // Heartbeat timeout threshold in seconds (e.g. 120s)
    private static final long OFFLINE_THRESHOLD_SECONDS = 120;

    public AlertEvaluationScheduler(DeviceRepository deviceRepository,
                                    AlertService alertService,
                                    PrometheusQueryService prometheusQueryService) {
        this.deviceRepository = deviceRepository;
        this.alertService = alertService;
        this.prometheusQueryService = prometheusQueryService;
    }

    /**
     * Periodically check registered devices for heartbeat / connectivity loss.
     * Evaluates every 30 seconds.
     */
    @Scheduled(fixedDelay = 30000)
    public void evaluateDeviceConnectivity() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(OFFLINE_THRESHOLD_SECONDS);
        List<Device> registeredDevices = deviceRepository.findAll();

        for (Device device : registeredDevices) {
            if (device.getStatus() == DeviceStatus.UNREGISTERED || device.getStatus() == DeviceStatus.PENDING) {
                continue;
            }

            if (device.getLastSeenAt() == null || device.getLastSeenAt().isBefore(cutoff)) {
                if (device.getStatus() == DeviceStatus.ONLINE) {
                    device.setStatus(DeviceStatus.OFFLINE);
                    deviceRepository.save(device);
                    log.warn("Device transitioned to OFFLINE: {} ({})", device.getDeviceName(), device.getDeviceUuid());
                }

                alertService.triggerOrUpdateAlert(
                        device,
                        AlertType.DEVICE_OFFLINE,
                        Severity.CRITICAL,
                        "Device " + device.getDeviceName() + " has not reported a heartbeat for over " + OFFLINE_THRESHOLD_SECONDS + " seconds.",
                        null,
                        (double) OFFLINE_THRESHOLD_SECONDS
                );
            } else {
                // Device has recent heartbeat
                if (device.getStatus() == DeviceStatus.OFFLINE) {
                    device.setStatus(DeviceStatus.ONLINE);
                    deviceRepository.save(device);
                    log.info("Device transitioned to ONLINE: {} ({})", device.getDeviceName(), device.getDeviceUuid());
                }
                // Auto resolve offline alert
                alertService.autoResolveAlert(device.getId(), AlertType.DEVICE_OFFLINE);
            }
        }
    }

    /**
     * Periodically evaluate hardware metrics thresholds from Prometheus for online devices.
     * Evaluates every 30 seconds.
     */
    @Scheduled(fixedDelay = 30000)
    public void evaluateHardwareMetrics() {
        List<Device> onlineDevices = deviceRepository.findByStatus(DeviceStatus.ONLINE);

        for (Device device : onlineDevices) {
            try {
                var metrics = prometheusQueryService.getLiveMetrics(device);

                // 1. CPU Usage > 90%
                if (metrics.getCpuPercent() != null && metrics.getCpuPercent() > 90.0) {
                    alertService.triggerOrUpdateAlert(
                            device,
                            AlertType.HIGH_CPU,
                            Severity.WARNING,
                            String.format("High CPU utilization detected on %s: %.1f%% (Threshold: 90%%)",
                                    device.getDeviceName(), metrics.getCpuPercent()),
                            metrics.getCpuPercent(),
                            90.0
                    );
                } else if (metrics.getCpuPercent() != null && metrics.getCpuPercent() < 85.0) {
                    alertService.autoResolveAlert(device.getId(), AlertType.HIGH_CPU);
                }

                // 2. Memory Usage > 90%
                if (metrics.getRamPercent() != null && metrics.getRamPercent() > 90.0) {
                    alertService.triggerOrUpdateAlert(
                            device,
                            AlertType.HIGH_MEMORY,
                            Severity.WARNING,
                            String.format("High Memory utilization detected on %s: %.1f%% (Threshold: 90%%)",
                                    device.getDeviceName(), metrics.getRamPercent()),
                            metrics.getRamPercent(),
                            90.0
                    );
                } else if (metrics.getRamPercent() != null && metrics.getRamPercent() < 85.0) {
                    alertService.autoResolveAlert(device.getId(), AlertType.HIGH_MEMORY);
                }

                // 3. Disk Usage > 90%
                if (metrics.getDiskPercent() != null && metrics.getDiskPercent() > 90.0) {
                    alertService.triggerOrUpdateAlert(
                            device,
                            AlertType.LOW_DISK,
                            Severity.CRITICAL,
                            String.format("Low disk space on %s: %.1f%% used (Threshold: 90%%)",
                                    device.getDeviceName(), metrics.getDiskPercent()),
                            metrics.getDiskPercent(),
                            90.0
                    );
                } else if (metrics.getDiskPercent() != null && metrics.getDiskPercent() < 85.0) {
                    alertService.autoResolveAlert(device.getId(), AlertType.LOW_DISK);
                }

            } catch (Exception e) {
                log.debug("Metric evaluation skipped for device {}: {}", device.getDeviceUuid(), e.getMessage());
            }
        }
    }
}