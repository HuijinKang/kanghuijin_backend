package com.example.remittanceservice.infrastructure.policy;

import com.example.remittanceservice.domain.policy.PolicyConfig;
import com.example.remittanceservice.domain.policy.PolicyType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyConfigJpaRepository extends JpaRepository<PolicyConfig, Long> {
    Optional<PolicyConfig> findByPolicyType(PolicyType policyType);
}
