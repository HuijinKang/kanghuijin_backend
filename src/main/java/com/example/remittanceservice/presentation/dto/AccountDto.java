package com.example.remittanceservice.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountDto {

    public record CreateAccountRequest(
            @NotBlank
            @Pattern(
                    regexp = "^\\d{10,14}$",
                    message = "계좌번호는 숫자만 10~14자리로 입력해주세요."
            )
            String accountNumber,
            @NotBlank String ownerName
    ) {
    }

    public record CreateAccountResponse(
            long accountId,
            String accountNumber,
            String ownerName
    ) {
        public static CreateAccountResponse of(long accountId, String accountNumber, String ownerName) {
            return new CreateAccountResponse(accountId, accountNumber, ownerName);
        }
    }
}
