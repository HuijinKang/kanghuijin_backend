package com.example.remittanceservice.common.error;

import com.example.remittanceservice.common.exception.CoreException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getHttpStatus().value())
                .body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR));
    }

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ErrorResponse> handleCore(CoreException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus().value())
                .body(ErrorResponse.of(errorCode, e.getMessage()));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleNotImplemented(UnsupportedOperationException e) {
        return ResponseEntity.status(ErrorCode.NOT_IMPLEMENTED.getHttpStatus().value())
                .body(ErrorResponse.of(ErrorCode.NOT_IMPLEMENTED));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception e) {
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getHttpStatus().value())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR));
    }
}
