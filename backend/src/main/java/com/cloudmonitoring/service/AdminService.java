package com.cloudmonitoring.service;

import com.cloudmonitoring.dto.response.AdminDashboardDto;
import com.cloudmonitoring.dto.response.AdminDeviceTableDto;
import com.cloudmonitoring.dto.response.DeviceMetricsDto;
import com.cloudmonitoring.dto.response.UserDto;
import com.cloudmonitoring.entity.*;
import com.cloudmonitoring.exception.ResourceNotFoundException;
import com.cloudmonitoring.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final AlertRepository alertRepository;
    private final IncidentRepository incidentRepository;
    private final PrometheusQueryService prometheusQueryService;
    private final AuditService auditService;

    public AdminService(UserRepository userRepository,
                        DeviceRepository deviceRepository,
                        AlertRepository alertRepository,
                        IncidentRepository incidentRepository,
                        PrometheusQueryService prometheusQueryService,
                        AuditService auditService) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.alertRepository = alertRepository;
        this.incidentRepository = incidentRepository;
        this.prometheusQueryService = prometheusQueryService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboardStats() {
        AdminDashboardDto stats = new AdminDashboardDto();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalDevices(deviceRepository.count());
        stats.setOnlineDevices(deviceRepository.countByStatus(DeviceStatus.ONLINE));
        stats.setOfflineDevices(deviceRepository.countByStatus(DeviceStatus.OFFLINE));
        stats.setPendingDevices(deviceRepository.countByStatus(DeviceStatus.PENDING));
        stats.setActiveAlerts(alertRepository.countByStatus(AlertStatus.ACTIVE));
        stats.setCriticalAlerts(alertRepository.countByStatusAndSeverity(AlertStatus.ACTIVE, Severity.CRITICAL));
        stats.setOpenIncidents(incidentRepository.countByStatus(IncidentStatus.OPEN));
        return stats;
    }

    @Transactional(readOnly = true)
    public Page<AdminDeviceTableDto> getAdminDevices(Pageable pageable) {
        Page<Device> devices = deviceRepository.findAll(pageable);
        return devices.map(device -> {
            AdminDeviceTableDto dto = new AdminDeviceTableDto();
            dto.setId(device.getId());
            dto.setDeviceUuid(device.getDeviceUuid());
            dto.setDeviceName(device.getDeviceName());
            dto.setDeviceType(device.getDeviceType());
            dto.setOwnerUserId(device.getUser().getId());
            dto.setOwnerUsername(device.getUser().getUsername());
            dto.setOwnerEmail(device.getUser().getEmail());
            dto.setHostname(device.getHostname());
            dto.setIpAddress(device.getIpAddress());
            dto.setOsName(device.getOsName());
            dto.setOsVersion(device.getOsVersion());
            dto.setCpuModel(device.getCpuModel());
            dto.setTotalCores(device.getTotalCores());
            dto.setTotalMemoryBytes(device.getTotalMemoryBytes());
            dto.setTotalDiskBytes(device.getTotalDiskBytes());
            dto.setStatus(device.getStatus());
            dto.setLastSeenAt(device.getLastSeenAt());
            dto.setCreatedAt(device.getCreatedAt());

            if (device.getStatus() == DeviceStatus.ONLINE) {
                DeviceMetricsDto metrics = prometheusQueryService.getCurrentMetrics(device.getDeviceUuid());
                dto.setCurrentCpuPercent(metrics.getCpuPercent());
                dto.setCurrentMemoryPercent(metrics.getRamPercent());
                dto.setCurrentDiskPercent(metrics.getDiskPercent());
            }

            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(user -> {
            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole());
            dto.setActive(user.isActive());
            dto.setCreatedAt(user.getCreatedAt());
            dto.setUpdatedAt(user.getUpdatedAt());
            return dto;
        });
    }

    @Transactional
    public UserDto updateUserStatus(Long userId, boolean active, String adminUsername, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setActive(active);
        User saved = userRepository.save(user);

        User admin = userRepository.findByUsername(adminUsername).orElse(null);
        auditService.logAction(admin, "UPDATE_USER_STATUS", "User", userId.toString(), ipAddress,
                "User " + user.getUsername() + " status set to active=" + active);

        UserDto dto = new UserDto();
        dto.setId(saved.getId());
        dto.setUsername(saved.getUsername());
        dto.setEmail(saved.getEmail());
        dto.setRole(saved.getRole());
        dto.setActive(saved.isActive());
        dto.setCreatedAt(saved.getCreatedAt());
        dto.setUpdatedAt(saved.getUpdatedAt());
        return dto;
    }
}