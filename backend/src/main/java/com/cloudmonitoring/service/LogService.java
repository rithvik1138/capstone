package com.cloudmonitoring.service;

import com.cloudmonitoring.dto.request.LogEntryDto;
import com.cloudmonitoring.dto.response.DeviceLogDto;
import com.cloudmonitoring.entity.*;
import com.cloudmonitoring.exception.BadRequestException;
import com.cloudmonitoring.exception.ResourceNotFoundException;
import com.cloudmonitoring.exception.UnauthorizedAccessException;
import com.cloudmonitoring.repository.DeviceLogRepository;
import com.cloudmonitoring.repository.DeviceRepository;
import com.cloudmonitoring.repository.DeviceTokenRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogService {

    private final DeviceLogRepository deviceLogRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    public LogService(DeviceLogRepository deviceLogRepository,
                      DeviceRepository deviceRepository,
                      DeviceTokenRepository deviceTokenRepository) {
        this.deviceLogRepository = deviceLogRepository;
        this.deviceRepository = deviceRepository;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Transactional
    public int ingestLogs(String rawToken, List<LogEntryDto> entries) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new UnauthorizedAccessException("Missing X-Device-Token header");
        }
        if (entries == null || entries.isEmpty()) {
            return 0;
        }

        String tokenHash = hashToken(rawToken);
        DeviceToken deviceToken = deviceTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedAccessException("Invalid or revoked device token"));

        if (deviceToken.getIsRevoked()) {
            throw new UnauthorizedAccessException("Device token has been revoked");
        }

        Device device = deviceToken.getDevice();
        List<DeviceLog> logsToSave = new ArrayList<>();

        for (LogEntryDto entry : entries) {
            if (entry.getMessage() == null || entry.getMessage().isBlank()) {
                continue;
            }
            DeviceLog log = new DeviceLog();
            log.setDevice(device);
            log.setTimestamp(entry.getTimestamp() != null ? entry.getTimestamp() : LocalDateTime.now());
            log.setLogLevel(entry.getLogLevel() != null ? entry.getLogLevel() : LogLevel.INFO);
            log.setServiceSource(entry.getServiceSource() != null ? entry.getServiceSource() : "system");
            log.setMessage(entry.getMessage());
            log.setEventType(entry.getEventType());
            logsToSave.add(log);
        }

        if (!logsToSave.isEmpty()) {
            deviceLogRepository.saveAll(logsToSave);
        }

        return logsToSave.size();
    }

    @Transactional(readOnly = true)
    public Page<DeviceLogDto> getDeviceLogs(Long deviceId, LogLevel logLevel, String search,
                                            LocalDateTime from, LocalDateTime to, Pageable pageable) {
        deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        Specification<DeviceLog> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("device").get("id"), deviceId));

            if (logLevel != null) {
                predicates.add(criteriaBuilder.equal(root.get("logLevel"), logLevel));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), to));
            }
            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate messageLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("message")), searchPattern);
                Predicate sourceLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("serviceSource")), searchPattern);
                predicates.add(criteriaBuilder.or(messageLike, sourceLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "timestamp")
        );

        return deviceLogRepository.findAll(spec, sortedPageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<DeviceLogDto> getAllLogs(LogLevel logLevel, String search, Pageable pageable) {
        Specification<DeviceLog> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (logLevel != null) {
                predicates.add(criteriaBuilder.equal(root.get("logLevel"), logLevel));
            }
            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate messageLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("message")), searchPattern);
                Predicate sourceLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("serviceSource")), searchPattern);
                predicates.add(criteriaBuilder.or(messageLike, sourceLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "timestamp")
        );

        return deviceLogRepository.findAll(spec, sortedPageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<DeviceLogDto> getRecentLogsForDevice(Long deviceId) {
        return deviceLogRepository.findTop50ByDeviceIdOrderByTimestampDesc(deviceId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private DeviceLogDto mapToDto(DeviceLog log) {
        return new DeviceLogDto(
                log.getId(),
                log.getDevice().getId(),
                log.getDevice().getDeviceName(),
                log.getDevice().getDeviceUuid(),
                log.getTimestamp(),
                log.getLogLevel(),
                log.getServiceSource(),
                log.getMessage(),
                log.getEventType(),
                log.getCreatedAt()
        );
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}