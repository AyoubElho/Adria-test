package org.example.account.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "customerReference is required")
    private String customerReference;

    @NotBlank(message = "currency is required")
    @Size(min = 3, max = 3, message = "currency must be a 3-letter code (e.g., EUR, USD, MAD)")
    private String currency;

    @NotNull(message = "initialBalance is required")
    @PositiveOrZero(message = "initialBalance must be positive or zero")
    private Double initialBalance;

    private AccountStatus status;
}
