package com.unisof.insumos.controller;

import com.unisof.insumos.dto.LoginRequest;
import com.unisof.insumos.dto.LoginResponse;
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
}
