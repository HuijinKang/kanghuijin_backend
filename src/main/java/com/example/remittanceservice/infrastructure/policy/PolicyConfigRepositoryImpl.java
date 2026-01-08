package com.example.remittanceservice.infrastructure.policy;

import com.example.remittanceservice.domain.policy.PolicyConfigRepository;
import com.example.remittanceservice.domain.policy.PolicyConfig;
import com.example.remittanceservice.domain.policy.PolicyType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PolicyConfigRepositoryImpl implements PolicyConfigRepository {

    private final PolicyConfigJpaRepository policyConfigJpaRepository;

    @Override
    public Optional<PolicyConfig> findByPolicyType(PolicyType policyType) {
        return policyConfigJpaRepository.findByPolicyType(policyType);
    }

    @Override
    public PolicyConfig save(PolicyConfig policyConfig) {
        return policyConfigJpaRepository.save(policyConfig);
    }
}
