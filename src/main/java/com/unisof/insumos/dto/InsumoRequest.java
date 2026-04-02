package com.unisof.insumos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InsumoRequest {

    @NotBlank
    private String codigo;

    @NotBlank
    private String nombre;

    @NotBlank
    private String categoriaCodigo;

    @NotBlank
    private String unidadMedida;

    @NotNull
    private BigDecimal stockDisponible;

    private BigDecimal stockMinimo;

    /** Costo/precio de referencia por unidad de medida (COP). Opcional. */
    @DecimalMin(value = "0", inclusive = true, message = "El precio unitario no puede ser negativo.")
    private BigDecimal precioUnitario;

    private String referenciaTela;

    private String color;

    private String observaciones;

    private String productosCatalogo;
}
