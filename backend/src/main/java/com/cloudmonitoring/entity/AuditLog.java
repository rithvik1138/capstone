package com.cloudmonitoring.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AuditLog JPA entity for recording administrative actions and security events.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Column(name = "resource_id", length = 50)
    private String resourceId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "details", columnDefinition = "JSON")
    private String details;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public AuditLog() {
    }

    public AuditLog(User user, String action, String resourceType, String resourceId, String ipAddress, String details) {
        this.user = user;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.ipAddress = ipAddress;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(User user, String action, String resourceType, String resourceId, String ipAddress, String details, LocalDateTime timestamp) {
        this.user = user;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.ipAddress = ipAddress;
        this.details = details;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}