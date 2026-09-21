package com.cloudmonitoring.dto.response;

import java.time.LocalDateTime;

public class DeviceMetricsDto {
    private String deviceUuid;
    private String status;
    private Double cpuUsagePercent;
    private Double memoryUsagePercent;
    private Double diskUsagePercent;
    private Long memoryUsedBytes;
    private Long memoryTotalBytes;
    private Long diskUsedBytes;
    private Long diskTotalBytes;
    private Double networkRxRateBytesPerSec;
    private Double networkTxRateBytesPerSec;
    private Long uptimeSeconds;
    private Double temperatureC;
    private String cpuModel;
    private LocalDateTime timestamp;

    public DeviceMetricsDto() {
        this.timestamp = LocalDateTime.now();
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getCpuUsagePercent() {
        return cpuUsagePercent;
    }

    public void setCpuUsagePercent(Double cpuUsagePercent) {
        this.cpuUsagePercent = cpuUsagePercent;
    }

    public Double getCpuPercent() {
        return cpuUsagePercent;
    }

    public void setCpuPercent(Double cpuPercent) {
        this.cpuUsagePercent = cpuPercent;
    }

    public Double getCpu() {
        return cpuUsagePercent;
    }

    public Double getMemoryUsagePercent() {
        return memoryUsagePercent;
    }

    public void setMemoryUsagePercent(Double memoryUsagePercent) {
        this.memoryUsagePercent = memoryUsagePercent;
    }

    public Double getRamPercent() {
        return memoryUsagePercent;
    }

    public void setRamPercent(Double ramPercent) {
        this.memoryUsagePercent = ramPercent;
    }

    public Double getRam() {
        return memoryUsagePercent;
    }

    public Double getDiskUsagePercent() {
        return diskUsagePercent;
    }

    public void setDiskUsagePercent(Double diskUsagePercent) {
        this.diskUsagePercent = diskUsagePercent;
    }

    public Double getDiskPercent() {
        return diskUsagePercent;
    }

    public void setDiskPercent(Double diskPercent) {
        this.diskUsagePercent = diskPercent;
    }

    public Double getDisk() {
        return diskUsagePercent;
    }

    public Long getMemoryUsedBytes() {
        return memoryUsedBytes;
    }

    public void setMemoryUsedBytes(Long memoryUsedBytes) {
        this.memoryUsedBytes = memoryUsedBytes;
    }

    public Long getRamUsedBytes() {
        return memoryUsedBytes;
    }

    public void setRamUsedBytes(Long ramUsedBytes) {
        this.memoryUsedBytes = ramUsedBytes;
    }

    public Long getMemoryTotalBytes() {
        return memoryTotalBytes;
    }

    public void setMemoryTotalBytes(Long memoryTotalBytes) {
        this.memoryTotalBytes = memoryTotalBytes;
    }

    public Long getRamTotalBytes() {
        return memoryTotalBytes;
    }

    public void setRamTotalBytes(Long ramTotalBytes) {
        this.memoryTotalBytes = ramTotalBytes;
    }

    public Long getDiskUsedBytes() {
        return diskUsedBytes;
    }

    public void setDiskUsedBytes(Long diskUsedBytes) {
        this.diskUsedBytes = diskUsedBytes;
    }

    public Long getDiskTotalBytes() {
        return diskTotalBytes;
    }

    public void setDiskTotalBytes(Long diskTotalBytes) {
        this.diskTotalBytes = diskTotalBytes;
    }

    public Double getNetworkRxRateBytesPerSec() {
        return networkRxRateBytesPerSec;
    }

    public void setNetworkRxRateBytesPerSec(Double networkRxRateBytesPerSec) {
        this.networkRxRateBytesPerSec = networkRxRateBytesPerSec;
    }

    public Double getNetworkTxRateBytesPerSec() {
        return networkTxRateBytesPerSec;
    }

    public void setNetworkTxRateBytesPerSec(Double networkTxRateBytesPerSec) {
        this.networkTxRateBytesPerSec = networkTxRateBytesPerSec;
    }

    public Long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(Long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    public Double getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(Double temperatureC) {
        this.temperatureC = temperatureC;
    }

    public String getCpuModel() {
        return cpuModel;
    }

    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
