package com.unisof.insumos.controller;

import com.unisof.insumos.dto.LoginRequest;
import com.unisof.insumos.dto.LoginResponse;
import com.unisof.insumos.dto.VerifyTokenRequest;
import com.unisof.insumos.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint para validar credenciales del usuario.
     * SCRUM-7: Validar credenciales del usuario por medio de usuario y contraseña
     * SCRUM-35: Si válidas, envía token 2FA por correo
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
     * SCRUM-35: Verificación en dos pasos. Valida el token enviado por correo.
     * Si es correcto: redirigir al panel principal. Si no: mensaje de error.
     */
    @PostMapping("/verify-token")
    public ResponseEntity<LoginResponse> verifyToken(@Valid @RequestBody VerifyTokenRequest request) {
        LoginResponse response = authService.validarToken2FA(request.getUsuario(), request.getToken());

        if (response.isValido()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }
}
