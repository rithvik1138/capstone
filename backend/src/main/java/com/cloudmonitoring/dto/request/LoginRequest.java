package com.cloudmonitoring.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @JsonProperty("usernameOrEmail")
    @JsonAlias({"username", "email", "usernameOrEmail"})
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public void setUsername(String username) {
        if (this.usernameOrEmail == null || this.usernameOrEmail.isBlank()) {
            this.usernameOrEmail = username;
        }
    }

    public void setEmail(String email) {
        if (this.usernameOrEmail == null || this.usernameOrEmail.isBlank()) {
            this.usernameOrEmail = email;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
