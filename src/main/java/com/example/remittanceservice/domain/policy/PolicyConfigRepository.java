package com.example.remittanceservice.domain.policy;

import java.util.Optional;

public interface PolicyConfigRepository {

    Optional<PolicyConfig> findByPolicyType(PolicyType policyType);

    PolicyConfig save(PolicyConfig policyConfig);
}
