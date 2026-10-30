package org.example.account.mcp;

import org.example.account.domain.AccountDTO;
import org.example.account.domain.AccountStatus;
import org.example.account.domain.CreateAccountRequest;
import org.example.account.domain.UpdateBalanceRequest;
import org.example.account.service.AccountService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * MCP Tools for Account operations, callable by Spring AI / AI assistants.
 * Reuses the exact same AccountService business layer.
 */
@Component
public class AccountMcpTools {

    private final AccountService accountService;

    public AccountMcpTools(AccountService accountService) {
        this.accountService = accountService;
    }

    public AccountDTO getAccount(UUID accountId) {
        return accountService.getAccountById(accountId);
    }

    public List<AccountDTO> getCustomerAccounts(String customerReference) {
        return accountService.getAccountsByCustomer(customerReference);
    }

    public List<AccountDTO> listAllAccounts() {
        return accountService.getAllAccounts();
    }

    public AccountDTO createAccount(String customerReference, String currency, Double initialBalance) {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .customerReference(customerReference)
                .currency(currency)
                .initialBalance(initialBalance != null ? initialBalance : 0.0)
                .status(AccountStatus.ACTIVE)
                .build();
        return accountService.createAccount(request);
    }

    public AccountDTO updateBalance(UUID accountId, Double amount, String currency) {
        UpdateBalanceRequest request = UpdateBalanceRequest.builder()
                .amount(amount)
                .currency(currency)
                .build();
        return accountService.updateBalance(accountId, request);
    }
}
