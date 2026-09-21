-- =============================================================================
-- Cloud Infrastructure Monitoring and Logging System
-- Database Schema Definition (MySQL 8.0+)
-- STRICT ZERO-DEMO-DATA: This script defines tables, constraints, and indexes only.
-- No fake/seed records are inserted.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS capstone
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE capstone;

-- -----------------------------------------------------------------------------
-- 1. Table: users
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ROLE_ADMIN', 'ROLE_USER') NOT NULL DEFAULT 'ROLE_USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. Table: devices
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_uuid VARCHAR(36) NOT NULL UNIQUE,
    device_name VARCHAR(100) NOT NULL,
    device_type ENUM(
        'LAPTOP_WINDOWS',
        'LAPTOP_LINUX',
        'LAPTOP_MAC',
        'RASPBERRY_PI_4',
        'AWS_EC2',
        'OTHER'
    ) NOT NULL DEFAULT 'LAPTOP_WINDOWS',
    hostname VARCHAR(150) NULL,
    ip_address VARCHAR(45) NULL,
    mac_address VARCHAR(50) NULL,
    os_name VARCHAR(100) NULL,
    os_version VARCHAR(100) NULL,
    cpu_model VARCHAR(150) NULL,
    total_cores INT NULL,
    total_memory_bytes BIGINT NULL,
    total_disk_bytes BIGINT NULL,
    agent_version VARCHAR(20) NULL,
    status ENUM('PENDING', 'ONLINE', 'OFFLINE', 'REVOKED') NOT NULL DEFAULT 'PENDING',
    last_seen_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_devices_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_devices_user_id (user_id),
    INDEX idx_devices_uuid (device_uuid),
    INDEX idx_devices_status (status),
    INDEX idx_devices_last_seen (last_seen_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. Table: device_tokens
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS device_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL UNIQUE,
    token_hash VARCHAR(255) NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    CONSTRAINT fk_device_tokens_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    INDEX idx_device_tokens_hash (token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. Table: device_logs
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS device_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    timestamp TIMESTAMP(3) NOT NULL,
    log_level ENUM('INFO', 'WARNING', 'ERROR', 'CRITICAL') NOT NULL DEFAULT 'INFO',
    service_source VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    event_type VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_logs_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    INDEX idx_logs_device_timestamp (device_id, timestamp DESC),
    INDEX idx_logs_device_level (device_id, log_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. Table: alerts
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity ENUM('INFO', 'WARNING', 'CRITICAL') NOT NULL DEFAULT 'WARNING',
    message TEXT NOT NULL,
    current_val DOUBLE NULL,
    threshold_val DOUBLE NULL,
    status ENUM('ACTIVE', 'ACKNOWLEDGED', 'RESOLVED') NOT NULL DEFAULT 'ACTIVE',
    triggered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_alerts_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    INDEX idx_alerts_device_status (device_id, status),
    INDEX idx_alerts_status (status),
    INDEX idx_alerts_severity (severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. Table: incidents
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS incidents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    alert_id BIGINT NULL,
    created_by_user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    severity ENUM('INFO', 'WARNING', 'CRITICAL') NOT NULL DEFAULT 'WARNING',
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    CONSTRAINT fk_incidents_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    CONSTRAINT fk_incidents_alert FOREIGN KEY (alert_id) REFERENCES alerts(id) ON DELETE SET NULL,
    CONSTRAINT fk_incidents_user FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_incidents_device (device_id),
    INDEX idx_incidents_status (status),
    INDEX idx_incidents_created_by (created_by_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. Table: incident_ai_analyses
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS incident_ai_analyses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    status ENUM('PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PROCESSING',
    ai_model VARCHAR(50) NULL,
    telemetry_context JSON NULL,
    root_cause TEXT NULL,
    impact_analysis TEXT NULL,
    recommended_actions JSON NULL,
    error_message TEXT NULL,
    raw_response LONGTEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    CONSTRAINT fk_ai_analyses_incident FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE,
    INDEX idx_ai_incident (incident_id),
    INDEX idx_ai_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. Table: audit_logs
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(50) NULL,
    ip_address VARCHAR(45) NULL,
    details JSON NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_timestamp (timestamp DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;