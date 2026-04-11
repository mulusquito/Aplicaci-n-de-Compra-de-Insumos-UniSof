package com.unisof.insumos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "facturas_proveedor")
@Getter
@Setter
@NoArgsConstructor
public class FacturaProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 32, unique = true)
    private String numeroFactura;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(nullable = false)
    private Instant fechaGeneracion = Instant.now();

    /** JSON con [{reporteId, fecha}] de los reportes consolidados de origen. */
    @Column(columnDefinition = "TEXT")
    private String reportesOrigenJson;

    /** BORRADOR | ENVIADA */
    @Column(length = 20, nullable = false)
    private String estado = "BORRADOR";

    private Instant enviadaAt;
}
