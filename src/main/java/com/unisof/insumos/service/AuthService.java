package com.unisof.insumos.service;

import com.unisof.insumos.dto.LoginRequest;
import com.unisof.insumos.dto.LoginResponse;
import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.unisof.insumos.config.UsuarioUserDetails;

/**
 * Servicio de autenticación.
 * <p>
 * SCRUM-7: Valida credenciales (usuario/contraseña).
 * SCRUM-35: Coordina el envío y verificación del token 2FA por correo.
 * </p>
 *
 * @see com.unisof.insumos.controller.AuthController
 * @see com.unisof.insumos.service.TokenVerificacionService
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenVerificacionService tokenVerificacionService;

    /**
     * Valida las credenciales del usuario (usuario y contraseña).
     * SCRUM-7: Si son correctas, retorna datos del usuario.
     * SCRUM-35: Genera un token de 6 digitos y lo envia al correo del usuario.
     *
     * @param request credenciales (usuario y contrasena)
     * @return respuesta con estado, mensaje y datos del usuario o error
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
     * SCRUM-35: Verifica el token 2FA recibido por correo.
     * SCRUM-36: Si es valido, establece la sesion (onSessionCreated) y la inactividad la invalida en 1 min.
     * <p>
     * El token debe coincidir, no estar expirado y no haber sido usado.
     * </p>
     *
     * @param usuarioNombre   nombre de usuario que realizo el login
     * @param token          codigo de 6 digitos recibido por correo
     * @param onSessionCreated callback con el usuario cuando la verificacion es exitosa (para crear sesion)
     * @return respuesta con usuario si es valido, o mensaje de error
     */
    public LoginResponse validarToken2FA(String usuarioNombre, String token, Consumer<Usuario> onSessionCreated) {
        Optional<Usuario> usuarioOpt = tokenVerificacionService.verificarToken(usuarioNombre, token);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            onSessionCreated.accept(usuario);
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

    /**
     * Establece la sesion del usuario en el contexto de seguridad.
     * SCRUM-36: La sesion expira tras 1 min de inactividad.
     *
     * @param usuario usuario autenticado
     */
    public void establecerSesion(Usuario usuario) {
        UsuarioUserDetails userDetails = new UsuarioUserDetails(usuario);
        var auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * SCRUM-36: Obtiene el usuario actual desde el contexto de seguridad.
     * Retorna empty si no hay sesion (expiro por inactividad o nunca se autentico).
     *
     * @return Optional con el usuario actual o vacio
     */
    public Optional<Usuario> obtenerUsuarioActual() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(auth -> auth.getPrincipal() instanceof UsuarioUserDetails)
                .map(auth -> ((UsuarioUserDetails) auth.getPrincipal()).getUsuario());
    }
}
