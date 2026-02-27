package com.unisof.insumos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request para crear una preferencia de pago en Mercado Pago.
 */
public record CheckoutRequest(
        @NotEmpty(message = "Debe incluir al menos un producto")
        List<CheckoutItem> items,

        @NotNull(message = "Datos del cliente son requeridos")
        @Valid
        ClienteData cliente
) {
    public record CheckoutItem(
            String id,
            String title,
            int quantity,
            double unit_price,
            String currency_id
    ) {}

    public record ClienteData(
            @NotBlank(message = "Nombre es requerido")
            String nombre,
            @NotBlank(message = "Email es requerido")
            String email,
            String telefono,
            String direccion,
            String docTipo,
            @NotBlank(message = "Número de documento es requerido")
            String docNumero
    ) {}
}
