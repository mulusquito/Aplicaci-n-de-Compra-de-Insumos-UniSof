package com.unisof.insumos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Token de verificación en dos pasos (2FA) enviado por correo electrónico.
 * <p>
 * SCRUM-35: Cada token es de 6 dígitos, tiene fecha de expiración (10 min) y
 * se marca como usado una vez validado.
 * </p>
 *
 * @see com.unisof.insumos.service.TokenVerificacionService
 * @see com.unisof.insumos.service.EmailService
 */
@Entity
@Table(name = "tokens_verificacion")
@Getter
@Setter
@NoArgsConstructor
public class TokenVerificacion {

    /** Identificador único del token */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario al que pertenece el token */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Código de 6 dígitos enviado por correo */
    @Column(nullable = false, length = 6)
    private String token;

    /** Fecha y hora límite de validez del token */
    @Column(nullable = false)
    private Instant fechaExpiracion;

    /** Indica si el token ya fue utilizado */
    @Column(nullable = false)
    private boolean usado = false;

    /**
     * Crea un token de verificación para un usuario.
     *
     * @param usuario          usuario que solicita el token
     * @param token            código de 6 dígitos
     * @param fechaExpiracion  instante en que expira
     */
    public TokenVerificacion(Usuario usuario, String token, Instant fechaExpiracion) {
        this.usuario = usuario;
        this.token = token;
        this.fechaExpiracion = fechaExpiracion;
    }
}
