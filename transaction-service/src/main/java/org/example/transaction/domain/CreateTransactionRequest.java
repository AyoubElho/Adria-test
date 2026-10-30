package org.example.transaction.domain;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

    @NotNull(message = "accountId is required")
    private UUID accountId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be strictly positive")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    @Size(min = 3, max = 3, message = "currency must be a 3-letter code")
    private String currency;

    @NotBlank(message = "merchantCategory is required")
    private String merchantCategory;

    @NotBlank(message = "country is required")
    @Size(min = 2, max = 2, message = "country must be a 2-letter ISO code (e.g. MA, FR, US)")
    private String country;
}
