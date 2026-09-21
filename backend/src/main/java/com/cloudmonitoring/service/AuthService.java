package com.cloudmonitoring.service;

import com.cloudmonitoring.dto.request.LoginRequest;
import com.cloudmonitoring.dto.request.SignupRequest;
import com.cloudmonitoring.dto.response.AuthResponse;
import com.cloudmonitoring.dto.response.UserDto;
import com.cloudmonitoring.entity.Role;
import com.cloudmonitoring.entity.User;
import com.cloudmonitoring.exception.BadRequestException;
import com.cloudmonitoring.exception.ResourceNotFoundException;
import com.cloudmonitoring.repository.UserRepository;
import com.cloudmonitoring.security.JwtTokenProvider;
import com.cloudmonitoring.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    @Autowired
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.auditService = auditService;
    }

    @Transactional
    public UserDto registerUser(SignupRequest signupRequest, HttpServletRequest request) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new BadRequestException("Username '" + signupRequest.getUsername() + "' is already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Email '" + signupRequest.getEmail() + "' is already in use!");
        }

        // Determine role: if this is the very first user registering on a clean DB, assign ROLE_ADMIN; otherwise ROLE_USER
        Role userRole = userRepository.count() == 0 ? Role.ROLE_ADMIN : Role.ROLE_USER;

        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword()),
                userRole
        );

        User savedUser = userRepository.save(user);

        String ipAddress = request != null ? request.getRemoteAddr() : "127.0.0.1";
        auditService.logAction(savedUser, "USER_SIGNUP", "USER", savedUser.getId().toString(), ipAddress,
                "{\"username\":\"" + savedUser.getUsername() + "\", \"role\":\"" + savedUser.getRole() + "\"}");

        return UserDto.fromEntity(savedUser);
    }

    public AuthResponse authenticateUser(LoginRequest loginRequest, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        String ipAddress = request != null ? request.getRemoteAddr() : "127.0.0.1";
        auditService.logAction(user, "USER_LOGIN", "USER", user.getId().toString(), ipAddress,
                "{\"username\":\"" + user.getUsername() + "\"}");

        return new AuthResponse(
                jwt,
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getEmail(),
                userPrincipal.getRole(),
                tokenProvider.getExpirationDurationMs()
        );
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUserProfile(UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
        return UserDto.fromEntity(user);
    }
}