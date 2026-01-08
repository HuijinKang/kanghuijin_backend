package com.example.remittanceservice.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionDto {

    public record DepositRequest(
            @Positive long amount
    ) {
    }

    public record WithdrawRequest(
            @Positive long amount
    ) {
    }

    public record TransferRequest(
            @NotBlank
            @Pattern(
                    regexp = "^\\d{10,14}$",
                    message = "계좌번호는 숫자만 10~14자리로 입력해주세요."
            )
            String fromAccountNumber,
            @NotBlank
            @Pattern(
                    regexp = "^\\d{10,14}$",
                    message = "계좌번호는 숫자만 10~14자리로 입력해주세요."
            )
            String toAccountNumber,
            @Positive long amount
    ) {
    }
}
