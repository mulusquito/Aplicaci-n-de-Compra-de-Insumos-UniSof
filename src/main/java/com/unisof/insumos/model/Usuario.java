package com.unisof.insumos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un usuario del sistema de compra de insumos.
 * <p>
 * Almacena credenciales, datos personales y rol. SCRUM-7: Validación de credenciales.
 * SCRUM-35: El correo se utiliza para enviar el token de verificación en dos pasos.
 * </p>
 *
 * @see com.unisof.insumos.repository.UsuarioRepository
 * @see com.unisof.insumos.service.AuthService
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    /** Identificador único del usuario */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de usuario para inicio de sesión (único) */
    @Column(nullable = false, unique = true)
    private String usuario;

    /** Contraseña encriptada (BCrypt) */
    @Column(nullable = false)
    private String contrasena;

    /** Nombre completo del usuario */
    @Column(nullable = false)
    private String nombre;

    /** Correo electrónico donde se recibe el token 2FA (único) */
    @Column(nullable = false, unique = true)
    private String correo;

    /** Número de identificación (único). SCRUM-12 */
    @Column(name = "numero_identificacion", unique = true)
    private String numeroIdentificacion;

    /** Celular de contacto. SCRUM-12 */
    @Column(length = 20)
    private String celular;

    /** Rol del usuario (ej: ADMINISTRADOR, VENDEDOR, JEFE DE COMPRAS; puede existir JEFE DE VENTAS en registros antiguos) */
    @Column(nullable = false)
    private String rol = "WORKER";

    /**
     * Crea un nuevo usuario con los datos básicos.
     *
     * @param usuario    nombre de usuario para login
     * @param contrasena contraseña en texto plano (se debe encriptar antes de guardar)
     * @param nombre     nombre completo
     * @param correo     correo electrónico
     */
    public Usuario(String usuario, String contrasena, String nombre, String correo) {
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.correo = correo;
    }
}
