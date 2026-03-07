package com.unisof.insumos.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unisof.insumos.model.Cliente;
import com.unisof.insumos.model.Recibo;
import com.unisof.insumos.repository.ClienteRepository;
import com.unisof.insumos.repository.ReciboRepository;
import com.unisof.insumos.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * API para crear recibos. SCRUM-16: Recibo con número incremental, persistido en BD.
 */
@RestController
@RequestMapping("/api/recibos")
public class ReciboController {

    private final ReciboRepository reciboRepository;
    private final ClienteRepository clienteRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.copias.emails:}")
    private String copiasEmails;

    public ReciboController(ReciboRepository reciboRepository, ClienteRepository clienteRepository, EmailService emailService) {
        this.reciboRepository = reciboRepository;
        this.clienteRepository = clienteRepository;
        this.emailService = emailService;
    }

    /**
     * Busca órdenes.
     * GET /api/recibos?numero=001 | ?cedula=... | ?fecha=... | ?mes=2026-02 | ?fechaDesde=...&fechaHasta=...
     */
    @GetMapping
    public ResponseEntity<?> buscar(
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) String cedula,
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String mes,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta) {
        ZoneId zone = ZoneId.systemDefault();
        List<Recibo> recibos = new ArrayList<>();
        if (numero != null && !numero.isBlank()) {
            try {
                int num = Integer.parseInt(numero.replaceAll("[^0-9]", ""), 10);
                reciboRepository.findByNumero(num).ifPresent(recibos::add);
            } catch (NumberFormatException e) {
                // número inválido, lista vacía
            }
        } else if (cedula != null && !cedula.isBlank()) {
            recibos = reciboRepository.findByClienteCedulaOrderByFechaDesc(cedula.trim());
        } else if (fecha != null && !fecha.isBlank()) {
            LocalDate d = LocalDate.parse(fecha);
            Instant inicio = d.atStartOfDay(zone).toInstant();
            Instant fin = d.plusDays(1).atStartOfDay(zone).toInstant();
            recibos = reciboRepository.findByFechaBetween(inicio, fin);
        } else if (mes != null && !mes.isBlank()) {
            LocalDate inicioMes = LocalDate.parse(mes + "-01");
            LocalDate finMes = inicioMes.plusMonths(1);
            Instant inicio = inicioMes.atStartOfDay(zone).toInstant();
            Instant fin = finMes.atStartOfDay(zone).toInstant();
            recibos = reciboRepository.findByFechaBetween(inicio, fin);
        } else if (fechaDesde != null && !fechaDesde.isBlank() && fechaHasta != null && !fechaHasta.isBlank()) {
            LocalDate dInicio = LocalDate.parse(fechaDesde);
            LocalDate dFin = LocalDate.parse(fechaHasta);
            Instant inicio = dInicio.atStartOfDay(zone).toInstant();
            Instant fin = dFin.plusDays(1).atStartOfDay(zone).toInstant();
            recibos = reciboRepository.findByFechaBetween(inicio, fin);
        } else {
            recibos = reciboRepository.findAll().stream()
                    .sorted((a, b) -> b.getFecha().compareTo(a.getFecha()))
                    .toList();
        }
        List<Map<String, Object>> resultado = recibos.stream().map(this::toMap).toList();
        return ResponseEntity.ok(resultado);
    }

    /**
     * Busca una orden por ID. GET /api/recibos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return reciboRepository.findById(id)
                .map(r -> ResponseEntity.ok(toMap(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> toMap(Recibo r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("numero", r.getNumero());
        m.put("numeroFormateado", String.format("%03d", r.getNumero()));
        m.put("clienteId", r.getCliente().getId());
        m.put("clienteNombre", r.getCliente().getNombre());
        m.put("clienteCedula", r.getCliente().getCedula());
        m.put("clienteCorreo", r.getCliente().getCorreo());
        m.put("clienteTelefono", r.getCliente().getTelefono() != null ? r.getCliente().getTelefono() : "");
        m.put("clienteDireccion", r.getCliente().getDireccion() != null ? r.getCliente().getDireccion() : "");
        m.put("fecha", r.getFecha().toString());
        m.put("total", r.getTotal());
        m.put("itemsJson", r.getItemsJson());
        m.put("estado", r.getEstado());
        return m;
    }

    /**
     * Actualiza una orden (estado, cliente). PUT /api/recibos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return reciboRepository.findById(id)
                .map(r -> {
                    if (body.containsKey("estado")) {
                        r.setEstado(body.get("estado").toString());
                    }
                    if (body.containsKey("clienteId")) {
                        Long clienteId = body.get("clienteId") instanceof Number
                                ? ((Number) body.get("clienteId")).longValue()
                                : Long.parseLong(body.get("clienteId").toString());
                        clienteRepository.findById(clienteId).ifPresent(r::setCliente);
                    }
                    reciboRepository.save(r);
                    return ResponseEntity.ok(toMap(r));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina una orden. DELETE /api/recibos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!reciboRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        reciboRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Orden eliminada"));
    }

    /**
     * Crea un recibo. Si el cliente no existe (por id), lo crea.
     * POST /api/recibos
     * Body: { clienteId?: number, cliente?: {nombre,cedula,correo,telefono,direccion}, items: [...], total: number }
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            Cliente cliente = obtenerOCrearCliente(body);
            if (cliente == null) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "Datos del cliente son requeridos"));
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            if (items == null || items.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "Debe incluir al menos un producto"));
            }
            final Object totalObj = body.get("total");
            final BigDecimal total = (totalObj instanceof Number)
                    ? BigDecimal.valueOf(((Number) totalObj).doubleValue())
                    : new BigDecimal(totalObj.toString());

            final String itemsAsJson = objectMapper.writeValueAsString(items);
            final int nextNumero = (reciboRepository.findMaxNumero() == null ? 0 : reciboRepository.findMaxNumero()) + 1;
            final String estado = body.containsKey("estado") ? body.get("estado").toString() : "PENDIENTE";
            final Recibo recibo = reciboRepository.save(new Recibo(nextNumero, cliente, total, itemsAsJson, estado));
            final String numeroReciboStr = String.format("%03d", recibo.getNumero());

            boolean enviarPorCorreo = Boolean.TRUE.equals(body.get("enviarPorCorreo"))
                    || Boolean.parseBoolean(String.valueOf(body.get("enviarPorCorreo")));
            boolean enviarACopias = Boolean.TRUE.equals(body.get("enviarACopias"))
                    || Boolean.parseBoolean(String.valueOf(body.get("enviarACopias")));

            if (enviarPorCorreo || enviarACopias) {
                ResponseEntity<?> resp = enviarRecibosPorCorreo(cliente, recibo, items, numeroReciboStr, estado, total, enviarPorCorreo, enviarACopias);
                if (resp != null) {
                    return resp;
                }
            }

            return ResponseEntity.ok(Map.of(
                    "id", recibo.getId(),
                    "numero", recibo.getNumero(),
                    "numeroFormateado", numeroReciboStr,
                    "cliente", recibo.getCliente().getNombre(),
                    "total", recibo.getTotal(),
                    "fecha", recibo.getFecha().toString()
            ));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Error en formato de items"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("mensaje", e.getMessage()));
        }
    }

    private ResponseEntity<?> enviarRecibosPorCorreo(Cliente cliente, Recibo recibo, List<Map<String, Object>> items,
            String numeroReciboStr, String estado, BigDecimal total, boolean enviarPorCorreo, boolean enviarACopias) {
        String itemsHtml = buildItemsHtml(items);
        String totalStr = String.format("%,d", total.intValue()).replace(",", ".");
        ZoneId zone = ZoneId.systemDefault();
        DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("d/M/yyyy, h:mm:ss a", new Locale("es", "CO"));
        DateTimeFormatter fmtEntrega = DateTimeFormatter.ofPattern("d/M/yyyy", new Locale("es", "CO"));
        ZonedDateTime fechaZ = recibo.getFecha().atZone(zone);
        String fechaStr = fmtFecha.format(fechaZ);
        String fechaEntregaStr = fmtEntrega.format(fechaZ.plusDays(15));

        if (enviarPorCorreo && cliente.getCorreo() != null && !cliente.getCorreo().isBlank()) {
            boolean enviado = emailService.enviarRecibo(
                    cliente.getCorreo(),
                    cliente.getNombre(),
                    cliente.getCedula(),
                    cliente.getCorreo(),
                    cliente.getTelefono() != null ? cliente.getTelefono() : "",
                    cliente.getDireccion() != null ? cliente.getDireccion() : "",
                    numeroReciboStr,
                    itemsHtml,
                    totalStr,
                    estado,
                    fechaStr,
                    fechaEntregaStr);
            if (!enviado) {
                return ResponseEntity.status(500).body(Map.of("mensaje", "Recibo creado pero no se pudo enviar el correo al cliente"));
            }
        }
        if (enviarACopias && copiasEmails != null && !copiasEmails.isBlank()) {
            try {
                Thread.sleep(600); // Pausa tras envío al cliente: Resend permite máx 2 req/seg
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            java.util.Set<String> copias = new java.util.LinkedHashSet<>();
            for (String e : copiasEmails.split("[,;]")) {
                String t = e.trim();
                if (!t.isEmpty()) {
                    copias.add(t);
                }
            }
            for (String email : copias) {
                try {
                    Thread.sleep(600); // Resend: máx 2 req/seg; pausa para evitar 429 Too Many Requests
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                emailService.enviarRecibo(
                        email,
                        cliente.getNombre(),
                        cliente.getCedula(),
                        cliente.getCorreo(),
                        cliente.getTelefono() != null ? cliente.getTelefono() : "",
                        cliente.getDireccion() != null ? cliente.getDireccion() : "",
                        numeroReciboStr,
                        itemsHtml,
                        totalStr,
                        estado,
                        fechaStr,
                        fechaEntregaStr);
            }
        }
        return null;
    }

    private Cliente obtenerOCrearCliente(Map<String, Object> body) {
        Object clienteIdObj = body.get("clienteId");
        if (clienteIdObj != null) {
            Long id = clienteIdObj instanceof Number ? ((Number) clienteIdObj).longValue() : Long.parseLong(clienteIdObj.toString());
            return clienteRepository.findById(id).orElse(null);
        }
        @SuppressWarnings("unchecked")
        Map<String, String> clienteData = (Map<String, String>) body.get("cliente");
        if (clienteData == null) return null;

        String nombre = clienteData.get("nombre");
        String cedula = clienteData.get("cedula");
        String correo = clienteData.get("correo");
        String telefono = clienteData.getOrDefault("telefono", "");
        String direccion = clienteData.getOrDefault("direccion", "");
        if (nombre == null || nombre.isBlank() || cedula == null || cedula.isBlank() || correo == null || correo.isBlank()) {
            return null;
        }
        String cedulaLimpia = cedula.replaceAll("[^0-9]", "").trim();
        return clienteRepository.findByCedula(cedulaLimpia)
                .orElseGet(() -> clienteRepository.save(new Cliente(nombre, cedulaLimpia, correo, telefono, direccion)));
    }

    private static String buildItemsHtml(List<Map<String, Object>> items) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> i : items) {
            String nombre = String.valueOf(i.getOrDefault("nombre", ""));
            String talla = String.valueOf(i.getOrDefault("talla", ""));
            int cantidad = ((Number) i.getOrDefault("cantidad", 0)).intValue();
            double precioUnit = ((Number) i.getOrDefault("precioUnit", 0)).doubleValue();
            double subtotal = ((Number) i.getOrDefault("subtotal", 0)).doubleValue();
            String precioUnitStr = String.format("%,d", (int) precioUnit).replace(",", ".");
            String subtotalStr = String.format("%,d", (int) subtotal).replace(",", ".");
            String tallaStr = (talla != null && !talla.isEmpty()) ? talla : "—";
            sb.append("<tr><td style=\"padding:6px 0;\">").append(nombre)
                    .append("</td><td>").append(tallaStr).append("</td><td>").append(cantidad).append("</td><td>")
                    .append(precioUnitStr).append("</td><td>").append(subtotalStr).append("</td></tr>");
        }
        return sb.toString();
    }
}
