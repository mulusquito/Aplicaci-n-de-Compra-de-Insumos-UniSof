package com.unisof.insumos.controller;

import com.unisof.insumos.dto.ProveedorRequest;
import com.unisof.insumos.dto.ProveedorResponse;
import com.unisof.insumos.service.AuditoriaService;
import com.unisof.insumos.service.ProveedorService;
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
 * API CRUD de proveedores.
 * SCRUM-64: Creación, edición y eliminación registradas en auditoría.
 */
@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;
    private final AuditoriaService auditoriaService;  // SCRUM-64

    @GetMapping
    public List<ProveedorResponse> listar(
            @RequestParam(required = false) String criterio,
            @RequestParam(required = false) String categoriaCodigo,
            @RequestParam(required = false) String nit) {
        return proveedorService.listar(criterio, categoriaCodigo, nit);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponse> obtener(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(proveedorService.obtener(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Registra un nuevo proveedor. SCRUM-64: Registra CREAR en módulo PROVEEDORES.
     */
    @PostMapping
    public ResponseEntity<?> crear(
            @Valid @RequestBody ProveedorRequest request,
            HttpServletRequest httpRequest) {
        try {
            ProveedorResponse creado = proveedorService.crear(request);
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_CREAR, AuditoriaService.MOD_PROVEEDORES,
                    "Proveedor creado: " + creado.getNombre() + " (NIT=" + creado.getNit() + ")",
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_EXITOSO, "ID=" + creado.getId()
            );
            Map<String, Object> body = new HashMap<>();
            body.put("mensaje", "Proveedor registrado correctamente.");
            body.put("proveedor", creado);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IllegalArgumentException ex) {
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_CREAR, AuditoriaService.MOD_PROVEEDORES,
                    "Intento fallido de crear proveedor: " + request.getNombre(),
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_FALLIDO, "Error: " + ex.getMessage()
            );
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }

    /**
     * Actualiza un proveedor. SCRUM-64: Registra EDITAR en módulo PROVEEDORES.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProveedorRequest request,
            HttpServletRequest httpRequest) {
        try {
            ProveedorResponse actualizado = proveedorService.actualizar(id, request);
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_EDITAR, AuditoriaService.MOD_PROVEEDORES,
                    "Proveedor actualizado: " + actualizado.getNombre() +
                    " (NIT=" + actualizado.getNit() + ")",
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_EXITOSO, "ID=" + id
            );
            Map<String, Object> body = new HashMap<>();
            body.put("mensaje", "Proveedor actualizado correctamente.");
            body.put("proveedor", actualizado);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException ex) {
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_EDITAR, AuditoriaService.MOD_PROVEEDORES,
                    "Intento fallido de actualizar proveedor ID=" + id,
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_FALLIDO, "Error: " + ex.getMessage()
            );
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }

    /**
     * Elimina un proveedor. SCRUM-64: Registra ELIMINAR en módulo PROVEEDORES.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            proveedorService.eliminar(id);
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_ELIMINAR, AuditoriaService.MOD_PROVEEDORES,
                    "Proveedor eliminado del sistema",
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_EXITOSO, "ID=" + id
            );
            return ResponseEntity.ok(Map.of("mensaje", "Proveedor eliminado correctamente."));
        } catch (IllegalArgumentException ex) {
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_ELIMINAR, AuditoriaService.MOD_PROVEEDORES,
                    "Intento fallido de eliminar proveedor ID=" + id,
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_FALLIDO, "Error: " + ex.getMessage()
            );
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }
}
