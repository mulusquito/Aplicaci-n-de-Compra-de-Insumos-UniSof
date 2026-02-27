package com.unisof.insumos.controller;

import com.unisof.insumos.dto.CheckoutRequest;
import com.unisof.insumos.service.MercadoPagoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para el flujo de checkout con Mercado Pago.
 * Requiere autenticación (vendedor).
 */
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final MercadoPagoService mercadoPagoService;

    public CheckoutController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    /**
     * Crea una preferencia de pago en Mercado Pago.
     * Retorna init_point: URL para redirigir al cliente al checkout.
     */
    @PostMapping("/create-preference")
    public ResponseEntity<?> createPreference(@Valid @RequestBody CheckoutRequest request) {
        try {
            String initPoint = mercadoPagoService.createPreference(request);
            return ResponseEntity.ok(Map.of("init_point", initPoint));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("mensaje", "Error inesperado: " + e.getMessage()));
        }
    }
}
