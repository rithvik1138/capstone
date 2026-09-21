package com.cloudmonitoring.dto.response;

import com.cloudmonitoring.entity.DeviceStatus;
import com.cloudmonitoring.entity.DeviceType;

import java.time.LocalDateTime;

public class AdminDeviceTableDto {

    private Long id;
    private String deviceUuid;
    private String deviceName;
    private DeviceType deviceType;
    private Long ownerUserId;
    private String ownerUsername;
    private String ownerEmail;
    private String hostname;
    private String ipAddress;
    private String osName;
    private String osVersion;
    private String cpuModel;
    private Integer totalCores;
    private Long totalMemoryBytes;
    private Long totalDiskBytes;
    private DeviceStatus status;
    private LocalDateTime lastSeenAt;
    private LocalDateTime createdAt;
    private Double currentCpuPercent;
    private Double currentMemoryPercent;
    private Double currentDiskPercent;

    public AdminDeviceTableDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getCpuModel() {
        return cpuModel;
    }

    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    public Integer getTotalCores() {
        return totalCores;
    }

    public void setTotalCores(Integer totalCores) {
        this.totalCores = totalCores;
    }

    public Long getTotalMemoryBytes() {
        return totalMemoryBytes;
    }

    public void setTotalMemoryBytes(Long totalMemoryBytes) {
        this.totalMemoryBytes = totalMemoryBytes;
    }

    public Long getTotalDiskBytes() {
        return totalDiskBytes;
    }

    public void setTotalDiskBytes(Long totalDiskBytes) {
        this.totalDiskBytes = totalDiskBytes;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getCurrentCpuPercent() {
        return currentCpuPercent;
    }

    public void setCurrentCpuPercent(Double currentCpuPercent) {
        this.currentCpuPercent = currentCpuPercent;
    }

    public Double getCurrentMemoryPercent() {
        return currentMemoryPercent;
    }

    public void setCurrentMemoryPercent(Double currentMemoryPercent) {
        this.currentMemoryPercent = currentMemoryPercent;
    }

    public Double getCurrentDiskPercent() {
        return currentDiskPercent;
    }

    public void setCurrentDiskPercent(Double currentDiskPercent) {
        this.currentDiskPercent = currentDiskPercent;
    }
}