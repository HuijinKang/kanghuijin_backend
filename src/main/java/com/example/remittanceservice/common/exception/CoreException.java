package com.example.remittanceservice.common.exception;

import com.example.remittanceservice.common.error.ErrorCode;
import lombok.Getter;

@Getter
public class CoreException extends RuntimeException {

    private final ErrorCode errorCode;

    public CoreException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public CoreException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
