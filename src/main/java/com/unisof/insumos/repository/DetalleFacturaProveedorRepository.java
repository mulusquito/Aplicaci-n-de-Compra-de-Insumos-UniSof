package com.unisof.insumos.repository;

import com.unisof.insumos.model.DetalleFacturaProveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleFacturaProveedorRepository extends JpaRepository<DetalleFacturaProveedor, Long> {

    List<DetalleFacturaProveedor> findByFacturaProveedor_IdOrderByInsumoNombreAsc(Long facturaId);
}
