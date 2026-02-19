package com.unisof.insumos.controller;

import com.unisof.insumos.dto.LoginRequest;
import com.unisof.insumos.dto.LoginResponse;
import com.unisof.insumos.dto.VerifyTokenRequest;
import com.unisof.insumos.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticacion.
 * <p>
 * SCRUM-7: Endpoint de login con usuario y contrasena.
 * SCRUM-35: Endpoint de verificacion del token 2FA recibido por correo.
 * SCRUM-36: Sesion expira tras 1 min de inactividad. Endpoints /me y /logout.
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Valida credenciales y envia token 2FA por correo si son correctas.
     * POST /api/auth/login
     *
     * @param request usuario y contrasena
     * @return 200 OK con usuario y requiereVerificacionDosPasos=true, o 401 si falla
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.validarCredenciales(request);

        if (response.isValido()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Verifica el token 2FA recibido por correo. Si es valido, crea la sesion y retorna el usuario.
     * SCRUM-36: La sesion expira tras 1 minuto de inactividad.
     * POST /api/auth/verify-token
     *
     * @param request usuario y codigo de 6 digitos
     * @return 200 OK con usuario si es correcto, o 401 si token invalido/expirado
     */
    @PostMapping("/verify-token")
    public ResponseEntity<LoginResponse> verifyToken(@Valid @RequestBody VerifyTokenRequest request) {
        LoginResponse response = authService.validarToken2FA(
                request.getUsuario(),
                request.getToken(),
                authService::establecerSesion
        );

        if (response.isValido()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * SCRUM-36: Devuelve el usuario actual si hay sesion valida. 401 si expiro por inactividad.
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<LoginResponse.UsuarioResponse> me() {
        return authService.obtenerUsuarioActual()
                .map(u -> ResponseEntity.ok(new LoginResponse.UsuarioResponse(
                        u.getId(), u.getUsuario(), u.getNombre(), u.getCorreo(), u.getRol())))
                .orElse(ResponseEntity.status(401).build());
    }
}
