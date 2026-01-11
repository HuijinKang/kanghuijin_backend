package com.example.remittanceservice.presentation.controller;

import com.example.remittanceservice.application.service.TransactionPolicyService;
import com.example.remittanceservice.domain.transactionpolicy.TransactionPolicy;
import com.example.remittanceservice.domain.transactionpolicy.TransactionPolicyType;
import com.example.remittanceservice.presentation.dto.ApiResponse;
import com.example.remittanceservice.presentation.dto.TransactionPolicyDto.PolicyResponse;
import com.example.remittanceservice.presentation.dto.TransactionPolicyDto.UpsertPolicyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(
        name = "TransactionPolicy",
        description = "(옵션) 거래 정책(한도/수수료) 관리 API. 필수 요구사항이 아니며, 기본 정책은 " +
                "애플리케이션 시작 시 data.sql로 자동 생성/업데이트됩니다."
)
public class TransactionPolicyController {

    private final TransactionPolicyService transactionPolicyService;

    @GetMapping("/v1/policies/{policyType}")
    @Operation(
            summary = "정책 조회(옵션)",
            description = "policyType(enum)으로 정책을 조회합니다."
    )
    public ResponseEntity<ApiResponse<PolicyResponse>> getPolicy(
            @PathVariable TransactionPolicyType policyType
    ) {
        TransactionPolicy policy = transactionPolicyService.getPolicy(policyType);
        return ResponseEntity.ok(ApiResponse.success(
                PolicyResponse.of(
                        policy.getPolicyType(),
                        policy.getWithdrawDailyLimit(),
                        policy.getTransferDailyLimit(),
                        policy.getTransferFeeBps()
                )));
    }

    @PutMapping("/v1/policies/{policyType}")
    @Operation(
            summary = "정책 Upsert(옵션)",
            description = "policyType(enum)으로 정책을 생성/수정합니다(Upsert)."
    )
    public ResponseEntity<ApiResponse<PolicyResponse>> updatePolicy(
            @PathVariable TransactionPolicyType policyType,
            @Valid @RequestBody UpsertPolicyRequest request
    ) {
        TransactionPolicy policy = transactionPolicyService.upsertPolicy(
                policyType,
                request.withdrawDailyLimit(),
                request.transferDailyLimit(),
                request.transferFeeBps()
        );
        return ResponseEntity.ok(ApiResponse.success(
                PolicyResponse.of(
                        policy.getPolicyType(),
                        policy.getWithdrawDailyLimit(),
                        policy.getTransferDailyLimit(),
                        policy.getTransferFeeBps()
                )));
    }
}
