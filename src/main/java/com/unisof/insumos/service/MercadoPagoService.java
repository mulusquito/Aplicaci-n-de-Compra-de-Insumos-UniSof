package com.unisof.insumos.service;

import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.unisof.insumos.dto.CheckoutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio para integrar con Mercado Pago usando el SDK oficial.
 * Crea preferencias de pago (Checkout Pro) y retorna la URL para que el cliente pague.
 *
 * @see <a href="https://github.com/mercadopago/sdk-java">Mercado Pago SDK Java</a>
 * @see <a href="https://www.mercadopago.com.ar/developers/es/docs/checkout-pro">Checkout Pro</a>
 */
@Service
@Slf4j
public class MercadoPagoService {

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final PreferenceClient preferenceClient = new PreferenceClient();

    /**
     * Crea una preferencia de pago en Mercado Pago.
     * Retorna el init_point (o sandbox_init_point) para redirigir al cliente al checkout.
     */
    public String createPreference(CheckoutRequest request) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException(
                    "Mercado Pago no está configurado. Agrega mercadopago.access-token en application.properties");
        }

        List<PreferenceItemRequest> items = request.items().stream()
                .map(item -> PreferenceItemRequest.builder()
                        .id(item.id())
                        .title(item.title())
                        .quantity(item.quantity())
                        .unitPrice(BigDecimal.valueOf(item.unit_price()))
                        .currencyId(item.currency_id() != null && !item.currency_id().isBlank() ? item.currency_id() : "COP")
                        .build())
                .toList();

        double totalAmount = request.items().stream()
                .mapToDouble(item -> item.unit_price() * item.quantity())
                .sum();
        if (totalAmount <= 0) {
            throw new IllegalStateException("El total debe ser mayor a cero. Revisa los precios de los productos.");
        }

        PreferencePayerRequest.PreferencePayerRequestBuilder payerBuilder = PreferencePayerRequest.builder()
                .email(request.cliente().email())
                .name(request.cliente().nombre());

        if (request.cliente().docNumero() != null && !request.cliente().docNumero().isBlank()) {
            String docTipo = request.cliente().docTipo() != null ? request.cliente().docTipo() : "CC";
            String docNumero = request.cliente().docNumero().replaceAll("[^0-9]", "");
            payerBuilder.identification(
                    IdentificationRequest.builder()
                            .type(docTipo)
                            .number(docNumero)
                            .build()
            );
        }

        String successUrl = baseUrl + "/ventas.html?payment=success";
        String failureUrl = baseUrl + "/ventas.html?payment=failure";
        String pendingUrl = baseUrl + "/ventas.html?payment=pending";
        String notificationUrl = baseUrl + "/api/webhooks/mercadopago";

        log.info("Mercado Pago - back_urls: success={}, failure={}, pending={}, total={}",
                successUrl, failureUrl, pendingUrl, totalAmount);

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(successUrl)
                .failure(failureUrl)
                .pending(pendingUrl)
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .payer(payerBuilder.build())
                .backUrls(backUrls)
                .autoReturn("approved")
                .notificationUrl(notificationUrl)
                .build();

        try {
            Preference preference = preferenceClient.create(preferenceRequest);

            String initPoint = preference.getSandboxInitPoint();
            if (initPoint == null || initPoint.isBlank()) {
                initPoint = preference.getInitPoint();
            }
            if (initPoint == null || initPoint.isBlank()) {
                throw new IllegalStateException("Mercado Pago no devolvió URL de pago");
            }

            log.info("Mercado Pago - Preferencia creada: id={}", preference.getId());
            return initPoint;
        } catch (MPApiException e) {
            String msg = parseApiError(e);
            throw new IllegalStateException("Mercado Pago rechazó la solicitud: " + msg);
        } catch (MPException e) {
            throw new IllegalStateException("Error al comunicarse con Mercado Pago: " + e.getMessage());
        }
    }

    private String parseApiError(MPApiException e) {
        if (e.getApiResponse() != null && e.getApiResponse().getContent() != null) {
            String content = e.getApiResponse().getContent();
            if (content.contains("\"message\"")) {
                int start = content.indexOf("\"message\":\"") + 10;
                int end = content.indexOf("\"", start);
                if (start > 9 && end > start) {
                    return content.substring(start, end);
                }
            }
            if (content.contains("\"description\"")) {
                int start = content.indexOf("\"description\":\"") + 15;
                int end = content.indexOf("\"", start);
                if (start > 14 && end > start) {
                    return content.substring(start, end);
                }
            }
        }
        return e.getMessage();
    }
}
