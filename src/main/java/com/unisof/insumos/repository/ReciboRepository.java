package com.unisof.insumos.repository;

import com.unisof.insumos.model.Recibo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para Recibo. SCRUM-16: Número incremental.
 */
public interface ReciboRepository extends JpaRepository<Recibo, Long> {

    @Query("SELECT COALESCE(MAX(r.numero), 0) FROM Recibo r")
    Integer findMaxNumero();

    Optional<Recibo> findByNumero(Integer numero);

    @Query("SELECT r FROM Recibo r WHERE r.fecha >= :inicio AND r.fecha < :fin ORDER BY r.fecha DESC")
    List<Recibo> findByFechaBetween(Instant inicio, Instant fin);

    @Query("SELECT r FROM Recibo r WHERE r.vendedor.id = :vendedorId AND r.fecha >= :inicio AND r.fecha < :fin ORDER BY r.fecha ASC")
    List<Recibo> findByVendedorIdAndFechaBetween(
            @Param("vendedorId") Long vendedorId,
            @Param("inicio") Instant inicio,
            @Param("fin") Instant fin);

    List<Recibo> findByClienteCedulaOrderByFechaDesc(String cedula);
}
