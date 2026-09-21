package com.cloudmonitoring.controller;

import com.cloudmonitoring.dto.response.ApiResponse;
import com.cloudmonitoring.dto.response.IncidentAiAnalysisDto;
import com.cloudmonitoring.security.CurrentUser;
import com.cloudmonitoring.security.UserPrincipal;
import com.cloudmonitoring.service.OmniRouteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiAnalysisController {

    private final OmniRouteService omniRouteService;

    public AiAnalysisController(OmniRouteService omniRouteService) {
        this.omniRouteService = omniRouteService;
    }

    @PostMapping("/incidents/{incidentId}/analyze")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<IncidentAiAnalysisDto>> analyzeIncident(
            @PathVariable Long incidentId,
            @CurrentUser UserPrincipal currentUser,
            HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        IncidentAiAnalysisDto analysis = omniRouteService.analyzeIncident(incidentId, currentUser, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("AI incident analysis completed", analysis));
    }
}