package com.unisof.insumos.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * SCRUM-35: Servicio de envío de correos electrónicos.
 * <p>
 * Envía el token de verificación 2FA al correo del usuario.
 * Si el correo está deshabilitado o no hay JavaMailSender configurado,
 * registra el token en log para desarrollo.
 * </p>
 *
 * @see TokenVerificacionService
 */
@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@unisof.com}")
    private String remitente;

    @Value("${app.mail.enabled:true}")
    private boolean emailHabilitado;

    /**
     * Envía el token de verificación 2FA por correo al usuario.
     *
     * @param correoDestino  dirección de correo del destinatario
     * @param nombreUsuario  nombre del usuario (para personalizar el mensaje)
     * @param token         código de 6 dígitos a enviar
     * @return true si se envió correctamente o si el correo está deshabilitado (modo dev)
     */
    public boolean enviarTokenVerificacion(String correoDestino, String nombreUsuario, String token) {
        if (!emailHabilitado || mailSender == null) {
            log.info("SCRUM-35 - Token de verificacion para {} ({}): {}", nombreUsuario, correoDestino, token);
            return true;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(correoDestino);
            mensaje.setSubject("Token de verificación - Aplicación de Insumos Unisof");
            mensaje.setText(String.format(
                    "Hola %s,\n\n" +
                    "Tu código de verificación es: %s\n\n" +
                    "Este token expira en 10 minutos. No compartas este código con nadie.\n\n" +
                    "Si no solicitaste este código, ignora este mensaje.\n\n" +
                    "Saludos,\nSistema de Insumos Unisof",
                    nombreUsuario, token
            ));
            mailSender.send(mensaje);
            log.info("Token enviado a {}", correoDestino);
            return true;
        } catch (Exception e) {
            log.error("Error enviando token a {}: {}", correoDestino, e.getMessage());
            return false;
        }
    }
}
