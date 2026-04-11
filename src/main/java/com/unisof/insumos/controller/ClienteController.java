package com.unisof.insumos.controller;

import com.unisof.insumos.model.Cliente;
import com.unisof.insumos.repository.ClienteRepository;
import com.unisof.insumos.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API CRUD para clientes.
 * SCRUM-16: Buscar cliente por cédula para asociar pedidos.
 * SCRUM-64: Creación, edición, eliminación y consultas quedan registradas en auditoría.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final AuditoriaService auditoriaService;  // SCRUM-64

    public ClienteController(ClienteRepository clienteRepository,
                             AuditoriaService auditoriaService) {
        this.clienteRepository = clienteRepository;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Lista todos los clientes.
     * GET /api/clientes
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listarTodos() {
        List<Map<String, Object>> lista = clienteRepository.findAll().stream()
                .map(c -> Map.<String, Object>of(
                        "id", c.getId(),
                        "nombre", c.getNombre(),
                        "cedula", c.getCedula(),
                        "correo", c.getCorreo(),
                        "telefono", c.getTelefono() != null ? c.getTelefono() : "",
                        "direccion", c.getDireccion() != null ? c.getDireccion() : ""
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    /**
     * Busca un cliente por ID.
     * GET /api/clientes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return clienteRepository.findById(id)
                .map(c -> ResponseEntity.ok(Map.of(
                        "id", c.getId(),
                        "nombre", c.getNombre(),
                        "cedula", c.getCedula(),
                        "correo", c.getCorreo(),
                        "telefono", c.getTelefono() != null ? c.getTelefono() : "",
                        "direccion", c.getDireccion() != null ? c.getDireccion() : ""
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca un cliente por número de cédula.
     * SCRUM-64: Registra CONSULTAR en auditoría.
     * GET /api/clientes/buscar?cedula=123456789
     */
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorCedula(
            @RequestParam String cedula,
            HttpServletRequest httpRequest) {

        String cedulaLimpia = cedula.replaceAll("[^0-9]", "").trim();
        if (cedulaLimpia.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Ingresa un número de cédula válido"));
        }

        var resultado = clienteRepository.findByCedula(cedulaLimpia);
        String[] ui = auditoriaService.obtenerUsuarioInfo();
        auditoriaService.registrar(
                AuditoriaService.ACC_CONSULTAR,
                AuditoriaService.MOD_CLIENTES,
                resultado.isPresent()
                        ? "Consulta de cliente por cédula: " + cedulaLimpia + " — encontrado: " + resultado.get().getNombre()
                        : "Consulta de cliente por cédula: " + cedulaLimpia + " — no encontrado",
                ui[0], ui[1],
                auditoriaService.obtenerIp(httpRequest),
                resultado.isPresent() ? AuditoriaService.RES_EXITOSO : AuditoriaService.RES_FALLIDO,
                "cedula=" + cedulaLimpia
        );

        return resultado
                .map(c -> ResponseEntity.ok(Map.of(
                        "encontrado", true,
                        "cliente", Map.of(
                                "id", c.getId(),
                                "nombre", c.getNombre(),
                                "cedula", c.getCedula(),
                                "correo", c.getCorreo(),
                                "telefono", c.getTelefono() != null ? c.getTelefono() : "",
                                "direccion", c.getDireccion() != null ? c.getDireccion() : ""
                        )
                )))
                .orElse(ResponseEntity.ok(Map.of("encontrado", false, "mensaje", "Cliente no encontrado")));
    }

    /**
     * Crea un nuevo cliente.
     * SCRUM-64: Registra CREAR en módulo CLIENTES.
     * POST /api/clientes
     */
    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {

        String nombre = body.get("nombre");
        String cedula = body.get("cedula");
        String correo = body.get("correo");
        String telefono = body.getOrDefault("telefono", "");
        String direccion = body.getOrDefault("direccion", "");
        String[] ui = auditoriaService.obtenerUsuarioInfo();

        if (nombre == null || nombre.isBlank() || cedula == null || cedula.isBlank()
                || correo == null || correo.isBlank()) {
            auditoriaService.registrar(
                    AuditoriaService.ACC_CREAR, AuditoriaService.MOD_CLIENTES,
                    "Intento fallido de crear cliente: datos incompletos",
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_FALLIDO, "Faltan: nombre, cédula o correo"
            );
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Nombre, cédula y correo son requeridos"));
        }

        String cedulaLimpia = cedula.replaceAll("[^0-9]", "").trim();
        if (clienteRepository.findByCedula(cedulaLimpia).isPresent()) {
            auditoriaService.registrar(
                    AuditoriaService.ACC_CREAR, AuditoriaService.MOD_CLIENTES,
                    "Intento fallido de crear cliente: cédula duplicada " + cedulaLimpia,
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_FALLIDO, "cedula=" + cedulaLimpia + " ya existe"
            );
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Ya existe un cliente con esa cédula"));
        }
        if (clienteRepository.findByCorreo(correo).isPresent()) {
            auditoriaService.registrar(
                    AuditoriaService.ACC_CREAR, AuditoriaService.MOD_CLIENTES,
                    "Intento fallido de crear cliente: correo duplicado " + correo,
                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                    AuditoriaService.RES_FALLIDO, "correo=" + correo + " ya existe"
            );
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Ya existe un cliente con ese correo"));
        }

        Cliente cliente = new Cliente(nombre, cedulaLimpia, correo, telefono, direccion);
        cliente = clienteRepository.save(cliente);

        auditoriaService.registrar(
                AuditoriaService.ACC_CREAR, AuditoriaService.MOD_CLIENTES,
                "Cliente creado: " + cliente.getNombre() + " (cédula=" + cliente.getCedula() + ")",
                ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                AuditoriaService.RES_EXITOSO, "ID=" + cliente.getId()
        );

        return ResponseEntity.ok(Map.of(
                "id", cliente.getId(),
                "nombre", cliente.getNombre(),
                "cedula", cliente.getCedula(),
                "correo", cliente.getCorreo(),
                "telefono", cliente.getTelefono() != null ? cliente.getTelefono() : "",
                "direccion", cliente.getDireccion() != null ? cliente.getDireccion() : ""
        ));
    }

    /**
     * Actualiza un cliente.
     * SCRUM-64: Registra EDITAR en módulo CLIENTES.
     * PUT /api/clientes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {

        String[] ui = auditoriaService.obtenerUsuarioInfo();
        return clienteRepository.findById(id)
                .map(c -> {
                    String nombre = body.get("nombre");
                    String cedula = body.get("cedula");
                    String correo = body.get("correo");
                    if (nombre != null && !nombre.isBlank()) c.setNombre(nombre);
                    if (cedula != null && !cedula.isBlank()) {
                        String cedulaLimpia = cedula.replaceAll("[^0-9]", "").trim();
                        if (!cedulaLimpia.equals(c.getCedula())
                                && clienteRepository.findByCedula(cedulaLimpia).isPresent()) {
                            auditoriaService.registrar(
                                    AuditoriaService.ACC_EDITAR, AuditoriaService.MOD_CLIENTES,
                                    "Intento fallido de actualizar cliente ID=" + id + ": cédula duplicada",
                                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                                    AuditoriaService.RES_FALLIDO, "cedula=" + cedulaLimpia + " ya existe"
                            );
                            return ResponseEntity.badRequest().<Object>body(
                                    Map.of("mensaje", "Ya existe un cliente con esa cédula"));
                        }
                        c.setCedula(cedulaLimpia);
                    }
                    if (correo != null && !correo.isBlank()) {
                        if (!correo.equalsIgnoreCase(c.getCorreo())
                                && clienteRepository.findByCorreo(correo).isPresent()) {
                            auditoriaService.registrar(
                                    AuditoriaService.ACC_EDITAR, AuditoriaService.MOD_CLIENTES,
                                    "Intento fallido de actualizar cliente ID=" + id + ": correo duplicado",
                                    ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                                    AuditoriaService.RES_FALLIDO, "correo=" + correo + " ya existe"
                            );
                            return ResponseEntity.badRequest().<Object>body(
                                    Map.of("mensaje", "Ya existe un cliente con ese correo"));
                        }
                        c.setCorreo(correo);
                    }
                    if (body.containsKey("telefono"))
                        c.setTelefono(body.get("telefono") != null ? body.get("telefono") : "");
                    if (body.containsKey("direccion"))
                        c.setDireccion(body.get("direccion") != null ? body.get("direccion") : "");
                    clienteRepository.save(c);

                    auditoriaService.registrar(
                            AuditoriaService.ACC_EDITAR, AuditoriaService.MOD_CLIENTES,
                            "Cliente actualizado: " + c.getNombre() + " (cédula=" + c.getCedula() + ")",
                            ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                            AuditoriaService.RES_EXITOSO, "ID=" + id
                    );
                    return ResponseEntity.ok(Map.of(
                            "id", c.getId(),
                            "nombre", c.getNombre(),
                            "cedula", c.getCedula(),
                            "correo", c.getCorreo(),
                            "telefono", c.getTelefono() != null ? c.getTelefono() : "",
                            "direccion", c.getDireccion() != null ? c.getDireccion() : ""
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un cliente.
     * SCRUM-64: Registra ELIMINAR en módulo CLIENTES.
     * DELETE /api/clientes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        if (!clienteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        clienteRepository.deleteById(id);

        String[] ui = auditoriaService.obtenerUsuarioInfo();
        auditoriaService.registrar(
                AuditoriaService.ACC_ELIMINAR, AuditoriaService.MOD_CLIENTES,
                "Cliente eliminado del sistema",
                ui[0], ui[1], auditoriaService.obtenerIp(httpRequest),
                AuditoriaService.RES_EXITOSO, "ID=" + id
        );
        return ResponseEntity.ok(Map.of("mensaje", "Cliente eliminado"));
    }
}
