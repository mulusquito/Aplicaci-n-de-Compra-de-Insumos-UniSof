package com.unisof.insumos.controller;

import com.unisof.insumos.model.Recibo;
import com.unisof.insumos.repository.InsumoRepository;
import com.unisof.insumos.repository.ProveedorRepository;
import com.unisof.insumos.repository.ReciboRepository;
import com.unisof.insumos.repository.UsuarioRepository;
import com.unisof.insumos.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API de estadísticas del dashboard (solo ADMINISTRADOR).
 * SCRUM-64: Accesos al dashboard quedan registrados en auditoría.
 * GET /api/dashboard/stats?fechaDesde=YYYY-MM-DD&fechaHasta=YYYY-MM-DD
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final String[] MES_LABELS = {
        "Ene", "Feb", "Mar", "Abr", "May", "Jun",
        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    private final UsuarioRepository usuarioRepository;
    private final ReciboRepository reciboRepository;
    private final InsumoRepository insumoRepository;
    private final ProveedorRepository proveedorRepository;
    private final AuditoriaService auditoriaService;  // SCRUM-64

    public DashboardController(
            UsuarioRepository usuarioRepository,
            ReciboRepository reciboRepository,
            InsumoRepository insumoRepository,
            ProveedorRepository proveedorRepository,
            AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.reciboRepository = reciboRepository;
        this.insumoRepository = insumoRepository;
        this.proveedorRepository = proveedorRepository;
        this.auditoriaService = auditoriaService;
    }

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");

    /**
     * Estadísticas del dashboard de administración.
     * SCRUM-64: Registra CONSULTAR en módulo DASHBOARD.
     */
    @GetMapping("/stats")
    public ResponseEntity<?> stats(
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            HttpServletRequest httpRequest) {

        ZoneId zone = ZONE;
        LocalDate dInicio;
        LocalDate dFin;
        if (fechaDesde != null && !fechaDesde.isBlank()
                && fechaHasta != null && !fechaHasta.isBlank()) {
            dInicio = LocalDate.parse(fechaDesde);
            dFin = LocalDate.parse(fechaHasta);
        } else {
            LocalDate hoy = LocalDate.now(zone);
            dInicio = hoy.withDayOfMonth(1);
            dFin = hoy;
        }
        Instant inicio = dInicio.atStartOfDay(zone).toInstant();
        Instant fin = dFin.plusDays(1).atStartOfDay(zone).toInstant();

        long usuarios = usuarioRepository.count();
        List<Recibo> recibosPeriodo = reciboRepository.findByFechaBetween(inicio, fin);
        int ordenes = recibosPeriodo.size();
        BigDecimal ventas = recibosPeriodo.stream()
                .map(Recibo::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Año actual: Ene a Dic (12 meses)
        int anioActual = LocalDate.now(zone).getYear();
        List<Map<String, Object>> ventasPorMes = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            LocalDate inicioMes = LocalDate.of(anioActual, mes, 1);
            LocalDate finMes = inicioMes.plusMonths(1);
            Instant instInicio = inicioMes.atStartOfDay(zone).toInstant();
            Instant instFin = finMes.atStartOfDay(zone).toInstant();
            List<Recibo> recibosMes = reciboRepository.findByFechaBetween(instInicio, instFin);
            BigDecimal totalMes = recibosMes.stream()
                    .map(Recibo::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            ventasPorMes.add(Map.<String, Object>of(
                    "anio", anioActual,
                    "mes", mes,
                    "mesLabel", MES_LABELS[mes - 1],
                    "total", totalMes,
                    "cantidad", recibosMes.size()
            ));
        }

        BigDecimal ticketPromedio = null;
        if (ordenes > 0) {
            ticketPromedio = ventas.divide(BigDecimal.valueOf(ordenes), 2, RoundingMode.HALF_UP);
        }

        Map<String, Object> finanzas = new LinkedHashMap<>();
        finanzas.put("ticketPromedio", ticketPromedio);
        finanzas.put("ticketPromedioDisponible", ordenes > 0);
        finanzas.put("comprasTotalDisponible", false);
        long insumosConPrecio = insumoRepository.countByPrecioUnitarioIsNotNull();
        BigDecimal valorInventario = insumoRepository.sumValorInventarioPorPrecioUnitario();
        if (valorInventario == null) valorInventario = BigDecimal.ZERO;
        finanzas.put("valorInventario", valorInventario);
        finanzas.put("valorInventarioDisponible", insumosConPrecio > 0);
        finanzas.put("insumosSinPrecioUnitario", insumoRepository.countSinPrecioUnitario());
        finanzas.put("resultadoOperativoDisponible", false);
        finanzas.put("insumosRegistrados", insumoRepository.count());
        finanzas.put("insumosBajoMinimo", insumoRepository.countBajoStockMinimo());
        finanzas.put("proveedoresRegistrados", proveedorRepository.count());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("usuarios", usuarios);
        body.put("ordenes", ordenes);
        body.put("ventas", ventas);
        body.put("fechaDesde", dInicio.format(DateTimeFormatter.ISO_LOCAL_DATE));
        body.put("fechaHasta", dFin.format(DateTimeFormatter.ISO_LOCAL_DATE));
        body.put("ventasPorMes", ventasPorMes);
        body.put("finanzas", finanzas);

        // SCRUM-64: registrar acceso al dashboard
        String[] ui = auditoriaService.obtenerUsuarioInfo();
        auditoriaService.registrar(
                AuditoriaService.ACC_CONSULTAR, AuditoriaService.MOD_DASHBOARD,
                "Consulta de estadísticas del dashboard — período: " +
                dInicio.format(DateTimeFormatter.ISO_LOCAL_DATE) + " a " +
                dFin.format(DateTimeFormatter.ISO_LOCAL_DATE),
                ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                AuditoriaService.RES_EXITOSO,
                "órdenes=" + ordenes + ", ventas=$" + ventas.intValue()
        );

        return ResponseEntity.ok(body);
    }
}
