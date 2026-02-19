package com.unisof.insumos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Solicitud de verificación del token 2FA recibido por correo.
 * <p>
 * SCRUM-35: DTO para el endpoint POST /api/auth/verify-token
 * </p>
 *
 * @see com.unisof.insumos.controller.AuthController
 * @see com.unisof.insumos.service.AuthService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyTokenRequest {

    /** Nombre de usuario que realizó el login */
    @NotBlank(message = "El usuario es obligatorio")
    private String usuario;

    /** Código de 6 dígitos recibido por correo */
    @NotBlank(message = "El token es obligatorio")
    private String token;
}
