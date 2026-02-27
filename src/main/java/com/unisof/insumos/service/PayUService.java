package com.unisof.insumos.service;

import com.unisof.insumos.dto.CheckoutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para integrar con PayU WebCheckout.
 * Genera los datos del formulario y la firma para redirigir al checkout de PayU.
 */
@Service
@Slf4j
public class PayUService {

    private static final String SANDBOX_URL = "https://sandbox.checkout.payulatam.com/ppp-web-gateway-payu/";
    private static final String PRODUCTION_URL = "https://checkout.payulatam.com/ppp-web-gateway-payu/";

    @Value("${payu.merchant-id:}")
    private String merchantId;

    @Value("${payu.account-id:}")
    private String accountId;

    @Value("${payu.api-key:}")
    private String apiKey;

    @Value("${payu.test:1}")
    private int testMode;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Genera los datos del formulario para enviar a PayU WebCheckout.
     * El frontend debe crear un form y hacer POST a formAction.
     */
    public Map<String, Object> createCheckoutForm(CheckoutRequest request) {
        if (merchantId == null || merchantId.isBlank() || accountId == null || accountId.isBlank()
                || apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "PayU no está configurado. Agrega payu.merchant-id, payu.account-id y payu.api-key en application.properties");
        }

        String referenceCode = "UNISOF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        double amount = request.items().stream()
                .mapToDouble(i -> i.unit_price() * i.quantity())
                .sum();
        amount = Math.round(amount * 100) / 100.0;

        // IVA Colombia 19%: amount = base + tax, base = amount/1.19
        double taxReturnBase = Math.round((amount / 1.19) * 100) / 100.0;
        double tax = Math.round((amount - taxReturnBase) * 100) / 100.0;

        String description = request.items().stream()
                .map(i -> i.title() + " x" + i.quantity())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Compra UNISOF");

        String signature = buildSignature(referenceCode, amount, "COP");

        Map<String, Object> form = new LinkedHashMap<>();
        form.put("formAction", testMode == 1 ? SANDBOX_URL : PRODUCTION_URL);
        form.put("merchantId", merchantId);
        form.put("accountId", accountId);
        form.put("description", description);
        form.put("referenceCode", referenceCode);
        form.put("amount", String.format("%.2f", amount));
        form.put("tax", String.format("%.2f", tax));
        form.put("taxReturnBase", String.format("%.2f", taxReturnBase));
        form.put("currency", "COP");
        form.put("signature", signature);
        form.put("test", String.valueOf(testMode));
        form.put("responseUrl", baseUrl + "/ventas.html");
        form.put("confirmationUrl", baseUrl + "/api/webhooks/payu");
        form.put("buyerEmail", request.cliente().email());
        form.put("buyerFullName", request.cliente().nombre());
        form.put("buyerDocumentType", request.cliente().docTipo() != null ? request.cliente().docTipo() : "CC");
        form.put("buyerDocument", request.cliente().docNumero().replaceAll("[^0-9]", ""));
        form.put("telephone", request.cliente().telefono() != null ? request.cliente().telefono() : "3000000000");
        form.put("payerFullName", request.cliente().nombre());
        form.put("payerEmail", request.cliente().email());
        form.put("payerPhone", request.cliente().telefono() != null ? request.cliente().telefono() : "3000000000");
        form.put("payerDocumentType", request.cliente().docTipo() != null ? request.cliente().docTipo() : "CC");
        form.put("payerDocument", request.cliente().docNumero().replaceAll("[^0-9]", ""));

        if (request.cliente().direccion() != null && !request.cliente().direccion().isBlank()) {
            form.put("shippingAddress", request.cliente().direccion());
            form.put("shippingCity", "Bogotá");
            form.put("shippingCountry", "CO");
        }

        log.info(">>> PayU - Form creado: referenceCode={}, amount={}", referenceCode, amount);
        return form;
    }

    private String buildSignature(String referenceCode, double amount, String currency) {
        String chain = apiKey + "~" + merchantId + "~" + referenceCode + "~"
                + String.format("%.2f", amount) + "~" + currency;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(chain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Error generando firma PayU", e);
        }
    }
}
