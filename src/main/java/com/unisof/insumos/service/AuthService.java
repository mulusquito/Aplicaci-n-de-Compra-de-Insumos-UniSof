package com.unisof.insumos.service;

import com.unisof.insumos.dto.LoginRequest;
import com.unisof.insumos.dto.LoginResponse;
import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenVerificacionService tokenVerificacionService;

    /**
     * Valida las credenciales del usuario (usuario y contraseña).
     * SCRUM-7: Validar credenciales del usuario por medio de usuario y contraseña
     * SCRUM-35: Si son válidas, genera token 2FA y lo envía por correo
     */
    public LoginResponse validarCredenciales(LoginRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsuario(request.getUsuario())
                .filter(u -> passwordEncoder.matches(request.getContrasena(), u.getContrasena()));
        if (usuarioOpt.isEmpty()) {
            return LoginResponse.builder()
                    .valido(false)
                    .mensaje("Credenciales inválidas")
                    .usuario(null)
                    .requiereVerificacionDosPasos(false)
                    .build();
        }
        Usuario usuario = usuarioOpt.get();
        if (tokenVerificacionService.generarYEnviarToken(usuario).isEmpty()) {
            return LoginResponse.builder()
                    .valido(false)
                    .mensaje("Error al enviar token")
                    .usuario(null)
                    .requiereVerificacionDosPasos(false)
                    .build();
        }
        return LoginResponse.builder()
                .valido(true)
                .mensaje("Token de verificacion enviado a tu correo electronico")
                .usuario(new LoginResponse.UsuarioResponse(
                        usuario.getId(),
                        usuario.getUsuario(),
                        usuario.getNombre(),
                        usuario.getCorreo(),
                        usuario.getRol()
                ))
                .requiereVerificacionDosPasos(true)
                .build();
    }

    /**
     * SCRUM-35: Verifica el token enviado por correo. Si es válido, retorna usuario para redirigir al panel principal
     */
    public LoginResponse validarToken2FA(String usuarioNombre, String token) {
        Optional<Usuario> usuarioOpt = tokenVerificacionService.verificarToken(usuarioNombre, token);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            return LoginResponse.builder()
                    .valido(true)
                    .mensaje("Verificación exitosa. Redirigiendo al panel principal")
                    .usuario(new LoginResponse.UsuarioResponse(
                            usuario.getId(),
                            usuario.getUsuario(),
                            usuario.getNombre(),
                            usuario.getCorreo(),
                            usuario.getRol()
                    ))
                    .requiereVerificacionDosPasos(false)
                    .build();
        } else {
            return LoginResponse.builder()
                    .valido(false)
                    .mensaje("Token inválido o expirado")
                    .usuario(null)
                    .requiereVerificacionDosPasos(false)
                    .build();
        }
    }
}
