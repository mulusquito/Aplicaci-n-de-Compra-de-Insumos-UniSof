package com.unisof.insumos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un cliente del sistema.
 * SCRUM-16: Permite buscar clientes por cédula para asociar pedidos y generar recibos.
 */
@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String cedula;

    @Column(nullable = false)
    private String correo;

    private String telefono;

    private String direccion;

    public Cliente(String nombre, String cedula, String correo, String telefono, String direccion) {
        this.nombre = nombre;
        this.cedula = cedula;
        this.correo = correo;
        this.telefono = telefono != null ? telefono : "";
        this.direccion = direccion != null ? direccion : "";
    }
}
