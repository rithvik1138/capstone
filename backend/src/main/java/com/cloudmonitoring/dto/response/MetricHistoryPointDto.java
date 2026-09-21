package com.cloudmonitoring.dto.response;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MetricHistoryPointDto {
    private Instant timestamp;
    private Double cpuPercent;
    private Double memoryPercent;
    private Double diskPercent;
    private Double networkRxRate;
    private Double networkTxRate;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    public MetricHistoryPointDto() {
    }

    public MetricHistoryPointDto(Instant timestamp, Double cpuPercent, Double memoryPercent, Double diskPercent, Double networkRxRate, Double networkTxRate) {
        this.timestamp = timestamp;
        this.cpuPercent = cpuPercent;
        this.memoryPercent = memoryPercent;
        this.diskPercent = diskPercent;
        this.networkRxRate = networkRxRate;
        this.networkTxRate = networkTxRate;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getTime() {
        if (timestamp == null) return "";
        try {
            return TIME_FORMATTER.format(timestamp);
        } catch (Exception e) {
            return timestamp.toString();
        }
    }

    public Double getCpuPercent() {
        return cpuPercent;
    }

    public void setCpuPercent(Double cpuPercent) {
        this.cpuPercent = cpuPercent;
    }

    public Double getCpu() {
        return cpuPercent;
    }

    public Double getMemoryPercent() {
        return memoryPercent;
    }

    public void setMemoryPercent(Double memoryPercent) {
        this.memoryPercent = memoryPercent;
    }

    public Double getRam() {
        return memoryPercent;
    }

    public Double getDiskPercent() {
        return diskPercent;
    }

    public void setDiskPercent(Double diskPercent) {
        this.diskPercent = diskPercent;
    }

    public Double getDisk() {
        return diskPercent;
    }

    public Double getNetworkRxRate() {
        return networkRxRate;
    }

    public void setNetworkRxRate(Double networkRxRate) {
        this.networkRxRate = networkRxRate;
    }

    public Double getNetRx() {
        return networkRxRate;
    }

    public Double getNetworkTxRate() {
        return networkTxRate;
    }

    public void setNetworkTxRate(Double networkTxRate) {
        this.networkTxRate = networkTxRate;
    }

    public Double getNetTx() {
        return networkTxRate;
    }
}
