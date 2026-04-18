package com.unisof.insumos.service;

import com.unisof.insumos.model.AuditoriaLog;
import com.unisof.insumos.repository.AuditoriaLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link AuditoriaService} — Proceso 3 (SCRUM-64).
 * Verifica que los logs de auditoría se persistan correctamente
 * y que la extracción de IP y usuario funcionen según lo esperado.
 */
@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaLogRepository repository;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AuditoriaService service;

    // ─── registrar ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("registrar persiste log con todos los campos correctos")
    void registrar_camposCompletos_persisteLog() {
        service.registrar(
                AuditoriaService.ACC_CREAR,
                AuditoriaService.MOD_INSUMOS,
                "Insumo creado: Tela algodón",
                "jcompras",
                "JEFE_COMPRAS",
                "192.168.1.10",
                AuditoriaService.RES_EXITOSO,
                "ID=42"
        );

        ArgumentCaptor<AuditoriaLog> captor = ArgumentCaptor.forClass(AuditoriaLog.class);
        verify(repository).save(captor.capture());

        AuditoriaLog log = captor.getValue();
        assertThat(log.getAccion()).isEqualTo("CREAR");
        assertThat(log.getModulo()).isEqualTo("INSUMOS");
        assertThat(log.getDescripcion()).isEqualTo("Insumo creado: Tela algodón");
        assertThat(log.getUsuarioNombre()).isEqualTo("jcompras");
        assertThat(log.getUsuarioRol()).isEqualTo("JEFE_COMPRAS");
        assertThat(log.getIpCliente()).isEqualTo("192.168.1.10");
        assertThat(log.getResultado()).isEqualTo("EXITOSO");
        assertThat(log.getDatosAdicionales()).isEqualTo("ID=42");
        assertThat(log.getFechaHora()).isNotNull();
    }

    @Test
    @DisplayName("registrar con usuarioNombre null guarda 'anonimo'")
    void registrar_usuarioNulo_guardaAnonimo() {
        service.registrar(
                AuditoriaService.ACC_CONSULTAR,
                AuditoriaService.MOD_INSUMOS,
                "Consulta anónima",
                null, null, "10.0.0.1",
                AuditoriaService.RES_EXITOSO, null
        );

        ArgumentCaptor<AuditoriaLog> captor = ArgumentCaptor.forClass(AuditoriaLog.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUsuarioNombre()).isEqualTo("anonimo");
    }

    @Test
    @DisplayName("registrar no lanza excepción cuando el repositorio falla")
    void registrar_repositorioFalla_noPropagaExcepcion() {
        doThrow(new RuntimeException("DB caída")).when(repository).save(any());

        // No debe propagarse: la auditoría nunca interrumpe el flujo de negocio
        service.registrar(
                AuditoriaService.ACC_CREAR, AuditoriaService.MOD_PROVEEDORES,
                "desc", "user", "ROL", "1.2.3.4", AuditoriaService.RES_EXITOSO, null
        );
        // Si llega aquí sin excepción, el test pasa
    }

    // ─── obtenerIp ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerIp usa X-Forwarded-For cuando está presente")
    void obtenerIp_conXForwardedFor_retornaPrimerIp() {
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("203.0.113.5, 10.0.0.1");

        String ip = service.obtenerIp(httpRequest);

        assertThat(ip).isEqualTo("203.0.113.5");
    }

    @Test
    @DisplayName("obtenerIp usa RemoteAddr cuando no hay X-Forwarded-For")
    void obtenerIp_sinXForwardedFor_usaRemoteAddr() {
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpRequest.getRemoteAddr()).thenReturn("192.168.0.5");

        String ip = service.obtenerIp(httpRequest);

        assertThat(ip).isEqualTo("192.168.0.5");
    }

    @Test
    @DisplayName("obtenerIp con request null retorna 'desconocida'")
    void obtenerIp_requestNulo_retornaDesconocida() {
        String ip = service.obtenerIp(null);
        assertThat(ip).isEqualTo("desconocida");
    }

    // ─── obtenerUsuarioInfo (sin sesión Spring Security activa) ──────────────

    @Test
    @DisplayName("obtenerUsuarioInfo sin sesión activa retorna anonimo")
    void obtenerUsuarioInfo_sinSesion_retornaAnonimo() {
        // Sin contexto de seguridad configurado, el Authentication es null
        String[] info = service.obtenerUsuarioInfo();

        assertThat(info[0]).isEqualTo("anonimo");
        assertThat(info[1]).isNull();
    }

    // ─── constantes ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("constantes de módulos y acciones tienen valores no nulos")
    void constantes_noSonNulas() {
        assertThat(AuditoriaService.MOD_INSUMOS).isEqualTo("INSUMOS");
        assertThat(AuditoriaService.MOD_PROVEEDORES).isEqualTo("PROVEEDORES");
        assertThat(AuditoriaService.MOD_AUTENTICACION).isEqualTo("AUTENTICACION");
        assertThat(AuditoriaService.ACC_CREAR).isEqualTo("CREAR");
        assertThat(AuditoriaService.ACC_EDITAR).isEqualTo("EDITAR");
        assertThat(AuditoriaService.ACC_ELIMINAR).isEqualTo("ELIMINAR");
        assertThat(AuditoriaService.ACC_CONSULTAR).isEqualTo("CONSULTAR");
        assertThat(AuditoriaService.RES_EXITOSO).isEqualTo("EXITOSO");
        assertThat(AuditoriaService.RES_FALLIDO).isEqualTo("FALLIDO");
    }
}
