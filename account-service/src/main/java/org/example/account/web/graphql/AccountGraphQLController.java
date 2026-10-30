package org.example.account.web.graphql;

import org.example.account.domain.AccountDTO;
import org.example.account.domain.CreateAccountRequest;
import org.example.account.domain.UpdateBalanceRequest;
import org.example.account.service.AccountService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class AccountGraphQLController {

    private final AccountService accountService;

    public AccountGraphQLController(AccountService accountService) {
        this.accountService = accountService;
    }

    @QueryMapping
    public AccountDTO accountById(@Argument UUID id) {
        return accountService.getAccountById(id);
    }

    @QueryMapping
    public List<AccountDTO> allAccounts() {
        return accountService.getAllAccounts();
    }

    @QueryMapping
    public List<AccountDTO> accountsByCustomer(@Argument String customerReference) {
        return accountService.getAccountsByCustomer(customerReference);
    }

    @MutationMapping
    public AccountDTO createAccount(@Argument CreateAccountRequest input) {
        return accountService.createAccount(input);
    }

    @MutationMapping
    public AccountDTO updateBalance(@Argument UUID id, @Argument UpdateBalanceRequest input) {
        return accountService.updateBalance(id, input);
    }

    @MutationMapping
    public AccountDTO blockAccount(@Argument UUID id) {
        return accountService.blockAccount(id);
    }

    @MutationMapping
    public AccountDTO activateAccount(@Argument UUID id) {
        return accountService.activateAccount(id);
    }
}
