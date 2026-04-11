package com.unisof.insumos.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unisof.insumos.model.*;
import com.unisof.insumos.repository.*;
import com.unisof.insumos.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CAMBIO 2/3: Facturas a Proveedores.
 *
 * POST /api/facturas-proveedor              → generar facturas desde reportes consolidados
 * GET  /api/facturas-proveedor              → listar agrupadas por proveedor
 * GET  /api/facturas-proveedor/{id}         → detalle con detalles de insumos
 * PATCH /api/facturas-proveedor/detalle/{id}/cantidad → ajustar cantidadAPedir
 * POST /api/facturas-proveedor/{id}/enviar  → enviar por correo + marcar ENVIADA
 */
@RestController
@RequestMapping("/api/facturas-proveedor")
@RequiredArgsConstructor
public class FacturaProveedorController {

    private final FacturaProveedorRepository       facturaRepo;
    private final DetalleFacturaProveedorRepository detalleRepo;
    private final ReporteConsolidadoRepository     reporteRepo;
    private final InsumoRepository                 insumoRepo;
    private final ProveedorRepository              proveedorRepo;
    private final EmailService                     emailService;
    private final ObjectMapper                     objectMapper;

    // ─── Generar facturas ─────────────────────────────────────────────────────

    @SuppressWarnings({"unchecked", "rawtypes"})
    @PostMapping
    @Transactional
    public ResponseEntity<?> generar(@RequestBody Map<String, Object> body) {

        Object rawIds = body.get("reporteIds");
        if (rawIds == null)
            return ResponseEntity.badRequest().body(Map.of("mensaje", "reporteIds requerido"));

        List<Long> reporteIds;
        try {
            reporteIds = ((List<?>) rawIds).stream()
                    .map(v -> Long.valueOf(v.toString()))
                    .toList();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "reporteIds inválido"));
        }
        if (reporteIds.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Selecciona al menos un reporte"));

        // ── Índice: lowercase(nombre) → Insumo (categoría es EAGER) ──────────
        List<Insumo> todosInsumos = insumoRepo.findAllByOrderByCategoria_CodigoAscNombreAsc();
        Map<String, Insumo> insumoMap = new HashMap<>();
        for (Insumo ins : todosInsumos) {
            insumoMap.put(ins.getNombre().trim().toLowerCase(), ins);
        }

        // ── Índice: categoriaId → Proveedor ───────────────────────────────────
        List<Proveedor> todosProveedores = proveedorRepo.findAllWithCategoriasOrderByNombreAsc();
        Map<Long, Proveedor> catIdToProveedor = new HashMap<>();
        Proveedor otrosProveedor = null;
        for (Proveedor p : todosProveedores) {
            for (CategoriaInsumo c : p.getCategorias()) {
                catIdToProveedor.put(c.getId(), p);
            }
            if ("901-PROV-OTR-07".equals(p.getNit())) otrosProveedor = p;
        }

        // ── Agrupar insumos por proveedor ─────────────────────────────────────
        Map<Long, Proveedor> groupedProveedores = new LinkedHashMap<>();
        Map<Long, List<Map<String, Object>>> groupedInsumos = new LinkedHashMap<>();
        List<Map<String, Object>> reportesInfo = new ArrayList<>();

        for (Long rId : reporteIds) {
            ReporteConsolidado reporte = reporteRepo.findById(rId).orElse(null);
            if (reporte == null || reporte.getInsumosJson() == null) continue;

            Map<String, Object> ri = new LinkedHashMap<>();
            ri.put("reporteId", reporte.getId());
            ri.put("fecha", reporte.getFecha().toString());
            reportesInfo.add(ri);

            List<Map<String, Object>> insumos;
            try {
                insumos = objectMapper.readValue(reporte.getInsumosJson(),
                        new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception e) {
                continue;
            }

            for (Map<String, Object> ins : insumos) {
                String nombre = strVal(ins, "insumoNombre");
                String unidad = strVal(ins, "unidadMedida");
                BigDecimal cant = toBigDecimal(ins.get("cantidadTotal"));
                if (nombre == null || cant.compareTo(BigDecimal.ZERO) <= 0) continue;

                // Encontrar proveedor
                Proveedor proveedor = otrosProveedor;
                Insumo insumoObj = insumoMap.get(nombre.trim().toLowerCase());
                if (insumoObj != null && insumoObj.getCategoria() != null) {
                    Proveedor found = catIdToProveedor.get(insumoObj.getCategoria().getId());
                    if (found != null) proveedor = found;
                }
                if (proveedor == null) continue;

                final Long provId = proveedor.getId();
                groupedProveedores.put(provId, proveedor);
                List<Map<String, Object>> provInsumos = groupedInsumos.computeIfAbsent(provId, k -> new ArrayList<>());

                // Merge cantidades si el mismo insumo aparece en varios reportes
                boolean merged = false;
                for (Map<String, Object> existing : provInsumos) {
                    if (nombre.equalsIgnoreCase((String) existing.get("nombre"))) {
                        BigDecimal existingCant = (BigDecimal) existing.get("cantidad");
                        existing.put("cantidad", existingCant.add(cant));
                        merged = true;
                        break;
                    }
                }
                if (!merged) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("nombre", nombre);
                    item.put("unidad", unidad != null ? unidad : "—");
                    item.put("cantidad", cant);
                    provInsumos.add(item);
                }
            }
        }

        if (groupedProveedores.isEmpty())
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "No se encontraron insumos con proveedores asignados"));

        String reportesJson;
        try {
            reportesJson = objectMapper.writeValueAsString(reportesInfo);
        } catch (Exception e) {
            reportesJson = "[]";
        }

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Long provId : groupedProveedores.keySet()) {
            Proveedor prov = groupedProveedores.get(provId);
            List<Map<String, Object>> provInsumos = groupedInsumos.get(provId);

            FacturaProveedor factura = new FacturaProveedor();
            factura.setProveedor(prov);
            factura.setFechaGeneracion(Instant.now());
            factura.setEstado("BORRADOR");
            factura.setReportesOrigenJson(reportesJson);
            factura = facturaRepo.save(factura);
            factura.setNumeroFactura("FAC-%05d".formatted(factura.getId()));
            facturaRepo.save(factura);

            List<DetalleFacturaProveedor> detalles = new ArrayList<>();
            for (Map<String, Object> ins : provInsumos) {
                DetalleFacturaProveedor det = new DetalleFacturaProveedor();
                det.setFacturaProveedor(factura);
                det.setInsumoNombre((String) ins.get("nombre"));
                det.setUnidadMedida((String) ins.get("unidad"));
                det.setCantidadMinima((BigDecimal) ins.get("cantidad"));
                det.setCantidadAPedir((BigDecimal) ins.get("cantidad"));
                detalles.add(det);
            }
            detalleRepo.saveAll(detalles);

            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", factura.getId());
            r.put("numeroFactura", factura.getNumeroFactura());
            r.put("proveedorNombre", prov.getNombre());
            resultado.add(r);
        }

        return ResponseEntity.ok(Map.of(
                "facturas", resultado,
                "total", resultado.size(),
                "mensaje", "Se generaron " + resultado.size() + " factura(s) a proveedores correctamente"
        ));
    }

    // ─── Listar agrupadas por proveedor ───────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> listar() {
        // Incluir TODOS los proveedores activos aunque no tengan facturas
        List<Proveedor> todosProveedores = proveedorRepo.findAllWithCategoriasOrderByNombreAsc();
        Map<Long, Map<String, Object>> grupos = new LinkedHashMap<>();
        for (Proveedor p : todosProveedores) {
            Map<String, Object> g = new LinkedHashMap<>();
            g.put("proveedorId",     p.getId());
            g.put("proveedorNombre", p.getNombre());
            g.put("proveedorNit",    p.getNit());
            g.put("proveedorCorreo", p.getCorreo());
            g.put("facturas",        new ArrayList<>());
            grupos.put(p.getId(), g);
        }

        // Poblar con las facturas existentes
        List<FacturaProveedor> todas = facturaRepo.findAllWithProveedor();
        for (FacturaProveedor f : todas) {
            Proveedor p = f.getProveedor();
            Map<String, Object> grupo = grupos.computeIfAbsent(p.getId(), k -> {
                Map<String, Object> g = new LinkedHashMap<>();
                g.put("proveedorId",     p.getId());
                g.put("proveedorNombre", p.getNombre());
                g.put("proveedorNit",    p.getNit());
                g.put("proveedorCorreo", p.getCorreo());
                g.put("facturas",        new ArrayList<>());
                return g;
            });
            ((List<Object>) grupo.get("facturas")).add(toSummary(f));
        }

        return ResponseEntity.ok(new ArrayList<>(grupos.values()));
    }

    // ─── Detalle de una factura ───────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable Long id) {
        FacturaProveedor f = facturaRepo.findById(id).orElse(null);
        if (f == null) return ResponseEntity.notFound().build();

        List<DetalleFacturaProveedor> detalles =
                detalleRepo.findByFacturaProveedor_IdOrderByInsumoNombreAsc(id);

        Map<String, Object> res = toSummary(f);
        res.put("detalles", detalles.stream().map(this::toDetalleMap).toList());
        return ResponseEntity.ok(res);
    }

    // ─── Ajustar cantidad a pedir ─────────────────────────────────────────────

    @PatchMapping("/detalle/{id}/cantidad")
    @Transactional
    public ResponseEntity<?> actualizarCantidad(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        DetalleFacturaProveedor det = detalleRepo.findById(id).orElse(null);
        if (det == null) return ResponseEntity.notFound().build();

        BigDecimal nueva = toBigDecimal(body.get("cantidadAPedir"));
        if (nueva.compareTo(det.getCantidadMinima()) < 0)
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "La cantidad no puede ser menor a la cantidad mínima requerida"));

        det.setCantidadAPedir(nueva);
        detalleRepo.save(det);
        return ResponseEntity.ok(toDetalleMap(det));
    }

    // ─── Enviar factura por correo ────────────────────────────────────────────

    @PostMapping("/{id}/enviar")
    @Transactional
    public ResponseEntity<?> enviar(@PathVariable Long id) {
        FacturaProveedor f = facturaRepo.findById(id).orElse(null);
        if (f == null) return ResponseEntity.notFound().build();

        if ("ENVIADA".equals(f.getEstado()))
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Esta factura ya fue enviada"));

        Proveedor prov = f.getProveedor();
        if (prov.getCorreo() == null || prov.getCorreo().isBlank())
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "El proveedor no tiene correo registrado"));

        List<DetalleFacturaProveedor> detalles =
                detalleRepo.findByFacturaProveedor_IdOrderByInsumoNombreAsc(id);

        String insumosHtml = buildInsumosHtmlTable(detalles);
        String fechaStr = new java.text.SimpleDateFormat("dd/MM/yyyy")
                .format(java.util.Date.from(f.getFechaGeneracion()));

        boolean enviado = emailService.enviarOrdenCompraProveedor(
                prov.getCorreo(),
                prov.getNombre(),
                prov.getNit() != null ? prov.getNit() : "—",
                f.getNumeroFactura(),
                fechaStr,
                insumosHtml
        );

        if (!enviado)
            return ResponseEntity.internalServerError().body(
                    Map.of("mensaje", "Error al enviar el correo. Verifique la configuración de correo."));

        f.setEstado("ENVIADA");
        f.setEnviadaAt(Instant.now());
        facturaRepo.save(f);

        Map<String, Object> res = toSummary(f);
        res.put("mensaje", "Factura enviada correctamente a " + prov.getCorreo());
        return ResponseEntity.ok(res);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> toSummary(FacturaProveedor f) {
        Proveedor p = f.getProveedor();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",              f.getId());
        m.put("numeroFactura",   f.getNumeroFactura());
        m.put("fechaGeneracion", f.getFechaGeneracion().toString());
        m.put("estado",          f.getEstado());
        m.put("enviadaAt",       f.getEnviadaAt() != null ? f.getEnviadaAt().toString() : null);
        m.put("proveedorId",     p.getId());
        m.put("proveedorNombre", p.getNombre());
        m.put("proveedorNit",    p.getNit());
        m.put("proveedorCorreo", p.getCorreo());
        return m;
    }

    private Map<String, Object> toDetalleMap(DetalleFacturaProveedor d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",              d.getId());
        m.put("insumoNombre",    d.getInsumoNombre());
        m.put("unidadMedida",    d.getUnidadMedida());
        m.put("cantidadMinima",  d.getCantidadMinima());
        m.put("cantidadAPedir",  d.getCantidadAPedir());
        m.put("observaciones",   d.getObservaciones());
        return m;
    }

    private String buildInsumosHtmlTable(List<DetalleFacturaProveedor> detalles) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table style=\"width:100%;border-collapse:collapse;font-size:13px;\">")
          .append("<thead><tr style=\"background:#f5a623;color:#1a1a1a;\">")
          .append("<th style=\"padding:8px;text-align:left;\">Insumo</th>")
          .append("<th style=\"padding:8px;text-align:left;\">Unidad</th>")
          .append("<th style=\"padding:8px;text-align:right;\">Cant. mínima</th>")
          .append("<th style=\"padding:8px;text-align:right;\">Cant. a pedir</th>")
          .append("</tr></thead><tbody>");
        for (DetalleFacturaProveedor d : detalles) {
            sb.append("<tr style=\"border-bottom:1px solid #ddd;\">")
              .append("<td style=\"padding:7px 8px;\">").append(escHtml(d.getInsumoNombre())).append("</td>")
              .append("<td style=\"padding:7px 8px;\">").append(escHtml(d.getUnidadMedida())).append("</td>")
              .append("<td style=\"padding:7px 8px;text-align:right;\">").append(formatNum(d.getCantidadMinima())).append("</td>")
              .append("<td style=\"padding:7px 8px;text-align:right;font-weight:bold;\">").append(formatNum(d.getCantidadAPedir())).append("</td>")
              .append("</tr>");
        }
        sb.append("</tbody></table>");
        return sb.toString();
    }

    private String strVal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return (v instanceof String s && !s.isBlank()) ? s.trim() : null;
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        try { return new BigDecimal(val.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private String escHtml(String s) {
        if (s == null) return "—";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String formatNum(BigDecimal val) {
        if (val == null) return "—";
        return val.stripTrailingZeros().toPlainString();
    }
}
