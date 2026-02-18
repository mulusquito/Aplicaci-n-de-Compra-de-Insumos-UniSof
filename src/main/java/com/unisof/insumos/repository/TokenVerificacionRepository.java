package com.unisof.insumos.repository;

import com.unisof.insumos.model.TokenVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TokenVerificacionRepository extends JpaRepository<TokenVerificacion, Long> {

    Optional<TokenVerificacion> findByUsuario_UsuarioAndTokenAndUsadoFalse(
            String usuario, String token);

    List<TokenVerificacion> findByUsuario_Id(Long usuarioId);

    void deleteByFechaExpiracionBefore(Instant fecha);
}
