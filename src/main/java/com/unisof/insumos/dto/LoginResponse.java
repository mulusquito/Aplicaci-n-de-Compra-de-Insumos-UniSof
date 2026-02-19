package com.unisof.insumos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta del proceso de login o verificación 2FA.
 * <p>
 * SCRUM-7: Respuesta tras validar credenciales.
 * SCRUM-35: Si requiereVerificacionDosPasos es true, el cliente debe enviar
 * el token recibido por correo al endpoint /api/auth/verify-token.
 * </p>
 *
 * @see com.unisof.insumos.controller.AuthController
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** Indica si la operación fue exitosa */
    private boolean valido;

    /** Mensaje descriptivo para el usuario */
    private String mensaje;

    /** Datos del usuario autenticado (null si falla) */
    private UsuarioResponse usuario;

    /** SCRUM-35: true cuando se requiere verificación en dos pasos */
    private boolean requiereVerificacionDosPasos;

    /**
     * Resumen de datos del usuario para la respuesta.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioResponse {
        /** ID del usuario */
        private Long id;
        /** Nombre de usuario */
        private String usuario;
        /** Nombre completo */
        private String nombre;
        /** Correo electrónico */
        private String correo;
        /** Rol (ADMINISTRADOR, WORKER, etc.) */
        private String rol;
    }
}
