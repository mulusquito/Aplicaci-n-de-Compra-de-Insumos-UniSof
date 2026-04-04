package com.unisof.insumos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Almacena el resultado de analizar una orden de compra contra las fichas técnicas.
 * Cada fila representa el último análisis realizado para una orden concreta.
 * SCRUM-53.
 */
@Entity
@Table(name = "analisis_ordenes")
@Getter
@Setter
@NoArgsConstructor
public class AnalisisOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del Recibo analizado (Recibo.id). */
    @Column(name = "orden_id", nullable = false, unique = true)
    private Long ordenId;

    /** Número de la orden (Recibo.numero). */
    @Column(name = "orden_numero", nullable = false)
    private Integer ordenNumero;

    /** Nombre del cliente al momento del análisis. */
    @Column(name = "cliente_nombre", nullable = false, length = 255)
    private String clienteNombre;

    /** Fecha y hora de la orden original. */
    @Column(name = "fecha_orden", nullable = false)
    private Instant fechaOrden;

    /** Estado de la orden: PENDIENTE / PAGADO / etc. */
    @Column(nullable = false, length = 30)
    private String estado;

    /** Fecha y hora en que se realizó este análisis. */
    @Column(name = "fecha_analisis", nullable = false)
    private Instant fechaAnalisis;

    /**
     * Resultado completo del análisis en JSON:
     * { ordenId, ordenNumero, clienteNombre, fechaOrden, estado,
     *   items: [ { prenda, genero, talla, cantidad, fichaTecnicaEncontrada,
     *              insumos: [ { insumoId, insumoNombre, unidadMedida,
     *                           cantidadUnitaria, cantidadTotal } ] } ] }
     */
    @Column(name = "resultado_json", columnDefinition = "TEXT", nullable = false)
    private String resultadoJson;

    /** Si ya se calcularon y descontaron los insumos faltantes del inventario. */
    @Column(name = "faltantes_calculados", nullable = false)
    private boolean faltantesCalculados = false;

    /** Fecha y hora en que se ejecutó el cálculo de faltantes. */
    @Column(name = "fecha_calculo")
    private Instant fechaCalculo;

    /**
     * JSON con el resultado del cálculo de faltantes:
     * { todoCompleto: bool, items: [{ prenda, genero, talla, cantidad, completo,
     *   insumos: [{ insumoId, insumoNombre, unidadMedida, cantidadRequerida,
     *               stockAntes, consumido, faltante, completo }] }] }
     */
    @Column(name = "faltantes_json", columnDefinition = "TEXT")
    private String faltantesJson;

    public AnalisisOrden(Long ordenId, Integer ordenNumero, String clienteNombre,
                         Instant fechaOrden, String estado, String resultadoJson) {
        this.ordenId       = ordenId;
        this.ordenNumero   = ordenNumero;
        this.clienteNombre = clienteNombre;
        this.fechaOrden    = fechaOrden;
        this.estado        = estado;
        this.fechaAnalisis = Instant.now();
        this.resultadoJson = resultadoJson;
    }
}
