package com.unisof.insumos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Representación ligera de un usuario para el panel de administración
 * (listados y resultados de búsqueda).
 */
@Data
@AllArgsConstructor
public class UsuarioResponse {

    private Long id;
    private String usuario;
    private String nombreCompleto;
    private String numeroIdentificacion;
    private String correo;
    private String celular;
    private String rol;
}

