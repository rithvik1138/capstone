package com.cloudmonitoring.security;

import com.cloudmonitoring.entity.Device;
import com.cloudmonitoring.entity.Role;
import com.cloudmonitoring.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("deviceSecurityGuard")
public class DeviceSecurityGuard {

    private final DeviceRepository deviceRepository;

    @Autowired
    public DeviceSecurityGuard(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public boolean isOwner(Authentication authentication, Long deviceId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return false;
        }

        if (userPrincipal.getRole() == Role.ROLE_ADMIN) {
            return true;
        }

        Optional<Device> deviceOptional = deviceRepository.findById(deviceId);
        if (deviceOptional.isEmpty()) {
            return false;
        }

        Device device = deviceOptional.get();
        return device.getUser() != null && device.getUser().getId().equals(userPrincipal.getId());
    }

    public boolean isOwnerByUuid(Authentication authentication, String deviceUuid) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return false;
        }

        if (userPrincipal.getRole() == Role.ROLE_ADMIN) {
            return true;
        }

        Optional<Device> deviceOptional = deviceRepository.findByDeviceUuid(deviceUuid);
        if (deviceOptional.isEmpty()) {
            return false;
        }

        Device device = deviceOptional.get();
        return device.getUser() != null && device.getUser().getId().equals(userPrincipal.getId());
    }
}