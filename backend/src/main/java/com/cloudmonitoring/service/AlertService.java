package com.cloudmonitoring.service;

import com.cloudmonitoring.dto.response.AlertDto;
import com.cloudmonitoring.entity.*;
import com.cloudmonitoring.exception.ResourceNotFoundException;
import com.cloudmonitoring.exception.UnauthorizedAccessException;
import com.cloudmonitoring.repository.AlertRepository;
import com.cloudmonitoring.repository.DeviceRepository;
import com.cloudmonitoring.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final DeviceRepository deviceRepository;
    private final AuditService auditService;

    public AlertService(AlertRepository alertRepository,
                        DeviceRepository deviceRepository,
                        AuditService auditService) {
        this.alertRepository = alertRepository;
        this.deviceRepository = deviceRepository;
        this.auditService = auditService;
    }

    /**
     * Trigger or update an active alert for a device based on real monitoring condition.
     */
    @Transactional
    public Alert triggerOrUpdateAlert(Device device, AlertType alertType, Severity severity,
                                      String message, Double currentVal, Double thresholdVal) {
        Optional<Alert> existingOpt = alertRepository.findByDeviceIdAndAlertTypeAndStatus(
                device.getId(), alertType, AlertStatus.ACTIVE);

        if (existingOpt.isPresent()) {
            Alert existing = existingOpt.get();
            existing.setCurrentVal(currentVal);
            existing.setThresholdVal(thresholdVal);
            existing.setMessage(message);
            existing.setSeverity(severity);
            existing.setUpdatedAt(LocalDateTime.now());
            return alertRepository.save(existing);
        }

        Alert alert = new Alert();
        alert.setDevice(device);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setCurrentVal(currentVal);
        alert.setThresholdVal(thresholdVal);
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setTriggeredAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        log.warn("ALERT TRIGGERED [Device: {}] [Type: {}] [Severity: {}]: {}",
                device.getDeviceName(), alertType, severity, message);

        return alertRepository.save(alert);
    }

    /**
     * Auto-resolve an active alert when telemetry returns to normal parameters.
     */
    @Transactional
    public void autoResolveAlert(Long deviceId, AlertType alertType) {
        Optional<Alert> existingOpt = alertRepository.findByDeviceIdAndAlertTypeAndStatus(
                deviceId, alertType, AlertStatus.ACTIVE);

        if (existingOpt.isPresent()) {
            Alert alert = existingOpt.get();
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedAt(LocalDateTime.now());
            alert.setUpdatedAt(LocalDateTime.now());
            alertRepository.save(alert);
            log.info("ALERT AUTO-RESOLVED [Device ID: {}] [Type: {}]", deviceId, alertType);
        }
    }

    /**
     * Manually resolve an alert.
     */
    @Transactional
    public AlertDto resolveAlert(Long alertId, UserPrincipal principal) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));

        if (!principal.isAdmin() && !alert.getDevice().getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to resolve alerts for this device");
        }

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());
        Alert saved = alertRepository.save(alert);

        auditService.recordAudit(principal.getId(), "RESOLVE_ALERT", "ALERT",
                String.valueOf(alertId), Map.of("deviceUuid", alert.getDevice().getDeviceUuid(), "type", alert.getAlertType().name()));

        return AlertDto.fromEntity(saved);
    }

    /**
     * Acknowledge an active alert.
     */
    @Transactional
    public AlertDto acknowledgeAlert(Long alertId, UserPrincipal principal) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));

        if (!principal.isAdmin() && !alert.getDevice().getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to acknowledge alerts for this device");
        }

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setUpdatedAt(LocalDateTime.now());
        Alert saved = alertRepository.save(alert);

        return AlertDto.fromEntity(saved);
    }

    /**
     * Get paginated alerts for user or admin with optional filtering.
     */
    @Transactional(readOnly = true)
    public Page<AlertDto> getAlerts(UserPrincipal principal, AlertStatus status, Severity severity, Pageable pageable) {
        if (principal.isAdmin()) {
            return alertRepository.findAllWithFilters(status, severity, pageable).map(AlertDto::fromEntity);
        } else {
            return alertRepository.findByUserIdWithFilters(principal.getId(), status, severity, pageable).map(AlertDto::fromEntity);
        }
    }

    /**
     * Get paginated alerts for a specific device.
     */
    @Transactional(readOnly = true)
    public Page<AlertDto> getDeviceAlerts(Long deviceId, Pageable pageable) {
        return alertRepository.findByDeviceId(deviceId, pageable).map(AlertDto::fromEntity);
    }

    /**
     * Get alert statistics.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAlertCounts(UserPrincipal principal) {
        Map<String, Object> counts = new HashMap<>();
        if (principal.isAdmin()) {
            counts.put("totalActive", alertRepository.countByStatus(AlertStatus.ACTIVE));
            counts.put("criticalActive", alertRepository.countByStatusAndSeverity(AlertStatus.ACTIVE, Severity.CRITICAL));
            counts.put("warningActive", alertRepository.countByStatusAndSeverity(AlertStatus.ACTIVE, Severity.WARNING));
            counts.put("acknowledged", alertRepository.countByStatus(AlertStatus.ACKNOWLEDGED));
            counts.put("resolved", alertRepository.countByStatus(AlertStatus.RESOLVED));
        } else {
            counts.put("totalActive", alertRepository.countByUserIdAndStatus(principal.getId(), AlertStatus.ACTIVE));
            counts.put("acknowledged", alertRepository.countByUserIdAndStatus(principal.getId(), AlertStatus.ACKNOWLEDGED));
            counts.put("resolved", alertRepository.countByUserIdAndStatus(principal.getId(), AlertStatus.RESOLVED));
        }
        return counts;
    }
}