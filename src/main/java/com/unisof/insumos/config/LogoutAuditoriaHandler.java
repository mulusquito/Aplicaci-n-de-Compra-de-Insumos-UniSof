package com.unisof.insumos.config;

import com.unisof.insumos.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * Handler de Spring Security que registra en auditoría cada cierre de sesión.
 * SCRUM-64: Se ejecuta antes de invalidar la sesión HTTP, por lo que aún
 * tiene acceso al usuario autenticado en el contexto de seguridad.
 */
@Component
@RequiredArgsConstructor
public class LogoutAuditoriaHandler implements LogoutHandler {

    private final AuditoriaService auditoriaService;

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        String nombreUsuario = "desconocido";
        String rol = null;

        if (authentication != null
                && authentication.getPrincipal() instanceof UsuarioUserDetails ud) {
            nombreUsuario = ud.getUsuario().getUsuario();
            rol = ud.getUsuario().getRol();
        }

        auditoriaService.registrar(
                AuditoriaService.ACC_LOGOUT,
                AuditoriaService.MOD_AUTENTICACION,
                "Cierre de sesión del usuario: " + nombreUsuario,
                nombreUsuario,
                rol,
                auditoriaService.obtenerIp(request),
                AuditoriaService.RES_EXITOSO,
                null
        );
    }
}
