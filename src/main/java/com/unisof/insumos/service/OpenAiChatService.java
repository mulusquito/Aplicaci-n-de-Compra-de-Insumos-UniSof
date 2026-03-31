package com.unisof.insumos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
public class OpenAiChatService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${app.openai.enabled:true}")
    private boolean enabled;

    @Value("${app.openai.api-key:}")
    private String apiKey;

    @Value("${app.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${app.openai.max-tokens:600}")
    private int maxTokens;

    public OpenAiChatService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        this.restTemplate = new RestTemplate(factory);
    }

    @PostConstruct
    public void logEstadoOpenAi() {
        if (!enabled) {
            log.info("Nova/OpenAI: desactivado (app.openai.enabled=false). Solo respuestas FAQ.");
        } else if (apiKey == null || apiKey.isBlank()) {
            log.info("Nova/OpenAI: no hay clave. Define OPENAI_API_KEY en el entorno antes de arrancar, o app.openai.api-key en application.properties. Mientras tanto solo FAQ.");
        } else {
            log.info("Nova/OpenAI: listo — modelo={}, clave configurada (longitud {}).", model, apiKey.trim().length());
        }
    }

    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    public Optional<String> responder(String mensajeUsuario) {
        if (!isConfigured()) {
            return Optional.empty();
        }
        if (mensajeUsuario == null || mensajeUsuario.isBlank()) {
            return Optional.empty();
        }

        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", model.trim());
            root.put("max_tokens", maxTokens);
            root.put("temperature", 0.65);

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode system = objectMapper.createObjectNode();
            system.put("role", "system");
            system.put("content", SYSTEM_PROMPT_UNISOF);
            messages.add(system);
            ObjectNode user = objectMapper.createObjectNode();
            user.put("role", "user");
            user.put("content", mensajeUsuario.trim());
            messages.add(user);
            root.set("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey.trim());

            String jsonBody = objectMapper.writeValueAsString(root);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    OPENAI_URL, HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("OpenAI: respuesta HTTP {}", response.getStatusCode());
                return Optional.empty();
            }

            JsonNode tree = objectMapper.readTree(response.getBody());
            JsonNode choices = tree.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                log.warn("OpenAI: sin choices en JSON");
                return Optional.empty();
            }
            String content = choices.get(0).path("message").path("content").asText("");
            if (content.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(content.trim());
        } catch (RestClientException e) {
            log.warn("OpenAI: error de red o API — {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("OpenAI: error al procesar — {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static final String SYSTEM_PROMPT_UNISOF =
            "Eres Nova, asistente virtual del sistema UNISOF (aplicacion web de compra de insumos, "
                    + "catalogo de ropa y gestion de pedidos).\n\n"
                    + "Ambito obligatorio:\n"
                    + "- Solo respondes sobre UNISOF: uso de la aplicacion, flujos de compra, login, roles, "
                    + "pedidos, catalogo, pagos configurados, sesion, recuperacion de contrasena.\n"
                    + "- Si la pregunta no tiene relacion con UNISOF, responde brevemente que solo puedes ayudar "
                    + "con dudas sobre UNISOF y sugiere reformular.\n\n"
                    + "Hechos del sistema (no inventes otras funciones):\n"
                    + "- Pagina principal con catalogo (en home es informativo); compra con carrito en modulos "
                    + "ventas/compras con sesion.\n"
                    + "- Login con usuario y contrasena; luego 2FA con codigo de 6 digitos por correo "
                    + "(revisar spam; expira en unos minutos).\n"
                    + "- Roles: Administrador, Vendedor, Jefe de compras. Admin registra usuarios en Gestion de personal.\n"
                    + "- Ordenes: modulo con listado y detalle Ver pedido.\n"
                    + "- Panel admin: dashboard, filtros por fechas, exportar PDF.\n"
                    + "- Sesion puede cerrarse por inactividad; puede haber aviso previo.\n"
                    + "- Pagos: Stripe o Mercado Pago segun configuracion.\n"
                    + "- Recuperacion de contrasena por correo.\n\n"
                    + "Estilo: espanol, claro y conciso (2-6 frases salvo que pidan mas). "
                    + "No pidas contrasenas ni datos sensibles. No ejecutes acciones en base de datos; solo orientas.";
}
