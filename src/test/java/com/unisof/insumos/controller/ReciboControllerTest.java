package com.unisof.insumos.controller;

import com.unisof.insumos.model.Cliente;
import com.unisof.insumos.model.Recibo;
import com.unisof.insumos.repository.ClienteRepository;
import com.unisof.insumos.repository.ReciboRepository;
import com.unisof.insumos.service.AuditoriaService;
import com.unisof.insumos.service.AuthService;
import com.unisof.insumos.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ReciboController (consultar órdenes, crear orden).
 */
@ExtendWith(MockitoExtension.class)
class ReciboControllerTest {

    @Mock
    private ReciboRepository reciboRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private AuthService authService;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private HttpServletRequest mockRequest;

    @InjectMocks
    private ReciboController controller;

    private Cliente cliente;
    private Recibo recibo;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "copiasEmails", "");
        cliente = new Cliente("Cliente Test", "123", "cliente@test.com", "", "");
        cliente.setId(1L);
        recibo = new Recibo(1, cliente, BigDecimal.valueOf(100000), "[]", "PENDIENTE", null);
        recibo.setId(1L);
    }

    @Test
    @DisplayName("Buscar órdenes sin filtros retorna todos los recibos")
    void buscar_sinParametros_retornaTodos() {
        when(reciboRepository.findAll()).thenReturn(List.of(recibo));

        ResponseEntity<?> response = controller.buscar(null, null, null, null, null, null);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat((List<?>) response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("buscar por número retorna recibo si existe")
    void buscar_porNumero_encontrado() {
        when(reciboRepository.findByNumero(1)).thenReturn(Optional.of(recibo));

        ResponseEntity<?> response = controller.buscar("001", null, null, null, null, null);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        List<?> body = (List<?>) response.getBody();
        assertThat(body).hasSize(1);
        assertThat(((Map<?, ?>) body.get(0)).get("numero")).isEqualTo(1);
    }

    @Test
    @DisplayName("Buscar orden por ID inexistente retorna 404")
    void buscarPorId_noExiste_404() {
        when(reciboRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.buscarPorId(999L);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    @DisplayName("Buscar orden por ID retorna recibo con número, cliente, total y estado")
    void buscarPorId_existe_retornaRecibo() {
        when(reciboRepository.findById(1L)).thenReturn(Optional.of(recibo));

        ResponseEntity<?> response = controller.buscarPorId(1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsKeys("numero", "clienteNombre", "total", "estado");
    }

    @Test
    @DisplayName("Crear orden sin items retorna Bad Request")
    void crear_sinItems_badRequest() {
        Map<String, Object> body = Map.of(
                "clienteId", 1L,
                "items", List.of(),
                "total", 100000);

        ResponseEntity<?> response = controller.crear(body, mockRequest);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    @DisplayName("Crear orden con cliente inexistente retorna Bad Request")
    void crear_clienteNoExiste_badRequest() {
        Map<String, Object> body = Map.of(
                "clienteId", 999L,
                "items", List.of(Map.of("nombre", "Prod", "cantidad", 1, "precioUnit", 100000, "subtotal", 100000)),
                "total", 100000);

        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.crear(body, mockRequest);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }
}
