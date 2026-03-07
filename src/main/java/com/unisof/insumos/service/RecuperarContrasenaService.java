package com.unisof.insumos.service;

import com.unisof.insumos.model.TokenRecuperacionContrasena;
import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.repository.TokenRecuperacionContrasenaRepository;
import com.unisof.insumos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para recuperación de contraseña.
 * Genera tokens únicos, los envía por correo y permite restablecer la contraseña.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RecuperarContrasenaService {

    private static final int EXPIRACION_HORAS = 1;

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionContrasenaRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Solicita recuperación: busca usuario por correo, genera token y envía email.
     * @return true si el correo existe y se envió el correo, false si no existe
     */
    @Transactional
    public boolean solicitarRecuperacion(String correo) {
        if (correo == null || correo.isBlank()) return false;

        Optional<Usuario> opt = usuarioRepository.findByCorreoIgnoreCase(correo.trim());
        if (opt.isEmpty()) {
            log.info("Recuperación solicitada para correo no registrado: {}", correo);
            return false;
        }

        Usuario usuario = opt.get();
        invalidarTokensAnteriores(usuario.getId());

        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expiracion = Instant.now().plusSeconds(EXPIRACION_HORAS * 3600L);
        TokenRecuperacionContrasena t = new TokenRecuperacionContrasena(usuario, token, expiracion);
        tokenRepository.save(t);

        boolean enviado = emailService.enviarLinkRecuperacionContrasena(
                usuario.getCorreo(), usuario.getNombre(), token);

        if (!enviado) {
            log.warn("No se pudo enviar el correo de recuperación a {}", usuario.getCorreo());
        }
        return true;
    }

    /**
     * Restablece la contraseña si el token es válido.
     * Retorna mensaje de error o null si fue exitoso.
     */
    @Transactional
    public String restablecerContrasena(String token, String nuevaClave, String confirmarClave) {
        if (token == null || token.isBlank()) return "Token inválido o expirado.";
        if (nuevaClave == null || nuevaClave.length() < 6) return "La contraseña debe tener al menos 6 caracteres.";
        if (!nuevaClave.equals(confirmarClave)) return "Las contraseñas no coinciden.";

        Optional<TokenRecuperacionContrasena> opt = tokenRepository.findByTokenAndUsadoFalse(token.trim());
        if (opt.isEmpty()) return "Token inválido o expirado.";

        TokenRecuperacionContrasena t = opt.get();
        if (Instant.now().isAfter(t.getFechaExpiracion())) {
            return "El enlace ha expirado. Solicita uno nuevo.";
        }

        Usuario usuario = t.getUsuario();
        usuario.setContrasena(passwordEncoder.encode(nuevaClave));
        usuarioRepository.save(usuario);

        t.setUsado(true);
        tokenRepository.save(t);

        log.info("Contraseña restablecida para usuario {}", usuario.getUsuario());
        return null;
    }

    private void invalidarTokensAnteriores(Long usuarioId) {
        tokenRepository.deleteByUsuario_Id(usuarioId);
    }
}
