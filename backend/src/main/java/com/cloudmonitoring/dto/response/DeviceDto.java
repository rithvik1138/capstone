package com.cloudmonitoring.dto.response;

import com.cloudmonitoring.entity.Device;
import com.cloudmonitoring.entity.DeviceStatus;
import com.cloudmonitoring.entity.DeviceType;

import java.time.LocalDateTime;

public class DeviceDto {

    private Long id;
    private String deviceUuid;
    private String deviceName;
    private DeviceType deviceType;
    private String hostname;
    private String ipAddress;
    private String macAddress;
    private String osName;
    private String osVersion;
    private String cpuModel;
    private Integer totalCores;
    private Long totalMemoryBytes;
    private Long totalDiskBytes;
    private String agentVersion;
    private DeviceStatus status;
    private LocalDateTime lastSeenAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private String username;

    public DeviceDto() {
    }

    public DeviceDto(Device device) {
        if (device != null) {
            this.id = device.getId();
            this.deviceUuid = device.getDeviceUuid();
            this.deviceName = device.getDeviceName();
            this.deviceType = device.getDeviceType();
            this.hostname = device.getHostname();
            this.ipAddress = device.getIpAddress();
            this.macAddress = device.getMacAddress();
            this.osName = device.getOsName();
            this.osVersion = device.getOsVersion();
            this.cpuModel = device.getCpuModel();
            this.totalCores = device.getTotalCores();
            this.totalMemoryBytes = device.getTotalMemoryBytes();
            this.totalDiskBytes = device.getTotalDiskBytes();
            this.agentVersion = device.getAgentVersion();
            this.status = device.getStatus();
            this.lastSeenAt = device.getLastSeenAt();
            this.createdAt = device.getCreatedAt();
            this.updatedAt = device.getUpdatedAt();
            if (device.getUser() != null) {
                this.userId = device.getUser().getId();
                this.username = device.getUser().getUsername();
            }
        }
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

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
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

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}