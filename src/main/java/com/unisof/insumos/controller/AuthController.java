package com.unisof.insumos.controller;

import com.unisof.insumos.dto.LoginRequest;
import com.unisof.insumos.dto.LoginResponse;
import com.unisof.insumos.dto.RestablecerContrasenaRequest;
import com.unisof.insumos.dto.SolicitarRecuperacionRequest;
import com.unisof.insumos.dto.VerifyTokenRequest;
import com.unisof.insumos.service.AuditoriaService;
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
 * SCRUM-64: Todos los eventos de autenticación se registran en auditoría.
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
    private final AuditoriaService auditoriaService;  // SCRUM-64

    /**
     * Valida credenciales y envia token 2FA por correo si son correctas.
     * SCRUM-64: Registra LOGIN_EXITOSO o LOGIN_FALLIDO en auditoría.
     * POST /api/auth/login
     *
     * @param request     usuario y contrasena
     * @param httpRequest petición HTTP para obtener IP
     * @return 200 OK con usuario y requiereVerificacionDosPasos=true, o 401 si falla
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        LoginResponse response = authService.validarCredenciales(request);
        String ip = auditoriaService.obtenerIp(httpRequest);

        if (response.isValido()) {
            auditoriaService.registrar(
                    AuditoriaService.ACC_LOGIN,
                    AuditoriaService.MOD_AUTENTICACION,
                    "Credenciales válidas para usuario: " + request.getUsuario() +
                    ". Token 2FA enviado por correo.",
                    request.getUsuario(), null,
                    ip, AuditoriaService.RES_EXITOSO,
                    "Pendiente verificación 2FA"
            );
            return ResponseEntity.ok(response);
        } else {
            auditoriaService.registrar(
                    AuditoriaService.ACC_LOGIN,
                    AuditoriaService.MOD_AUTENTICACION,
                    "Intento de login fallido para usuario: " + request.getUsuario(),
                    request.getUsuario(), null,
                    ip, AuditoriaService.RES_FALLIDO,
                    "Credenciales inválidas"
            );
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Verifica el token 2FA recibido por correo. Si es valido, crea la sesion y retorna el usuario.
     * SCRUM-64: Registra VERIFICACION_2FA exitosa o fallida.
     * POST /api/auth/verify-token
     *
     * @param request     usuario y codigo de 6 digitos
     * @param httpRequest petición HTTP (sesión y IP)
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

        String ip = auditoriaService.obtenerIp(httpRequest);

        if (response.isValido()) {
            // Spring Security 6: guardar contexto explícitamente para que persista en la sesión
            SecurityContext context = SecurityContextHolder.getContext();
            securityContextRepository.saveContext(context, httpRequest, httpResponse);

            String rol = response.getUsuario() != null ? response.getUsuario().getRol() : null;
            auditoriaService.registrar(
                    AuditoriaService.ACC_VERIFY_2FA,
                    AuditoriaService.MOD_AUTENTICACION,
                    "Verificación 2FA exitosa. Sesión iniciada para: " + request.getUsuario(),
                    request.getUsuario(), rol,
                    ip, AuditoriaService.RES_EXITOSO, null
            );
            return ResponseEntity.ok(response);
        } else {
            auditoriaService.registrar(
                    AuditoriaService.ACC_VERIFY_2FA,
                    AuditoriaService.MOD_AUTENTICACION,
                    "Token 2FA inválido o expirado para usuario: " + request.getUsuario(),
                    request.getUsuario(), null,
                    ip, AuditoriaService.RES_FALLIDO,
                    "Token incorrecto o expirado"
            );
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Solicita recuperación de contraseña. Envía enlace por correo si el usuario existe.
     * SCRUM-64: Registra RECUPERAR_CONTRASENA en auditoría.
     * POST /api/auth/solicitar-recuperacion
     */
    @PostMapping("/solicitar-recuperacion")
    public ResponseEntity<Map<String, Object>> solicitarRecuperacion(
            @Valid @RequestBody SolicitarRecuperacionRequest request,
            HttpServletRequest httpRequest) {

        boolean existe = recuperarContrasenaService.solicitarRecuperacion(request.getCorreo());
        String ip = auditoriaService.obtenerIp(httpRequest);

        if (existe) {
            auditoriaService.registrar(
                    AuditoriaService.ACC_RECUPERAR_CONTRASENA,
                    AuditoriaService.MOD_AUTENTICACION,
                    "Solicitud de recuperación de contraseña para correo: " + request.getCorreo(),
                    "anonimo", null,
                    ip, AuditoriaService.RES_EXITOSO,
                    "Correo de recuperación enviado"
            );
            return ResponseEntity.ok(Map.of(
                    "existe", true,
                    "mensaje", "Se le envió un correo de recuperación. Revisa tu bandeja de entrada y spam."
            ));
        }
        auditoriaService.registrar(
                AuditoriaService.ACC_RECUPERAR_CONTRASENA,
                AuditoriaService.MOD_AUTENTICACION,
                "Intento de recuperación para correo no registrado: " + request.getCorreo(),
                "anonimo", null,
                ip, AuditoriaService.RES_FALLIDO,
                "Correo no encontrado en el sistema"
        );
        return ResponseEntity.ok(Map.of("existe", false, "mensaje", "Ese correo no existe."));
    }

    /**
     * Restablece la contraseña con el token recibido por correo.
     * SCRUM-64: Registra RESTABLECER_CONTRASENA en auditoría.
     * POST /api/auth/restablecer-contrasena
     */
    @PostMapping("/restablecer-contrasena")
    public ResponseEntity<Map<String, String>> restablecerContrasena(
            @Valid @RequestBody RestablecerContrasenaRequest request,
            HttpServletRequest httpRequest) {

        String error = recuperarContrasenaService.restablecerContrasena(
                request.getToken(), request.getNuevaClave(), request.getConfirmarClave());
        String ip = auditoriaService.obtenerIp(httpRequest);

        if (error != null) {
            auditoriaService.registrar(
                    AuditoriaService.ACC_RESTABLECER_CONTRASENA,
                    AuditoriaService.MOD_AUTENTICACION,
                    "Intento fallido de restablecer contraseña",
                    "anonimo", null,
                    ip, AuditoriaService.RES_FALLIDO,
                    "Error: " + error
            );
            return ResponseEntity.badRequest().body(Map.of("mensaje", error));
        }
        auditoriaService.registrar(
                AuditoriaService.ACC_RESTABLECER_CONTRASENA,
                AuditoriaService.MOD_AUTENTICACION,
                "Contraseña restablecida exitosamente mediante token de recuperación",
                "anonimo", null,
                ip, AuditoriaService.RES_EXITOSO, null
        );
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
