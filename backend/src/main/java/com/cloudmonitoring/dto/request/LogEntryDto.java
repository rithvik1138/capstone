package com.cloudmonitoring.dto.request;

import com.cloudmonitoring.entity.LogLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class LogEntryDto {

    private LocalDateTime timestamp;

    @NotNull(message = "Log level is required")
    private LogLevel logLevel;

    private String serviceSource;

    @NotBlank(message = "Log message cannot be blank")
    private String message;

    private String eventType;

    public LogEntryDto() {
    }

    public LogEntryDto(LocalDateTime timestamp, LogLevel logLevel, String serviceSource, String message, String eventType) {
        this.timestamp = timestamp;
        this.logLevel = logLevel;
        this.serviceSource = serviceSource;
        this.message = message;
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getServiceSource() {
        return serviceSource;
    }

    public void setServiceSource(String serviceSource) {
        this.serviceSource = serviceSource;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}