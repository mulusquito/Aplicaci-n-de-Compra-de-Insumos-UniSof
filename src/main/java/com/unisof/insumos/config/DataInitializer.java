package com.unisof.insumos.config;

import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

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
    }
}
