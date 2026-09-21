package com.cloudmonitoring.repository;

import com.cloudmonitoring.entity.Alert;
import com.cloudmonitoring.entity.AlertStatus;
import com.cloudmonitoring.entity.AlertType;
import com.cloudmonitoring.entity.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByDeviceIdAndStatus(Long deviceId, AlertStatus status);

    Optional<Alert> findByDeviceIdAndAlertTypeAndStatus(Long deviceId, AlertType alertType, AlertStatus status);

    Page<Alert> findByDeviceId(Long deviceId, Pageable pageable);

    @Query("SELECT a FROM Alert a WHERE a.device.user.id = :userId " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:severity IS NULL OR a.severity = :severity)")
    Page<Alert> findByUserIdWithFilters(@Param("userId") Long userId,
                                        @Param("status") AlertStatus status,
                                        @Param("severity") Severity severity,
                                        Pageable pageable);

    @Query("SELECT a FROM Alert a WHERE (:status IS NULL OR a.status = :status) " +
           "AND (:severity IS NULL OR a.severity = :severity)")
    Page<Alert> findAllWithFilters(@Param("status") AlertStatus status,
                                  @Param("severity") Severity severity,
                                  Pageable pageable);

    long countByStatus(AlertStatus status);

    long countByStatusAndSeverity(AlertStatus status, Severity severity);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.device.user.id = :userId AND a.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") AlertStatus status);
}