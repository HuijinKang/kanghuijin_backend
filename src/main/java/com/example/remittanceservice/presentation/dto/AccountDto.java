package com.example.remittanceservice.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountDto {

    public record CreateAccountRequest(
            @NotBlank String accountNumber,
            @NotBlank String ownerName
    ) {
    }

    public record CreateAccountResponse(
            long accountId,
            String accountNumber,
            String ownerName
    ) {
        public static CreateAccountResponse notImplemented() {
            return new CreateAccountResponse(0L, "NOT_IMPLEMENTED", "NOT_IMPLEMENTED");
        }
    }
}
