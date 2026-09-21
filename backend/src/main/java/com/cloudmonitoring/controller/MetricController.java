package com.cloudmonitoring.controller;

import com.cloudmonitoring.dto.response.ApiResponse;
import com.cloudmonitoring.dto.response.DeviceMetricsDto;
import com.cloudmonitoring.dto.response.MetricHistoryPointDto;
import com.cloudmonitoring.entity.Device;
import com.cloudmonitoring.security.CurrentUser;
import com.cloudmonitoring.security.UserPrincipal;
import com.cloudmonitoring.service.DeviceService;
import com.cloudmonitoring.service.PrometheusQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class MetricController {

    private final DeviceService deviceService;
    private final PrometheusQueryService prometheusQueryService;

    public MetricController(DeviceService deviceService, PrometheusQueryService prometheusQueryService) {
        this.deviceService = deviceService;
        this.prometheusQueryService = prometheusQueryService;
    }

    @GetMapping("/{id}/metrics/current")
    @PreAuthorize("hasRole('ADMIN') or @deviceSecurityGuard.isOwner(authentication, #id)")
    public ResponseEntity<ApiResponse<DeviceMetricsDto>> getCurrentMetrics(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        Device device = deviceService.getDeviceEntity(id, currentUser);
        DeviceMetricsDto metrics = prometheusQueryService.getCurrentDeviceMetrics(device);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @GetMapping("/{id}/metrics/history")
    @PreAuthorize("hasRole('ADMIN') or @deviceSecurityGuard.isOwner(authentication, #id)")
    public ResponseEntity<ApiResponse<List<MetricHistoryPointDto>>> getMetricHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1h") String range,
            @RequestParam(defaultValue = "15s") String step,
            @CurrentUser UserPrincipal currentUser) {
        Device device = deviceService.getDeviceEntity(id, currentUser);
        List<MetricHistoryPointDto> history = prometheusQueryService.getMetricHistory(device, range, step);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}