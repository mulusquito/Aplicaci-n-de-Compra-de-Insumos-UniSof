package com.unisof.insumos.repository;

import com.unisof.insumos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
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

    /**
     * Indica si ya existe un usuario con el correo dado. SCRUM-12
     */
    boolean existsByCorreo(String correo);

    /**
     * Indica si ya existe un usuario con el número de identificación dado. SCRUM-12
     */
    boolean existsByNumeroIdentificacion(String numeroIdentificacion);

    /**
     * Busca usuarios cuyo nombre completo contenga el texto indicado
     * (ignorando mayúsculas/minúsculas). Usado para el buscador del panel
     * de administración (CRUD de personal).
     */
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca un usuario por su número de identificación.
     */
    Optional<Usuario> findByNumeroIdentificacion(String numeroIdentificacion);
}
