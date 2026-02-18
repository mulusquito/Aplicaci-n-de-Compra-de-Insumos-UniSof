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

/**
 * SCRUM-35: Servicio para verificación en dos pasos mediante token enviado por correo
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

    @Transactional
    public Optional<TokenVerificacion> generarYEnviarToken(Usuario usuario) {
        invalidarTokensAnteriores(usuario.getId());

        String token = generarTokenNumerico();
        Instant expiracion = Instant.now().plusSeconds(EXPIRACION_MINUTOS * 60L);

        TokenVerificacion tokenVerificacion = new TokenVerificacion(usuario, token, expiracion);
        tokenRepository.save(tokenVerificacion);

        boolean enviado = emailService.enviarTokenVerificacion(usuario.getCorreo(), usuario.getNombre(), token);

        if (!enviado) {
            log.warn("No se pudo enviar el token por correo a {}", usuario.getCorreo());
        }

        return Optional.of(tokenVerificacion);
    }

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
