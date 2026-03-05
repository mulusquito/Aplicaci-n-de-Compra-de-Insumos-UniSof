package com.unisof.insumos.controller;

import com.unisof.insumos.dto.RegistroUsuarioRequest;
import com.unisof.insumos.dto.UpdateUsuarioRequest;
import com.unisof.insumos.dto.UsuarioResponse;
import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.service.RegistroUsuarioService;
import com.unisof.insumos.service.UsuarioAdminService;
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
 * por el administrador.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final RegistroUsuarioService registroUsuarioService;
    private final UsuarioAdminService usuarioAdminService;

    /**
     * Registra un nuevo usuario. Solo administrador.
     * Valida que todos los campos estén llenos y que no exista usuario con el mismo
     * usuario, correo o número de identificación.
     */
    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        try {
            Usuario usuario = registroUsuarioService.registrar(request);
            String mensaje = "Usuario " + usuario.getNombre() + " registrado correctamente.";
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensaje", mensaje,
                    "nombre", usuario.getNombre(),
                    "id", usuario.getId(),
                    "correo", usuario.getCorreo()
            ));
        } catch (IllegalArgumentException e) {
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
     * Actualiza un usuario.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUsuarioRequest request
    ) {
        try {
            UsuarioResponse actualizado = usuarioAdminService.actualizar(id, request);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Usuario actualizado correctamente.",
                    "usuario", actualizado
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /**
     * Elimina un usuario.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        usuarioAdminService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado correctamente."));
    }
}
