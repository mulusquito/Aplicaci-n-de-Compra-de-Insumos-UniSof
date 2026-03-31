package com.unisof.insumos.repository;

import com.unisof.insumos.model.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    List<Insumo> findByCategoria_CodigoOrderByNombreAsc(String codigoCategoria);

    List<Insumo> findByCodigoContainingIgnoreCaseOrderByCategoria_CodigoAscNombreAsc(String fragmento);

    List<Insumo> findByNombreContainingIgnoreCaseOrderByCategoria_CodigoAscNombreAsc(String fragmento);

    List<Insumo> findAllByOrderByCategoria_CodigoAscNombreAsc();

    boolean existsByCodigoIgnoreCase(String codigo);
}
