package com.unisof.insumos.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias para LoginRequest.
 */
class LoginRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Credenciales válidas (usuario y contraseña) no generan violaciones de validación")
    void loginRequest_valido_sinViolaciones() {
        LoginRequest request = new LoginRequest("admin", "password123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Usuario vacío genera violación de validación")
    void loginRequest_usuarioVacio_tieneViolacion() {
        LoginRequest request = new LoginRequest("", "password123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("usuario"));
    }

    @Test
    @DisplayName("Contraseña nula genera violación de validación")
    void loginRequest_contrasenaNull_tieneViolacion() {
        LoginRequest request = new LoginRequest("admin", null);
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("contraseña"));
    }

    @Test
    @DisplayName("Getters y setters del DTO de login funcionan correctamente")
    void loginRequest_gettersSetters() {
        LoginRequest request = new LoginRequest();
        request.setUsuario("vendedor");
        request.setContrasena("clave456");

        assertThat(request.getUsuario()).isEqualTo("vendedor");
        assertThat(request.getContrasena()).isEqualTo("clave456");
    }
}
