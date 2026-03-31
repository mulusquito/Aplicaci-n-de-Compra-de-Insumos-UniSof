package com.unisof.insumos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsumoResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private String categoriaCodigo;
    private String categoriaNombre;
    private String unidadMedida;
    private BigDecimal stockDisponible;
    private BigDecimal stockMinimo;
    private String referenciaTela;
    private String color;
    private String observaciones;
    private String productosCatalogo;
}
