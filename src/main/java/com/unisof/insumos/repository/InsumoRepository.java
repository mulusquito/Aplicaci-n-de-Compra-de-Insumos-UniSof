package com.unisof.insumos.repository;

import com.unisof.insumos.model.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    @Query("SELECT COUNT(i) FROM Insumo i WHERE i.stockMinimo IS NOT NULL AND i.stockDisponible IS NOT NULL "
            + "AND i.stockDisponible <= i.stockMinimo")
    long countBajoStockMinimo();

    List<Insumo> findByCategoria_CodigoOrderByNombreAsc(String codigoCategoria);

    List<Insumo> findByCodigoContainingIgnoreCaseOrderByCategoria_CodigoAscNombreAsc(String fragmento);

    List<Insumo> findByNombreContainingIgnoreCaseOrderByCategoria_CodigoAscNombreAsc(String fragmento);

    List<Insumo> findAllByOrderByCategoria_CodigoAscNombreAsc();

    boolean existsByCodigoIgnoreCase(String codigo);

    long countByPrecioUnitarioIsNotNull();

    @Query("SELECT COALESCE(SUM(i.stockDisponible * i.precioUnitario), 0) FROM Insumo i "
            + "WHERE i.precioUnitario IS NOT NULL AND i.stockDisponible IS NOT NULL")
    BigDecimal sumValorInventarioPorPrecioUnitario();

    @Query("SELECT COUNT(i) FROM Insumo i WHERE i.precioUnitario IS NULL")
    long countSinPrecioUnitario();
}
