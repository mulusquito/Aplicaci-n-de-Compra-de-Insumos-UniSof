package com.unisof.insumos.controller;

import com.unisof.insumos.model.FichaTecnica;
import com.unisof.insumos.model.Insumo;
import com.unisof.insumos.repository.FichaTecnicaRepository;
import com.unisof.insumos.repository.InsumoRepository;
import com.unisof.insumos.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CRUD de fichas técnicas de fabricación.
 * Acceso: ADMINISTRADOR y JEFE DE COMPRAS (configurado en SecurityConfig).
 */
@RestController
@RequestMapping("/api/fichas-tecnicas")
@RequiredArgsConstructor
public class FichaTecnicaController {

    private final FichaTecnicaRepository fichaRepo;
    private final InsumoRepository insumoRepo;
    private final AuditoriaService auditoriaService;

    /** Lista de nombres de prenda que tienen ficha técnica. */
    @GetMapping("/prendas")
    public ResponseEntity<List<String>> listarPrendas() {
        return ResponseEntity.ok(fichaRepo.findDistinctNombrePrenda());
    }

    /** Mapa insumoId → lista de {nombrePrenda, genero} para mostrar en inventario. */
    @GetMapping("/prendas-por-insumo")
    public ResponseEntity<Map<Long, List<Map<String, String>>>> prendasPorInsumo() {
        List<Object[]> rows = fichaRepo.findPrendasPorInsumo();
        Map<Long, List<Map<String, String>>> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long insumoId = ((Number) row[0]).longValue();
            String nombrePrenda = (String) row[1];
            String genero = (String) row[2];
            result.computeIfAbsent(insumoId, k -> new java.util.ArrayList<>())
                  .add(Map.of("nombrePrenda", nombrePrenda, "genero", genero));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Consulta fichas:
     *  - sin parámetros → todas
     *  - ?prenda=xxx&genero=yyy → todas las tallas de esa prenda+género
     *  - ?prenda=xxx&genero=yyy&talla=zzz → combinación exacta
     */
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String prenda,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) String talla) {

        if (prenda == null || prenda.isBlank() || genero == null || genero.isBlank()) {
            return ResponseEntity.ok(fichaRepo.findAll().stream().map(this::toMap).toList());
        }
        if (talla == null || talla.isBlank()) {
            return ResponseEntity.ok(fichaRepo.findByPrendaAndGenero(prenda, genero).stream().map(this::toMap).toList());
        }
        return ResponseEntity.ok(fichaRepo.findByPrendaGeneroTalla(prenda, genero, talla).stream().map(this::toMap).toList());
    }

    /** Crea una nueva línea de ficha técnica. */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        String nombrePrenda = strVal(body, "nombrePrenda");
        String tallaVal     = strVal(body, "talla");
        Long   insumoId     = longVal(body, "insumoId");
        Double cantidad     = doubleVal(body, "cantidadRequerida");

        String generoVal = strVal(body, "genero");
        if (nombrePrenda == null || generoVal == null || tallaVal == null || insumoId == null || cantidad == null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Campos requeridos: nombrePrenda, genero, talla, insumoId, cantidadRequerida"));
        }
        if (cantidad <= 0) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "La cantidad debe ser mayor a cero"));
        }

        Insumo insumo = insumoRepo.findById(insumoId).orElse(null);
        if (insumo == null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Insumo no encontrado: id=" + insumoId));
        }

        FichaTecnica ft = new FichaTecnica(nombrePrenda, generoVal, tallaVal, insumo, BigDecimal.valueOf(cantidad));
        ft = fichaRepo.save(ft);

        String[] ui = auditoriaService.obtenerUsuarioInfo();
        auditoriaService.registrar(
                AuditoriaService.ACC_CREAR, AuditoriaService.MOD_COMPRAS,
                "Ficha técnica creada: " + nombrePrenda + " — talla " + tallaVal + " — " + insumo.getNombre(),
                ui[0], ui[1], auditoriaService.obtenerIp(req),
                AuditoriaService.RES_EXITOSO, "ID=" + ft.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(ft));
    }

    /** Actualiza la cantidad requerida de una línea de ficha. */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest req) {

        FichaTecnica ft = fichaRepo.findById(id).orElse(null);
        if (ft == null) return ResponseEntity.notFound().build();

        Double cantidad = doubleVal(body, "cantidadRequerida");
        if (cantidad == null || cantidad <= 0) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "cantidadRequerida debe ser mayor a cero"));
        }

        ft.setCantidadRequerida(BigDecimal.valueOf(cantidad));
        fichaRepo.save(ft);

        String[] ui = auditoriaService.obtenerUsuarioInfo();
        auditoriaService.registrar(
                AuditoriaService.ACC_EDITAR, AuditoriaService.MOD_COMPRAS,
                "Ficha técnica actualizada: " + ft.getNombrePrenda() + " — talla " + ft.getTalla() + " — " + ft.getInsumo().getNombre(),
                ui[0], ui[1], auditoriaService.obtenerIp(req),
                AuditoriaService.RES_EXITOSO, "ID=" + id);

        return ResponseEntity.ok(toMap(ft));
    }

    /** Elimina una línea de ficha técnica. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, HttpServletRequest req) {
        FichaTecnica ft = fichaRepo.findById(id).orElse(null);
        if (ft == null) return ResponseEntity.notFound().build();

        String desc = ft.getNombrePrenda() + " — talla " + ft.getTalla() + " — " + ft.getInsumo().getNombre();
        fichaRepo.deleteById(id);

        String[] ui = auditoriaService.obtenerUsuarioInfo();
        auditoriaService.registrar(
                AuditoriaService.ACC_ELIMINAR, AuditoriaService.MOD_COMPRAS,
                "Ficha técnica eliminada: " + desc,
                ui[0], ui[1], auditoriaService.obtenerIp(req),
                AuditoriaService.RES_EXITOSO, "ID=" + id);

        return ResponseEntity.ok(Map.of("mensaje", "Línea de ficha eliminada correctamente"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    private Map<String, Object> toMap(FichaTecnica ft) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",                ft.getId());
        m.put("nombrePrenda",      ft.getNombrePrenda());
        m.put("genero",            ft.getGenero());
        m.put("talla",             ft.getTalla());
        m.put("insumoId",          ft.getInsumo().getId());
        m.put("insumoCodigo",      ft.getInsumo().getCodigo());
        m.put("insumoNombre",      ft.getInsumo().getNombre());
        m.put("unidadMedida",      ft.getInsumo().getUnidadMedida());
        m.put("cantidadRequerida", ft.getCantidadRequerida());
        return m;
    }

    private String strVal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return (v instanceof String s && !s.isBlank()) ? s.trim() : null;
    }

    private Long longVal(Map<String, Object> m, String key) {
        try { return m.get(key) != null ? Long.valueOf(m.get(key).toString()) : null; }
        catch (NumberFormatException e) { return null; }
    }

    private Double doubleVal(Map<String, Object> m, String key) {
        try { return m.get(key) != null ? Double.valueOf(m.get(key).toString()) : null; }
        catch (NumberFormatException e) { return null; }
    }
}
