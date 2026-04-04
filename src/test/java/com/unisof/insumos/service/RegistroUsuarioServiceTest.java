package com.unisof.insumos.service;

import com.unisof.insumos.dto.RegistroUsuarioRequest;
import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para RegistroUsuarioService.
 */
@ExtendWith(MockitoExtension.class)
class RegistroUsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistroUsuarioService service;

    private RegistroUsuarioRequest requestValido;

    @BeforeEach
    void setUp() {
        requestValido = new RegistroUsuarioRequest();
        requestValido.setNombreCompleto("Juan Perez");
        requestValido.setUsuario("juanp");
        requestValido.setNumeroIdentificacion("123456");
        requestValido.setCorreo("juan@test.com");
        requestValido.setCelular("3001234567");
        requestValido.setClave("clave123");
        requestValido.setConfirmarClave("clave123");
        requestValido.setRol("VENDEDOR");
    }

    @Test
    @DisplayName("Registro falla cuando contraseña y confirmación no coinciden")
    void registrar_claveNoCoincide_lanzaExcepcion() {
        requestValido.setConfirmarClave("otraclave");

        assertThatThrownBy(() -> service.registrar(requestValido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no coinciden");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registro falla cuando nombre de usuario ya está en uso")
    void registrar_usuarioExiste_lanzaExcepcion() {
        when(usuarioRepository.findByUsuario("juanp")).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> service.registrar(requestValido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre de usuario");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registro falla cuando correo electrónico ya está registrado")
    void registrar_correoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByUsuario("juanp")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByCorreo("juan@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(requestValido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("correo");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registro falla cuando número de identificación ya existe")
    void registrar_numeroIdentificacionExiste_lanzaExcepcion() {
        when(usuarioRepository.findByUsuario("juanp")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByCorreo("juan@test.com")).thenReturn(false);
        when(usuarioRepository.existsByNumeroIdentificacion("123456")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(requestValido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identificación");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registro exitoso guarda usuario con contraseña encriptada y rol normalizado")
    void registrar_exitoso_guardaUsuario() {
        when(usuarioRepository.findByUsuario("juanp")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByCorreo("juan@test.com")).thenReturn(false);
        when(usuarioRepository.existsByNumeroIdentificacion("123456")).thenReturn(false);
        when(passwordEncoder.encode("clave123")).thenReturn("hashNuevo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        Usuario resultado = service.registrar(requestValido);

        assertThat(resultado.getUsuario()).isEqualTo("juanp");
        assertThat(resultado.getNombre()).isEqualTo("Juan Perez");
        assertThat(resultado.getCorreo()).isEqualTo("juan@test.com");
        assertThat(resultado.getContrasena()).isEqualTo("hashNuevo");
        assertThat(resultado.getRol()).isEqualTo("VENDEDOR");
    }
}
