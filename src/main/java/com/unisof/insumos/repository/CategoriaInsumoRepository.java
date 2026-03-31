package com.unisof.insumos.repository;

import com.unisof.insumos.model.CategoriaInsumo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaInsumoRepository extends JpaRepository<CategoriaInsumo, Long> {

    Optional<CategoriaInsumo> findByCodigo(String codigo);
}
