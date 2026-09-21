package com.cloudmonitoring.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AgentHeartbeatRequest {

    @NotBlank(message = "Device UUID is required")
    private String deviceUuid;

    @NotNull(message = "Timestamp is required")
    private Long timestamp;

    private Double cpuPercent;
    private Double ramPercent;
    private Long ramUsedBytes;
    private Long ramTotalBytes;
    private Double diskPercent;
    private Long diskUsedBytes;
    private Long diskTotalBytes;
    private Double networkRxRateBps;
    private Double networkTxRateBps;
    private Long uptimeSeconds;
    private Double temperatureC;

    public AgentHeartbeatRequest() {
    }

    public AgentHeartbeatRequest(String deviceUuid, Long timestamp) {
        this.deviceUuid = deviceUuid;
        this.timestamp = timestamp;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getCpuPercent() {
        return cpuPercent;
    }

    public void setCpuPercent(Double cpuPercent) {
        this.cpuPercent = cpuPercent;
    }

    public Double getRamPercent() {
        return ramPercent;
    }

    public void setRamPercent(Double ramPercent) {
        this.ramPercent = ramPercent;
    }

    public Long getRamUsedBytes() {
        return ramUsedBytes;
    }

    public void setRamUsedBytes(Long ramUsedBytes) {
        this.ramUsedBytes = ramUsedBytes;
    }

    public Long getRamTotalBytes() {
        return ramTotalBytes;
    }

    public void setRamTotalBytes(Long ramTotalBytes) {
        this.ramTotalBytes = ramTotalBytes;
    }

    public Double getDiskPercent() {
        return diskPercent;
    }

    public void setDiskPercent(Double diskPercent) {
        this.diskPercent = diskPercent;
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

    public Double getNetworkRxRateBps() {
        return networkRxRateBps;
    }

    public void setNetworkRxRateBps(Double networkRxRateBps) {
        this.networkRxRateBps = networkRxRateBps;
    }

    public Double getNetworkTxRateBps() {
        return networkTxRateBps;
    }

    public void setNetworkTxRateBps(Double networkTxRateBps) {
        this.networkTxRateBps = networkTxRateBps;
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
}
