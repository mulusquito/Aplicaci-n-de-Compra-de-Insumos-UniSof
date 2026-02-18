package com.unisof.insumos.config;

import com.unisof.insumos.model.Usuario;
import com.unisof.insumos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "Administrador",
                    "admin@unisof.com"
            );
            admin.setRol("ADMINISTRADOR");
            usuarioRepository.save(admin);
        }
    }
}
