package com.unisof.insumos.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unisof.insumos.model.AnalisisOrden;
import com.unisof.insumos.model.FichaTecnica;
import com.unisof.insumos.model.Insumo;
import com.unisof.insumos.model.Recibo;
import com.unisof.insumos.repository.AnalisisOrdenRepository;
import com.unisof.insumos.repository.FichaTecnicaRepository;
import com.unisof.insumos.repository.InsumoRepository;
import com.unisof.insumos.repository.ReciboRepository;
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
 * Genera y consulta análisis de órdenes de compra contra fichas técnicas.
 *
 * POST /api/analisis-ordenes/{reciboId}               → analiza y guarda; pone la orden en "POR REVISAR"
 * POST /api/analisis-ordenes/{id}/calcular-faltantes  → descuenta inventario; actualiza estado de producción
 * GET  /api/analisis-ordenes                          → lista (con filtros opcionales)
 * GET  /api/analisis-ordenes/{id}                     → detalle por id de análisis
 * SCRUM-53.
 */
@RestController
@RequestMapping("/api/analisis-ordenes")
@RequiredArgsConstructor
public class AnalisisOrdenController {

    private final ReciboRepository        reciboRepo;
    private final FichaTecnicaRepository  fichaRepo;
    private final InsumoRepository        insumoRepo;
    private final AnalisisOrdenRepository analisisRepo;
    private final ObjectMapper            objectMapper;

    /** Estados que corresponden a pago confirmado (antes de entrar al flujo de producción). */
    private static final Set<String> ESTADOS_PAGO = Set.of("PAGADO", "PENDIENTE", "APROBADO", "APPROVED");

    // ─── Generar análisis de ficha técnica ──────────────────────────────────

    @PostMapping("/{reciboId}")
    @Transactional
    public ResponseEntity<?> analizar(@PathVariable Long reciboId) {

        Recibo recibo = reciboRepo.findById(reciboId).orElse(null);
        if (recibo == null) return ResponseEntity.notFound().build();

        // Parsear ítems de la orden
        List<Map<String, Object>> orderItems = parseJsonList(recibo.getItemsJson());

        // Analizar cada ítem contra fichas técnicas
        List<Map<String, Object>> analysisItems = new ArrayList<>();
        for (Map<String, Object> oi : orderItems) {
            String prenda   = strVal(oi, "nombre");
            String genero   = strVal(oi, "genero");
            String talla    = strVal(oi, "talla");
            int    cantidad = intVal(oi, "cantidad");

            if (prenda == null || prenda.isBlank()) continue;
            if (genero == null || genero.isBlank()) genero = "Caballero";
            if (talla  == null || talla.isBlank())  talla  = "M";

            List<FichaTecnica> fichas = fichaRepo.findByPrendaGeneroTalla(prenda, genero, talla);

            List<Map<String, Object>> insumos = new ArrayList<>();
            for (FichaTecnica ft : fichas) {
                BigDecimal cantUnit  = ft.getCantidadRequerida();
                BigDecimal cantTotal = cantUnit.multiply(BigDecimal.valueOf(cantidad));
                Map<String, Object> ins = new LinkedHashMap<>();
                ins.put("insumoId",        ft.getInsumo().getId());
                ins.put("insumoNombre",    ft.getInsumo().getNombre());
                ins.put("unidadMedida",    ft.getInsumo().getUnidadMedida());
                ins.put("cantidadUnitaria", cantUnit);
                ins.put("cantidadTotal",   cantTotal);
                insumos.add(ins);
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("prenda",                 prenda);
            item.put("genero",                 genero);
            item.put("talla",                  talla);
            item.put("cantidad",               cantidad);
            item.put("fichaTecnicaEncontrada", !fichas.isEmpty());
            item.put("insumos",                insumos);
            analysisItems.add(item);
        }

        // Construir JSON resultado
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("ordenId",       recibo.getId());
        resultado.put("ordenNumero",   recibo.getNumero());
        resultado.put("clienteNombre", recibo.getCliente() != null ? recibo.getCliente().getNombre() : "—");
        resultado.put("fechaOrden",    recibo.getFecha().toString());
        resultado.put("estado",        recibo.getEstado());
        resultado.put("items",         analysisItems);

        String json;
        try { json = objectMapper.writeValueAsString(resultado); }
        catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("mensaje", "Error serializando resultado: " + e.getMessage()));
        }

        // Si la orden está en estado de pago → pasa a "POR REVISAR" (entra al flujo de producción)
        String estadoActual = recibo.getEstado() != null ? recibo.getEstado().toUpperCase() : "";
        if (ESTADOS_PAGO.contains(estadoActual)) {
            recibo.setEstado("POR REVISAR");
            reciboRepo.save(recibo);
        }

        // Guardar o actualizar análisis
        String clienteNombre = recibo.getCliente() != null ? recibo.getCliente().getNombre() : "—";
        AnalisisOrden analisis = analisisRepo.findByOrdenId(recibo.getId()).orElse(new AnalisisOrden());
        analisis.setOrdenId(recibo.getId());
        analisis.setOrdenNumero(recibo.getNumero());
        analisis.setClienteNombre(clienteNombre);
        analisis.setFechaOrden(recibo.getFecha());
        analisis.setEstado(recibo.getEstado()); // "POR REVISAR" o el estado actual de producción
        analisis.setFechaAnalisis(Instant.now());
        analisis.setResultadoJson(json);
        // Si se re-analiza, resetear faltantes (la ficha pudo haber cambiado)
        analisis.setFaltantesCalculados(false);
        analisis.setFechaCalculo(null);
        analisis.setFaltantesJson(null);
        analisis = analisisRepo.save(analisis);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("id",        analisis.getId());
        respuesta.put("mensaje",   "Análisis generado y guardado en Reporte faltantes");
        respuesta.put("resultado", resultado);
        return ResponseEntity.ok(respuesta);
    }

    // ─── Calcular y descontar insumos faltantes ──────────────────────────────

    @PostMapping("/{id}/calcular-faltantes")
    @Transactional
    public ResponseEntity<?> calcularFaltantes(@PathVariable Long id) {

        AnalisisOrden analisis = analisisRepo.findById(id).orElse(null);
        if (analisis == null) return ResponseEntity.notFound().build();

        // Si ya fue calculado, devolver resultado existente sin modificar inventario
        if (analisis.isFaltantesCalculados()) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("yaCalculado", true);
            resp.put("mensaje",     "Este análisis ya fue calculado. Los insumos ya fueron descontados del inventario.");
            resp.put("estado",      analisis.getEstado());
            try {
                if (analisis.getFaltantesJson() != null) {
                    resp.put("resultado",
                        objectMapper.readValue(analisis.getFaltantesJson(), new TypeReference<Map<String, Object>>() {}));
                }
            } catch (Exception ignored) {}
            return ResponseEntity.ok(resp);
        }

        // Parsear ítems del análisis original
        List<Map<String, Object>> items = parseAnalysisItems(analisis.getResultadoJson());

        boolean todoCompleto = true;
        List<Map<String, Object>> faltantesItems = new ArrayList<>();

        for (Map<String, Object> item : items) {
            String  prenda   = strVal(item, "prenda");
            String  genero   = strVal(item, "genero");
            String  talla    = strVal(item, "talla");
            int     cantidad = intVal(item, "cantidad");
            boolean ftFound  = Boolean.TRUE.equals(item.get("fichaTecnicaEncontrada"));

            List<Map<String, Object>> insAnalisis = extractInsumos(item);
            boolean itemCompleto = ftFound;
            List<Map<String, Object>> insResult   = new ArrayList<>();

            for (Map<String, Object> ins : insAnalisis) {
                Long insumoId = toLong(ins.get("insumoId"));
                if (insumoId == null) continue;

                Insumo insumo = insumoRepo.findById(insumoId).orElse(null);
                if (insumo == null) continue;

                BigDecimal cantRequerida = toBigDecimal(ins.get("cantidadTotal"));
                BigDecimal stockAntes    = insumo.getStockDisponible();

                BigDecimal consumir;
                BigDecimal faltante;
                boolean    insCompleto;

                if (stockAntes.compareTo(cantRequerida) >= 0) {
                    consumir   = cantRequerida;
                    faltante   = BigDecimal.ZERO;
                    insCompleto = true;
                } else {
                    consumir   = stockAntes;
                    faltante   = cantRequerida.subtract(stockAntes).stripTrailingZeros();
                    insCompleto = false;
                    itemCompleto = false;
                    todoCompleto = false;
                }

                // Descontar del inventario
                insumo.setStockDisponible(stockAntes.subtract(consumir));
                insumoRepo.save(insumo);

                Map<String, Object> r = new LinkedHashMap<>();
                r.put("insumoId",          insumoId);
                r.put("insumoNombre",      ins.get("insumoNombre"));
                r.put("unidadMedida",      ins.get("unidadMedida"));
                r.put("cantidadRequerida", cantRequerida);
                r.put("stockAntes",        stockAntes);
                r.put("consumido",         consumir);
                r.put("faltante",          faltante);
                r.put("completo",          insCompleto);
                insResult.add(r);
            }

            if (!ftFound) todoCompleto = false;

            Map<String, Object> itemR = new LinkedHashMap<>();
            itemR.put("prenda",                 prenda);
            itemR.put("genero",                 genero);
            itemR.put("talla",                  talla);
            itemR.put("cantidad",               cantidad);
            itemR.put("completo",               itemCompleto);
            itemR.put("fichaTecnicaEncontrada", ftFound);
            itemR.put("insumos",                insResult);
            faltantesItems.add(itemR);
        }

        Map<String, Object> faltantesResultado = new LinkedHashMap<>();
        faltantesResultado.put("todoCompleto", todoCompleto);
        faltantesResultado.put("items",        faltantesItems);

        String faltantesJson;
        try { faltantesJson = objectMapper.writeValueAsString(faltantesResultado); }
        catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("mensaje", "Error serializando faltantes: " + e.getMessage()));
        }

        // Nuevo estado según resultado
        String nuevoEstado = todoCompleto ? "EN CONFECCION" : "EN ESPERA POR PRODUCCION";

        // Actualizar análisis
        analisis.setFaltantesCalculados(true);
        analisis.setFechaCalculo(Instant.now());
        analisis.setFaltantesJson(faltantesJson);
        analisis.setEstado(nuevoEstado);
        analisisRepo.save(analisis);

        // Actualizar estado del Recibo
        Recibo recibo = reciboRepo.findById(analisis.getOrdenId()).orElse(null);
        if (recibo != null) {
            recibo.setEstado(nuevoEstado);
            reciboRepo.save(recibo);
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("yaCalculado",  false);
        respuesta.put("todoCompleto", todoCompleto);
        respuesta.put("nuevoEstado",  nuevoEstado);
        respuesta.put("mensaje", todoCompleto
                ? "¡Todos los insumos están disponibles! La orden pasa a En confección."
                : "Algunos insumos no tienen stock suficiente. La orden queda En espera por producción.");
        respuesta.put("resultado", faltantesResultado);
        return ResponseEntity.ok(respuesta);
    }

    // ─── Listar solo análisis con faltantes ──────────────────────────────────

    @GetMapping("/con-faltantes")
    public ResponseEntity<?> listarConFaltantes() {
        return ResponseEntity.ok(
            analisisRepo.findConFaltantes().stream().map(a -> {
                Map<String, Object> m = toSummaryMap(a);
                // Extraer qué insumos faltan y en qué prendas
                if (a.getFaltantesJson() != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> fj = objectMapper.readValue(a.getFaltantesJson(),
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>() {});
                        @SuppressWarnings("unchecked")
                        List<Map<String,Object>> items = (List<Map<String,Object>>) fj.getOrDefault("items", List.of());
                        long prendasFaltantes = items.stream()
                                .filter(i -> !Boolean.TRUE.equals(i.get("completo")))
                                .count();
                        m.put("prendasConFaltantes", prendasFaltantes);
                    } catch (Exception ignored) {}
                }
                return m;
            }).toList()
        );
    }

    // ─── Listar análisis ─────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String mes) {

        List<AnalisisOrden> lista;

        if (numero != null && !numero.isBlank()) {
            try {
                int num = Integer.parseInt(numero.trim().replaceAll("^0+(?!$)", ""));
                lista = analisisRepo.findByOrdenNumeroOrderByFechaAnalisisDesc(num);
            } catch (NumberFormatException e) { lista = List.of(); }
        } else if (fecha != null && !fecha.isBlank()) {
            try {
                LocalDate d      = LocalDate.parse(fecha);
                Instant   inicio = d.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant   fin    = d.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                lista = analisisRepo.findByFechaOrdenBetween(inicio, fin);
            } catch (Exception e) { lista = List.of(); }
        } else if (mes != null && !mes.isBlank()) {
            try {
                YearMonth ym    = YearMonth.parse(mes);
                Instant   inicio = ym.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant   fin    = ym.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                lista = analisisRepo.findByMes(inicio, fin);
            } catch (Exception e) { lista = List.of(); }
        } else {
            lista = analisisRepo.findAllByOrderByFechaAnalisisDesc();
        }

        return ResponseEntity.ok(lista.stream().map(this::toSummaryMap).toList());
    }

    // ─── Detalle de un análisis ───────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable Long id) {
        AnalisisOrden analisis = analisisRepo.findById(id).orElse(null);
        if (analisis == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toDetailMap(analisis));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Map<String, Object> toSummaryMap(AnalisisOrden a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",                    a.getId());
        m.put("ordenId",               a.getOrdenId());
        m.put("ordenNumero",           a.getOrdenNumero());
        m.put("ordenNumeroFormateado", String.format("%03d", a.getOrdenNumero()));
        m.put("clienteNombre",         a.getClienteNombre());
        m.put("fechaOrden",            a.getFechaOrden().toString());
        m.put("estado",                a.getEstado());
        m.put("fechaAnalisis",         a.getFechaAnalisis().toString());
        m.put("faltantesCalculados",   a.isFaltantesCalculados());
        m.put("fechaCalculo",          a.getFechaCalculo() != null ? a.getFechaCalculo().toString() : null);
        return m;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toDetailMap(AnalisisOrden a) {
        Map<String, Object> m = toSummaryMap(a);
        // Ítems del análisis ficha técnica
        try {
            Map<String, Object> res = objectMapper.readValue(a.getResultadoJson(),
                    new TypeReference<Map<String, Object>>() {});
            m.put("items", res.getOrDefault("items", List.of()));
        } catch (Exception e) {
            m.put("items", List.of());
        }
        // Resultado de faltantes si ya fue calculado
        if (a.isFaltantesCalculados() && a.getFaltantesJson() != null) {
            try {
                m.put("faltantesResultado", objectMapper.readValue(a.getFaltantesJson(),
                        new TypeReference<Map<String, Object>>() {}));
            } catch (Exception ignored) {}
        }
        return m;
    }

    // Parsea la lista de ítems de itemsJson (campo del Recibo)
    private List<Map<String, Object>> parseJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) { return List.of(); }
    }

    // Parsea los ítems del resultadoJson del AnalisisOrden
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseAnalysisItems(String resultadoJson) {
        if (resultadoJson == null) return List.of();
        try {
            Map<String, Object> r = objectMapper.readValue(resultadoJson,
                    new TypeReference<Map<String, Object>>() {});
            Object items = r.get("items");
            if (items instanceof List<?> l) {
                return l.stream()
                        .filter(i -> i instanceof Map)
                        .map(i -> (Map<String, Object>) i)
                        .toList();
            }
        } catch (Exception ignored) {}
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractInsumos(Map<String, Object> item) {
        Object ins = item.get("insumos");
        if (ins instanceof List<?> l) {
            return l.stream()
                    .filter(i -> i instanceof Map)
                    .map(i -> (Map<String, Object>) i)
                    .toList();
        }
        return List.of();
    }

    private String strVal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return (v instanceof String s && !s.isBlank()) ? s.trim() : null;
    }

    private int intVal(Map<String, Object> m, String key) {
        try {
            Object v = m.get(key);
            return v != null ? Integer.parseInt(v.toString()) : 1;
        } catch (NumberFormatException e) { return 1; }
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        try { return Long.valueOf(val.toString()); } catch (Exception e) { return null; }
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        try { return new BigDecimal(val.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
