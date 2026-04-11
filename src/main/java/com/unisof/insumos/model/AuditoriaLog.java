package com.unisof.insumos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entidad de auditoría. Registra cada acción relevante realizada en el sistema.
 * SCRUM-64: Trazabilidad y control de acciones de usuarios.
 *
 * <p>Campos principales:
 * <ul>
 *   <li>{@code fechaHora} — Instante UTC exacto de la acción.</li>
 *   <li>{@code usuarioNombre} — Usuario que la realizó ("anonimo" si no hay sesión).</li>
 *   <li>{@code usuarioRol} — Rol del usuario en ese momento.</li>
 *   <li>{@code accion} — Código de acción: LOGIN, LOGOUT, CREAR, EDITAR, ELIMINAR, etc.</li>
 *   <li>{@code modulo} — Módulo del sistema: AUTENTICACION, VENTAS, COMPRAS, etc.</li>
 *   <li>{@code descripcion} — Descripción legible de lo ocurrido.</li>
 *   <li>{@code ipCliente} — IP del cliente HTTP.</li>
 *   <li>{@code resultado} — EXITOSO o FALLIDO.</li>
 *   <li>{@code datosAdicionales} — Información extra en texto libre (IDs, nombres, etc.).</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "auditoria_logs", indexes = {
        @Index(name = "idx_auditoria_fecha", columnList = "fechaHora"),
        @Index(name = "idx_auditoria_usuario", columnList = "usuarioNombre"),
        @Index(name = "idx_auditoria_modulo", columnList = "modulo"),
        @Index(name = "idx_auditoria_resultado", columnList = "resultado")
})
@Getter
@Setter
@NoArgsConstructor
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Fecha y hora exacta en UTC de cuando ocurrió la acción */
    @Column(nullable = false)
    private Instant fechaHora;

    /** Nombre de usuario que realizó la acción. "anonimo" si no hay sesión activa. */
    @Column(length = 100)
    private String usuarioNombre;

    /** Rol del usuario al momento de la acción (ADMINISTRADOR, VENDEDOR, etc.) */
    @Column(length = 60)
    private String usuarioRol;

    /**
     * Código de la acción realizada.
     * Valores posibles: LOGIN, LOGOUT, VERIFICACION_2FA, RECUPERAR_CONTRASENA,
     * RESTABLECER_CONTRASENA, CREAR, EDITAR, ELIMINAR, CONSULTAR, CREAR_PAGO, CONSULTA_NOVA
     */
    @Column(nullable = false, length = 60)
    private String accion;

    /**
     * Módulo del sistema donde ocurrió la acción.
     * Valores: AUTENTICACION, USUARIOS, CLIENTES, INSUMOS, PROVEEDORES,
     * VENTAS, COMPRAS, DASHBOARD, CHATBOT
     */
    @Column(nullable = false, length = 50)
    private String modulo;

    /** Descripción detallada y legible de la acción */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /** Dirección IP del cliente HTTP que realizó la petición */
    @Column(length = 100)
    private String ipCliente;

    /** Resultado de la operación: EXITOSO o FALLIDO */
    @Column(nullable = false, length = 20)
    private String resultado;

    /** Datos adicionales relevantes (IDs afectados, nombres, contexto extra) */
    @Column(columnDefinition = "TEXT")
    private String datosAdicionales;
}
