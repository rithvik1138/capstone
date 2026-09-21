package com.cloudmonitoring.controller;

import com.cloudmonitoring.dto.request.LogEntryDto;
import com.cloudmonitoring.dto.response.ApiResponse;
import com.cloudmonitoring.dto.response.DeviceLogDto;
import com.cloudmonitoring.entity.LogLevel;
import com.cloudmonitoring.service.LogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * Agent ingest endpoint for batched sanitized device logs.
     * Authenticated via X-Device-Token.
     */
    @PostMapping("/agent/logs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ingestLogs(
            @RequestHeader(value = "X-Device-Token", required = false) String rawToken,
            @RequestBody @Valid List<LogEntryDto> entries) {
        int savedCount = logService.ingestLogs(rawToken, entries);
        return ResponseEntity.ok(ApiResponse.success("Logs ingested successfully", Map.of("ingestedCount", savedCount)));
    }

    /**
     * User or Admin view of device-specific logs.
     */
    @GetMapping("/devices/{deviceId}/logs")
    @PreAuthorize("hasRole('ADMIN') or @deviceSecurityGuard.isOwner(authentication, #deviceId)")
    public ResponseEntity<ApiResponse<Page<DeviceLogDto>>> getDeviceLogs(
            @PathVariable Long deviceId,
            @RequestParam(required = false) LogLevel level,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable) {
        Page<DeviceLogDto> logs = logService.getDeviceLogs(deviceId, level, search, from, to, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    /**
     * Recent 50 logs for instant dashboard display.
     */
    @GetMapping("/devices/{deviceId}/logs/recent")
    @PreAuthorize("hasRole('ADMIN') or @deviceSecurityGuard.isOwner(authentication, #deviceId)")
    public ResponseEntity<ApiResponse<List<DeviceLogDto>>> getRecentLogs(@PathVariable Long deviceId) {
        List<DeviceLogDto> logs = logService.getRecentLogsForDevice(deviceId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    /**
     * Admin view of global fleet logs.
     */
    @GetMapping("/admin/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<DeviceLogDto>>> getAllLogs(
            @RequestParam(required = false) LogLevel level,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<DeviceLogDto> logs = logService.getAllLogs(level, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}