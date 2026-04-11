package com.unisof.insumos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Una línea de la ficha técnica de fabricación: cuánto de un insumo concreto
 * se necesita para confeccionar una prenda en una talla determinada.
 *
 * La tabla completa de fichas para una prenda + talla responde a la pregunta:
 * "Para fabricar N unidades de <nombrePrenda> en talla <talla>,
 *  ¿cuántas unidades de cada insumo requiero?" → cantidad × N por fila.
 */
@Entity
@Table(
    name = "fichas_tecnicas",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_ficha_prenda_genero_talla_insumo",
            columnNames = {"nombre_prenda", "genero", "talla", "insumo_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class FichaTecnica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre exacto de la prenda, tal como aparece en el catálogo de ventas. */
    @Column(name = "nombre_prenda", nullable = false, length = 200)
    private String nombrePrenda;

    /** Género al que va dirigida la prenda: "Caballero" o "Dama". */
    @Column(nullable = false, length = 20)
    private String genero;

    /** Talla: XS / S / M / L / XL / 2XL / 3XL */
    @Column(nullable = false, length = 10)
    private String talla;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    /**
     * Cantidad del insumo requerida <strong>por una sola unidad</strong> de la prenda en esa talla,
     * expresada en la unidad de medida del insumo (metros, ud, cono, docena…).
     */
    @Column(name = "cantidad_requerida", nullable = false, precision = 10, scale = 4)
    private BigDecimal cantidadRequerida;

    public FichaTecnica(String nombrePrenda, String genero, String talla, Insumo insumo, BigDecimal cantidadRequerida) {
        this.nombrePrenda = nombrePrenda;
        this.genero = genero;
        this.talla = talla;
        this.insumo = insumo;
        this.cantidadRequerida = cantidadRequerida;
    }
}
