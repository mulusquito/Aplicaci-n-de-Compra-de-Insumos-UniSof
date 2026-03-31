package com.unisof.insumos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProveedorRequest {

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(max = 200)
    private String nombre;

    /** Códigos de {@link com.unisof.insumos.model.CategoriaInsumo} (ej. TELAS, HILOS). Al menos una. */
    @NotEmpty(message = "Seleccione al menos una categoría de insumo")
    private List<@NotBlank String> categoriaCodigos;

    @Size(max = 32)
    private String nit;

    @Size(max = 120)
    private String contactoNombre;

    @Size(max = 40)
    private String telefono;

    @Size(max = 160)
    private String correo;

    @Size(max = 300)
    private String direccion;

    private String observaciones;

    private Boolean activo;
}
