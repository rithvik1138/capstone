package com.cloudmonitoring.dto.request;

import com.cloudmonitoring.entity.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DeviceRegistrationRequest {

    @NotBlank(message = "Device name is required")
    @Size(min = 2, max = 100, message = "Device name must be between 2 and 100 characters")
    private String deviceName;

    @NotNull(message = "Device type is required")
    private DeviceType deviceType;

    public DeviceRegistrationRequest() {
    }

    public DeviceRegistrationRequest(String deviceName, DeviceType deviceType) {
        this.deviceName = deviceName;
        this.deviceType = deviceType;
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
}