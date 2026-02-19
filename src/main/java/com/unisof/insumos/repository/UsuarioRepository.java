package com.unisof.insumos.repository;

import com.unisof.insumos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio para la entidad {@link Usuario}.
 * <p>
 * Proporciona acceso a la base de datos para operaciones CRUD y búsquedas por usuario.
 * </p>
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario (campo usuario).
     *
     * @param usuario nombre de usuario a buscar
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<Usuario> findByUsuario(String usuario);
}
