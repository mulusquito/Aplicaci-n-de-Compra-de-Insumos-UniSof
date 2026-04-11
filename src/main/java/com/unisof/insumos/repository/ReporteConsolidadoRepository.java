package com.unisof.insumos.repository;

import com.unisof.insumos.model.ReporteConsolidado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface ReporteConsolidadoRepository extends JpaRepository<ReporteConsolidado, Long> {

    List<ReporteConsolidado> findAllByOrderByFechaDesc();

    @Query("SELECT r FROM ReporteConsolidado r WHERE r.fecha >= :inicio AND r.fecha < :fin ORDER BY r.fecha DESC")
    List<ReporteConsolidado> findByFechaBetween(@Param("inicio") Instant inicio, @Param("fin") Instant fin);
}
