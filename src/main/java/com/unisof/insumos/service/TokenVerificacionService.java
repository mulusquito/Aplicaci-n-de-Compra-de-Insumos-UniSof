package com.unisof.insumos.service;

import com.unisof.insumos.model.TokenVerificacion;
import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.repository.TokenVerificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * SCRUM-35: Servicio para verificación en dos pasos (2FA) mediante token enviado por correo.
 * <p>
 * Genera tokens numéricos de 6 dígitos, los guarda con expiración de 10 minutos,
 * invalida tokens anteriores del mismo usuario y delega el envío por email a {@link EmailService}.
 * </p>
 *
 * @see EmailService
 * @see AuthService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenVerificacionService {

    private static final int TOKEN_LENGTH = 6;
    private static final int EXPIRACION_MINUTOS = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TokenVerificacionRepository tokenRepository;
    private final EmailService emailService;

    /**
     * Genera un token, lo guarda y envía el correo en segundo plano para no bloquear el login.
     * Invalida cualquier token previo no usado del mismo usuario.
     *
     * @param usuario usuario que solicitó el login
     * @return Optional con el token creado (siempre presente si se guardó)
     */
    @Transactional
    public Optional<TokenVerificacion> generarYEnviarToken(Usuario usuario) {
        invalidarTokensAnteriores(usuario.getId());

        String token = generarTokenNumerico();
        Instant expiracion = Instant.now().plusSeconds(EXPIRACION_MINUTOS * 60L);

        TokenVerificacion tokenVerificacion = new TokenVerificacion(usuario, token, expiracion);
        tokenRepository.save(tokenVerificacion);

        String correo = usuario.getCorreo();
        String nombre = usuario.getNombre();
        CompletableFuture.runAsync(() -> {
            boolean enviado = emailService.enviarTokenVerificacion(correo, nombre, token);
            if (!enviado) {
                log.warn("No se pudo enviar el token por correo a {}", correo);
            }
        });

        return Optional.of(tokenVerificacion);
    }

    /**
     * Verifica que el token sea válido (no expirado, no usado).
     * Si es correcto, marca el token como usado y retorna el usuario.
     *
     * @param usuarioNombre nombre de usuario
     * @param token        código de 6 dígitos
     * @return Optional con el usuario si el token es válido
     */
    @Transactional
    public Optional<Usuario> verificarToken(String usuarioNombre, String token) {
        return tokenRepository.findByUsuario_UsuarioAndTokenAndUsadoFalse(usuarioNombre, token)
                .filter(t -> !t.getFechaExpiracion().isBefore(Instant.now()))
                .map(t -> {
                    t.setUsado(true);
                    tokenRepository.save(t);
                    return t.getUsuario();
                });
    }

    private String generarTokenNumerico() {
        StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private void invalidarTokensAnteriores(Long usuarioId) {
        tokenRepository.findByUsuario_Id(usuarioId)
                .forEach(t -> {
                    t.setUsado(true);
                    tokenRepository.save(t);
                });
    }
}
