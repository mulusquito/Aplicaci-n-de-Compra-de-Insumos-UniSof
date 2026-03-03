package com.unisof.insumos.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.param.checkout.SessionCreateParams;
import com.unisof.insumos.dto.CheckoutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para integrar Stripe Checkout.
 * Crea una Checkout Session y retorna la URL para redirigir al cliente.
 *
 * @see <a href="https://stripe.com/docs/checkout">Stripe Checkout</a>
 */
@Service
@Slf4j
public class StripeService {

    @Value("${stripe.secret-key:}")
    private String secretKey;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        if (secretKey != null && !secretKey.isBlank()) {
            Stripe.apiKey = secretKey.trim();
            log.info("Stripe configurado correctamente");
        }
    }

    /**
     * Crea una Checkout Session en Stripe.
     * Retorna la URL para redirigir al cliente al pago.
     */
    public String createCheckoutSession(CheckoutRequest request) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "Stripe no está configurado. Agrega stripe.secret-key en application.properties");
        }

        double totalAmount = request.items().stream()
                .mapToDouble(item -> item.unit_price() * item.quantity())
                .sum();
        if (totalAmount <= 0) {
            throw new IllegalStateException("El total debe ser mayor a cero. Revisa los precios de los productos.");
        }

        String successUrl = baseUrl + "/ventas.html?payment=success";
        String cancelUrl = baseUrl + "/ventas.html?payment=cancelled";

        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        for (var item : request.items()) {
            String currency = (item.currency_id() != null && !item.currency_id().isBlank())
                    ? item.currency_id().toLowerCase() : "cop";
            // Stripe: cantidad en la unidad más pequeña (centavos para COP)
            long unitAmount = (long) Math.round(item.unit_price() * 100);

            SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency(currency)
                    .setProductData(
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(item.title())
                                    .build()
                    )
                    .setUnitAmount(unitAmount)
                    .build();

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPriceData(priceData)
                    .setQuantity((long) item.quantity())
                    .build();
            lineItems.add(lineItem);
        }

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addAllLineItem(lineItems);

        if (request.cliente().email() != null && !request.cliente().email().isBlank()) {
            paramsBuilder.setCustomerEmail(request.cliente().email());
        }

        try {
            com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.create(
                    paramsBuilder.build()
            );
            String url = session.getUrl();
            if (url == null || url.isBlank()) {
                throw new IllegalStateException("Stripe no devolvió URL de pago");
            }
            log.info("Stripe - Checkout Session creada: id={}", session.getId());
            return url;
        } catch (StripeException e) {
            throw new IllegalStateException("Stripe rechazó la solicitud: " + e.getMessage());
        }
    }
}
