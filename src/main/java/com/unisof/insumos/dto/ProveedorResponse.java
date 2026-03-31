package com.unisof.insumos.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class ProveedorResponse {
    Long id;
    String nombre;
    String nit;
    String contactoNombre;
    String telefono;
    String correo;
    String direccion;
    String observaciones;
    boolean activo;
    Instant fechaRegistro;
    List<CategoriaInsumoResponse> categorias;
}
