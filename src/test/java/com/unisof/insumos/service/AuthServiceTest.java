package com.unisof.insumos.service;

import com.unisof.insumos.dto.LoginRequest;
import com.unisof.insumos.dto.LoginResponse;
import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AuthService (login completo).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenVerificacionService tokenVerificacionService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("vendedor", "hash", "Vendedor Test", "vendedor@test.com");
        usuario.setId(1L);
        usuario.setRol("VENDEDOR");

        loginRequest = new LoginRequest("vendedor", "clave123");
    }

    @Test
    @DisplayName("Login con usuario inexistente retorna credenciales inválidas")
    void validarCredenciales_usuarioNoExiste_retornaInvalido() {
        when(usuarioRepository.findByUsuario("vendedor")).thenReturn(Optional.empty());

        LoginResponse response = authService.validarCredenciales(loginRequest);

        assertThat(response.isValido()).isFalse();
        assertThat(response.getMensaje()).contains("Credenciales inválidas");
        assertThat(response.getUsuario()).isNull();
        verify(tokenVerificacionService, never()).generarYEnviarToken(any());
    }

    @Test
    @DisplayName("validarCredenciales retorna inválido cuando contraseña incorrecta")
    void validarCredenciales_contrasenaIncorrecta_retornaInvalido() {
        when(usuarioRepository.findByUsuario("vendedor")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave123", "hash")).thenReturn(false);

        LoginResponse response = authService.validarCredenciales(loginRequest);

        assertThat(response.isValido()).isFalse();
        assertThat(response.getMensaje()).contains("Credenciales inválidas");
        verify(tokenVerificacionService, never()).generarYEnviarToken(any());
    }

    @Test
    @DisplayName("Login exitoso con credenciales correctas requiere verificación 2FA")
    void validarCredenciales_exitoso_retornaValido() {
        when(usuarioRepository.findByUsuario("vendedor")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave123", "hash")).thenReturn(true);
        when(tokenVerificacionService.generarYEnviarToken(usuario)).thenReturn(Optional.of(mock(com.unisof.insumos.model.TokenVerificacion.class)));

        LoginResponse response = authService.validarCredenciales(loginRequest);

        assertThat(response.isValido()).isTrue();
        assertThat(response.getUsuario()).isNotNull();
        assertThat(response.getUsuario().getNombre()).isEqualTo("Vendedor Test");
        assertThat(response.isRequiereVerificacionDosPasos()).isTrue();
    }

    @Test
    @DisplayName("Token 2FA inválido retorna mensaje de error")
    void validarToken2FA_invalido_retornaError() {
        when(tokenVerificacionService.verificarToken("vendedor", "000000")).thenReturn(Optional.empty());

        LoginResponse response = authService.validarToken2FA("vendedor", "000000", u -> {});

        assertThat(response.isValido()).isFalse();
        assertThat(response.getMensaje()).contains("inválido");
    }

    @Test
    @DisplayName("Token 2FA válido completa login y retorna usuario autenticado")
    void validarToken2FA_valido_ejecutaCallback() {
        when(tokenVerificacionService.verificarToken("vendedor", "123456")).thenReturn(Optional.of(usuario));

        Usuario[] usuarioCapturado = new Usuario[1];
        LoginResponse response = authService.validarToken2FA("vendedor", "123456", u -> usuarioCapturado[0] = u);

        assertThat(response.isValido()).isTrue();
        assertThat(usuarioCapturado[0]).isNotNull();
        assertThat(usuarioCapturado[0].getUsuario()).isEqualTo("vendedor");
    }
}
