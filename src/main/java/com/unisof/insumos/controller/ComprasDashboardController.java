package com.unisof.insumos.controller;

import com.unisof.insumos.model.Insumo;
import com.unisof.insumos.service.ComprasInsumoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * KPIs y análisis de insumos para el panel Jefe de compras.
 * <p>
 * "Requerido" en {@code analisisInsumos} corresponde al stock mínimo operativo del insumo
 * hasta exista integración con pedidos/BOM.
 * </p>
 */
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class ComprasDashboardController {

    private final ComprasInsumoService comprasInsumoService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        List<Insumo> todos = comprasInsumoService.todosOrdenados();
        BigDecimal sumaMin = BigDecimal.ZERO;
        BigDecimal sumaDisp = BigDecimal.ZERO;
        long bajo = 0;
        List<Map<String, Object>> filas = new ArrayList<>();

        for (Insumo i : todos) {
            if (i.getStockDisponible() != null) {
                sumaDisp = sumaDisp.add(i.getStockDisponible());
            }
            BigDecimal min = i.getStockMinimo();
            if (min != null) {
                sumaMin = sumaMin.add(min);
            }
            if (min != null && i.getStockDisponible() != null
                    && i.getStockDisponible().compareTo(min) <= 0) {
                bajo++;
            }

            BigDecimal disp = i.getStockDisponible() != null ? i.getStockDisponible() : BigDecimal.ZERO;
            BigDecimal req = min;
            BigDecimal dif = null;
            if (req != null) {
                dif = disp.subtract(req);
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("insumo", i.getNombre());
            row.put("insumoNombre", i.getNombre());
            row.put("codigo", i.getCodigo());
            row.put("requerido", req);
            row.put("disponible", disp);
            row.put("diferencia", dif);
            row.put("unidad", i.getUnidadMedida());
            row.put("categoria", i.getCategoria() != null ? i.getCategoria().getNombre() : null);
            row.put("categoriaCodigo", i.getCategoria() != null ? i.getCategoria().getCodigo() : null);
            row.put("referenciaTela", i.getReferenciaTela());
            row.put("color", i.getColor());
            row.put("stockMinimo", min);
            row.put("precioUnitario", i.getPrecioUnitario());
            row.put("productosCatalogo", i.getProductosCatalogo());
            filas.add(row);
        }

        long conStockPositivo = todos.stream()
                .filter(i -> i.getStockDisponible() != null
                        && i.getStockDisponible().compareTo(BigDecimal.ZERO) > 0)
                .count();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pedidosActivos", null);
        body.put("pedidosAltaPrioridad", null);
        body.put("totalInsumos", (long) todos.size());
        body.put("insumosConStockPositivo", conStockPositivo);
        if (todos.isEmpty()) {
            body.put("insumosRequeridos", null);
            body.put("insumosDisponibles", null);
            body.put("faltantes", null);
        } else {
            body.put("insumosRequeridos", sumaMin);
            body.put("insumosDisponibles", sumaDisp);
            body.put("faltantes", bajo);
        }
        body.put("faltantesEstimadoCOP", null);
        body.put("unidadesTotalesPedidos", null);
        body.put("analisisInsumos", filas);
        return ResponseEntity.ok(body);
    }
}
