package com.example.remittanceservice.presentation.controller;

import com.example.remittanceservice.application.service.PolicyService;
import com.example.remittanceservice.domain.policy.PolicyConfig;
import com.example.remittanceservice.domain.policy.PolicyType;
import com.example.remittanceservice.presentation.dto.PolicyDto.PolicyResponse;
import com.example.remittanceservice.presentation.dto.PolicyDto.UpsertPolicyRequest;
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
        name = "Policy",
        description = "정책(한도/수수료) 관리 API (옵션). 과제 필수 요구사항이 아니며, 기본 정책(PolicyType.DEFAULT)은 애플리케이션 시작 시 data.sql로 자동 생성/업데이트됩니다."
)
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping("/v1/policies/{policyType}")
    @Operation(
            summary = "정책 조회(옵션)",
            description = "policyType(enum)으로 정책을 조회합니다."
    )
    public ResponseEntity<PolicyResponse> get(@PathVariable PolicyType policyType) {
        PolicyConfig cfg = policyService.getPolicyConfig(policyType);
        return ResponseEntity.ok(
                PolicyResponse.of(cfg.getPolicyType(), cfg.getWithdrawDailyLimit(), cfg.getTransferDailyLimit(), cfg.getTransferFeeBps())
        );
    }

    @PutMapping("/v1/policies/{policyType}")
    @Operation(
            summary = "정책 Upsert(옵션)",
            description = "policyType(enum)으로 정책을 생성/수정합니다(Upsert)."
    )
    public ResponseEntity<PolicyResponse> update(@PathVariable PolicyType policyType, @Valid @RequestBody UpsertPolicyRequest request) {
        PolicyConfig cfg = policyService.upsertPolicy(
                policyType,
                request.withdrawDailyLimit(),
                request.transferDailyLimit(),
                request.transferFeeBps()
        );
        return ResponseEntity.ok(
                PolicyResponse.of(cfg.getPolicyType(), cfg.getWithdrawDailyLimit(), cfg.getTransferDailyLimit(), cfg.getTransferFeeBps())
        );
    }
}

