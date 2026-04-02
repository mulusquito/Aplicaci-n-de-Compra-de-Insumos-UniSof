package com.unisof.insumos.service;

import com.unisof.insumos.dto.UpdateUsuarioRequest;
import com.unisof.insumos.dto.UsuarioResponse;
import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.repository.TokenVerificacionRepository;
import com.unisof.insumos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de administración de usuarios para el panel del administrador.
 * SCRUM-12: CRUD de personal (listar, buscar, actualizar y eliminar).
 */
@Service
public class UsuarioAdminService {

    private final UsuarioRepository usuarioRepository;
    private final TokenVerificacionRepository tokenVerificacionRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioAdminService(UsuarioRepository usuarioRepo,
                               TokenVerificacionRepository tokenRepo,
                               PasswordEncoder pwEncoder) {
        this.usuarioRepository = usuarioRepo;
        this.tokenVerificacionRepository = tokenRepo;
        this.passwordEncoder = pwEncoder;
    }

    /**
     * Lista todos los usuarios ordenados por nombre.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(this::toResponse)
                .toList();
    }

    /**
     * Busca usuarios según el tipo:
     * - "identificacion": por número de identificación (0 o 1 resultado)
     * - "nombre": por nombre que contenga el texto (1..N resultados)
     * - otro/null: devuelve todos.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> buscar(String criterio, String tipo) {
        if (criterio == null || criterio.isBlank()) {
            return listarTodos();
        }
        String trimmed = criterio.trim();
        String t = tipo != null ? tipo.toLowerCase() : "todos";
        if ("identificacion".equals(t)) {
            Optional<Usuario> u = usuarioRepository.findByNumeroIdentificacion(trimmed);
            return u.map(value -> List.of(toResponse(value))).orElseGet(Collections::emptyList);
        }
        if ("nombre".equals(t)) {
            return usuarioRepository.findByNombreContainingIgnoreCase(trimmed)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }
        return listarTodos();
    }

    /**
     * Obtiene un usuario por su identificador.
     */
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return toResponse(u);
    }

    /**
     * Actualiza los datos de un usuario. Valida unicidad de usuario, correo y
     * número de identificación, y opcionalmente actualiza la contraseña.
     */
    @Transactional
    public UsuarioResponse actualizar(Long id, UpdateUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String nuevoUsuario = request.getUsuario().trim();
        usuarioRepository.findByUsuario(nuevoUsuario)
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new IllegalArgumentException("Ya existe otro usuario con ese nombre de usuario.");
                });

        String nuevoCorreo = request.getCorreo().trim();
        usuarioRepository.findByNumeroIdentificacion(request.getNumeroIdentificacion().trim())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new IllegalArgumentException("Ya existe otro usuario con ese número de identificación.");
                });

        usuarioRepository.findByNumeroIdentificacion(request.getNumeroIdentificacion().trim())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new IllegalArgumentException("Ya existe otro usuario con ese número de identificación.");
                });

        usuario.setUsuario(nuevoUsuario);
        usuario.setNombre(request.getNombreCompleto().trim());
        usuario.setNumeroIdentificacion(request.getNumeroIdentificacion().trim());
        usuario.setCorreo(nuevoCorreo);
        usuario.setCelular(request.getCelular() != null ? request.getCelular().trim() : null);
        usuario.setRol(normalizarRol(request.getRol()));

        if (request.getClave() != null && !request.getClave().isBlank()
                && request.getConfirmarClave() != null && !request.getConfirmarClave().isBlank()) {
            if (!request.getClave().equals(request.getConfirmarClave())) {
                throw new IllegalArgumentException("La nueva clave y su confirmación no coinciden.");
            }
            usuario.setContrasena(passwordEncoder.encode(request.getClave()));
        }

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponse(guardado);
    }

    /**
     * Elimina un usuario por su id.
     * Elimina primero los tokens de verificación 2FA para evitar violación de FK.
     */
    @Transactional
    public void eliminar(Long id) {
        tokenVerificacionRepository.deleteByUsuario_Id(id);
        usuarioRepository.deleteById(id);
    }

    private static String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) return "VENDEDOR";
        String r = rol.trim().toUpperCase();
        if ("ADMINISTRADOR".equals(r) || "VENDEDOR".equals(r) || "JEFE DE COMPRAS".equals(r) || "JEFE DE VENTAS".equals(r)) {
            return r;
        }
        return "VENDEDOR";
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getUsuario(),
                u.getNombre(),
                u.getNumeroIdentificacion(),
                u.getCorreo(),
                u.getCelular(),
                u.getRol()
        );
    }
}

