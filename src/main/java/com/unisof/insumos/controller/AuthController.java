package com.unisof.insumos.controller;

import com.unisof.insumos.dto.LoginRequest;
import com.unisof.insumos.dto.LoginResponse;
import com.unisof.insumos.dto.RestablecerContrasenaRequest;
import com.unisof.insumos.dto.SolicitarRecuperacionRequest;
import com.unisof.insumos.dto.VerifyTokenRequest;
import com.unisof.insumos.service.AuthService;
import com.unisof.insumos.service.RecuperarContrasenaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de autenticacion.
 * <p>
 * SCRUM-7: Endpoint de login con usuario y contrasena.
 * SCRUM-35: Endpoint de verificacion del token 2FA recibido por correo.
 * Sesión HTTP: timeout en server.servlet.session.timeout; el front renueva con /me al interactuar.
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RecuperarContrasenaService recuperarContrasenaService;
    private final SecurityContextRepository securityContextRepository;

    /**
     * Valida credenciales y envia token 2FA por correo si son correctas.
     * POST /api/auth/login
     *
     * @param request usuario y contrasena
     * @return 200 OK con usuario y requiereVerificacionDosPasos=true, o 401 si falla
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.validarCredenciales(request);

        if (response.isValido()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Verifica el token 2FA recibido por correo. Si es valido, crea la sesion y retorna el usuario.
     * Tras verificar, queda sesión HTTP; timeout en configuración; el cliente controla avisos de inactividad.
     * POST /api/auth/verify-token
     *
     * @param request usuario y codigo de 6 digitos
     * @return 200 OK con usuario si es correcto, o 401 si token invalido/expirado
     */
    @PostMapping("/verify-token")
    public ResponseEntity<LoginResponse> verifyToken(
            @Valid @RequestBody VerifyTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        httpRequest.getSession(true); // SCRUM-36: Garantiza sesión para persistir autenticación
        LoginResponse response = authService.validarToken2FA(
                request.getUsuario(),
                request.getToken(),
                authService::establecerSesion
        );

        if (response.isValido()) {
            // Spring Security 6: guardar contexto explícitamente para que persista en la sesión
            SecurityContext context = SecurityContextHolder.getContext();
            securityContextRepository.saveContext(context, httpRequest, httpResponse);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Solicita recuperación de contraseña. Envía enlace por correo si el usuario existe.
     * Por seguridad, siempre retorna 200 (evita enumeración de correos).
     * POST /api/auth/solicitar-recuperacion
     */
    @PostMapping("/solicitar-recuperacion")
    public ResponseEntity<Map<String, Object>> solicitarRecuperacion(@Valid @RequestBody SolicitarRecuperacionRequest request) {
        boolean existe = recuperarContrasenaService.solicitarRecuperacion(request.getCorreo());
        if (existe) {
            return ResponseEntity.ok(Map.of("existe", true, "mensaje", "Se le envió un correo de recuperación. Revisa tu bandeja de entrada y spam."));
        }
        return ResponseEntity.ok(Map.of("existe", false, "mensaje", "Ese correo no existe."));
    }

    /**
     * Restablece la contraseña con el token recibido por correo.
     * POST /api/auth/restablecer-contrasena
     */
    @PostMapping("/restablecer-contrasena")
    public ResponseEntity<Map<String, String>> restablecerContrasena(@Valid @RequestBody RestablecerContrasenaRequest request) {
        String error = recuperarContrasenaService.restablecerContrasena(
                request.getToken(), request.getNuevaClave(), request.getConfirmarClave());
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", error));
        }
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada. Ya puedes iniciar sesión."));
    }

    /**
     * SCRUM-36: Devuelve el usuario actual si hay sesion valida. 401 si expiro por inactividad.
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<LoginResponse.UsuarioResponse> me() {
        return authService.obtenerUsuarioActual()
                .map(u -> ResponseEntity.ok(new LoginResponse.UsuarioResponse(
                        u.getId(), u.getUsuario(), u.getNombre(), u.getCorreo(), u.getRol())))
                .orElse(ResponseEntity.status(401).build());
    }
}
