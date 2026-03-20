package com.unisof.insumos.controller;

import com.unisof.insumos.service.ChatService;
import com.unisof.insumos.service.OpenAiChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Chatbot Nova: intenta OpenAI (si está configurada) y si no, FAQ por reglas.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final OpenAiChatService openAiChatService;

    public ChatController(ChatService chatService, OpenAiChatService openAiChatService) {
        this.chatService = chatService;
        this.openAiChatService = openAiChatService;
    }

    /**
     * POST /api/chat
     * Body: { "mensaje": "..." }
     * Response: { "respuesta": "..." }
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> body) {
        String mensaje = body != null ? body.get("mensaje") : null;
        String respuesta = openAiChatService.responder(mensaje)
                .orElseGet(() -> chatService.responder(mensaje));
        return ResponseEntity.ok(Map.of("respuesta", respuesta));
    }
}
