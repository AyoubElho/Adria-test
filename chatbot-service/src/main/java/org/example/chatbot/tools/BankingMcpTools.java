package org.example.chatbot.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Tools connectés aux micro-services bancaires (Account-Service & Transaction-Service).
 * Spring AI permet au LLM Groq d'appeler ces outils de façon agentique et autonome.
 */
@Component
public class BankingMcpTools {

    private final RestClient restClient;
    private final String accountUrl;
    private final String transactionUrl;

    public BankingMcpTools(@Value("${services.account-url:http://localhost:8081}") String accountUrl,
                           @Value("${services.transaction-url:http://localhost:8082}") String transactionUrl) {
        this.accountUrl = accountUrl;
        this.transactionUrl = transactionUrl;
        this.restClient = RestClient.builder().build();
    }

    @Tool(description = "Consulter les informations et le solde d'un compte bancaire à partir de son identifiant UUID.")
    public String getAccountDetails(String accountId) {
        try {
            return restClient.get()
                    .uri(accountUrl + "/api/accounts/" + accountId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
        } catch (Exception ex) {
            return "Erreur lors de la récupération du compte : " + ex.getMessage();
        }
    }

    @Tool(description = "Lister tous les comptes bancaires existants dans le système.")
    public String listAllAccounts() {
        try {
            return restClient.get()
                    .uri(accountUrl + "/api/accounts")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
        } catch (Exception ex) {
            return "Erreur lors de la liste des comptes : " + ex.getMessage();
        }
    }

    @Tool(description = "Créer un nouveau compte bancaire avec une référence client, une devise (EUR, MAD, USD) et un solde initial.")
    public String createAccount(String customerReference, String currency, Double initialBalance) {
        try {
            return restClient.post()
                    .uri(accountUrl + "/api/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "customerReference", customerReference,
                            "currency", currency,
                            "initialBalance", initialBalance != null ? initialBalance : 0.0,
                            "status", "ACTIVE"
                    ))
                    .retrieve()
                    .body(String.class);
        } catch (Exception ex) {
            return "Erreur lors de la création du compte : " + ex.getMessage();
        }
    }

    @Tool(description = "Enregistrer une transaction ou un paiement sur un compte bancaire actif.")
    public String recordTransaction(String accountId, Double amount, String currency, String merchantCategory, String country) {
        try {
            return restClient.post()
                    .uri(transactionUrl + "/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "accountId", accountId,
                            "amount", amount,
                            "currency", currency,
                            "merchantCategory", merchantCategory != null ? merchantCategory : "RETAIL",
                            "country", country != null ? country : "MA"
                    ))
                    .retrieve()
                    .body(String.class);
        } catch (Exception ex) {
            return "Erreur lors de l'enregistrement de la transaction : " + ex.getMessage();
        }
    }

    @Tool(description = "Effectuer un virement bancaire entre un compte source et un compte destinataire.")
    public String executeTransfer(String sourceAccountId, String targetAccountId, Double amount, String currency) {
        try {
            return restClient.post()
                    .uri(transactionUrl + "/api/transactions/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "sourceAccountId", sourceAccountId,
                            "targetAccountId", targetAccountId,
                            "amount", amount,
                            "currency", currency,
                            "merchantCategory", "TRANSFER",
                            "country", "MA"
                    ))
                    .retrieve()
                    .body(String.class);
        } catch (Exception ex) {
            return "Erreur lors du virement bancaire : " + ex.getMessage();
        }
    }

    @Tool(description = "Consulter l'historique des transactions d'un compte bancaire à partir de son UUID.")
    public String getTransactionsByAccount(String accountId) {
        try {
            return restClient.get()
                    .uri(transactionUrl + "/api/transactions/account/" + accountId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
        } catch (Exception ex) {
            return "Erreur lors de la récupération des transactions : " + ex.getMessage();
        }
    }
}
