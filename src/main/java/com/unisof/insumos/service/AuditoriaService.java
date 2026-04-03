package com.unisof.insumos.service;

import com.unisof.insumos.config.UsuarioUserDetails;
import com.unisof.insumos.model.AuditoriaLog;
import com.unisof.insumos.repository.AuditoriaLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Servicio central de auditoría del sistema.
 * SCRUM-64: Registra todas las acciones relevantes en la tabla {@code auditoria_logs}.
 *
 * <p>Uso desde cualquier controller:</p>
 * <pre>
 *   String[] ui = auditoriaService.obtenerUsuarioInfo();
 *   auditoriaService.registrar(
 *       AuditoriaService.ACC_CREAR, AuditoriaService.MOD_CLIENTES,
 *       "Cliente creado: Juan Pérez", ui[0], ui[1],
 *       auditoriaService.obtenerIp(request), AuditoriaService.RES_EXITOSO, null
 *   );
 * </pre>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditoriaService {

    // ─── Módulos ───────────────────────────────────────────────────────────
    public static final String MOD_AUTENTICACION = "AUTENTICACION";
    public static final String MOD_USUARIOS      = "USUARIOS";
    public static final String MOD_CLIENTES      = "CLIENTES";
    public static final String MOD_INSUMOS       = "INSUMOS";
    public static final String MOD_PROVEEDORES   = "PROVEEDORES";
    public static final String MOD_VENTAS        = "VENTAS";
    public static final String MOD_COMPRAS       = "COMPRAS";
    public static final String MOD_DASHBOARD     = "DASHBOARD";
    public static final String MOD_CHATBOT       = "CHATBOT";

    // ─── Acciones ──────────────────────────────────────────────────────────
    public static final String ACC_LOGIN                  = "LOGIN";
    public static final String ACC_LOGOUT                 = "LOGOUT";
    public static final String ACC_VERIFY_2FA             = "VERIFICACION_2FA";
    public static final String ACC_RECUPERAR_CONTRASENA   = "RECUPERAR_CONTRASENA";
    public static final String ACC_RESTABLECER_CONTRASENA = "RESTABLECER_CONTRASENA";
    public static final String ACC_CREAR                  = "CREAR";
    public static final String ACC_EDITAR                 = "EDITAR";
    public static final String ACC_ELIMINAR               = "ELIMINAR";
    public static final String ACC_CONSULTAR              = "CONSULTAR";
    public static final String ACC_CREAR_PAGO             = "CREAR_PAGO";
    public static final String ACC_CONSULTA_NOVA          = "CONSULTA_NOVA";

    // ─── Resultados ────────────────────────────────────────────────────────
    public static final String RES_EXITOSO = "EXITOSO";
    public static final String RES_FALLIDO = "FALLIDO";

    private final AuditoriaLogRepository repository;

    /**
     * Registra un log de auditoría en la base de datos.
     * Si falla la persistencia, loguea el error pero NO lanza excepción
     * para que el fallo en auditoría nunca interrumpa el flujo de negocio.
     *
     * @param accion            código de acción (usar constantes ACC_*)
     * @param modulo            módulo del sistema (usar constantes MOD_*)
     * @param descripcion       descripción legible de la acción
     * @param usuarioNombre     nombre de usuario
     * @param usuarioRol        rol del usuario
     * @param ip                IP del cliente HTTP
     * @param resultado         EXITOSO o FALLIDO (usar constantes RES_*)
     * @param datosAdicionales  información extra en texto libre
     */
    public void registrar(String accion, String modulo, String descripcion,
                          String usuarioNombre, String usuarioRol,
                          String ip, String resultado, String datosAdicionales) {
        try {
            AuditoriaLog entrada = new AuditoriaLog();
            entrada.setFechaHora(Instant.now());
            entrada.setAccion(accion);
            entrada.setModulo(modulo);
            entrada.setDescripcion(descripcion);
            entrada.setUsuarioNombre(usuarioNombre != null ? usuarioNombre : "anonimo");
            entrada.setUsuarioRol(usuarioRol);
            entrada.setIpCliente(ip);
            entrada.setResultado(resultado);
            entrada.setDatosAdicionales(datosAdicionales);
            repository.save(entrada);
        } catch (Exception e) {
            log.warn("SCRUM-64: No se pudo registrar log de auditoría [{}/{}]: {}",
                    modulo, accion, e.getMessage());
        }
    }

    /**
     * Obtiene [nombreUsuario, rol] del usuario autenticado en el contexto de seguridad actual.
     * Retorna ["anonimo", null] si no hay sesión activa.
     *
     * @return arreglo de 2 elementos: [0]=nombreUsuario, [1]=rol
     */
    public String[] obtenerUsuarioInfo() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioUserDetails ud) {
            return new String[]{ud.getUsuario().getUsuario(), ud.getUsuario().getRol()};
        }
        return new String[]{"anonimo", null};
    }

    /**
     * Extrae la IP del cliente considerando proxies (cabecera X-Forwarded-For).
     *
     * @param request petición HTTP actual
     * @return dirección IP del cliente
     */
    public String obtenerIp(HttpServletRequest request) {
        if (request == null) return "desconocida";
        String xForwarded = request.getHeader("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isBlank()) {
            return xForwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
