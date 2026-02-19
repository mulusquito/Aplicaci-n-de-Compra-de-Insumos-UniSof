package com.unisof.insumos.repository;

import com.unisof.insumos.model.TokenVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad {@link TokenVerificacion}.
 * <p>
 * SCRUM-35: Consultas para validar tokens 2FA, listar por usuario y eliminar expirados.
 * </p>
 */
public interface TokenVerificacionRepository extends JpaRepository<TokenVerificacion, Long> {

    /**
     * Busca un token no usado que coincida con usuario y código.
     *
     * @param usuario nombre de usuario
     * @param token   código de 6 dígitos
     * @return Optional con el token si existe y no está usado
     */
    Optional<TokenVerificacion> findByUsuario_UsuarioAndTokenAndUsadoFalse(
            String usuario, String token);

    /**
     * Lista todos los tokens asociados a un usuario.
     *
     * @param usuarioId ID del usuario
     * @return lista de tokens
     */
    List<TokenVerificacion> findByUsuario_Id(Long usuarioId);

    /**
     * Elimina tokens cuya fecha de expiración sea anterior a la indicada.
     *
     * @param fecha fecha límite
     */
    void deleteByFechaExpiracionBefore(Instant fecha);
}
