package com.cloudmonitoring.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incident_ai_analyses")
public class IncidentAiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(name = "ai_model", length = 50)
    private String aiModel;

    @Column(name = "telemetry_context", columnDefinition = "JSON")
    private String telemetryContextJson;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "impact_analysis", columnDefinition = "TEXT")
    private String impactAnalysis;

    @Column(name = "recommended_actions", columnDefinition = "JSON")
    private String recommendedActionsJson;

    @Column(name = "raw_response", columnDefinition = "LONGTEXT")
    private String rawResponse;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public IncidentAiAnalysis() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    public String getAiModel() {
        return aiModel;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public String getTelemetryContextJson() {
        return telemetryContextJson;
    }

    public void setTelemetryContextJson(String telemetryContextJson) {
        this.telemetryContextJson = telemetryContextJson;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getImpactAnalysis() {
        return impactAnalysis;
    }

    public void setImpactAnalysis(String impactAnalysis) {
        this.impactAnalysis = impactAnalysis;
    }

    public String getRecommendedActionsJson() {
        return recommendedActionsJson;
    }

    public void setRecommendedActionsJson(String recommendedActionsJson) {
        this.recommendedActionsJson = recommendedActionsJson;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}