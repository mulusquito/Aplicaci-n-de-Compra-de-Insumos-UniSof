package com.unisof.insumos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Solicitud de actualización de usuario por el administrador.
 * Permite modificar los datos básicos del usuario y, opcionalmente,
 * actualizar su contraseña.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUsuarioRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @NotBlank(message = "El usuario es obligatorio")
    private String usuario;

    @NotBlank(message = "El número de identificación es obligatorio")
    private String numeroIdentificacion;

    @NotBlank(message = "El correo electrónico es obligatorio")
    private String correo;

    private String celular;

    /**
     * Rol del usuario (ADMINISTRADOR, VENDEDOR, JEFE DE COMPRAS; JEFE DE VENTAS solo por compatibilidad).
     */
    @NotBlank(message = "El rol es obligatorio")
    private String rol;

    /**
     * Nueva clave (opcional). Si se envía junto con confirmarClave
     * y coinciden, se actualiza la contraseña del usuario.
     */
    private String clave;

    /** Confirmación de la nueva clave (opcional, ver {@link #clave}). */
    private String confirmarClave;
}

