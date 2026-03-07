package com.unisof.insumos.controller;

import com.unisof.insumos.model.Recibo;
import com.unisof.insumos.repository.ReciboRepository;
import com.unisof.insumos.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * API de estadísticas del dashboard (solo ADMINISTRADOR).
 * GET /api/dashboard/stats?fechaDesde=YYYY-MM-DD&fechaHasta=YYYY-MM-DD
 * Si no se envían fechas, se usa el mes actual.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final String[] MES_LABELS = { "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };

    private final UsuarioRepository usuarioRepository;
    private final ReciboRepository reciboRepository;

    public DashboardController(UsuarioRepository usuarioRepository, ReciboRepository reciboRepository) {
        this.usuarioRepository = usuarioRepository;
        this.reciboRepository = reciboRepository;
    }

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");

    @GetMapping("/stats")
    public ResponseEntity<?> stats(
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta) {
        ZoneId zone = ZONE;
        LocalDate dInicio;
        LocalDate dFin;
        if (fechaDesde != null && !fechaDesde.isBlank() && fechaHasta != null && !fechaHasta.isBlank()) {
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
        BigDecimal ventas = recibosPeriodo.stream().map(Recibo::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Año actual: Ene a Dic (12 meses). Meses sin ventas = 0.
        int anioActual = LocalDate.now(zone).getYear();
        List<Map<String, Object>> ventasPorMes = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            LocalDate inicioMes = LocalDate.of(anioActual, mes, 1);
            LocalDate finMes = inicioMes.plusMonths(1);
            Instant instInicio = inicioMes.atStartOfDay(zone).toInstant();
            Instant instFin = finMes.atStartOfDay(zone).toInstant();
            List<Recibo> recibosMes = reciboRepository.findByFechaBetween(instInicio, instFin);
            BigDecimal totalMes = recibosMes.stream().map(Recibo::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            ventasPorMes.add(Map.<String, Object>of(
                    "anio", anioActual,
                    "mes", mes,
                    "mesLabel", MES_LABELS[mes - 1],
                    "total", totalMes,
                    "cantidad", recibosMes.size()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "usuarios", usuarios,
                "ordenes", ordenes,
                "ventas", ventas,
                "fechaDesde", dInicio.format(DateTimeFormatter.ISO_LOCAL_DATE),
                "fechaHasta", dFin.format(DateTimeFormatter.ISO_LOCAL_DATE),
                "ventasPorMes", ventasPorMes
        ));
    }
}
