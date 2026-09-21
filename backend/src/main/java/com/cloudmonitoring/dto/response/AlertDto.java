package com.cloudmonitoring.dto.response;

import com.cloudmonitoring.entity.Alert;
import com.cloudmonitoring.entity.AlertStatus;
import com.cloudmonitoring.entity.AlertType;
import com.cloudmonitoring.entity.Severity;

import java.time.LocalDateTime;

public class AlertDto {

    private Long id;
    private Long deviceId;
    private String deviceName;
    private String deviceUuid;
    private String username;
    private AlertType alertType;
    private Severity severity;
    private String message;
    private Double currentVal;
    private Double thresholdVal;
    private AlertStatus status;
    private LocalDateTime triggeredAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime updatedAt;

    public AlertDto() {
    }

    public static AlertDto fromEntity(Alert alert) {
        AlertDto dto = new AlertDto();
        dto.setId(alert.getId());
        if (alert.getDevice() != null) {
            dto.setDeviceId(alert.getDevice().getId());
            dto.setDeviceName(alert.getDevice().getDeviceName());
            dto.setDeviceUuid(alert.getDevice().getDeviceUuid());
            if (alert.getDevice().getUser() != null) {
                dto.setUsername(alert.getDevice().getUser().getUsername());
            }
        }
        dto.setAlertType(alert.getAlertType());
        dto.setSeverity(alert.getSeverity());
        dto.setMessage(alert.getMessage());
        dto.setCurrentVal(alert.getCurrentVal());
        dto.setThresholdVal(alert.getThresholdVal());
        dto.setStatus(alert.getStatus());
        dto.setTriggeredAt(alert.getTriggeredAt());
        dto.setResolvedAt(alert.getResolvedAt());
        dto.setUpdatedAt(alert.getUpdatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getCurrentVal() {
        return currentVal;
    }

    public void setCurrentVal(Double currentVal) {
        this.currentVal = currentVal;
    }

    public Double getThresholdVal() {
        return thresholdVal;
    }

    public void setThresholdVal(Double thresholdVal) {
        this.thresholdVal = thresholdVal;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}