package com.unisof.insumos.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unisof.insumos.model.AnalisisOrden;
import com.unisof.insumos.model.ReporteConsolidado;
import com.unisof.insumos.repository.AnalisisOrdenRepository;
import com.unisof.insumos.repository.ReporteConsolidadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Genera y consulta reportes consolidados de insumos faltantes.
 *
 * POST /api/reportes-consolidados         → crear reporte a partir de analisisIds seleccionados
 * GET  /api/reportes-consolidados         → listar (filtros: fecha, mes)
 * GET  /api/reportes-consolidados/{id}    → detalle
 */
@RestController
@RequestMapping("/api/reportes-consolidados")
@RequiredArgsConstructor
public class ReporteConsolidadoController {

    private final ReporteConsolidadoRepository reporteRepo;
    private final AnalisisOrdenRepository      analisisRepo;
    private final ObjectMapper                 objectMapper;

    // ─── Crear reporte consolidado ────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    @PostMapping
    @Transactional
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {

        Object rawIds = body.get("analisisIds");
        if (rawIds == null) return ResponseEntity.badRequest().body(Map.of("mensaje", "analisisIds requerido"));

        List<Long> ids;
        try {
            ids = ((List<?>) rawIds).stream()
                    .map(v -> Long.valueOf(v.toString()))
                    .toList();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "analisisIds inválido"));
        }
        if (ids.isEmpty()) return ResponseEntity.badRequest().body(Map.of("mensaje", "Selecciona al menos una orden"));

        // ── Consolidar insumos faltantes ──
        // Mapa: insumoNombre → {unidadMedida, cantidadTotal}
        Map<String, double[]>  consolidado     = new LinkedHashMap<>();
        Map<String, String>    unidades        = new LinkedHashMap<>();
        List<Map<String, Object>> ordenesInfo  = new ArrayList<>();

        for (Long analisisId : ids) {
            AnalisisOrden analisis = analisisRepo.findById(analisisId).orElse(null);
            if (analisis == null || !analisis.isFaltantesCalculados() || analisis.getFaltantesJson() == null)
                continue;

            try {
                Map<String, Object> fj = objectMapper.readValue(analisis.getFaltantesJson(),
                        new TypeReference<Map<String, Object>>() {});
                List<Map<String, Object>> items = (List<Map<String, Object>>) fj.getOrDefault("items", List.of());

                for (Map<String, Object> item : items) {
                    List<Map<String, Object>> insumos = (List<Map<String, Object>>) item.getOrDefault("insumos", List.of());
                    for (Map<String, Object> ins : insumos) {
                        BigDecimal faltante = toBigDecimal(ins.get("faltante"));
                        if (faltante.compareTo(BigDecimal.ZERO) <= 0) continue;

                        String nombre  = strVal(ins, "insumoNombre");
                        String unidad  = strVal(ins, "unidadMedida");
                        if (nombre == null) continue;

                        consolidado.merge(nombre, new double[]{faltante.doubleValue()}, (a, b) -> new double[]{a[0] + b[0]});
                        unidades.putIfAbsent(nombre, unidad != null ? unidad : "—");
                    }
                }

                // Marcar como consolidado
                analisis.setConsolidado(true);
                analisisRepo.save(analisis);

            } catch (Exception ignored) {}

            Map<String, Object> info = new LinkedHashMap<>();
            info.put("analisisId",    analisis.getId());
            info.put("ordenNumero",   analisis.getOrdenNumero());
            info.put("clienteNombre", analisis.getClienteNombre());
            ordenesInfo.add(info);
        }

        if (ordenesInfo.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Ninguna orden seleccionada tiene faltantes calculados"));

        // Construir lista de insumos consolidados
        List<Map<String, Object>> insumosConsolidados = new ArrayList<>();
        for (Map.Entry<String, double[]> e : consolidado.entrySet()) {
            Map<String, Object> ins = new LinkedHashMap<>();
            ins.put("insumoNombre",   e.getKey());
            ins.put("unidadMedida",   unidades.get(e.getKey()));
            ins.put("cantidadTotal",  e.getValue()[0]);
            insumosConsolidados.add(ins);
        }

        String ordenesJson;
        String insumosJson;
        try {
            ordenesJson = objectMapper.writeValueAsString(ordenesInfo);
            insumosJson = objectMapper.writeValueAsString(insumosConsolidados);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("mensaje", "Error serializando reporte"));
        }

        ReporteConsolidado reporte = new ReporteConsolidado();
        reporte.setFecha(Instant.now());
        reporte.setOrdenesJson(ordenesJson);
        reporte.setInsumosJson(insumosJson);
        reporte.setTotalInsumosDistintos(insumosConsolidados.size());
        reporte = reporteRepo.save(reporte);

        return ResponseEntity.ok(Map.of(
                "id",      reporte.getId(),
                "mensaje", "Reporte consolidado generado correctamente. Puedes consultarlo en el apartado Órdenes de Compra.",
                "totalInsumosDistintos", reporte.getTotalInsumosDistintos()
        ));
    }

    // ─── Listar reportes ──────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String mes) {

        List<ReporteConsolidado> lista;

        if (fecha != null && !fecha.isBlank()) {
            try {
                LocalDate d      = LocalDate.parse(fecha);
                Instant   inicio = d.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant   fin    = d.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                lista = reporteRepo.findByFechaBetween(inicio, fin);
            } catch (Exception e) { lista = List.of(); }
        } else if (mes != null && !mes.isBlank()) {
            try {
                YearMonth ym    = YearMonth.parse(mes);
                Instant inicio  = ym.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant fin     = ym.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                lista = reporteRepo.findByFechaBetween(inicio, fin);
            } catch (Exception e) { lista = List.of(); }
        } else {
            lista = reporteRepo.findAllByOrderByFechaDesc();
        }

        return ResponseEntity.ok(lista.stream().map(this::toSummary).toList());
    }

    // ─── Detalle ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable Long id) {
        ReporteConsolidado r = reporteRepo.findById(id).orElse(null);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toDetail(r));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Map<String, Object> toSummary(ReporteConsolidado r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",                    r.getId());
        m.put("fecha",                 r.getFecha().toString());
        m.put("totalInsumosDistintos", r.getTotalInsumosDistintos());
        // Extraer números de orden de ordenesJson
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> ordenes = objectMapper.readValue(r.getOrdenesJson(),
                    new TypeReference<List<Map<String, Object>>>() {});
            String nums = ordenes.stream()
                    .map(o -> "#" + String.format("%03d", toLong(o.get("ordenNumero"))))
                    .reduce((a, b) -> a + ", " + b).orElse("—");
            m.put("ordenesNums",   nums);
            m.put("ordenesCount",  ordenes.size());
        } catch (Exception e) {
            m.put("ordenesNums",  "—");
            m.put("ordenesCount", 0);
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toDetail(ReporteConsolidado r) {
        Map<String, Object> m = toSummary(r);
        try {
            m.put("ordenes", objectMapper.readValue(r.getOrdenesJson(),
                    new TypeReference<List<Map<String, Object>>>() {}));
        } catch (Exception e) { m.put("ordenes", List.of()); }
        try {
            m.put("insumos", objectMapper.readValue(r.getInsumosJson(),
                    new TypeReference<List<Map<String, Object>>>() {}));
        } catch (Exception e) { m.put("insumos", List.of()); }
        return m;
    }

    private String strVal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return (v instanceof String s && !s.isBlank()) ? s.trim() : null;
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        try { return new BigDecimal(val.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private Long toLong(Object val) {
        if (val == null) return 0L;
        try { return Long.valueOf(val.toString()); } catch (Exception e) { return 0L; }
    }
}
