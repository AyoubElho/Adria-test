package org.example.account.service;

import org.example.account.domain.AccountDTO;
import org.example.account.domain.CreateAccountRequest;
import org.example.account.domain.UpdateBalanceRequest;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    AccountDTO createAccount(CreateAccountRequest request);
    AccountDTO getAccountById(UUID id);
    List<AccountDTO> getAllAccounts();
    List<AccountDTO> getAccountsByCustomer(String customerReference);
    AccountDTO updateBalance(UUID id, UpdateBalanceRequest request);
    AccountDTO blockAccount(UUID id);
    AccountDTO activateAccount(UUID id);
}
