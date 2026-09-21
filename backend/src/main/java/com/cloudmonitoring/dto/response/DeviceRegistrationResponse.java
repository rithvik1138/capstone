package com.cloudmonitoring.dto.response;

public class DeviceRegistrationResponse {

    private DeviceDto device;
    private String agentToken;
    private String agentInstallToken;
    private String installCommand;
    private String linuxInstallCommand;
    private String windowsInstallCommand;

    public DeviceRegistrationResponse() {
    }

    public DeviceRegistrationResponse(DeviceDto device, String agentToken, String linuxInstallCommand, String windowsInstallCommand) {
        this.device = device;
        this.agentToken = agentToken;
        this.agentInstallToken = agentToken;
        this.linuxInstallCommand = linuxInstallCommand;
        this.windowsInstallCommand = windowsInstallCommand;
        if (device != null && device.getDeviceType() != null && device.getDeviceType().name().contains("WINDOWS")) {
            this.installCommand = windowsInstallCommand;
        } else {
            this.installCommand = linuxInstallCommand;
        }
    }

    public DeviceDto getDevice() {
        return device;
    }

    public void setDevice(DeviceDto device) {
        this.device = device;
    }

    public String getAgentToken() {
        return agentToken;
    }

    public void setAgentToken(String agentToken) {
        this.agentToken = agentToken;
        this.agentInstallToken = agentToken;
    }

    public String getAgentInstallToken() {
        return agentInstallToken != null ? agentInstallToken : agentToken;
    }

    public void setAgentInstallToken(String agentInstallToken) {
        this.agentInstallToken = agentInstallToken;
        this.agentToken = agentInstallToken;
    }

    public String getInstallCommand() {
        return installCommand != null ? installCommand : linuxInstallCommand;
    }

    public void setInstallCommand(String installCommand) {
        this.installCommand = installCommand;
    }

    public String getLinuxInstallCommand() {
        return linuxInstallCommand;
    }

    public void setLinuxInstallCommand(String linuxInstallCommand) {
        this.linuxInstallCommand = linuxInstallCommand;
    }

    public String getWindowsInstallCommand() {
        return windowsInstallCommand;
    }

    public void setWindowsInstallCommand(String windowsInstallCommand) {
        this.windowsInstallCommand = windowsInstallCommand;
    }
}
