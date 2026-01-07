package com.example.remittanceservice.common.error;

import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        Instant timestamp,
        String details
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, Instant.now(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getDefaultMessage(), Instant.now(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String details) {
        return new ErrorResponse(errorCode.name(), errorCode.getDefaultMessage(), Instant.now(), details);
    }
}
