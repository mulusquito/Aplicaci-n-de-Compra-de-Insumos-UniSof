package com.unisof.insumos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "detalles_factura_proveedor")
@Getter
@Setter
@NoArgsConstructor
public class DetalleFacturaProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_proveedor_id", nullable = false)
    private FacturaProveedor facturaProveedor;

    @Column(nullable = false, length = 300)
    private String insumoNombre;

    @Column(length = 80)
    private String unidadMedida;

    /** Cantidad mínima requerida según el reporte consolidado. */
    @Column(precision = 14, scale = 4)
    private BigDecimal cantidadMinima;

    /** Cantidad ajustada a pedir (puede ser mayor que cantidadMinima). */
    @Column(precision = 14, scale = 4)
    private BigDecimal cantidadAPedir;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
