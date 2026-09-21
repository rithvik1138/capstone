package com.cloudmonitoring.service;

import com.cloudmonitoring.dto.request.AgentHandshakeRequest;
import com.cloudmonitoring.dto.request.AgentHeartbeatRequest;
import com.cloudmonitoring.dto.request.DeviceRegistrationRequest;
import com.cloudmonitoring.dto.request.DeviceUpdateRequest;
import com.cloudmonitoring.dto.response.DeviceDto;
import com.cloudmonitoring.dto.response.DeviceMetricsDto;
import com.cloudmonitoring.dto.response.DeviceRegistrationResponse;
import com.cloudmonitoring.entity.*;
import com.cloudmonitoring.exception.BadRequestException;
import com.cloudmonitoring.exception.ResourceNotFoundException;
import com.cloudmonitoring.exception.UnauthorizedAccessException;
import com.cloudmonitoring.repository.DeviceRepository;
import com.cloudmonitoring.repository.DeviceTokenRepository;
import com.cloudmonitoring.repository.UserRepository;
import com.cloudmonitoring.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final PrometheusQueryService prometheusQueryService;
    private final ObjectMapper objectMapper;

    @Value("${app.public-base-url:${app.server.base-url:http://localhost:8080}}")
    private String publicBaseUrl;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository,
                         DeviceTokenRepository deviceTokenRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         AuditService auditService,
                         PrometheusQueryService prometheusQueryService,
                         ObjectMapper objectMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.prometheusQueryService = prometheusQueryService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DeviceRegistrationResponse registerDevice(Long userId, DeviceRegistrationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String deviceUuid = UUID.randomUUID().toString();
        Device device = new Device();
        device.setUser(user);
        device.setDeviceUuid(deviceUuid);
        device.setDeviceName(request.getDeviceName());
        device.setDeviceType(request.getDeviceType());
        device.setStatus(DeviceStatus.PENDING);

        Device savedDevice = deviceRepository.save(device);

        String rawToken = "cmd_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String tokenHash = passwordEncoder.encode(rawToken);

        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setDevice(savedDevice);
        deviceToken.setTokenHash(tokenHash);
        deviceToken.setIsRevoked(false);
        deviceTokenRepository.save(deviceToken);

        String linuxInstallCmd = buildLinuxInstallCommand(rawToken, deviceUuid);
        String windowsInstallCmd = buildWindowsInstallCommand(rawToken, deviceUuid);

        auditService.logAction(user, "REGISTER_DEVICE", "DEVICE", savedDevice.getId().toString(),
                "Device registered: " + savedDevice.getDeviceName() + " (" + savedDevice.getDeviceUuid() + ")");

        return new DeviceRegistrationResponse(new DeviceDto(savedDevice), rawToken, linuxInstallCmd, windowsInstallCmd);
    }

    @Transactional(readOnly = true)
    public Page<DeviceDto> getUserDevices(Long userId, Pageable pageable) {
        return deviceRepository.findByUserId(userId, pageable)
                .map(DeviceDto::new);
    }

    @Transactional(readOnly = true)
    public Page<DeviceDto> getAllDevices(Pageable pageable) {
        return deviceRepository.findAll(pageable)
                .map(DeviceDto::new);
    }

    @Transactional(readOnly = true)
    public DeviceDto getDeviceById(Long deviceId, UserPrincipal currentUser) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        validateOwnershipOrAdmin(device, currentUser);
        return new DeviceDto(device);
    }

    @Transactional(readOnly = true)
    public DeviceDto getDeviceByUuid(String deviceUuid, UserPrincipal currentUser) {
        Device device = deviceRepository.findByDeviceUuid(deviceUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with uuid: " + deviceUuid));

        validateOwnershipOrAdmin(device, currentUser);
        return new DeviceDto(device);
    }

    @Transactional
    public DeviceDto updateDevice(Long deviceId, DeviceUpdateRequest request, UserPrincipal currentUser) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        validateOwnershipOrAdmin(device, currentUser);

        device.setDeviceName(request.getDeviceName());
        Device updatedDevice = deviceRepository.save(device);

        User user = userRepository.findById(currentUser.getId()).orElse(null);
        auditService.logAction(user, "UPDATE_DEVICE", "DEVICE", deviceId.toString(),
                "Updated device name to: " + request.getDeviceName());

        return new DeviceDto(updatedDevice);
    }

    @Transactional
    public void deleteDevice(Long deviceId, UserPrincipal currentUser) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        validateOwnershipOrAdmin(device, currentUser);

        String deviceUuid = device.getDeviceUuid();
        String deviceName = device.getDeviceName();

        // Evict cached telemetry
        prometheusQueryService.removeDeviceFromCache(deviceUuid);

        // Delete device token and device entity (foreign key cascades handle associated logs, alerts, incidents)
        deviceTokenRepository.deleteByDeviceId(deviceId);
        deviceRepository.delete(device);

        // Record structured DELETE_DEVICE audit log
        Map<String, Object> auditPayload = Map.of(
                "deviceId", deviceId,
                "deviceUuid", deviceUuid,
                "deviceName", deviceName,
                "osName", device.getOsName() != null ? device.getOsName() : "Unknown",
                "action", "DELETE_DEVICE"
        );

        String auditDetailsJson;
        try {
            auditDetailsJson = objectMapper.writeValueAsString(auditPayload);
        } catch (Exception e) {
            auditDetailsJson = "{\"deviceId\":" + deviceId + ",\"deviceName\":\"" + deviceName + "\"}";
        }

        User user = userRepository.findById(currentUser.getId()).orElse(null);
        auditService.logAction(user, "DELETE_DEVICE", "DEVICE", deviceId.toString(), auditDetailsJson);
    }

    @Transactional
    public DeviceRegistrationResponse regenerateToken(Long deviceId, UserPrincipal currentUser) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        validateOwnershipOrAdmin(device, currentUser);

        deviceTokenRepository.deleteByDeviceId(deviceId);

        String rawToken = "cmd_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String tokenHash = passwordEncoder.encode(rawToken);

        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setDevice(device);
        deviceToken.setTokenHash(tokenHash);
        deviceToken.setIsRevoked(false);
        deviceTokenRepository.save(deviceToken);

        String linuxInstallCmd = buildLinuxInstallCommand(rawToken, device.getDeviceUuid());
        String windowsInstallCmd = buildWindowsInstallCommand(rawToken, device.getDeviceUuid());

        User user = userRepository.findById(currentUser.getId()).orElse(null);
        auditService.logAction(user, "REGENERATE_DEVICE_TOKEN", "DEVICE", deviceId.toString(),
                "Regenerated agent token for device: " + device.getDeviceName());

        return new DeviceRegistrationResponse(new DeviceDto(device), rawToken, linuxInstallCmd, windowsInstallCmd);
    }

    @Transactional
    public DeviceDto processAgentHandshake(String rawToken, AgentHandshakeRequest request) {
        Device device = deviceRepository.findByDeviceUuid(request.getDeviceUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with UUID: " + request.getDeviceUuid()));

        DeviceToken deviceToken = deviceTokenRepository.findByDeviceId(device.getId())
                .orElseThrow(() -> new UnauthorizedAccessException("No agent token associated with this device"));

        if (deviceToken.getIsRevoked() || !passwordEncoder.matches(rawToken, deviceToken.getTokenHash())) {
            throw new UnauthorizedAccessException("Invalid or revoked agent authentication token");
        }

        device.setHostname(request.getHostname());
        device.setIpAddress(request.getIpAddress());
        device.setMacAddress(request.getMacAddress());
        device.setOsName(request.getOsName());
        device.setOsVersion(request.getOsVersion());
        device.setCpuModel(request.getCpuModel());
        device.setTotalCores(request.getTotalCores());
        device.setTotalMemoryBytes(request.getTotalMemoryBytes());
        device.setTotalDiskBytes(request.getTotalDiskBytes());
        device.setAgentVersion(request.getAgentVersion());
        device.setStatus(DeviceStatus.ONLINE);
        device.setLastSeenAt(LocalDateTime.now());

        Device updated = deviceRepository.save(device);
        return new DeviceDto(updated);
    }

    @Transactional
    public void processAgentHeartbeat(String rawToken, AgentHeartbeatRequest request) {
        Device device = deviceRepository.findByDeviceUuid(request.getDeviceUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with UUID: " + request.getDeviceUuid()));

        DeviceToken deviceToken = deviceTokenRepository.findByDeviceId(device.getId())
                .orElseThrow(() -> new UnauthorizedAccessException("No agent token associated with this device"));

        if (deviceToken.getIsRevoked() || !passwordEncoder.matches(rawToken, deviceToken.getTokenHash())) {
            throw new UnauthorizedAccessException("Invalid or revoked agent authentication token");
        }

        device.setStatus(DeviceStatus.ONLINE);
        device.setLastSeenAt(LocalDateTime.now());
        deviceRepository.save(device);

        // If heartbeat included live telemetry, ingest it into the live metrics cache immediately
        if (request.getCpuPercent() != null || request.getRamPercent() != null || request.getDiskPercent() != null || request.getUptimeSeconds() != null) {
            DeviceMetricsDto metrics = new DeviceMetricsDto();
            metrics.setDeviceUuid(device.getDeviceUuid());
            metrics.setStatus("ONLINE");
            metrics.setCpuUsagePercent(request.getCpuPercent());
            metrics.setMemoryUsagePercent(request.getRamPercent());
            metrics.setMemoryUsedBytes(request.getRamUsedBytes());
            metrics.setMemoryTotalBytes(request.getRamTotalBytes());
            metrics.setDiskUsagePercent(request.getDiskPercent());
            metrics.setDiskUsedBytes(request.getDiskUsedBytes());
            metrics.setDiskTotalBytes(request.getDiskTotalBytes());
            metrics.setNetworkRxRateBytesPerSec(request.getNetworkRxRateBps());
            metrics.setNetworkTxRateBytesPerSec(request.getNetworkTxRateBps());
            metrics.setUptimeSeconds(request.getUptimeSeconds());
            metrics.setTemperatureC(request.getTemperatureC());
            metrics.setCpuModel(device.getCpuModel());
            prometheusQueryService.updateLiveMetrics(device.getDeviceUuid(), metrics);
        }
    }

    public Device getDeviceEntity(Long deviceId, UserPrincipal currentUser) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));
        validateOwnershipOrAdmin(device, currentUser);
        return device;
    }

    private void validateOwnershipOrAdmin(Device device, UserPrincipal currentUser) {
        if (Role.ROLE_ADMIN.name().equals(currentUser.getRole())) {
            return;
        }

        if (device.getUser() == null || !device.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to access this device");
        }
    }

    private String getNormalizedPublicBaseUrl() {
        if (publicBaseUrl == null || publicBaseUrl.trim().isEmpty()) {
            return "http://localhost:8080";
        }
        String trimmed = publicBaseUrl.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String buildLinuxInstallCommand(String rawToken, String deviceUuid) {
        String baseUrl = getNormalizedPublicBaseUrl();
        return String.format("curl -sSL %s/install.sh | sudo bash -s -- --token=%s --uuid=%s --server=%s",
                baseUrl, rawToken, deviceUuid, baseUrl);
    }

    private String buildWindowsInstallCommand(String rawToken, String deviceUuid) {
        String baseUrl = getNormalizedPublicBaseUrl();
        return String.format("powershell -ExecutionPolicy Bypass -Command \"Invoke-WebRequest -Uri '%s/install.ps1' -OutFile 'install.ps1'; .\\install.ps1 -Token '%s' -Uuid '%s' -ServerUrl '%s'\"",
                baseUrl, rawToken, deviceUuid, baseUrl);
    }
}
