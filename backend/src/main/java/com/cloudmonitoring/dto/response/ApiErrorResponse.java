package com.cloudmonitoring.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Standard API error response model.
 */
public class ApiErrorResponse {

    private boolean success;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> fieldErrors;
    private Instant timestamp;

    public ApiErrorResponse() {
        this.success = false;
        this.timestamp = Instant.now();
    }

    public ApiErrorResponse(int status, String error, String message, String path) {
        this.success = false;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now();
    }

    public ApiErrorResponse(int status, String error, String message, String path, List<String> fieldErrors) {
        this.success = false;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
        this.timestamp = Instant.now();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}