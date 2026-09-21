package com.cloudmonitoring.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DeviceUpdateRequest {

    @NotBlank(message = "Device name is required")
    @Size(min = 2, max = 100, message = "Device name must be between 2 and 100 characters")
    private String deviceName;

    public DeviceUpdateRequest() {
    }

    public DeviceUpdateRequest(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}