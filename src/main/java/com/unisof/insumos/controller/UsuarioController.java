package com.unisof.insumos.controller;

import com.unisof.insumos.dto.RegistroUsuarioRequest;
import com.unisof.insumos.dto.UpdateUsuarioRequest;
import com.unisof.insumos.dto.UsuarioResponse;
import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.service.AuditoriaService;
import com.unisof.insumos.service.RegistroUsuarioService;
import com.unisof.insumos.service.UsuarioAdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoints de gestión de usuarios. SCRUM-12: registro y CRUD de personal
 * por el administrador. SCRUM-64: toda creación, edición y eliminación
 * queda registrada en auditoría.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final RegistroUsuarioService registroUsuarioService;
    private final UsuarioAdminService usuarioAdminService;
    private final AuditoriaService auditoriaService;  // SCRUM-64

    /**
     * Registra un nuevo usuario. Solo administrador.
     * SCRUM-64: Registra CREAR en módulo USUARIOS.
     */
    @PostMapping
    public ResponseEntity<?> registrar(
            @Valid @RequestBody RegistroUsuarioRequest request,
            HttpServletRequest httpRequest) {
        try {
            Usuario usuario = registroUsuarioService.registrar(request);
            String mensaje = "Usuario " + usuario.getNombre() + " registrado correctamente.";

            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_CREAR,
                    AuditoriaService.MOD_USUARIOS,
                    "Nuevo usuario creado: " + usuario.getNombre() +
                    " (login=" + usuario.getUsuario() + ", rol=" + usuario.getRol() + ")",
                    ui[0], ui[1],
                    auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_EXITOSO,
                    "ID=" + usuario.getId()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensaje", mensaje,
                    "nombre", usuario.getNombre(),
                    "id", usuario.getId(),
                    "correo", usuario.getCorreo()
            ));
        } catch (IllegalArgumentException e) {
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_CREAR,
                    AuditoriaService.MOD_USUARIOS,
                    "Intento fallido de crear usuario: " + request.getNombreCompleto(),
                    ui[0], ui[1],
                    auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_FALLIDO,
                    "Error: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /**
     * Lista o busca usuarios para el panel de administración.
     *
     * @param criterio texto a buscar (opcional)
     * @param tipo     "nombre", "identificacion" o "todos"
     */
    @GetMapping
    public List<UsuarioResponse> listarOBuscar(
            @RequestParam(required = false) String criterio,
            @RequestParam(required = false, defaultValue = "todos") String tipo
    ) {
        return usuarioAdminService.buscar(criterio, tipo);
    }

    /**
     * Obtiene un usuario por id (para edición).
     */
    @GetMapping("/{id}")
    public UsuarioResponse obtenerPorId(@PathVariable Long id) {
        return usuarioAdminService.obtenerPorId(id);
    }

    /**
     * Actualiza un usuario. SCRUM-64: Registra EDITAR en módulo USUARIOS.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUsuarioRequest request,
            HttpServletRequest httpRequest) {
        try {
            UsuarioResponse actualizado = usuarioAdminService.actualizar(id, request);
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_EDITAR,
                    AuditoriaService.MOD_USUARIOS,
                    "Usuario actualizado: " + actualizado.getNombreCompleto() +
                    " (usuario=" + actualizado.getUsuario() + ", rol=" + actualizado.getRol() + ")",
                    ui[0], ui[1],
                    auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_EXITOSO,
                    "ID=" + id
            );
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Usuario actualizado correctamente.",
                    "usuario", actualizado
            ));
        } catch (IllegalArgumentException e) {
            String[] ui = auditoriaService.obtenerUsuarioInfo();
            auditoriaService.registrar(
                    AuditoriaService.ACC_EDITAR,
                    AuditoriaService.MOD_USUARIOS,
                    "Intento fallido de actualizar usuario ID=" + id,
                    ui[0], ui[1],
                    auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_FALLIDO,
                    "Error: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /**
     * Elimina un usuario. SCRUM-64: Registra ELIMINAR en módulo USUARIOS.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        usuarioAdminService.eliminar(id);
        String[] ui = auditoriaService.obtenerUsuarioInfo();
        auditoriaService.registrar(
                AuditoriaService.ACC_ELIMINAR,
                AuditoriaService.MOD_USUARIOS,
                "Usuario eliminado del sistema",
                ui[0], ui[1],
                auditoriaService.obtenerIp(httpRequest),
                AuditoriaService.RES_EXITOSO,
                "ID=" + id
        );
        return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado correctamente."));
    }
}
