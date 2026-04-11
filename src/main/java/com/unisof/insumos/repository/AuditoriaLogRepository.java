package com.unisof.insumos.repository;

import com.unisof.insumos.model.AuditoriaLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repositorio JPA para {@link AuditoriaLog}.
 * SCRUM-64: Persistencia de logs de auditoría en PostgreSQL.
 */
@Repository
public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Long> {

    /** Todos los registros paginados, más recientes primero */
    Page<AuditoriaLog> findAllByOrderByFechaHoraDesc(Pageable pageable);

    /** Filtrar por módulo (insensible a mayúsculas) */
    List<AuditoriaLog> findByModuloIgnoreCaseOrderByFechaHoraDesc(String modulo);

    /** Filtrar por usuario */
    List<AuditoriaLog> findByUsuarioNombreIgnoreCaseOrderByFechaHoraDesc(String usuarioNombre);

    /** Filtrar por rango de fechas */
    List<AuditoriaLog> findByFechaHoraBetweenOrderByFechaHoraDesc(Instant desde, Instant hasta);

    /** Filtrar por resultado: EXITOSO o FALLIDO */
    List<AuditoriaLog> findByResultadoOrderByFechaHoraDesc(String resultado);

    /** Filtrar por acción */
    List<AuditoriaLog> findByAccionIgnoreCaseOrderByFechaHoraDesc(String accion);
}
