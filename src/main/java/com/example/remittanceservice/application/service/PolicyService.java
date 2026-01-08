package com.example.remittanceservice.application.service;

import com.example.remittanceservice.domain.policy.PolicyConfigRepository;
import com.example.remittanceservice.common.error.ErrorCode;
import com.example.remittanceservice.common.exception.CoreException;
import com.example.remittanceservice.domain.policy.PolicyConfig;
import com.example.remittanceservice.domain.policy.PolicyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyConfigRepository policyConfigRepository;

    @Transactional(readOnly = true)
    public PolicyConfig getPolicyConfig(PolicyType policyType) {
        return policyConfigRepository.findByPolicyType(policyType)
                .orElseThrow(() -> new CoreException(ErrorCode.NOT_FOUND, "PolicyConfig(%s) not found".formatted(policyType)));
    }

    @Transactional
    public PolicyConfig upsertPolicy(PolicyType policyType, long withdrawDailyLimit, long transferDailyLimit, int transferFeeBps) {
        return policyConfigRepository.findByPolicyType(policyType)
                .map(existing -> {
                    existing.update(withdrawDailyLimit, transferDailyLimit, transferFeeBps);
                    return policyConfigRepository.save(existing);
                })
                .orElseGet(() -> policyConfigRepository.save(
                        PolicyConfig.of(policyType, withdrawDailyLimit, transferDailyLimit, transferFeeBps)
                ));
    }

}
