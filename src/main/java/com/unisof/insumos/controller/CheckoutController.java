package com.unisof.insumos.controller;

import com.unisof.insumos.dto.CheckoutRequest;
import com.unisof.insumos.service.AuditoriaService;
import com.unisof.insumos.service.MercadoPagoService;
import com.unisof.insumos.service.StripeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para el flujo de checkout.
 * Soporta Mercado Pago y Stripe según configuración (app.payment-provider).
 * SCRUM-64: Inicio de sesión de pago registrado en auditoría módulo VENTAS.
 */
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final MercadoPagoService mercadoPagoService;
    private final StripeService stripeService;
    private final AuditoriaService auditoriaService;  // SCRUM-64

    @Value("${app.payment-provider:mercadopago}")
    private String paymentProvider;

    public CheckoutController(MercadoPagoService mercadoPagoService,
                              StripeService stripeService,
                              AuditoriaService auditoriaService) {
        this.mercadoPagoService = mercadoPagoService;
        this.stripeService = stripeService;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Crea una sesión de pago (Stripe o Mercado Pago).
     * SCRUM-64: Registra CREAR_PAGO en módulo VENTAS.
     */
    @PostMapping("/create-preference")
    public ResponseEntity<?> createPreference(
            @Valid @RequestBody CheckoutRequest request,
            HttpServletRequest httpRequest) {
        try {
            String url;
            if ("stripe".equalsIgnoreCase(paymentProvider)) {
                url = stripeService.createCheckoutSession(request);
            } else {
                url = mercadoPagoService.createPreference(request);
            }

            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_CREAR_PAGO, AuditoriaService.MOD_VENTAS,
                    "Sesión de pago creada vía " + paymentProvider.toUpperCase(),
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_EXITOSO,
                    "proveedor=" + paymentProvider
            );
            return ResponseEntity.ok(Map.of("init_point", url));
        } catch (IllegalStateException e) {
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_CREAR_PAGO, AuditoriaService.MOD_VENTAS,
                    "Intento fallido de crear sesión de pago",
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_FALLIDO, "Error: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_CREAR_PAGO, AuditoriaService.MOD_VENTAS,
                    "Error inesperado al crear sesión de pago",
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_FALLIDO, "Error: " + e.getMessage()
            );
            return ResponseEntity.internalServerError()
                    .body(Map.of("mensaje", "Error inesperado: " + e.getMessage()));
        }
    }
}
