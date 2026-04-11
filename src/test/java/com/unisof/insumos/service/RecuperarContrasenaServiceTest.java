package com.unisof.insumos.service;

import com.unisof.insumos.model.TokenRecuperacionContrasena;
import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.repository.TokenRecuperacionContrasenaRepository;
import com.unisof.insumos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para RecuperarContrasenaService.
 */
@ExtendWith(MockitoExtension.class)
class RecuperarContrasenaServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TokenRecuperacionContrasenaRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RecuperarContrasenaService service;

    private Usuario usuario;
    private TokenRecuperacionContrasena tokenValido;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("testuser", "hash", "Test User", "test@example.com");
        usuario.setId(1L);
        tokenValido = new TokenRecuperacionContrasena(usuario, "abc123", Instant.now().plusSeconds(3600));
        tokenValido.setId(1L);
    }

    @Test
    @DisplayName("Restablecer contraseña con token nulo retorna mensaje de error")
    void restablecerContrasena_tokenNull_retornaError() {
        String resultado = service.restablecerContrasena(null, "nueva123", "nueva123");
        assertThat(resultado).isEqualTo("Token inválido o expirado.");
        verify(tokenRepository, never()).findByTokenAndUsadoFalse(anyString());
    }

    @Test
    @DisplayName("Restablecer contraseña con token vacío retorna mensaje de error")
    void restablecerContrasena_tokenVacio_retornaError() {
        String resultado = service.restablecerContrasena("   ", "nueva123", "nueva123");
        assertThat(resultado).isEqualTo("Token inválido o expirado.");
    }

    @Test
    @DisplayName("Contraseña con menos de 6 caracteres retorna error de validación")
    void restablecerContrasena_passwordCorta_retornaError() {
        String resultado = service.restablecerContrasena("abc123", "12345", "12345");
        assertThat(resultado).isEqualTo("La contraseña debe tener al menos 6 caracteres.");
    }

    @Test
    @DisplayName("Contraseña y confirmación no coinciden retorna error")
    void restablecerContrasena_passwordNoCoinciden_retornaError() {
        String resultado = service.restablecerContrasena("abc123", "nueva123", "otra456");
        assertThat(resultado).isEqualTo("Las contraseñas no coinciden.");
    }

    @Test
    @DisplayName("Token inexistente en base de datos retorna error")
    void restablecerContrasena_tokenNoExiste_retornaError() {
        when(tokenRepository.findByTokenAndUsadoFalse("abc123")).thenReturn(Optional.empty());

        String resultado = service.restablecerContrasena("abc123", "nueva123", "nueva123");

        assertThat(resultado).isEqualTo("Token inválido o expirado.");
    }

    @Test
    @DisplayName("Token expirado retorna mensaje indicando solicitar nuevo enlace")
    void restablecerContrasena_tokenExpirado_retornaError() {
        tokenValido.setFechaExpiracion(Instant.now().minusSeconds(60));
        when(tokenRepository.findByTokenAndUsadoFalse("abc123")).thenReturn(Optional.of(tokenValido));

        String resultado = service.restablecerContrasena("abc123", "nueva123", "nueva123");

        assertThat(resultado).isEqualTo("El enlace ha expirado. Solicita uno nuevo.");
    }

    @Test
    @DisplayName("Restablecer contraseña exitoso: actualiza usuario y marca token como usado")
    void restablecerContrasena_exitoso_retornaNull() {
        when(tokenRepository.findByTokenAndUsadoFalse("abc123")).thenReturn(Optional.of(tokenValido));
        when(passwordEncoder.encode("nueva123")).thenReturn("hashNuevo");

        String resultado = service.restablecerContrasena("abc123", "nueva123", "nueva123");

        assertThat(resultado).isNull();
        verify(usuarioRepository).save(usuario);
        verify(tokenRepository).save(tokenValido);
        assertThat(usuario.getContrasena()).isEqualTo("hashNuevo");
        assertThat(tokenValido.isUsado()).isTrue();
    }

    @Test
    @DisplayName("Solicitar recuperación con correo nulo retorna false")
    void solicitarRecuperacion_correoNull_retornaFalse() {
        boolean resultado = service.solicitarRecuperacion(null);
        assertThat(resultado).isFalse();
        verify(usuarioRepository, never()).findByCorreoIgnoreCase(anyString());
    }

    @Test
    @DisplayName("Solicitar recuperación con correo no registrado retorna false")
    void solicitarRecuperacion_correoNoExiste_retornaFalse() {
        when(usuarioRepository.findByCorreoIgnoreCase("noexiste@test.com")).thenReturn(Optional.empty());

        boolean resultado = service.solicitarRecuperacion("noexiste@test.com");

        assertThat(resultado).isFalse();
        verify(tokenRepository, never()).save(any());
    }
}
