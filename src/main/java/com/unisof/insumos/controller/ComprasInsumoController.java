package com.unisof.insumos.controller;

import com.unisof.insumos.dto.CategoriaInsumoResponse;
import com.unisof.insumos.dto.InsumoRequest;
import com.unisof.insumos.dto.InsumoResponse;
import com.unisof.insumos.service.AuditoriaService;
import com.unisof.insumos.service.ComprasInsumoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API de insumos/inventario. SCRUM-64: Todas las operaciones de escritura
 * (crear, editar, eliminar) quedan registradas en auditoría.
 */
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class ComprasInsumoController {

    private final ComprasInsumoService comprasInsumoService;
    private final AuditoriaService auditoriaService;  // SCRUM-64

    @GetMapping("/categorias-insumo")
    public List<CategoriaInsumoResponse> categorias() {
        return comprasInsumoService.listarCategorias();
    }

    /**
     * Lista insumos con filtros opcionales. SCRUM-64: Registra consulta de inventario.
     */
    @GetMapping("/insumos")
    public List<InsumoResponse> insumos(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String criterio,
            @RequestParam(required = false, defaultValue = "nombre") String tipo,
            HttpServletRequest httpRequest) {

        List<InsumoResponse> resultado = comprasInsumoService.listarInsumos(categoria, criterio, tipo);
        String[] ui = auditoriaService.obtenerUsuarioInfo();
        auditoriaService.registrar(
                AuditoriaService.ACC_CONSULTAR, AuditoriaService.MOD_INSUMOS,
                "Consulta de inventario de insumos" +
                (criterio != null ? " — criterio: " + criterio : "") +
                (categoria != null ? ", categoría: " + categoria : ""),
                ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                AuditoriaService.RES_EXITOSO,
                "Resultados: " + resultado.size()
        );
        return resultado;
    }

    @GetMapping("/insumos/{id}")
    public ResponseEntity<InsumoResponse> insumoPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(comprasInsumoService.obtener(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crea un nuevo insumo. SCRUM-64: Registra CREAR en módulo INSUMOS.
     */
    @PostMapping("/insumos")
    public ResponseEntity<?> crear(
            @Valid @RequestBody InsumoRequest request,
            HttpServletRequest httpRequest) {
        try {
            InsumoResponse creado = comprasInsumoService.crear(request);
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_CREAR, AuditoriaService.MOD_INSUMOS,
                    "Insumo creado: " + creado.getNombre() + " (código=" + creado.getCodigo() + ")",
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_EXITOSO, "ID=" + creado.getId()
            );
            Map<String, Object> body = new HashMap<>();
            body.put("mensaje", "Insumo creado correctamente.");
            body.put("insumo", creado);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IllegalArgumentException ex) {
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_CREAR, AuditoriaService.MOD_INSUMOS,
                    "Intento fallido de crear insumo: " + request.getNombre(),
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_FALLIDO, "Error: " + ex.getMessage()
            );
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }

    /**
     * Actualiza un insumo. SCRUM-64: Registra EDITAR en módulo INSUMOS.
     */
    @PutMapping("/insumos/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody InsumoRequest request,
            HttpServletRequest httpRequest) {
        try {
            InsumoResponse actualizado = comprasInsumoService.actualizar(id, request);
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_EDITAR, AuditoriaService.MOD_INSUMOS,
                    "Insumo actualizado: " + actualizado.getNombre() +
                    " (código=" + actualizado.getCodigo() + ", stock=" + actualizado.getStockDisponible() + ")",
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_EXITOSO, "ID=" + id
            );
            Map<String, Object> body = new HashMap<>();
            body.put("mensaje", "Insumo actualizado correctamente.");
            body.put("insumo", actualizado);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException ex) {
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_EDITAR, AuditoriaService.MOD_INSUMOS,
                    "Intento fallido de actualizar insumo ID=" + id,
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_FALLIDO, "Error: " + ex.getMessage()
            );
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }

    /**
     * Elimina un insumo. SCRUM-64: Registra ELIMINAR en módulo INSUMOS.
     */
    @DeleteMapping("/insumos/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            comprasInsumoService.eliminar(id);
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_ELIMINAR, AuditoriaService.MOD_INSUMOS,
                    "Insumo desactivado del inventario (soft-delete)",
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_EXITOSO, "ID=" + id
            );
            return ResponseEntity.ok(Map.of("mensaje", "Insumo eliminado correctamente."));
        } catch (IllegalArgumentException ex) {
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_ELIMINAR, AuditoriaService.MOD_INSUMOS,
                    "Intento fallido de eliminar insumo ID=" + id,
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_FALLIDO, "Error: " + ex.getMessage()
            );
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }
}
