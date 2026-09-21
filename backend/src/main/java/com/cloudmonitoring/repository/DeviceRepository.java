package com.cloudmonitoring.repository;

import com.cloudmonitoring.entity.Device;
import com.cloudmonitoring.entity.DeviceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceUuid(String deviceUuid);

    boolean existsByDeviceUuid(String deviceUuid);

    Page<Device> findByUserId(Long userId, Pageable pageable);

    List<Device> findByUserId(Long userId);

    Page<Device> findByUserIdAndStatus(Long userId, DeviceStatus status, Pageable pageable);

    Page<Device> findByStatus(DeviceStatus status, Pageable pageable);

    List<Device> findByStatus(DeviceStatus status);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, DeviceStatus status);

    long countByStatus(DeviceStatus status);

    @Query("SELECT d FROM Device d WHERE d.status = :status AND (d.lastSeenAt IS NULL OR d.lastSeenAt < :threshold)")
    List<Device> findStaleOnlineDevices(DeviceStatus status, LocalDateTime threshold);
}