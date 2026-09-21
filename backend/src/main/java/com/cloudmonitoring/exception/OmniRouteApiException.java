package com.cloudmonitoring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class OmniRouteApiException extends RuntimeException {

    public OmniRouteApiException(String message) {
        super(message);
    }

    public OmniRouteApiException(String message, Throwable cause) {
        super(message, cause);
    }
}