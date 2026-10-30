package org.example.transaction.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.example.transaction.client.AccountFeignClient;
import org.example.transaction.domain.AccountDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Service encapsulating OpenFeign communication with account-service,
 * enhanced with Resilience4j @CircuitBreaker and @Retry annotations.
 */
@Service
public class AccountServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceClient.class);

    private final AccountFeignClient accountFeignClient;

    public AccountServiceClient(AccountFeignClient accountFeignClient) {
        this.accountFeignClient = accountFeignClient;
    }

    @CircuitBreaker(name = "accountServiceCircuitBreaker", fallbackMethod = "getAccountFallback")
    @Retry(name = "accountServiceRetry")
    public AccountDTO getAccount(UUID accountId) {
        return accountFeignClient.getAccountById(accountId);
    }

    @CircuitBreaker(name = "accountServiceCircuitBreaker", fallbackMethod = "updateBalanceFallback")
    @Retry(name = "accountServiceRetry")
    public AccountDTO updateBalance(UUID accountId, double amount, String currency) {
        return accountFeignClient.updateBalance(accountId, Map.of("amount", amount, "currency", currency));
    }

    // Resilience4J Fallbacks
    public AccountDTO getAccountFallback(UUID accountId, Throwable t) {
        log.error("Resilience4j Circuit Breaker opened for getAccount({}): {}", accountId, t.getMessage());
        throw new IllegalStateException("Tolérance aux pannes active: account-service est indisponible pour le compte " + accountId + ". " + t.getMessage(), t);
    }

    public AccountDTO updateBalanceFallback(UUID accountId, double amount, String currency, Throwable t) {
        log.error("Resilience4j Circuit Breaker opened for updateBalance({}): {}", accountId, t.getMessage());
        throw new IllegalStateException("Tolérance aux pannes active: mise à jour du solde impossible pour le compte " + accountId + ". " + t.getMessage(), t);
    }
}
