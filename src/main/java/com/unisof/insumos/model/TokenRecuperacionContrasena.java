package com.unisof.insumos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Token para recuperación de contraseña.
 * Se genera al solicitar "Olvidé mi contraseña", se envía por correo
 * y permite restablecer la contraseña en un enlace con validez de 1 hora.
 */
@Entity
@Table(name = "tokens_recuperacion_contrasena")
@Getter
@Setter
@NoArgsConstructor
public class TokenRecuperacionContrasena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(nullable = false)
    private Instant fechaExpiracion;

    @Column(nullable = false)
    private boolean usado = false;

    public TokenRecuperacionContrasena(Usuario usuario, String token, Instant fechaExpiracion) {
        this.usuario = usuario;
        this.token = token;
        this.fechaExpiracion = fechaExpiracion;
    }
}
