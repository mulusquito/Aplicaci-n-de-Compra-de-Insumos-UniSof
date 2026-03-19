package com.unisof.insumos.repository;

import com.unisof.insumos.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio para la entidad Cliente.
 * SCRUM-16: Búsqueda por cédula.
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCedula(String cedula);

    Optional<Cliente> findByCorreo(String correo);
}
