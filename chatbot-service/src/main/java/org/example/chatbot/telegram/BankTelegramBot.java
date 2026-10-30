package org.example.chatbot.telegram;

import org.example.chatbot.service.ChatbotAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

/**
 * Client Telegram Bot pour le Chatbot Agentique Bancaire.
 * S'active automatiquement lorsque la propriété telegram.bot.token est renseignée.
 */
@Component
@ConditionalOnProperty(prefix = "telegram.bot", name = "token")
public class BankTelegramBot implements SpringLongPollingBot, LongPollingUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(BankTelegramBot.class);

    private final String botToken;
    private final ChatbotAgentService chatbotAgentService;
    private final TelegramClient telegramClient;

    public BankTelegramBot(@Value("${telegram.bot.token}") String botToken,
                           ChatbotAgentService chatbotAgentService) {
        this.botToken = botToken;
        this.chatbotAgentService = chatbotAgentService;
        this.telegramClient = new OkHttpTelegramClient(botToken);
        log.info(">>> Telegram Bot initialized and ready to receive messages.");
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();

                log.info("Message reçu sur Telegram [chatId={}]: {}", chatId, messageText);

                try {
                    // Délégation de la requête à l'agent conversationnel Spring AI
                    String aiResponse = chatbotAgentService.chat(messageText);

                    // Réponse envoyée à l'utilisateur Telegram
                    SendMessage responseMessage = SendMessage.builder()
                            .chatId(chatId)
                            .text(aiResponse)
                            .build();

                    telegramClient.execute(responseMessage);
                } catch (Exception e) {
                    log.error("Erreur lors du traitement du message Telegram: {}", e.getMessage(), e);
                    try {
                        telegramClient.execute(SendMessage.builder()
                                .chatId(chatId)
                                .text("Désolé, une erreur est survenue lors du traitement de votre demande.")
                                .build());
                    } catch (Exception sendEx) {
                        log.error("Impossible d'envoyer le message d'erreur: {}", sendEx.getMessage());
                    }
                }
            }
        }
    }
}
