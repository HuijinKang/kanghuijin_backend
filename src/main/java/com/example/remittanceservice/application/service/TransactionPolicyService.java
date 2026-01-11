package com.example.remittanceservice.application.service;

import com.example.remittanceservice.domain.transactionpolicy.TransactionPolicyRepository;
import com.example.remittanceservice.common.error.ErrorCode;
import com.example.remittanceservice.common.exception.CoreException;
import com.example.remittanceservice.domain.transactionpolicy.TransactionPolicy;
import com.example.remittanceservice.domain.transactionpolicy.TransactionPolicyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionPolicyService {

    private final TransactionPolicyRepository transactionPolicyRepository;

    @Transactional(readOnly = true)
    public TransactionPolicy getPolicy(TransactionPolicyType policyType) {
        return transactionPolicyRepository.findByPolicyType(policyType)
                .orElseThrow(() -> new CoreException(
                        ErrorCode.NOT_FOUND,
                        "TransactionPolicy(%s) not found".formatted(policyType)
                ));
    }

    @Transactional(readOnly = true)
    public long getWithdrawDailyLimit() {
        return getPolicy(TransactionPolicyType.DEFAULT).getWithdrawDailyLimit();
    }

    @Transactional(readOnly = true)
    public long getTransferDailyLimit() {
        return getPolicy(TransactionPolicyType.DEFAULT).getTransferDailyLimit();
    }

    @Transactional(readOnly = true)
    public long calculateTransferFee(long amount) {
        int feeBps = getPolicy(TransactionPolicyType.DEFAULT).getTransferFeeBps();
        // feeBps: 100 bps = 1.00%
        return (amount * feeBps) / 10_000L;
    }

    @Transactional
    public TransactionPolicy upsertPolicy(
            TransactionPolicyType policyType,
            long withdrawDailyLimit,
            long transferDailyLimit,
            int transferFeeBps
    ) {
        return transactionPolicyRepository.findByPolicyType(policyType)
                .map(existing -> {
                    existing.update(withdrawDailyLimit, transferDailyLimit, transferFeeBps);
                    return transactionPolicyRepository.save(existing);
                })
                .orElseGet(() -> transactionPolicyRepository.save(
                        TransactionPolicy.of(policyType, withdrawDailyLimit, transferDailyLimit, transferFeeBps)
                ));
    }
}
