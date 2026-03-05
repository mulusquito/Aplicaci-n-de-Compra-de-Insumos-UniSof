package com.unisof.insumos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Solicitud de registro de usuario por el administrador.
 * SCRUM-12: Registrar usuario (nombre, identificación, correo, celular, clave, rol).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUsuarioRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    /** Usuario para inicio de sesión (único, con el que ingresará a la app) */
    @NotBlank(message = "El usuario (para inicio de sesión) es obligatorio")
    private String usuario;

    @NotBlank(message = "El número de identificación es obligatorio")
    private String numeroIdentificacion;

    @NotBlank(message = "El correo electrónico es obligatorio")
    private String correo;

    @NotBlank(message = "El celular es obligatorio")
    private String celular;

    @NotBlank(message = "La clave de acceso es obligatoria")
    private String clave;

    @NotBlank(message = "Confirmar clave es obligatorio")
    private String confirmarClave;

    @NotBlank(message = "El rol es obligatorio")
    private String rol;
}
