package com.unisof.insumos.controller;

import com.unisof.insumos.model.AuditoriaLog;
import com.unisof.insumos.repository.AuditoriaLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * API de consulta de logs de auditoría. Solo accesible para ADMINISTRADOR.
 * SCRUM-64: Permite al administrador consultar el historial completo de acciones.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /api/auditoria                    → últimos N registros (paginado)</li>
 *   <li>GET /api/auditoria?modulo=VENTAS       → filtrar por módulo</li>
 *   <li>GET /api/auditoria?usuario=jperez      → filtrar por usuario</li>
 *   <li>GET /api/auditoria?resultado=FALLIDO   → filtrar por resultado</li>
 *   <li>GET /api/auditoria?accion=LOGIN        → filtrar por acción</li>
 *   <li>GET /api/auditoria?desde=2026-04-01&hasta=2026-04-30 → filtrar por rango de fechas</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");

    private final AuditoriaLogRepository auditoriaLogRepository;

    /**
     * Consulta los logs de auditoría con filtros opcionales.
     * Sin filtros retorna los últimos 100 registros (paginado).
     *
     * @param page      número de página (0-based)
     * @param size      tamaño de página (máx 200)
     * @param modulo    filtrar por módulo (ej: VENTAS, AUTENTICACION)
     * @param usuario   filtrar por nombre de usuario
     * @param resultado filtrar por EXITOSO o FALLIDO
     * @param accion    filtrar por acción (ej: LOGIN, CREAR)
     * @param desde     fecha inicio (yyyy-MM-dd) en hora Bogotá
     * @param hasta     fecha fin   (yyyy-MM-dd) en hora Bogotá
     */
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String resultado,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        // Filtro por rango de fechas (tiene prioridad si se envían ambas)
        if (desde != null && hasta != null) {
            Instant instDesde = desde.atStartOfDay(ZONE).toInstant();
            Instant instHasta = hasta.plusDays(1).atStartOfDay(ZONE).toInstant();
            List<AuditoriaLog> logs = auditoriaLogRepository
                    .findByFechaHoraBetweenOrderByFechaHoraDesc(instDesde, instHasta);
            return ResponseEntity.ok(logs);
        }

        // Filtros simples
        if (modulo != null && !modulo.isBlank()) {
            return ResponseEntity.ok(
                    auditoriaLogRepository.findByModuloIgnoreCaseOrderByFechaHoraDesc(modulo));
        }
        if (usuario != null && !usuario.isBlank()) {
            return ResponseEntity.ok(
                    auditoriaLogRepository.findByUsuarioNombreIgnoreCaseOrderByFechaHoraDesc(usuario));
        }
        if (resultado != null && !resultado.isBlank()) {
            return ResponseEntity.ok(
                    auditoriaLogRepository.findByResultadoOrderByFechaHoraDesc(resultado));
        }
        if (accion != null && !accion.isBlank()) {
            return ResponseEntity.ok(
                    auditoriaLogRepository.findByAccionIgnoreCaseOrderByFechaHoraDesc(accion));
        }

        // Sin filtros: paginado
        int safeSize = Math.min(size, 200);
        Page<AuditoriaLog> pagina = auditoriaLogRepository
                .findAllByOrderByFechaHoraDesc(PageRequest.of(page, safeSize));
        return ResponseEntity.ok(pagina);
    }

    /**
     * Obtiene un registro de auditoría por ID.
     * GET /api/auditoria/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return auditoriaLogRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
