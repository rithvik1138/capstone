package com.cloudmonitoring.dto.response;

public class AdminDashboardDto {

    private long totalUsers;
    private long totalDevices;
    private long onlineDevices;
    private long offlineDevices;
    private long pendingDevices;
    private long activeAlerts;
    private long criticalAlerts;
    private long openIncidents;

    public AdminDashboardDto() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalDevices() {
        return totalDevices;
    }

    public void setTotalDevices(long totalDevices) {
        this.totalDevices = totalDevices;
    }

    public long getOnlineDevices() {
        return onlineDevices;
    }

    public void setOnlineDevices(long onlineDevices) {
        this.onlineDevices = onlineDevices;
    }

    public long getOfflineDevices() {
        return offlineDevices;
    }

    public void setOfflineDevices(long offlineDevices) {
        this.offlineDevices = offlineDevices;
    }

    public long getPendingDevices() {
        return pendingDevices;
    }

    public void setPendingDevices(long pendingDevices) {
        this.pendingDevices = pendingDevices;
    }

    public long getActiveAlerts() {
        return activeAlerts;
    }

    public void setActiveAlerts(long activeAlerts) {
        this.activeAlerts = activeAlerts;
    }

    public long getCriticalAlerts() {
        return criticalAlerts;
    }

    public void setCriticalAlerts(long criticalAlerts) {
        this.criticalAlerts = criticalAlerts;
    }

    public long getOpenIncidents() {
        return openIncidents;
    }

    public void setOpenIncidents(long openIncidents) {
        this.openIncidents = openIncidents;
    }
}