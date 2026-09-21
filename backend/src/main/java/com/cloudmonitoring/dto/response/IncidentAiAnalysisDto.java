package com.cloudmonitoring.dto.response;

import java.time.LocalDateTime;

public class IncidentAiAnalysisDto {

    private Long id;
    private Long incidentId;
    private String aiModel;
    private String telemetryContextJson;
    private String rootCause;
    private String impactAnalysis;
    private String recommendedActionsJson;
    private LocalDateTime createdAt;

    public IncidentAiAnalysisDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(Long incidentId) {
        this.incidentId = incidentId;
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

    public void setRecommendedActions(java.util.List<?> actions) {
        if (actions != null) {
            try {
                this.recommendedActionsJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(actions);
            } catch (Exception e) {
                this.recommendedActionsJson = "[]";
            }
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}