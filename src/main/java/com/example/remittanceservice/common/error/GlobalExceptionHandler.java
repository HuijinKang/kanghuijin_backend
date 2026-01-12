package com.example.remittanceservice.common.error;

import com.example.remittanceservice.common.exception.CoreException;
import com.example.remittanceservice.presentation.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidation(MethodArgumentNotValidException e) {
        log.warn("[VALIDATION_ERROR] message={}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.VALIDATION_ERROR);
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getHttpStatus().value())
                .body(ApiResponse.error(errorResponse.message()));
    }

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleCore(CoreException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("[CORE_EXCEPTION] code={}, message={}", errorCode.name(), e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(errorCode, e.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatus().value())
                .body(ApiResponse.error(errorResponse.message()));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleNotImplemented(UnsupportedOperationException e) {
        log.error("[NOT_IMPLEMENTED] message={}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.NOT_IMPLEMENTED);
        return ResponseEntity.status(ErrorCode.NOT_IMPLEMENTED.getHttpStatus().value())
                .body(ApiResponse.error(errorResponse.message()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleDataIntegrity(DataIntegrityViolationException e) {
        log.error("[DATA_INTEGRITY_VIOLATION] message={}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_ERROR);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getHttpStatus().value())
                .body(ApiResponse.error(errorResponse.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUnknown(Exception e) {
        log.error("[INTERNAL_ERROR] message={}", e.getMessage(), e);
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_ERROR);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getHttpStatus().value())
                .body(ApiResponse.error(errorResponse.message()));
    }
}
