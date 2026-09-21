package com.cloudmonitoring.controller;

import com.cloudmonitoring.dto.response.*;
import com.cloudmonitoring.entity.AuditLog;
import com.cloudmonitoring.repository.AuditLogRepository;
import com.cloudmonitoring.security.CurrentUser;
import com.cloudmonitoring.security.UserPrincipal;
import com.cloudmonitoring.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AuditLogRepository auditLogRepository;

    public AdminController(AdminService adminService, AuditLogRepository auditLogRepository) {
        this.adminService = adminService;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getDashboardStats() {
        AdminDashboardDto stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<Page<AdminDeviceTableDto>>> getAdminDevices(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AdminDeviceTableDto> page = adminService.getAdminDevices(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserDto> page = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserDto>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> statusUpdate,
            @CurrentUser UserPrincipal currentUser,
            HttpServletRequest request) {
        boolean active = statusUpdate.getOrDefault("active", true);
        String ipAddress = request.getRemoteAddr();
        UserDto updated = adminService.updateUserStatus(id, active, currentUser.getUsername(), ipAddress);
        return ResponseEntity.ok(ApiResponse.success("User status updated", updated));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(
            @PageableDefault(size = 25, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findAllByOrderByTimestampDesc(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
}