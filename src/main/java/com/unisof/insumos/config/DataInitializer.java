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
 * Si la base de datos esta vacia, crea un usuario administrador (admin/admin123)
 * con el correo configurado en app.admin.email.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@unisof.com}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "Administrador",
                    adminEmail
            );
            admin.setRol("ADMINISTRADOR");
            usuarioRepository.save(admin);
        }
    }
}
