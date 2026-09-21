package com.cloudmonitoring.service;

import com.cloudmonitoring.dto.request.IncidentCreateRequest;
import com.cloudmonitoring.dto.request.IncidentStatusUpdateRequest;
import com.cloudmonitoring.dto.response.IncidentAiAnalysisDto;
import com.cloudmonitoring.dto.response.IncidentDto;
import com.cloudmonitoring.entity.*;
import com.cloudmonitoring.exception.BadRequestException;
import com.cloudmonitoring.exception.ResourceNotFoundException;
import com.cloudmonitoring.exception.UnauthorizedAccessException;
import com.cloudmonitoring.repository.*;
import com.cloudmonitoring.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentAiAnalysisRepository aiAnalysisRepository;
    private final DeviceRepository deviceRepository;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public IncidentService(IncidentRepository incidentRepository,
                           IncidentAiAnalysisRepository aiAnalysisRepository,
                           DeviceRepository deviceRepository,
                           AlertRepository alertRepository,
                           UserRepository userRepository,
                           AuditService auditService) {
        this.incidentRepository = incidentRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.deviceRepository = deviceRepository;
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public IncidentDto createIncident(IncidentCreateRequest request, UserPrincipal currentUser, String ipAddress) {
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + request.getDeviceId()));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()));

        if (!isAdmin && !device.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to create an incident for this device");
        }

        Alert alert = null;
        if (request.getAlertId() != null) {
            alert = alertRepository.findById(request.getAlertId())
                    .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + request.getAlertId()));
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Incident incident = new Incident();
        incident.setDevice(device);
        incident.setAlert(alert);
        incident.setTitle(request.getTitle());
        incident.setDescription(request.getDescription());
        incident.setSeverity(request.getSeverity());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setCreatedByUser(user);

        Incident saved = incidentRepository.save(incident);
        auditService.logAction(user, "CREATE_INCIDENT", "Incident", saved.getId().toString(), ipAddress, "Created incident: " + saved.getTitle());

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public IncidentDto getIncidentById(Long id, UserPrincipal currentUser) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()));

        if (!isAdmin && !incident.getDevice().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You do not have access to this incident");
        }

        return mapToDto(incident);
    }

    @Transactional(readOnly = true)
    public Page<IncidentDto> getUserIncidents(UserPrincipal currentUser, IncidentStatus status, Pageable pageable) {
        Page<Incident> page;
        if (status != null) {
            page = incidentRepository.findByDeviceUserIdAndStatus(currentUser.getId(), status, pageable);
        } else {
            page = incidentRepository.findByDeviceUserId(currentUser.getId(), pageable);
        }
        return page.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<IncidentDto> getAllIncidents(IncidentStatus status, Pageable pageable) {
        Page<Incident> page;
        if (status != null) {
            page = incidentRepository.findByStatus(status, pageable);
        } else {
            page = incidentRepository.findAll(pageable);
        }
        return page.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<IncidentDto> getDeviceIncidents(Long deviceId, UserPrincipal currentUser, Pageable pageable) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()));

        if (!isAdmin && !device.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You do not have access to this device's incidents");
        }

        return incidentRepository.findByDeviceId(deviceId, pageable).map(this::mapToDto);
    }

    @Transactional
    public IncidentDto updateIncidentStatus(Long id, IncidentStatusUpdateRequest request, UserPrincipal currentUser, String ipAddress) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()));

        if (!isAdmin && !incident.getDevice().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to update this incident");
        }

        incident.setStatus(request.getStatus());
        if (request.getStatus() == IncidentStatus.RESOLVED || request.getStatus() == IncidentStatus.CLOSED) {
            incident.setResolvedAt(LocalDateTime.now());
        }

        Incident updated = incidentRepository.save(incident);
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        auditService.logAction(user, "UPDATE_INCIDENT_STATUS", "Incident", id.toString(), ipAddress, "Status changed to " + request.getStatus());

        return mapToDto(updated);
    }

    public IncidentDto mapToDto(Incident incident) {
        IncidentDto dto = new IncidentDto();
        dto.setId(incident.getId());
        dto.setDeviceId(incident.getDevice().getId());
        dto.setDeviceName(incident.getDevice().getDeviceName());
        dto.setDeviceUuid(incident.getDevice().getDeviceUuid());
        dto.setAlertId(incident.getAlert() != null ? incident.getAlert().getId() : null);
        dto.setTitle(incident.getTitle());
        dto.setDescription(incident.getDescription());
        dto.setSeverity(incident.getSeverity());
        dto.setStatus(incident.getStatus());
        dto.setCreatedByUserId(incident.getCreatedByUser() != null ? incident.getCreatedByUser().getId() : null);
        dto.setCreatedByUsername(incident.getCreatedByUser() != null ? incident.getCreatedByUser().getUsername() : null);
        dto.setCreatedAt(incident.getCreatedAt());
        dto.setUpdatedAt(incident.getUpdatedAt());
        dto.setResolvedAt(incident.getResolvedAt());

        aiAnalysisRepository.findTopByIncidentIdOrderByCreatedAtDesc(incident.getId())
                .ifPresent(analysis -> {
                    IncidentAiAnalysisDto aiDto = new IncidentAiAnalysisDto();
                    aiDto.setId(analysis.getId());
                    aiDto.setIncidentId(incident.getId());
                    aiDto.setAiModel(analysis.getAiModel());
                    aiDto.setTelemetryContextJson(analysis.getTelemetryContextJson());
                    aiDto.setRootCause(analysis.getRootCause());
                    aiDto.setImpactAnalysis(analysis.getImpactAnalysis());
                    aiDto.setRecommendedActionsJson(analysis.getRecommendedActionsJson());
                    aiDto.setCreatedAt(analysis.getCreatedAt());
                    dto.setLatestAiAnalysis(aiDto);
                });

        return dto;
    }
}