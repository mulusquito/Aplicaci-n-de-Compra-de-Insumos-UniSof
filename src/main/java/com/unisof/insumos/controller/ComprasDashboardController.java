package com.unisof.insumos.controller;

import com.unisof.insumos.model.Insumo;
import com.unisof.insumos.repository.AnalisisOrdenRepository;
import com.unisof.insumos.repository.ReciboRepository;
import com.unisof.insumos.service.AuditoriaService;
import com.unisof.insumos.service.ComprasInsumoService;
import jakarta.servlet.http.HttpServletRequest;
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
 * SCRUM-64: Acceso al dashboard de compras registrado en auditoría.
 */
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class ComprasDashboardController {

    private final ComprasInsumoService   comprasInsumoService;
    private final AuditoriaService       auditoriaService;
    private final ReciboRepository       reciboRepository;
    private final AnalisisOrdenRepository analisisOrdenRepository;

    /**
     * Resumen de KPIs del módulo de compras.
     * SCRUM-64: Registra CONSULTAR en módulo COMPRAS.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(HttpServletRequest httpRequest) {
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
        // Total de pedidos (recibos) registrados en el sistema
        long totalPedidos = reciboRepository.count();
        // Pedidos con insumos insuficientes: estado EN ESPERA POR PRODUCCION
        long pedidosConFaltantes = analisisOrdenRepository.countConFaltantes();

        body.put("pedidosActivos", totalPedidos);
        body.put("pedidosAltaPrioridad", null);
        body.put("totalInsumos", (long) todos.size());
        body.put("insumosConStockPositivo", conStockPositivo);
        body.put("insumosRequeridos",  todos.isEmpty() ? null : sumaMin);
        body.put("insumosDisponibles", todos.isEmpty() ? null : sumaDisp);
        // Faltantes = pedidos con insumos insuficientes (no insumos bajo mínimo)
        body.put("faltantes", pedidosConFaltantes);
        body.put("faltantesEstimadoCOP", null);
        body.put("unidadesTotalesPedidos", null);
        body.put("analisisInsumos", filas);

        // SCRUM-64: auditoría de acceso al dashboard de compras
        String[] ui = auditoriaService.obtenerUsuarioInfo();
        auditoriaService.registrar(
                AuditoriaService.ACC_CONSULTAR, AuditoriaService.MOD_COMPRAS,
                "Consulta del dashboard de compras — pedidos: " + totalPedidos +
                ", insumos: " + todos.size() + ", pedidos con faltantes: " + pedidosConFaltantes,
                ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                AuditoriaService.RES_EXITOSO,
                "pedidos=" + totalPedidos + ", insumos=" + todos.size() + ", faltantes=" + pedidosConFaltantes
        );

        return ResponseEntity.ok(body);
    }
}
