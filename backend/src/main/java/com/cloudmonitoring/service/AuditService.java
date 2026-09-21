package com.cloudmonitoring.service;

import com.cloudmonitoring.entity.AuditLog;
import com.cloudmonitoring.entity.User;
import com.cloudmonitoring.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Async
    public void logAction(User user, String action, String resourceType, String resourceId, String ipAddress, String details) {
        try {
            String sanitizedJson = toJson(details);
            AuditLog auditLog = new AuditLog(
                    user,
                    action,
                    resourceType,
                    resourceId,
                    ipAddress,
                    sanitizedJson,
                    LocalDateTime.now()
            );
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            logger.error("Failed to record audit log for action: {}", action, e);
        }
    }

    @Async
    public void logAction(User user, String action, String resourceType, String resourceId, String details) {
        logAction(user, action, resourceType, resourceId, "SYSTEM", details);
    }

    @Async
    public void recordAudit(Long userId, String action, String resourceType, String resourceId, Object details) {
        try {
            String sanitizedJson = toJson(details);
            AuditLog auditLog = new AuditLog(
                    null,
                    action,
                    resourceType,
                    resourceId,
                    "SYSTEM",
                    sanitizedJson,
                    LocalDateTime.now()
            );
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            logger.error("Failed to record audit log for action: {}", action, e);
        }
    }

    private String toJson(Object details) {
        if (details == null) {
            return null;
        }
        if (details instanceof String) {
            String str = ((String) details).trim();
            if ((str.startsWith("{") && str.endsWith("}")) || (str.startsWith("[") && str.endsWith("]"))) {
                try {
                    objectMapper.readTree(str);
                    return str;
                } catch (Exception ignored) {
                    // Fall through if not valid JSON
                }
            }
            try {
                return objectMapper.writeValueAsString(Map.of("message", str));
            } catch (Exception e) {
                return "{\"message\":\"" + str.replace("\"", "\\\"") + "\"}";
            }
        }
        try {
            return objectMapper.writeValueAsString(details);
        } catch (Exception e) {
            return "{\"message\":\"" + details.toString().replace("\"", "\\\"") + "\"}";
        }
    }
}