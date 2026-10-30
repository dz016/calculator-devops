package com.spe.calculator.dto;

import java.time.Instant;

/**
 * Structured error body returned for every 400 response.
 */
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {

    public static ErrorResponse badRequest(String message, String path) {
        return new ErrorResponse(Instant.now(), 400, "Bad Request", message, path);
    }
}
