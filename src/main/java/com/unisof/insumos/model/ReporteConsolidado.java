package com.unisof.insumos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

/**
 * Almacena un reporte consolidado de insumos faltantes generado desde el panel de Reporte Faltantes.
 */
@Entity
@Table(name = "reportes_consolidados")
@Getter @Setter @NoArgsConstructor
public class ReporteConsolidado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Fecha y hora de generación del reporte. */
    @Column(nullable = false)
    private Instant fecha;

    /** JSON: [{analisisId, ordenNumero, clienteNombre}, ...] */
    @Column(name = "ordenes_json", columnDefinition = "TEXT", nullable = false)
    private String ordenesJson;

    /** Cantidad de tipos de insumos distintos con faltante > 0. */
    @Column(name = "total_insumos_distintos", nullable = false)
    private Integer totalInsumosDistintos;

    /** JSON: [{insumoNombre, unidadMedida, cantidadTotal}, ...] */
    @Column(name = "insumos_json", columnDefinition = "TEXT", nullable = false)
    private String insumosJson;

    /** true cuando ya se generaron facturas a proveedores desde este reporte. */
    @Column(name = "facturas_generadas", nullable = false)
    private boolean facturasGeneradas = false;
}
