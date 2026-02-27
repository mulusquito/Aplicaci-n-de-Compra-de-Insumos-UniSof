package com.unisof.insumos.service;

import com.unisof.insumos.dto.CheckoutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para integrar con Epayco Checkout.
 * Genera los datos para el checkout.js del frontend (modal de pago).
 */
@Service
@Slf4j
public class EpaycoService {

    @Value("${epayco.public-key:}")
    private String publicKey;

    @Value("${epayco.test:true}")
    private boolean testMode;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Genera los datos del checkout para Epayco.
     * El frontend usa checkout.js con handler.open(data).
     */
    public Map<String, Object> createCheckoutData(CheckoutRequest request) {
        if (publicKey == null || publicKey.isBlank()) {
            throw new IllegalStateException(
                    "Epayco no está configurado. Agrega epayco.public-key en application.properties");
        }

        String invoice = "UNISOF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        double amount = request.items().stream()
                .mapToDouble(i -> i.unit_price() * i.quantity())
                .sum();
        amount = Math.round(amount * 100) / 100.0;

        double taxBase = Math.round((amount / 1.19) * 100) / 100.0;
        double tax = Math.round((amount - taxBase) * 100) / 100.0;

        String description = request.items().stream()
                .map(i -> i.title() + " x" + i.quantity())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Compra UNISOF");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("key", publicKey);
        data.put("test", testMode);
        data.put("name", description);
        data.put("description", description);
        data.put("invoice", invoice);
        data.put("currency", "cop");
        data.put("amount", String.format("%.0f", amount));
        data.put("tax_base", String.format("%.0f", taxBase));
        data.put("tax", String.format("%.0f", tax));
        data.put("tax_ico", "0");
        data.put("country", "co");
        data.put("lang", "es");
        data.put("external", "false");
        data.put("confirmation", baseUrl + "/api/webhooks/epayco");
        data.put("response", baseUrl + "/ventas.html");
        data.put("name_billing", request.cliente().nombre());
        data.put("address_billing", request.cliente().direccion() != null ? request.cliente().direccion() : "N/A");
        data.put("type_doc_billing", request.cliente().docTipo() != null ? request.cliente().docTipo().toLowerCase() : "cc");
        data.put("mobilephone_billing", request.cliente().telefono() != null ? request.cliente().telefono() : "3000000000");
        data.put("number_doc_billing", request.cliente().docNumero().replaceAll("[^0-9]", ""));
        data.put("email_billing", request.cliente().email());

        log.info(">>> Epayco - Checkout creado: invoice={}, amount={}", invoice, amount);
        return data;
    }
}
