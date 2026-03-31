package com.unisof.insumos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Categorías de insumo (telas, hilos, etc.) que este proveedor puede abastecer para la confección UNISOF.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "proveedor_categorias_insumo",
            joinColumns = @JoinColumn(name = "proveedor_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<CategoriaInsumo> categorias = new HashSet<>();

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 32, unique = true)
    private String nit;

    @Column(length = 120)
    private String contactoNombre;

    @Column(length = 40)
    private String telefono;

    /** Único cuando está informado (varios proveedores pueden omitir correo). */
    @Column(length = 160, unique = true)
    private String correo;

    @Column(length = 300)
    private String direccion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private Instant fechaRegistro = Instant.now();
}
