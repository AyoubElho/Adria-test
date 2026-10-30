package org.example.chatbot.service;

import org.example.chatbot.tools.BankingMcpTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Service agentique basé sur Spring AI ChatClient.
 * Invoque les tools bancaires de manière autonome en fonction des demandes utilisateurs.
 */
@Service
public class ChatbotAgentService {

    private final ChatClient chatClient;

    public ChatbotAgentService(ChatClient.Builder chatClientBuilder, BankingMcpTools bankingMcpTools) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        Tu es l'assistant conversationnel intelligent de la banque.
                        Tu es connecté aux serveurs de gestion des comptes et des transactions bancaires.
                        Tu as à ta disposition des outils (tools) pour :
                        1. Lister les comptes et consulter les détails et soldes d'un compte précis par son identifiant UUID.
                        2. Créer un nouveau compte bancaire (référence client, devise EUR/MAD/USD, solde).
                        3. Enregistrer des transactions de paiement.
                        4. Effectuer des virements entre deux comptes bancaires.
                        5. Consulter l'historique des transactions d'un compte.

                        Règles :
                        - Utilise systématiquement tes outils pour interroger et manipuler les données réelles.
                        - Réponds toujours de façon claire, polie et professionnelle en français.
                        - Résume clairement les résultats (soldes, devises, statut) à l'utilisateur.
                        """)
                .defaultTools(bankingMcpTools)
                .build();
    }

    public String chat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}
