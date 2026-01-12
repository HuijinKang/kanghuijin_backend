package com.example.remittanceservice.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountDto {

    public record CreateAccountRequest(
            @NotBlank(message = "소유자 이름은 필수입니다")
            @Size(min = 2, max = 50, message = "이름은 2~50자 사이여야 합니다")
            String ownerName,
            @NotBlank(message = "전화번호는 필수입니다")
            @Pattern(
                    regexp = "^\\+?\\d{10,15}$",
                    message = "전화번호는 숫자만 10~15자리로 입력해주세요(국가번호는 + 포함 가능)"
            )
            String phoneNumber
    ) {
    }

    public record CreateAccountResponse(
            long accountId,
            String accountNumber,
            String ownerName
    ) {
        public static CreateAccountResponse of(
                long accountId,
                String accountNumber,
                String ownerName
        ) {
            return new CreateAccountResponse(
                    accountId,
                    accountNumber,
                    ownerName
            );
        }
    }

    public record AccountDetailResponse(
            long accountId,
            String accountNumber,
            String ownerName,
            long balance,
            String status
    ) {
        public static AccountDetailResponse of(
                long accountId,
                String accountNumber,
                String ownerName,
                long balance,
                String status
        ) {
            return new AccountDetailResponse(
                    accountId,
                    accountNumber,
                    ownerName,
                    balance,
                    status
            );
        }
    }
}
