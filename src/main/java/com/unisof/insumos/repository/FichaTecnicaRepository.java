package com.unisof.insumos.repository;

import com.unisof.insumos.model.FichaTecnica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FichaTecnicaRepository extends JpaRepository<FichaTecnica, Long> {

    /** Todas las líneas de ficha de una prenda + género, ordenadas por talla e insumo. */
    @Query("SELECT f FROM FichaTecnica f WHERE LOWER(f.nombrePrenda) = LOWER(:prenda) AND LOWER(f.genero) = LOWER(:genero) ORDER BY f.talla ASC, f.insumo.nombre ASC")
    List<FichaTecnica> findByPrendaAndGenero(@Param("prenda") String prenda, @Param("genero") String genero);

    /** Líneas de ficha para prenda + género + talla concreta. */
    @Query("SELECT f FROM FichaTecnica f WHERE LOWER(f.nombrePrenda) = LOWER(:prenda) AND LOWER(f.genero) = LOWER(:genero) AND f.talla = :talla ORDER BY f.insumo.nombre ASC")
    List<FichaTecnica> findByPrendaGeneroTalla(@Param("prenda") String prenda, @Param("genero") String genero, @Param("talla") String talla);

    /** Nombres únicos de prenda con ficha registrada. */
    @Query("SELECT DISTINCT f.nombrePrenda FROM FichaTecnica f ORDER BY f.nombrePrenda ASC")
    List<String> findDistinctNombrePrenda();

    boolean existsByNombrePrendaIgnoreCaseAndGenero(String nombrePrenda, String genero);

    boolean existsByGeneroIsNotNull();

    long countByNombrePrendaIgnoreCaseAndGenero(String nombrePrenda, String genero);
}
