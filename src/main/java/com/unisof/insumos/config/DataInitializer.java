package com.unisof.insumos.config;

import com.unisof.insumos.model.Cliente;
import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.repository.ClienteRepository;
import com.unisof.insumos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos al arrancar la aplicacion.
 * <p>
 * Si la base de datos esta vacia, crea un usuario administrador (admin/admin123).
 * Siempre crea el vendedor de prueba (vendedor/vendedor123) si no existe.
 * </p>
 * <p>
 * Para restablecer contraseñas (evitar advertencias de Chrome): activa app.security.reset-passwords=true
 * y define app.admin.password y app.vendedor.password en application.properties.
 * </p>
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${app.admin.email:admin@unisof.com}")
    private String adminEmail;

    @Value("${app.security.reset-passwords:false}")
    private boolean resetPasswords;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.vendedor.password:vendedor123}")
    private String vendedorPassword;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario(
                    "admin",
                    passwordEncoder.encode(adminPassword),
                    "Administrador",
                    adminEmail
            );
            admin.setRol("ADMINISTRADOR");
            usuarioRepository.save(admin);
        }
        if (usuarioRepository.findByUsuario("vendedor").isEmpty()) {
            Usuario vendedor = new Usuario(
                    "vendedor",
                    passwordEncoder.encode(vendedorPassword),
                    "Alfonso",
                    "alfonsoocampo08@gmail.com"
            );
            vendedor.setRol("VENDEDOR");
            usuarioRepository.save(vendedor);
        }
        if (resetPasswords) {
            usuarioRepository.findByUsuario("admin").ifPresent(u -> {
                u.setContrasena(passwordEncoder.encode(adminPassword));
                usuarioRepository.save(u);
            });
            usuarioRepository.findByUsuario("vendedor").ifPresent(u -> {
                u.setContrasena(passwordEncoder.encode(vendedorPassword));
                usuarioRepository.save(u);
            });
        }
        initClientes();
    }

    private void initClientes() {
        if (clienteRepository.count() == 0) {
            clienteRepository.save(new Cliente("Juan Perez Garcia", "123456789", "juan.perez@ejemplo.com", "+57 300 123 4567", "Calle 10 #5-20, Bogota"));
            clienteRepository.save(new Cliente("Maria Lopez Sanchez", "987654321", "maria.lopez@ejemplo.com", "+57 310 987 6543", "Carrera 15 #20-30, Medellin"));
            clienteRepository.save(new Cliente("Carlos Rodriguez", "456789123", "carlos.rodriguez@ejemplo.com", "+57 320 555 1234", "Av 68 #45-10, Bogota"));
            clienteRepository.save(new Cliente("Ana Martinez", "789123456", "ana.martinez@ejemplo.com", "+57 315 444 5678", "Calle 50 #30-15, Cali"));
            clienteRepository.save(new Cliente("Pedro Sanchez", "321654987", "pedro.sanchez@ejemplo.com", "+57 318 777 9012", "Carrera 43 #80-25, Medellin"));
        }
    }
}
