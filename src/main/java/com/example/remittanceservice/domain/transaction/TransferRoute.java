package com.example.remittanceservice.domain.transaction;

public enum TransferRoute {
    INTERNAL_CORE,     // 자행
    OPEN_BANKING,      // 타행
    INTERNATIONAL      // 해외
}
