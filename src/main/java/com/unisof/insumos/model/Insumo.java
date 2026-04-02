package com.unisof.insumos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "insumos")
@Getter
@Setter
@NoArgsConstructor
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nombre;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaInsumo categoria;

    @Column(nullable = false, length = 32)
    private String unidadMedida;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal stockDisponible = BigDecimal.ZERO;

    @Column(precision = 18, scale = 4)
    private BigDecimal stockMinimo;

    /**
     * Costo o precio de referencia en COP <strong>por una unidad de {@link #unidadMedida}</strong>
     * (ej. COP por metro si {@code unidadMedida} es {@code m}, COP por {@code ud}, por {@code cono}, etc.).
     * En órdenes de compra: cantidad pedida (en esa unidad) × este precio = subtotal estimado de la línea.
     */
    @Column(precision = 18, scale = 4)
    private BigDecimal precioUnitario;

    @Column(length = 120)
    private String referenciaTela;

    @Column(length = 120)
    private String color;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(columnDefinition = "TEXT")
    private String productosCatalogo;
}
