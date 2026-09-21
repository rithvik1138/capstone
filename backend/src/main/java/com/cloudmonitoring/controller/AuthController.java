package com.cloudmonitoring.controller;

import com.cloudmonitoring.dto.request.LoginRequest;
import com.cloudmonitoring.dto.request.SignupRequest;
import com.cloudmonitoring.dto.response.ApiResponse;
import com.cloudmonitoring.dto.response.AuthResponse;
import com.cloudmonitoring.dto.response.UserDto;
import com.cloudmonitoring.security.CurrentUser;
import com.cloudmonitoring.security.UserPrincipal;
import com.cloudmonitoring.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserDto>> registerUser(@Valid @RequestBody SignupRequest signupRequest,
                                                             HttpServletRequest request) {
        UserDto registeredUser = authService.registerUser(signupRequest, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                                                      HttpServletRequest request) {
        AuthResponse authResponse = authService.authenticateUser(loginRequest, request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", authResponse));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        UserDto userProfile = authService.getCurrentUserProfile(currentUser);
        return ResponseEntity.ok(ApiResponse.success(userProfile));
    }
}