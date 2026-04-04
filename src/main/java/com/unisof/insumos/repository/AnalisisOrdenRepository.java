package com.unisof.insumos.repository;

import com.unisof.insumos.model.AnalisisOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para AnalisisOrden. SCRUM-53.
 */
public interface AnalisisOrdenRepository extends JpaRepository<AnalisisOrden, Long> {

    /** Busca el análisis de una orden concreta por su ID de Recibo. */
    Optional<AnalisisOrden> findByOrdenId(Long ordenId);

    /** Busca análisis por número de orden. */
    List<AnalisisOrden> findByOrdenNumeroOrderByFechaAnalisisDesc(Integer ordenNumero);

    /** Todos los análisis ordenados por fecha de análisis descendente. */
    List<AnalisisOrden> findAllByOrderByFechaAnalisisDesc();

    /** Análisis cuya fecha de orden esté en el rango dado (para filtrar por fecha exacta). */
    @Query("SELECT a FROM AnalisisOrden a WHERE a.fechaOrden >= :inicio AND a.fechaOrden < :fin ORDER BY a.fechaAnalisis DESC")
    List<AnalisisOrden> findByFechaOrdenBetween(@Param("inicio") Instant inicio, @Param("fin") Instant fin);

    /** Análisis cuya fecha de orden esté en el mes/año indicados. */
    @Query("SELECT a FROM AnalisisOrden a WHERE a.fechaOrden >= :inicio AND a.fechaOrden < :fin ORDER BY a.fechaAnalisis DESC")
    List<AnalisisOrden> findByMes(@Param("inicio") Instant inicio, @Param("fin") Instant fin);

    /** Análisis con insumos faltantes (cálculo realizado y resultado NO completo). */
    @Query("SELECT a FROM AnalisisOrden a WHERE a.faltantesCalculados = true AND a.estado = 'EN ESPERA POR PRODUCCION' ORDER BY a.fechaCalculo DESC")
    List<AnalisisOrden> findConFaltantes();

    /** Cuenta de pedidos con insumos faltantes. */
    @Query("SELECT COUNT(a) FROM AnalisisOrden a WHERE a.faltantesCalculados = true AND a.estado = 'EN ESPERA POR PRODUCCION'")
    long countConFaltantes();
}
