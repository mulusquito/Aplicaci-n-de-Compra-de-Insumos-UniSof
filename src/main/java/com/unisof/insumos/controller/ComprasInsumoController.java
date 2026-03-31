package com.unisof.insumos.controller;

import com.unisof.insumos.dto.CategoriaInsumoResponse;
import com.unisof.insumos.dto.InsumoRequest;
import com.unisof.insumos.dto.InsumoResponse;
import com.unisof.insumos.service.ComprasInsumoService;
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
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class ComprasInsumoController {

    private final ComprasInsumoService comprasInsumoService;

    @GetMapping("/categorias-insumo")
    public List<CategoriaInsumoResponse> categorias() {
        return comprasInsumoService.listarCategorias();
    }

    @GetMapping("/insumos")
    public List<InsumoResponse> insumos(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String criterio,
            @RequestParam(required = false, defaultValue = "nombre") String tipo
    ) {
        return comprasInsumoService.listarInsumos(categoria, criterio, tipo);
    }

    @GetMapping("/insumos/{id}")
    public ResponseEntity<InsumoResponse> insumoPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(comprasInsumoService.obtener(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/insumos")
    public ResponseEntity<?> crear(@Valid @RequestBody InsumoRequest request) {
        try {
            InsumoResponse creado = comprasInsumoService.crear(request);
            Map<String, Object> body = new HashMap<>();
            body.put("mensaje", "Insumo creado correctamente.");
            body.put("insumo", creado);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }

    @PutMapping("/insumos/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody InsumoRequest request) {
        try {
            InsumoResponse actualizado = comprasInsumoService.actualizar(id, request);
            Map<String, Object> body = new HashMap<>();
            body.put("mensaje", "Insumo actualizado correctamente.");
            body.put("insumo", actualizado);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }

    @DeleteMapping("/insumos/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            comprasInsumoService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Insumo eliminado correctamente."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }
}
