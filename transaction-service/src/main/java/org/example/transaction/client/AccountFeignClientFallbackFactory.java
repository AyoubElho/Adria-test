package org.example.transaction.client;

import org.example.transaction.domain.AccountDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;


@Component
public class AccountFeignClientFallbackFactory implements FallbackFactory<AccountFeignClient> {

    private static final Logger log = LoggerFactory.getLogger(AccountFeignClientFallbackFactory.class);

    @Override
    public AccountFeignClient create(Throwable cause) {
        return new AccountFeignClient() {
            @Override
            public AccountDTO getAccountById(UUID id) {
                log.error("Fallback triggered for getAccountById({}): {}", id, cause.getMessage());
                throw new IllegalStateException("Service de compte temporairement indisponible (Resilience4j Circuit Breaker actif). Cause: " + cause.getMessage(), cause);
            }

            @Override
            public AccountDTO updateBalance(UUID id, Map<String, Object> request) {
                log.error("Fallback triggered for updateBalance({}): {}", id, cause.getMessage());
                throw new IllegalStateException("Impossible de mettre à jour le solde du compte (Service account-service indisponible). Cause: " + cause.getMessage(), cause);
            }
        };
    }
}
