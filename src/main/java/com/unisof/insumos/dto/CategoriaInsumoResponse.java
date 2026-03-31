package com.unisof.insumos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaInsumoResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
}
