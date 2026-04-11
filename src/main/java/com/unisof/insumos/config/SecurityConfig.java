

package com.unisof.insumos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Map;

/**
 * Configuracion de Spring Security.
 * <p>
 * Permite acceso publico a /api/auth/login y /api/auth/verify-token.
 * Timeout de sesión en servidor: ver application.properties; la política de aviso/cierre por inactividad la aplica el cliente (auth.js).
 * Resto de endpoints requiere autenticacion.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Handler de auditoría para logout. Se inyecta aquí para registrar
     * el cierre de sesión antes de que Spring Security invalide la sesión.
     * SCRUM-64.
     */
    private final LogoutAuditoriaHandler logoutAuditoriaHandler;

    public SecurityConfig(LogoutAuditoriaHandler logoutAuditoriaHandler) {
        this.logoutAuditoriaHandler = logoutAuditoriaHandler;
    }

    /** Codificador BCrypt para contrasenas */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Repositorio para persistir SecurityContext en HttpSession (SCRUM-36) */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /** Cadena de filtros de seguridad HTTP */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(f -> f.disable())
                .httpBasic(b -> b.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(json401EntryPoint())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/verify-token", "/api/auth/solicitar-recuperacion", "/api/auth/restablecer-contrasena", "/api/webhooks/**", "/api/chat").permitAll()
                        // SCRUM-64: logs de auditoría solo para ADMINISTRADOR
                        .requestMatchers("/api/usuarios/**", "/api/dashboard/**", "/api/proveedores/**", "/api/auditoria/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/compras/**").hasAnyRole("ADMINISTRADOR", "JEFE DE COMPRAS", "JEFE DE VENTAS")
                        .requestMatchers("/api/fichas-tecnicas/**").hasAnyRole("ADMINISTRADOR", "JEFE DE COMPRAS", "JEFE DE VENTAS")
                        .requestMatchers("/api/analisis-ordenes/**").hasAnyRole("ADMINISTRADOR", "JEFE DE COMPRAS", "JEFE DE VENTAS")
                        .requestMatchers("/api/reportes-consolidados/**").hasAnyRole("ADMINISTRADOR", "JEFE DE COMPRAS", "JEFE DE VENTAS")
                        .requestMatchers("/api/facturas-proveedor/**").hasAnyRole("ADMINISTRADOR", "JEFE DE COMPRAS", "JEFE DE VENTAS")
                        .requestMatchers("/api/auth/logout", "/api/auth/me", "/api/checkout/create-preference", "/api/clientes/**", "/api/recibos/**").authenticated()
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/verificar-token.html",
                                "/recuperar-contrasena.html",
                                "/restablecer-contrasena.html",
                                "/panel-admin.html",
                                "/personal.html",
                                "/terminos-y-condiciones.html",
                                "/ventas.html",
                                "/compras.html",
                                "/inventario-insumos.html",
                                "/clientes.html",
                                "/ordenes.html",
                                "/proveedores.html",
                                "/ordenes-compras.html",
                                "/fichas-tecnicas.html",
                                "/reporte-faltantes.html",
                                "/analisis-insumos.html",
                                "/ordenes-compra.html",
                                "/facturas-proveedores.html",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        // SCRUM-64: registrar logout en auditoría ANTES de invalidar la sesión
                        .addLogoutHandler(logoutAuditoriaHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_OK))
                );
        return http.build();
    }

    /** SCRUM-36: Respuesta JSON en 401 (API) o redireccion a login (navegacion HTML) */
    @Bean
    public AuthenticationEntryPoint json401EntryPoint() {
        return (request, response, authException) -> {
            HttpServletRequest req = request;
            String accept = req.getHeader("Accept") != null ? req.getHeader("Accept").toLowerCase() : "";
            String path = req.getRequestURI() != null ? req.getRequestURI() : "";
            boolean pideHtml = accept.contains("text/html") || path.equals("/") || path.equals("/index.html") || path.endsWith(".html");
            if (pideHtml) {
                response.sendRedirect("/login.html");
                return;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            try {
                response.getWriter().write(new ObjectMapper().writeValueAsString(
                        Map.of("mensaje", "Sesion expirada o no autenticado. Inicie sesion nuevamente.")
                ));
            } catch (java.io.IOException e) {
                throw new IllegalStateException("Error escribiendo respuesta 401", e);
            }
        };
    }
}
