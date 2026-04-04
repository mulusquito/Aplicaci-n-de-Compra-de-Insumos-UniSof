package com.unisof.insumos.service;

import com.unisof.insumos.dto.UpdateUsuarioRequest;
import com.unisof.insumos.dto.UsuarioResponse;
import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.repository.TokenVerificacionRepository;
import com.unisof.insumos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para UsuarioAdminService (administrador).
 */
@ExtendWith(MockitoExtension.class)
class UsuarioAdminServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TokenVerificacionRepository tokenVerificacionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioAdminService service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new UsuarioAdminService(usuarioRepository, tokenVerificacionRepository, passwordEncoder);
        usuario = new Usuario("admin", "hash", "Admin User", "admin@test.com");
        usuario.setId(1L);
        usuario.setNumeroIdentificacion("123");
        usuario.setCelular("300");
        usuario.setRol("ADMINISTRADOR");
    }

    @Test
    @DisplayName("Listar usuarios retorna lista ordenada alfabéticamente por nombre")
    void listarTodos_retornaOrdenados() {
        Usuario u2 = new Usuario("vendedor", "hash", "Ana Vendedor", "ana@test.com");
        u2.setId(2L);
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario, u2));

        List<UsuarioResponse> resultado = service.listarTodos();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombreCompleto()).isEqualTo("Admin User");
        assertThat(resultado.get(1).getNombreCompleto()).isEqualTo("Ana Vendedor");
    }

    @Test
    @DisplayName("Buscar con criterio vacío retorna todos los usuarios")
    void buscar_criterioVacio_retornaTodos() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<UsuarioResponse> resultado = service.buscar("", "nombre");

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("buscar por identificación retorna usuario si existe")
    void buscar_porIdentificacion_encontrado() {
        when(usuarioRepository.findByNumeroIdentificacion("123")).thenReturn(Optional.of(usuario));

        List<UsuarioResponse> resultado = service.buscar("123", "identificacion");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNumeroIdentificacion()).isEqualTo("123");
    }

    @Test
    @DisplayName("Buscar por identificación inexistente retorna lista vacía")
    void buscar_porIdentificacion_noEncontrado() {
        when(usuarioRepository.findByNumeroIdentificacion("999")).thenReturn(Optional.empty());

        List<UsuarioResponse> resultado = service.buscar("999", "identificacion");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Obtener usuario por ID inexistente lanza excepción")
    void obtenerPorId_noExiste_lanzaExcepcion() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    @DisplayName("Eliminar usuario borra tokens de verificación y registro del usuario")
    void eliminar_eliminaTokensYUsuario() {
        service.eliminar(1L);

        verify(tokenVerificacionRepository).deleteByUsuario_Id(1L);
        verify(usuarioRepository).deleteById(1L);
    }
}
