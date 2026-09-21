package com.cloudmonitoring.repository;

import com.cloudmonitoring.entity.Incident;
import com.cloudmonitoring.entity.IncidentStatus;
import com.cloudmonitoring.entity.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Page<Incident> findByDeviceId(Long deviceId, Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE i.device.user.id = :userId")
    Page<Incident> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE i.device.user.id = :userId")
    Page<Incident> findByDeviceUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE i.device.user.id = :userId AND i.status = :status")
    Page<Incident> findByDeviceUserIdAndStatus(@Param("userId") Long userId, @Param("status") IncidentStatus status, Pageable pageable);

    Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE i.device.user.id = :userId AND (:status IS NULL OR i.status = :status) AND (:severity IS NULL OR i.severity = :severity)")
    Page<Incident> findByUserIdAndFilters(@Param("userId") Long userId,
                                         @Param("status") IncidentStatus status,
                                         @Param("severity") Severity severity,
                                         Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE (:status IS NULL OR i.status = :status) AND (:severity IS NULL OR i.severity = :severity)")
    Page<Incident> findAllWithFilters(@Param("status") IncidentStatus status,
                                     @Param("severity") Severity severity,
                                     Pageable pageable);

    long countByStatus(IncidentStatus status);

    long countByStatusAndSeverity(IncidentStatus status, Severity severity);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.device.user.id = :userId AND i.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") IncidentStatus status);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.device.user.id = :userId AND i.status = :status AND i.severity = :severity")
    long countByUserIdAndStatusAndSeverity(@Param("userId") Long userId, @Param("status") IncidentStatus status, @Param("severity") Severity severity);

    List<Incident> findTop5ByOrderByCreatedAtDesc();
}