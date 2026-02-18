package com.unisof.insumos.service;

import com.unisof.insumos.dto.LoginRequest;
import com.unisof.insumos.dto.LoginResponse;
import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Valida las credenciales del usuario (usuario y contraseña).
     * SCRUM-7: Validar credenciales del usuario por medio de usuario y contraseña
     */
    public LoginResponse validarCredenciales(LoginRequest request) {
        return usuarioRepository.findByUsuario(request.getUsuario())
                .filter(usuario -> passwordEncoder.matches(request.getContrasena(), usuario.getContrasena()))
                .map(usuario -> LoginResponse.builder()
                        .valido(true)
                        .mensaje("Credenciales válidas")
                        .usuario(new LoginResponse.UsuarioResponse(
                                usuario.getId(),
                                usuario.getUsuario(),
                                usuario.getNombre(),
                                usuario.getCorreo(),
                                usuario.getRol()
                        ))
                        .build())
                .orElse(LoginResponse.builder()
                        .valido(false)
                        .mensaje("Credenciales inválidas")
                        .usuario(null)
                        .build());
    }
}
