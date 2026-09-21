package com.cloudmonitoring.controller;

import com.cloudmonitoring.dto.request.IncidentCreateRequest;
import com.cloudmonitoring.dto.request.IncidentStatusUpdateRequest;
import com.cloudmonitoring.dto.response.ApiResponse;
import com.cloudmonitoring.dto.response.IncidentDto;
import com.cloudmonitoring.entity.IncidentStatus;
import com.cloudmonitoring.entity.Role;
import com.cloudmonitoring.security.CurrentUser;
import com.cloudmonitoring.security.UserPrincipal;
import com.cloudmonitoring.service.IncidentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IncidentDto>> createIncident(
            @Valid @RequestBody IncidentCreateRequest request,
            @CurrentUser UserPrincipal currentUser,
            HttpServletRequest servletRequest) {
        String ipAddress = servletRequest.getRemoteAddr();
        IncidentDto created = incidentService.createIncident(request, currentUser, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Incident created successfully", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentDto>> getIncidentById(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        IncidentDto dto = incidentService.getIncidentById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<IncidentDto>>> getIncidents(
            @RequestParam(required = false) IncidentStatus status,
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()));

        Page<IncidentDto> page;
        if (isAdmin) {
            page = incidentService.getAllIncidents(status, pageable);
        } else {
            page = incidentService.getUserIncidents(currentUser, status, pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<ApiResponse<Page<IncidentDto>>> getDeviceIncidents(
            @PathVariable Long deviceId,
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<IncidentDto> page = incidentService.getDeviceIncidents(deviceId, currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<IncidentDto>> updateIncidentStatus(
            @PathVariable Long id,
            @Valid @RequestBody IncidentStatusUpdateRequest request,
            @CurrentUser UserPrincipal currentUser,
            HttpServletRequest servletRequest) {
        String ipAddress = servletRequest.getRemoteAddr();
        IncidentDto updated = incidentService.updateIncidentStatus(id, request, currentUser, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Incident status updated", updated));
    }
}