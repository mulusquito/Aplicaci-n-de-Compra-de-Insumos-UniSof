package com.unisof.insumos.controller;

import com.unisof.insumos.dto.CheckoutRequest;
import com.unisof.insumos.service.MercadoPagoService;
import com.unisof.insumos.service.StripeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para el flujo de checkout.
 * Soporta Mercado Pago y Stripe según configuración (app.payment-provider).
 * Requiere autenticación (vendedor).
 */
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final MercadoPagoService mercadoPagoService;
    private final StripeService stripeService;

    @Value("${app.payment-provider:mercadopago}")
    private String paymentProvider;

    public CheckoutController(MercadoPagoService mercadoPagoService, StripeService stripeService) {
        this.mercadoPagoService = mercadoPagoService;
        this.stripeService = stripeService;
    }

    /**
     * Crea una sesión de pago (Stripe o Mercado Pago según configuración).
     * Retorna init_point: URL para redirigir al cliente al checkout.
     */
    @PostMapping("/create-preference")
    public ResponseEntity<?> createPreference(@Valid @RequestBody CheckoutRequest request) {
        try {
            String url;
            if ("stripe".equalsIgnoreCase(paymentProvider)) {
                url = stripeService.createCheckoutSession(request);
            } else {
                url = mercadoPagoService.createPreference(request);
            }
            return ResponseEntity.ok(Map.of("init_point", url));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("mensaje", "Error inesperado: " + e.getMessage()));
        }
    }
}
