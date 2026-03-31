package com.unisof.insumos.controller;

import com.unisof.insumos.dto.ProveedorRequest;
import com.unisof.insumos.dto.ProveedorResponse;
import com.unisof.insumos.service.ProveedorService;
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

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    public List<ProveedorResponse> listar(
            @RequestParam(required = false) String criterio,
            @RequestParam(required = false) String categoriaCodigo,
            @RequestParam(required = false) String nit
    ) {
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

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody ProveedorRequest request) {
        try {
            ProveedorResponse creado = proveedorService.crear(request);
            Map<String, Object> body = new HashMap<>();
            body.put("mensaje", "Proveedor registrado correctamente.");
            body.put("proveedor", creado);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorRequest request) {
        try {
            ProveedorResponse actualizado = proveedorService.actualizar(id, request);
            Map<String, Object> body = new HashMap<>();
            body.put("mensaje", "Proveedor actualizado correctamente.");
            body.put("proveedor", actualizado);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            proveedorService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Proveedor eliminado correctamente."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }
}
