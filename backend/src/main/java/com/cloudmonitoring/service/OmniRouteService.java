package com.cloudmonitoring.service;

import com.cloudmonitoring.dto.response.DeviceMetricsDto;
import com.cloudmonitoring.dto.response.IncidentAiAnalysisDto;
import com.cloudmonitoring.entity.*;
import com.cloudmonitoring.exception.BadRequestException;
import com.cloudmonitoring.exception.OmniRouteApiException;
import com.cloudmonitoring.exception.ResourceNotFoundException;
import com.cloudmonitoring.exception.UnauthorizedAccessException;
import com.cloudmonitoring.repository.DeviceLogRepository;
import com.cloudmonitoring.repository.IncidentAiAnalysisRepository;
import com.cloudmonitoring.repository.IncidentRepository;
import com.cloudmonitoring.repository.UserRepository;
import com.cloudmonitoring.security.UserPrincipal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OmniRouteService {

    private static final Logger log = LoggerFactory.getLogger(OmniRouteService.class);

    private final WebClient webClient;
    private final IncidentRepository incidentRepository;
    private final IncidentAiAnalysisRepository aiAnalysisRepository;
    private final DeviceLogRepository deviceLogRepository;
    private final UserRepository userRepository;
    private final PrometheusQueryService prometheusQueryService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Value("${omniroute.api.url:https://api.omniroute.ai/v1}")
    private String omniRouteUrl;

    @Value("${omniroute.api.key:}")
    private String omniRouteApiKey;

    @Value("${omniroute.model:claude-3-5-sonnet-20241022}")
    private String defaultModel;

    public OmniRouteService(@Qualifier("omniRouteWebClient") WebClient webClient,
                            IncidentRepository incidentRepository,
                            IncidentAiAnalysisRepository aiAnalysisRepository,
                            DeviceLogRepository deviceLogRepository,
                            UserRepository userRepository,
                            PrometheusQueryService prometheusQueryService,
                            AuditService auditService,
                            ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.incidentRepository = incidentRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.deviceLogRepository = deviceLogRepository;
        this.userRepository = userRepository;
        this.prometheusQueryService = prometheusQueryService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public IncidentAiAnalysisDto analyzeIncident(Long incidentId, UserPrincipal currentUser, String ipAddress) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + incidentId));

        Device device = incident.getDevice();

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()));

        if (!isAdmin && !device.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to run AI analysis for this incident");
        }

        // Gather real telemetry snapshot
        DeviceMetricsDto currentMetrics = prometheusQueryService.getCurrentMetrics(device.getDeviceUuid());

        // Gather recent real device logs (last 30 logs)
        List<DeviceLog> recentLogs = deviceLogRepository.findByDeviceIdOrderByTimestampDesc(
                device.getId(), PageRequest.of(0, 30)).getContent();

        String logContext = recentLogs.stream()
                .map(l -> String.format("[%s] [%s] (%s): %s", l.getTimestamp(), l.getLogLevel(), l.getServiceSource(), l.getMessage()))
                .collect(Collectors.joining("\n"));

        Map<String, Object> telemetryMap = new LinkedHashMap<>();
        telemetryMap.put("cpuPercent", currentMetrics.getCpuPercent());
        telemetryMap.put("ramPercent", currentMetrics.getRamPercent());
        telemetryMap.put("diskPercent", currentMetrics.getDiskPercent());
        telemetryMap.put("uptimeSeconds", currentMetrics.getUptimeSeconds());
        telemetryMap.put("deviceOs", device.getOsName() + " " + (device.getOsVersion() != null ? device.getOsVersion() : ""));
        telemetryMap.put("cpuModel", device.getCpuModel());
        telemetryMap.put("totalCores", device.getTotalCores());

        String telemetryJson;
        try {
            telemetryJson = objectMapper.writeValueAsString(telemetryMap);
        } catch (Exception e) {
            telemetryJson = "{}";
        }

        String prompt = buildPrompt(incident, device, currentMetrics, logContext);

        // Call OmniRoute AI
        IncidentAiAnalysis analysis = callOmniRoute(incident, prompt, telemetryJson);

        IncidentAiAnalysis saved = aiAnalysisRepository.save(analysis);

        User user = userRepository.findById(currentUser.getId()).orElse(null);
        auditService.logAction(user, "AI_INCIDENT_ANALYSIS", "Incident", incidentId.toString(), ipAddress,
                "Executed OmniRoute AI incident diagnosis for incident #" + incidentId);

        return mapToDto(saved);
    }

    private String buildPrompt(Incident incident, Device device, DeviceMetricsDto metrics, String logContext) {
        return "You are an expert site reliability engineer (SRE) and cloud infrastructure diagnostics AI.\n" +
                "Analyze the following real production incident and provide a structured diagnosis.\n\n" +
                "### Incident Details:\n" +
                "- Incident Title: " + incident.getTitle() + "\n" +
                "- Severity: " + incident.getSeverity() + "\n" +
                "- Description: " + incident.getDescription() + "\n" +
                "- Created At: " + incident.getCreatedAt() + "\n\n" +
                "### Target Device Profile:\n" +
                "- Device Name: " + device.getDeviceName() + "\n" +
                "- Device Type: " + device.getDeviceType() + "\n" +
                "- OS: " + device.getOsName() + " " + (device.getOsVersion() != null ? device.getOsVersion() : "") + "\n" +
                "- CPU Model: " + device.getCpuModel() + " (" + device.getTotalCores() + " cores)\n\n" +
                "### Real-time Telemetry Metrics:\n" +
                "- Current CPU Usage: " + metrics.getCpuPercent() + "%\n" +
                "- Current Memory Usage: " + metrics.getRamPercent() + "%\n" +
                "- Current Disk Usage: " + metrics.getDiskPercent() + "%\n" +
                "- System Uptime: " + metrics.getUptimeSeconds() + " seconds\n\n" +
                "### Recent System / Application Logs:\n" +
                (logContext.isEmpty() ? "No recent log entries recorded on device." : logContext) + "\n\n" +
                "### Required Output Format:\n" +
                "Respond ONLY with a valid JSON object matching this exact structure without markdown backticks:\n" +
                "{\n" +
                "  \"rootCause\": \"Detailed explanation of the root cause based on the metrics and logs\",\n" +
                "  \"impactAnalysis\": \"Assessment of system performance impact, affected services, and risk level\",\n" +
                "  \"recommendedActions\": [\n" +
                "    \"Immediate step 1 to mitigate issue\",\n" +
                "    \"Investigation step 2\",\n" +
                "    \"Long-term fix step 3\"\n" +
                "  ]\n" +
                "}";
    }

    private IncidentAiAnalysis callOmniRoute(Incident incident, String prompt, String telemetryJson) {
        if (omniRouteApiKey == null || omniRouteApiKey.isBlank() || omniRouteApiKey.equals("mock-key-for-dev")) {
            // Local fallback structure when live key is not supplied in environment
            IncidentAiAnalysis analysis = new IncidentAiAnalysis();
            analysis.setIncident(incident);
            analysis.setAiModel(defaultModel);
            analysis.setTelemetryContextJson(telemetryJson);
            analysis.setRootCause("Automatic diagnosis requires OMNIROUTE_API_KEY environment variable. Incident generated for device " + incident.getDevice().getDeviceName() + " with severity " + incident.getSeverity());
            analysis.setImpactAnalysis("Device resource threshold exceeded or heartbeat interruption detected.");
            analysis.setRecommendedActionsJson("[\"Configure OMNIROUTE_API_KEY in environment variables\", \"Inspect system logs on target device\", \"Check host load averages and memory pressure\"]");
            analysis.setRawResponse("{\"status\": \"api_key_not_configured\"}");
            analysis.setCreatedAt(LocalDateTime.now());
            return analysis;
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", defaultModel);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "You are an infrastructure monitoring AI diagnostics engine. You always output pure JSON."),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.2);

            String responseString = webClient.post()
                    .uri(omniRouteUrl + "/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + omniRouteApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseString);
            String aiContent = root.path("choices").path(0).path("message").path("content").asText();

            // Sanitize possible codeblocks in response
            if (aiContent.startsWith("```json")) {
                aiContent = aiContent.substring(7);
            }
            if (aiContent.startsWith("```")) {
                aiContent = aiContent.substring(3);
            }
            if (aiContent.endsWith("```")) {
                aiContent = aiContent.substring(0, aiContent.length() - 3);
            }
            aiContent = aiContent.trim();

            JsonNode parsedJson = objectMapper.readTree(aiContent);
            String rootCause = parsedJson.path("rootCause").asText("Root cause undetermined.");
            String impact = parsedJson.path("impactAnalysis").asText("Impact assessment unavailable.");
            String actionsJson = parsedJson.path("recommendedActions").toString();

            IncidentAiAnalysis analysis = new IncidentAiAnalysis();
            analysis.setIncident(incident);
            analysis.setAiModel(defaultModel);
            analysis.setTelemetryContextJson(telemetryJson);
            analysis.setRootCause(rootCause);
            analysis.setImpactAnalysis(impact);
            analysis.setRecommendedActionsJson(actionsJson);
            analysis.setRawResponse(responseString);
            analysis.setCreatedAt(LocalDateTime.now());
            return analysis;
        } catch (Exception e) {
            log.error("OmniRoute API invocation failed: {}", e.getMessage());
            throw new OmniRouteApiException("Failed to communicate with OmniRoute AI service: " + e.getMessage());
        }
    }

    private IncidentAiAnalysisDto mapToDto(IncidentAiAnalysis analysis) {
        IncidentAiAnalysisDto dto = new IncidentAiAnalysisDto();
        dto.setId(analysis.getId());
        dto.setIncidentId(analysis.getIncident().getId());
        dto.setAiModel(analysis.getAiModel());
        dto.setTelemetryContextJson(analysis.getTelemetryContextJson());
        dto.setRootCause(analysis.getRootCause());
        dto.setImpactAnalysis(analysis.getImpactAnalysis());
        dto.setCreatedAt(analysis.getCreatedAt());

        if (analysis.getRecommendedActionsJson() != null && !analysis.getRecommendedActionsJson().isBlank()) {
            try {
                List<String> actions = objectMapper.readValue(
                        analysis.getRecommendedActionsJson(),
                        new TypeReference<List<String>>() {}
                );
                dto.setRecommendedActions(actions);
            } catch (Exception e) {
                dto.setRecommendedActions(Collections.singletonList(analysis.getRecommendedActionsJson()));
            }
        } else {
            dto.setRecommendedActions(Collections.emptyList());
        }

        return dto;
    }
}