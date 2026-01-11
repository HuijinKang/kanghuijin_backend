package com.example.remittanceservice.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionDto {

    public record DepositRequest(
            @Positive(message = "입금액은 양수여야 합니다")
            long amount
    ) {
    }

    public record WithdrawRequest(
            @Positive(message = "출금액은 양수여야 합니다")
            long amount
    ) {
    }

    public record TransferRequest(
            @NotBlank(message = "출금 계좌번호는 필수입니다")
            @Pattern(
                    regexp = "^\\d{10,14}$",
                    message = "계좌번호는 숫자만 10~14자리로 입력해주세요"
            )
            String fromAccountNumber,
            @NotBlank(message = "입금 계좌번호는 필수입니다")
            @Pattern(
                    regexp = "^\\d{10,14}$",
                    message = "계좌번호는 숫자만 10~14자리로 입력해주세요"
            )
            String toAccountNumber,
            @Positive(message = "이체 금액은 양수여야 합니다")
            long amount
    ) {
    }
}
