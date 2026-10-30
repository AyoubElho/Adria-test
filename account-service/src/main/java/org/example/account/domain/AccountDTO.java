package org.example.account.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private UUID id;
    private String customerReference;
    private AccountStatus status;
    private String currency;
    private double balance;

    public static AccountDTO fromEntity(Account account) {
        if (account == null) return null;
        return AccountDTO.builder()
                .id(account.getId())
                .customerReference(account.getCustomerReference())
                .status(account.getStatus())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .build();
    }
}
