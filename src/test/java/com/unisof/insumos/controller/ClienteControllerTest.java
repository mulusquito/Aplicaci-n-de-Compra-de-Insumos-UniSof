package com.unisof.insumos.controller;

import com.unisof.insumos.model.Cliente;
import com.unisof.insumos.repository.ClienteRepository;
import com.unisof.insumos.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ClienteController (crear cliente, consultar cliente).
 */
@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private HttpServletRequest mockRequest;

    @InjectMocks
    private ClienteController controller;

    @Test
    @DisplayName("Listar todos los clientes retorna lista con nombre y cédula")
    void listarTodos_retornaLista() {
        Cliente c = new Cliente("Juan", "123", "juan@test.com", "300", "Calle 1");
        c.setId(1L);
        when(clienteRepository.findAll()).thenReturn(List.of(c));

        ResponseEntity<List<Map<String, Object>>> response = controller.listarTodos();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).get("nombre")).isEqualTo("Juan");
        assertThat(response.getBody().get(0).get("cedula")).isEqualTo("123");
    }

    @Test
    @DisplayName("Buscar cliente por ID inexistente retorna 404")
    void buscarPorId_noExiste_404() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.buscarPorId(999L);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    @DisplayName("buscarPorCedula con cedula vacía retorna bad request")
    void buscarPorCedula_cedulaVacia_badRequest() {
        ResponseEntity<?> response = controller.buscarPorCedula("   ", mockRequest);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getBody()).get("mensaje")).toString().contains("cédula");
    }

    @Test
    @DisplayName("Buscar por cédula existente retorna cliente encontrado")
    void buscarPorCedula_existe_retornaCliente() {
        Cliente c = new Cliente("Maria", "456", "maria@test.com", "", "");
        c.setId(2L);
        when(clienteRepository.findByCedula("456")).thenReturn(Optional.of(c));

        ResponseEntity<?> response = controller.buscarPorCedula("456", mockRequest);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isInstanceOf(Map.class);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body.get("encontrado")).isEqualTo(true);
    }

    @Test
    @DisplayName("Crear cliente sin campos requeridos (ej. correo) retorna Bad Request")
    void crear_faltanCampos_badRequest() {
        Map<String, String> body = Map.of("nombre", "Test", "cedula", "789");
        // falta correo

        ResponseEntity<?> response = controller.crear(body, mockRequest);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear cliente con cédula duplicada retorna Bad Request")
    void crear_cedulaExiste_badRequest() {
        Map<String, String> body = Map.of(
                "nombre", "Nuevo", "cedula", "789", "correo", "nuevo@test.com",
                "telefono", "", "direccion", "");
        when(clienteRepository.findByCedula("789")).thenReturn(Optional.of(new Cliente()));

        ResponseEntity<?> response = controller.crear(body, mockRequest);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear exitoso guarda cliente")
    void crear_exitoso_guardaCliente() {
        Map<String, String> body = Map.of(
                "nombre", "Pedro", "cedula", "111", "correo", "pedro@test.com",
                "telefono", "300", "direccion", "Calle 2");
        when(clienteRepository.findByCedula("111")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        ResponseEntity<?> response = controller.crear(body, mockRequest);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getBody()).get("nombre")).isEqualTo("Pedro");
    }

    @Test
    @DisplayName("Eliminar cliente inexistente retorna 404")
    void eliminar_noExiste_404() {
        when(clienteRepository.existsById(999L)).thenReturn(false);

        ResponseEntity<?> response = controller.eliminar(999L, mockRequest);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        verify(clienteRepository, never()).deleteById(any());
    }
}
