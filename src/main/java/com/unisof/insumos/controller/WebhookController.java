package com.unisof.insumos.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Webhook para recibir notificaciones de Mercado Pago.
 * Mercado Pago envía POST cuando cambia el estado de un pago.
 * La URL debe ser pública (ej: https://tu-ngrok.ngrok-free.dev/api/webhooks/mercadopago)
 */
@RestController
@RequestMapping("/api/webhooks")
@Slf4j
public class WebhookController {

    /**
     * Recibe notificaciones de Mercado Pago.
     * Responde 200 OK para confirmar recepción.
     */
    @PostMapping("/mercadopago")
    public ResponseEntity<Void> mercadopago(@RequestBody(required = false) String body,
                                            @RequestParam(value = "topic", required = false) String topic,
                                            @RequestParam(value = "id", required = false) String id) {
        log.info(">>> Mercado Pago webhook: topic={}, id={}", topic, id);
        return ResponseEntity.ok().build();
    }

    /**
     * Recibe confirmaciones de PayU tras el pago.
     * PayU envía POST con el estado de la transacción.
     */
    @PostMapping("/payu")
    public ResponseEntity<Void> payu(@RequestBody(required = false) String body) {
        log.info(">>> PayU webhook: {}", body != null ? body.substring(0, Math.min(200, body.length())) : "empty");
        return ResponseEntity.ok().build();
    }

    /**
     * Recibe confirmaciones de Epayco tras el pago.
     * Epayco envía POST con ref_payco y estado de la transacción.
     */
    @PostMapping("/epayco")
    public ResponseEntity<Void> epayco(@RequestBody(required = false) String body,
                                       @RequestParam(value = "ref_payco", required = false) String refPayco) {
        log.info(">>> Epayco webhook: ref_payco={}, body={}", refPayco,
                body != null ? body.substring(0, Math.min(200, body.length())) : "empty");
        return ResponseEntity.ok().build();
    }
}
