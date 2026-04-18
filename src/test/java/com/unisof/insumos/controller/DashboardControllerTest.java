package com.unisof.insumos.controller;

import com.unisof.insumos.model.Cliente;
import com.unisof.insumos.model.Recibo;
import com.unisof.insumos.repository.InsumoRepository;
import com.unisof.insumos.repository.ProveedorRepository;
import com.unisof.insumos.repository.ReciboRepository;
import com.unisof.insumos.repository.UsuarioRepository;
import com.unisof.insumos.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para DashboardController.
 */
@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ReciboRepository reciboRepository;

    @Mock
    private InsumoRepository insumoRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private HttpServletRequest mockRequest;

    @InjectMocks
    private DashboardController controller;

    @BeforeEach
    void stubAuditoria() {
        when(auditoriaService.obtenerUsuarioInfo()).thenReturn(new String[]{"testuser", "ADMIN"});
        when(auditoriaService.obtenerIp(any())).thenReturn("127.0.0.1");
    }

    @Test
    @DisplayName("Estadísticas del dashboard retornan estructura con usuarios, órdenes, ventas y fechas")
    void stats_retornaEstructuraCorrecta() {
        when(usuarioRepository.count()).thenReturn(5L);
        when(reciboRepository.findByFechaBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        var response = controller.stats(null, null, mockRequest);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isInstanceOf(java.util.Map.class);

        @SuppressWarnings("unchecked")
        var body = (java.util.Map<String, Object>) response.getBody();
        assertThat(body).containsKeys("usuarios", "ordenes", "ventas", "fechaDesde", "fechaHasta", "ventasPorMes");
        assertThat(body.get("usuarios")).isEqualTo(5L);
        assertThat(body.get("ordenes")).isEqualTo(0);
    }

    @Test
    @DisplayName("Ventas por mes contiene exactamente 12 meses (enero a diciembre)")
    void stats_ventasPorMesTiene12Meses() {
        when(usuarioRepository.count()).thenReturn(5L);
        when(reciboRepository.findByFechaBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        var response = controller.stats(null, null, mockRequest);

        @SuppressWarnings("unchecked")
        var body = (java.util.Map<String, Object>) response.getBody();
        @SuppressWarnings("unchecked")
        var ventasPorMes = (List<?>) body.get("ventasPorMes");

        assertThat(ventasPorMes).hasSize(12);
    }

    @Test
    @DisplayName("Estadísticas con fechas personalizadas filtran órdenes en el rango indicado")
    void stats_conFechasPersonalizadas_usaRango() {
        when(usuarioRepository.count()).thenReturn(5L);
        when(reciboRepository.findByFechaBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(crearReciboMock(BigDecimal.valueOf(100000))));

        var response = controller.stats("2026-01-01", "2026-01-31", mockRequest);

        @SuppressWarnings("unchecked")
        var body = (java.util.Map<String, Object>) response.getBody();
        assertThat(body.get("fechaDesde")).isEqualTo("2026-01-01");
        assertThat(body.get("fechaHasta")).isEqualTo("2026-01-31");
        assertThat(body.get("ordenes")).isEqualTo(1);
    }

    private Recibo crearReciboMock(BigDecimal total) {
        Cliente cliente = new Cliente("Test", "123", "test@test.com", null, null);
        return new Recibo(1, cliente, total, "[]", "PENDIENTE", null);
    }
}
