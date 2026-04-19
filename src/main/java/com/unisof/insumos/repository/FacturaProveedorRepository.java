package com.unisof.insumos.repository;

import com.unisof.insumos.model.FacturaProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface FacturaProveedorRepository extends JpaRepository<FacturaProveedor, Long> {

    @Query("SELECT f FROM FacturaProveedor f JOIN FETCH f.proveedor ORDER BY f.fechaGeneracion DESC")
    List<FacturaProveedor> findAllWithProveedor();

    List<FacturaProveedor> findByProveedor_IdOrderByFechaGeneracionDesc(Long proveedorId);

    /** {@code hasta} exclusivo (mismo criterio que recibos en el dashboard). */
    List<FacturaProveedor> findByFechaGeneracionGreaterThanEqualAndFechaGeneracionLessThan(
            Instant desde, Instant hastaExclusivo);
}
