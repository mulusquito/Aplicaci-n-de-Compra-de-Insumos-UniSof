package com.unisof.insumos.repository;

import com.unisof.insumos.model.TokenRecuperacionContrasena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repositorio para tokens de recuperación de contraseña.
 */
public interface TokenRecuperacionContrasenaRepository extends JpaRepository<TokenRecuperacionContrasena, Long> {

    Optional<TokenRecuperacionContrasena> findByTokenAndUsadoFalse(String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM TokenRecuperacionContrasena t WHERE t.usuario.id = :usuarioId")
    void deleteByUsuario_Id(@Param("usuarioId") Long usuarioId);
}
