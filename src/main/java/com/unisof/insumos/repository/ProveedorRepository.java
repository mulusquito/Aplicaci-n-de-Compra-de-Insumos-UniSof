package com.unisof.insumos.repository;

import com.unisof.insumos.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    Optional<Proveedor> findByNit(String nit);

    Optional<Proveedor> findByCorreoIgnoreCase(String correo);

    List<Proveedor> findByNombreContainingIgnoreCaseOrderByNombreAsc(String fragmento);

    List<Proveedor> findAllByOrderByNombreAsc();

    @Query("SELECT DISTINCT p FROM Proveedor p LEFT JOIN FETCH p.categorias ORDER BY p.nombre ASC")
    List<Proveedor> findAllWithCategoriasOrderByNombreAsc();

    @Query("SELECT DISTINCT p FROM Proveedor p LEFT JOIN FETCH p.categorias "
            + "WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :criterio, '%')) ORDER BY p.nombre ASC")
    List<Proveedor> findByNombreContainingIgnoreCaseWithCategorias(@Param("criterio") String criterio);

    @Query("SELECT DISTINCT p FROM Proveedor p JOIN FETCH p.categorias c "
            + "WHERE p IN (SELECT p2 FROM Proveedor p2 JOIN p2.categorias cat WHERE cat.codigo = :codigo) "
            + "ORDER BY p.nombre ASC")
    List<Proveedor> findByCategoriaCodigoWithCategorias(@Param("codigo") String codigo);

    @Query("SELECT DISTINCT p FROM Proveedor p LEFT JOIN FETCH p.categorias WHERE p.id = :id")
    Optional<Proveedor> findByIdWithCategorias(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Proveedor p LEFT JOIN FETCH p.categorias "
            + "WHERE p.nit IS NOT NULL AND LOWER(p.nit) LIKE LOWER(CONCAT('%', :nit, '%')) ORDER BY p.nombre ASC")
    List<Proveedor> findByNitContainingIgnoreCaseWithCategorias(@Param("nit") String nit);
}
