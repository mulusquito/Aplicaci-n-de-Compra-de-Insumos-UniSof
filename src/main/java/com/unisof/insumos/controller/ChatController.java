package com.unisof.insumos.controller;

import com.unisof.insumos.service.AuditoriaService;
import com.unisof.insumos.service.ChatService;
import com.unisof.insumos.service.OpenAiChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Chatbot Nova: intenta OpenAI (si está configurada) y si no, FAQ por reglas.
 * SCRUM-64: Cada consulta al chatbot queda registrada en auditoría módulo CHATBOT.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final OpenAiChatService openAiChatService;
    private final AuditoriaService auditoriaService;  // SCRUM-64

    public ChatController(ChatService chatService,
                          OpenAiChatService openAiChatService,
                          AuditoriaService auditoriaService) {
        this.chatService = chatService;
        this.openAiChatService = openAiChatService;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Procesa una consulta al chatbot Nova.
     * SCRUM-64: Registra CONSULTA_NOVA en módulo CHATBOT.
     * POST /api/chat
     * Body: { "mensaje": "..." }
     * Response: { "respuesta": "..." }
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {

        String mensaje = body != null ? body.get("mensaje") : null;

        boolean usaOpenAi = false;
        String respuesta;
        var openAiRespuesta = openAiChatService.responder(mensaje);
        if (openAiRespuesta.isPresent()) {
            respuesta = openAiRespuesta.get();
            usaOpenAi = true;
        } else {
            respuesta = chatService.responder(mensaje);
        }

        // SCRUM-64: auditoría de consultas al chatbot
        String[] ui = auditoriaService.obtenerUsuarioInfo();
        String mensajeResumen = (mensaje != null && mensaje.length() > 80)
                ? mensaje.substring(0, 80) + "..." : mensaje;
        auditoriaService.registrar(
                AuditoriaService.ACC_CONSULTA_NOVA, AuditoriaService.MOD_CHATBOT,
                "Consulta al chatbot Nova: \"" + mensajeResumen + "\"",
                ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                AuditoriaService.RES_EXITOSO,
                "motor=" + (usaOpenAi ? "OpenAI GPT-4o-mini" : "FAQ-reglas")
        );

        return ResponseEntity.ok(Map.of("respuesta", respuesta));
    }
}
