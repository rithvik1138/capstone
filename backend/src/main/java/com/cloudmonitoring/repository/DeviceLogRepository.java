package com.cloudmonitoring.repository;

import com.cloudmonitoring.entity.DeviceLog;
import com.cloudmonitoring.entity.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeviceLogRepository extends JpaRepository<DeviceLog, Long>, JpaSpecificationExecutor<DeviceLog> {

    Page<DeviceLog> findByDeviceIdOrderByTimestampDesc(Long deviceId, Pageable pageable);

    Page<DeviceLog> findByDeviceIdAndLogLevelOrderByTimestampDesc(Long deviceId, LogLevel logLevel, Pageable pageable);

    Page<DeviceLog> findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(Long deviceId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    List<DeviceLog> findTop50ByDeviceIdOrderByTimestampDesc(Long deviceId);

    long countByDeviceId(Long deviceId);

    long countByLogLevel(LogLevel logLevel);
}