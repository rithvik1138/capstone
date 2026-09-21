package com.cloudmonitoring.controller;

import com.cloudmonitoring.dto.request.AgentHandshakeRequest;
import com.cloudmonitoring.dto.request.AgentHeartbeatRequest;
import com.cloudmonitoring.dto.response.ApiResponse;
import com.cloudmonitoring.dto.response.DeviceDto;
import com.cloudmonitoring.exception.UnauthorizedAccessException;
import com.cloudmonitoring.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
public class AgentIngestController {

    private final DeviceService deviceService;

    @Autowired
    public AgentIngestController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/handshake")
    public ResponseEntity<ApiResponse<DeviceDto>> handshake(
            @RequestHeader(value = "X-Agent-Token", required = false) String agentToken,
            @Valid @RequestBody AgentHandshakeRequest request) {
        if (agentToken == null || agentToken.isBlank()) {
            throw new UnauthorizedAccessException("Missing X-Agent-Token header");
        }
        DeviceDto deviceDto = deviceService.processAgentHandshake(agentToken, request);
        return ResponseEntity.ok(ApiResponse.success("Agent connected and device hardware specs synchronized", deviceDto));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<ApiResponse<Void>> heartbeat(
            @RequestHeader(value = "X-Agent-Token", required = false) String agentToken,
            @Valid @RequestBody AgentHeartbeatRequest request) {
        if (agentToken == null || agentToken.isBlank()) {
            throw new UnauthorizedAccessException("Missing X-Agent-Token header");
        }
        deviceService.processAgentHeartbeat(agentToken, request);
        return ResponseEntity.ok(ApiResponse.success("Heartbeat acknowledged", null));
    }
}