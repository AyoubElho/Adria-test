package org.example.chatbot.web;

import org.example.chatbot.service.ChatbotAgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Client REST pour tester le Chatbot Agentique.
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatbotRestController {

    private final ChatbotAgentService chatbotAgentService;

    public ChatbotRestController(ChatbotAgentService chatbotAgentService) {
        this.chatbotAgentService = chatbotAgentService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> chatPost(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        if (message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le message ne peut pas être vide."));
        }
        String response = chatbotAgentService.chat(message);
        return ResponseEntity.ok(Map.of(
                "query", message,
                "response", response
        ));
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> chatGet(@RequestParam String message) {
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le paramètre message est obligatoire."));
        }
        String response = chatbotAgentService.chat(message);
        return ResponseEntity.ok(Map.of(
                "query", message,
                "response", response
        ));
    }
}
