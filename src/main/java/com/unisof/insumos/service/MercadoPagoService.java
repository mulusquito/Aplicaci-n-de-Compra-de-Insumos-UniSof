package com.unisof.insumos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unisof.insumos.dto.CheckoutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para integrar con la API de Mercado Pago.
 * Crea preferencias de pago (Checkout Pro) y obtiene la URL para que el cliente pague.
 */
@Service
@Slf4j
public class MercadoPagoService {

    private static final String MP_API = "https://api.mercadopago.com/checkout/preferences";

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Crea una preferencia de pago en Mercado Pago.
     * Retorna el init_point (URL) para redirigir al cliente al checkout de MP.
     */
    public String createPreference(CheckoutRequest request) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException(
                    "Mercado Pago no está configurado. Agrega mercadopago.access-token en application.properties");
        }

        List<Map<String, Object>> items = request.items().stream()
                .map(item -> Map.<String, Object>of(
                        "id", item.id(),
                        "title", item.title(),
                        "quantity", item.quantity(),
                        "unit_price", item.unit_price(),
                        "currency_id", item.currency_id() != null && !item.currency_id().isBlank() ? item.currency_id() : "COP"
                ))
                .collect(Collectors.toList());

        double totalAmount = request.items().stream()
                .mapToDouble(item -> item.unit_price() * item.quantity())
                .sum();
        if (totalAmount <= 0) {
            throw new IllegalStateException("El total debe ser mayor a cero. Revisa los precios de los productos.");
        }

        // Payer con identificación para Mercado Pago
        Map<String, Object> payer = new java.util.HashMap<>(Map.of(
                "email", request.cliente().email(),
                "name", request.cliente().nombre()
        ));
        if (request.cliente().docNumero() != null && !request.cliente().docNumero().isBlank()) {
            String docTipo = request.cliente().docTipo() != null ? request.cliente().docTipo() : "CC";
            payer.put("identification", Map.of("type", docTipo, "number", request.cliente().docNumero().replaceAll("[^0-9]", "")));
        }

        // back_urls: URLs completas (Mercado Pago puede rechazar localhost en algunos casos)
        String successUrl = baseUrl + "/ventas.html?payment=success";
        String failureUrl = baseUrl + "/ventas.html?payment=failure";
        String pendingUrl = baseUrl + "/ventas.html?payment=pending";

        Map<String, Object> backUrls = Map.of(
                "success", successUrl,
                "failure", failureUrl,
                "pending", pendingUrl
        );

        Map<String, Object> body = new java.util.HashMap<>(Map.of(
                "items", items,
                "payer", payer,
                "back_urls", backUrls,
                "auto_return", "approved"
        ));
        body.put("notification_url", baseUrl + "/api/webhooks/mercadopago");

        log.info(">>> Mercado Pago - Request back_urls: success={}, failure={}, pending={}, total_calculado={}", successUrl, failureUrl, pendingUrl, totalAmount);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/json;charset=UTF-8"));
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    MP_API,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("Error al crear preferencia en Mercado Pago");
            }

            String responseBody = response.getBody();
            log.info(">>> Mercado Pago - Respuesta completa: {}", responseBody);

            try {
                return extractInitPoint(responseBody);
            } catch (Exception ex) {
                throw new IllegalStateException("Error al procesar respuesta: " + ex.getMessage());
            }
        } catch (HttpClientErrorException e) {
            String errorMsg = e.getResponseBodyAsString();
            throw new IllegalStateException(
                    "Mercado Pago rechazó la solicitud: " + parseMpError(errorMsg));
        }
    }

    private String parseMpError(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode msg = root.path("message");
            if (!msg.isMissingNode()) return msg.asText();
            JsonNode cause = root.path("cause");
            if (!cause.isMissingNode() && cause.isArray() && cause.size() > 0) {
                return cause.get(0).path("description").asText("");
            }
        } catch (Exception ignored) { }
        return "Revisa los datos del cliente y los productos.";
    }

    private String extractInitPoint(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode initPoint = root.path("sandbox_init_point");
        if (initPoint.isMissingNode() || initPoint.asText().isEmpty()) {
            initPoint = root.path("init_point");
        }
        String url = initPoint.asText();
        if (url.isEmpty()) {
            throw new IllegalStateException("Mercado Pago no devolvió URL de pago");
        }
        return url;
    }
}
