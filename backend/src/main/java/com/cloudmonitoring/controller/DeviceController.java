package com.cloudmonitoring.controller;

import com.cloudmonitoring.dto.request.DeviceRegistrationRequest;
import com.cloudmonitoring.dto.request.DeviceUpdateRequest;
import com.cloudmonitoring.dto.response.ApiResponse;
import com.cloudmonitoring.dto.response.DeviceDto;
import com.cloudmonitoring.dto.response.DeviceRegistrationResponse;
import com.cloudmonitoring.security.CurrentUser;
import com.cloudmonitoring.security.UserPrincipal;
import com.cloudmonitoring.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeviceRegistrationResponse>> registerDevice(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody DeviceRegistrationRequest request) {
        DeviceRegistrationResponse response = deviceService.registerDevice(currentUser.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Device registered successfully. Please install the agent on the target machine.", response), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DeviceDto>>> getDevices(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<DeviceDto> devices = deviceService.getUserDevices(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @deviceSecurityGuard.isOwner(authentication, #id)")
    public ResponseEntity<ApiResponse<DeviceDto>> getDeviceById(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        DeviceDto device = deviceService.getDeviceById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(device));
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<ApiResponse<DeviceDto>> getDeviceByUuid(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable String uuid) {
        DeviceDto device = deviceService.getDeviceByUuid(uuid, currentUser);
        return ResponseEntity.ok(ApiResponse.success(device));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @deviceSecurityGuard.isOwner(authentication, #id)")
    public ResponseEntity<ApiResponse<DeviceDto>> updateDevice(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody DeviceUpdateRequest request) {
        DeviceDto device = deviceService.updateDevice(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Device updated successfully", device));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @deviceSecurityGuard.isOwner(authentication, #id)")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        deviceService.deleteDevice(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Device unregistered and deleted successfully", null));
    }

    @PostMapping("/{id}/regenerate-token")
    @PreAuthorize("hasRole('ADMIN') or @deviceSecurityGuard.isOwner(authentication, #id)")
    public ResponseEntity<ApiResponse<DeviceRegistrationResponse>> regenerateToken(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        DeviceRegistrationResponse response = deviceService.regenerateToken(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Agent token regenerated successfully", response));
    }
}