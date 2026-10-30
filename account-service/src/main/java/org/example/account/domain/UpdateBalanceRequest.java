package org.example.account.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBalanceRequest {

    @NotNull(message = "amount is required")
    private Double amount; // Positive to credit, negative to debit

    @NotBlank(message = "currency is required")
    private String currency;
}
