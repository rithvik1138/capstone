package com.cloudmonitoring.dto.response;

import com.cloudmonitoring.entity.LogLevel;

import java.time.LocalDateTime;

public class DeviceLogDto {
    private Long id;
    private Long deviceId;
    private String deviceName;
    private String deviceUuid;
    private LocalDateTime timestamp;
    private LogLevel logLevel;
    private String serviceSource;
    private String message;
    private String eventType;
    private LocalDateTime createdAt;

    public DeviceLogDto() {
    }

    public DeviceLogDto(Long id, Long deviceId, String deviceName, String deviceUuid, LocalDateTime timestamp, LogLevel logLevel, String serviceSource, String message, String eventType, LocalDateTime createdAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceUuid = deviceUuid;
        this.timestamp = timestamp;
        this.logLevel = logLevel;
        this.serviceSource = serviceSource;
        this.message = message;
        this.eventType = eventType;
        this.createdAt = createdAt;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getServiceSource() {
        return serviceSource;
    }

    public void setServiceSource(String serviceSource) {
        this.serviceSource = serviceSource;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}