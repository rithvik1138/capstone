package com.cloudmonitoring.controller;

import com.cloudmonitoring.dto.response.AlertDto;
import com.cloudmonitoring.dto.response.ApiResponse;
import com.cloudmonitoring.entity.AlertStatus;
import com.cloudmonitoring.entity.Severity;
import com.cloudmonitoring.security.CurrentUser;
import com.cloudmonitoring.security.UserPrincipal;
import com.cloudmonitoring.service.AlertService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Get paginated alerts for the authenticated user (or all alerts for admin).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AlertDto>>> getAlerts(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "triggeredAt"));
        Page<AlertDto> alerts = alertService.getAlerts(currentUser, status, severity, pageRequest);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    /**
     * Get alerts for a specific device.
     */
    @GetMapping("/device/{deviceId}")
    @PreAuthorize("hasRole('ADMIN') or @deviceSecurityGuard.isOwner(authentication, #deviceId)")
    public ResponseEntity<ApiResponse<Page<AlertDto>>> getDeviceAlerts(
            @PathVariable Long deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "triggeredAt"));
        Page<AlertDto> alerts = alertService.getDeviceAlerts(deviceId, pageRequest);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    /**
     * Get summary counts of alerts (active, critical, acknowledged, resolved).
     */
    @GetMapping("/counts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAlertCounts(
            @CurrentUser UserPrincipal currentUser) {
        Map<String, Object> counts = alertService.getAlertCounts(currentUser);
        return ResponseEntity.ok(ApiResponse.success(counts));
    }

    /**
     * Acknowledge an alert.
     */
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<AlertDto>> acknowledgeAlert(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        AlertDto result = alertService.acknowledgeAlert(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Alert acknowledged", result));
    }

    /**
     * Manually resolve an alert.
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<AlertDto>> resolveAlert(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        AlertDto result = alertService.resolveAlert(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Alert resolved successfully", result));
    }
}