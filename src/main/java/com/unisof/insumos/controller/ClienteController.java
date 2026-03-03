package com.unisof.insumos.controller;

import com.unisof.insumos.model.Cliente;
import com.unisof.insumos.repository.ClienteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API CRUD para clientes.
 * SCRUM-16: Buscar cliente por cédula para asociar pedidos.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteRepository clienteRepository;

    public ClienteController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
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
     * GET /api/clientes/buscar?cedula=123456789
     */
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorCedula(@RequestParam String cedula) {
        String cedulaLimpia = cedula.replaceAll("[^0-9]", "").trim();
        if (cedulaLimpia.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Ingresa un número de cédula válido"));
        }
        return clienteRepository.findByCedula(cedulaLimpia)
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
     * Crea un nuevo cliente (para cuando no se encuentra en la búsqueda).
     * POST /api/clientes
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, String> body) {
        String nombre = body.get("nombre");
        String cedula = body.get("cedula");
        String correo = body.get("correo");
        String telefono = body.getOrDefault("telefono", "");
        String direccion = body.getOrDefault("direccion", "");
        if (nombre == null || nombre.isBlank() || cedula == null || cedula.isBlank() || correo == null || correo.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Nombre, cédula y correo son requeridos"));
        }
        String cedulaLimpia = cedula.replaceAll("[^0-9]", "").trim();
        if (clienteRepository.findByCedula(cedulaLimpia).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Ya existe un cliente con esa cédula"));
        }
        Cliente cliente = new Cliente(nombre, cedulaLimpia, correo, telefono, direccion);
        cliente = clienteRepository.save(cliente);
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
     * PUT /api/clientes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return clienteRepository.findById(id)
                .map(c -> {
                    String nombre = body.get("nombre");
                    String cedula = body.get("cedula");
                    String correo = body.get("correo");
                    if (nombre != null && !nombre.isBlank()) c.setNombre(nombre);
                    if (cedula != null && !cedula.isBlank()) {
                        String cedulaLimpia = cedula.replaceAll("[^0-9]", "").trim();
                        if (!cedulaLimpia.equals(c.getCedula()) && clienteRepository.findByCedula(cedulaLimpia).isPresent()) {
                            return ResponseEntity.badRequest().<Object>body(Map.of("mensaje", "Ya existe un cliente con esa cédula"));
                        }
                        c.setCedula(cedulaLimpia);
                    }
                    if (correo != null && !correo.isBlank()) c.setCorreo(correo);
                    if (body.containsKey("telefono")) c.setTelefono(body.get("telefono") != null ? body.get("telefono") : "");
                    if (body.containsKey("direccion")) c.setDireccion(body.get("direccion") != null ? body.get("direccion") : "");
                    clienteRepository.save(c);
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
     * DELETE /api/clientes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!clienteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        clienteRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Cliente eliminado"));
    }
}
