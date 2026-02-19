package com.unisof.insumos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Solicitud de login con usuario y contraseña.
 * <p>
 * SCRUM-7: DTO para el endpoint POST /api/auth/login
 * </p>
 *
 * @see com.unisof.insumos.controller.AuthController
 * @see com.unisof.insumos.service.AuthService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /** Nombre de usuario para autenticación */
    @NotBlank(message = "El usuario es obligatorio")
    private String usuario;

    /** Contraseña del usuario */
    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;
}
