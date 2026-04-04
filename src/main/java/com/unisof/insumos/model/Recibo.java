package com.unisof.insumos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad Recibo. SCRUM-16: Recibo con número incremental, relacionado con Cliente.
 */
@Entity
@Table(name = "recibos")
@Getter
@Setter
@NoArgsConstructor
public class Recibo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private Instant fecha;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    /** Items en JSON: [{"nombre","talla","cantidad","precioUnit","subtotal"},...] */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String itemsJson;

    /** Estado: PENDIENTE, PAGADO, POR REVISAR, EN ESPERA POR PRODUCCION, EN CONFECCION */
    @Column(nullable = false, length = 50)
    private String estado = "PENDIENTE";

    /** Vendedor que realizó la venta */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;

    public Recibo(Integer numero, Cliente cliente, BigDecimal total, String itemsJson, String estado, Usuario vendedor) {
        this.numero = numero;
        this.cliente = cliente;
        this.fecha = Instant.now();
        this.total = total;
        this.itemsJson = itemsJson;
        this.estado = estado != null ? estado : "PENDIENTE";
        this.vendedor = vendedor;
    }
}
