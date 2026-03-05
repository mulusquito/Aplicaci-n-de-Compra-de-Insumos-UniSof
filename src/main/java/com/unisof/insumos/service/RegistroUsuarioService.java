package com.unisof.insumos.service;

import com.unisof.insumos.dto.RegistroUsuarioRequest;
import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para el registro de usuarios por el administrador.
 * SCRUM-12: Valida campos obligatorios y que el usuario no exista (correo o número de identificación).
 */
@Service
@RequiredArgsConstructor
public class RegistroUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo usuario. Valida que todos los campos estén presentes,
     * que clave y confirmarClave coincidan, y que no exista ya un usuario con el mismo
     * correo o número de identificación.
     *
     * @param request datos del registro
     * @return usuario creado
     * @throws IllegalArgumentException si validación falla o el usuario ya existe
     */
    @Transactional
    public Usuario registrar(RegistroUsuarioRequest request) {
        if (request.getClave() == null || !request.getClave().equals(request.getConfirmarClave())) {
            throw new IllegalArgumentException("La clave y confirmar clave no coinciden.");
        }
        if (usuarioRepository.findByUsuario(request.getUsuario().trim()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre de usuario. Elija otro.");
        }
        if (usuarioRepository.existsByCorreo(request.getCorreo().trim())) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con este correo electrónico.");
        }
        if (usuarioRepository.existsByNumeroIdentificacion(request.getNumeroIdentificacion().trim())) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con este número de identificación.");
        }

        Usuario u = new Usuario();
        u.setUsuario(request.getUsuario().trim()); // con este usuario ingresará al sistema
        u.setContrasena(passwordEncoder.encode(request.getClave()));
        u.setNombre(request.getNombreCompleto().trim());
        u.setCorreo(request.getCorreo().trim());
        u.setNumeroIdentificacion(request.getNumeroIdentificacion().trim());
        u.setCelular(request.getCelular() != null ? request.getCelular().trim() : null);
        u.setRol(normalizarRol(request.getRol()));
        return usuarioRepository.save(u);
    }

    private static String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) return "VENDEDOR";
        String r = rol.trim().toUpperCase();
        if ("ADMINISTRADOR".equals(r) || "VENDEDOR".equals(r) || "JEFE DE VENTAS".equals(r)) return r;
        return "VENDEDOR";
    }
}
