package io.github.ghoshsa.mule.common.web;

import java.time.Instant;

public record ErrorResponse(int status, String error, String message, Instant timeStamp, String path) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, Instant.now(), path);
    }
}