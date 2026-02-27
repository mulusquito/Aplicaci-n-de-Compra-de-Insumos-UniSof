package com.unisof.insumos.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * SCRUM-35: Servicio de envío de correos electrónicos.
 * <p>
 * Envía el token de verificación 2FA al correo del usuario con diseño HTML y logo UNISOF.
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

    @Value("${app.mail.fallback-log-on-error:false}")
    private boolean fallbackLogOnError;

    /**
     * Envía el token de verificación 2FA por correo con diseño HTML y logo UNISOF.
     */
    public boolean enviarTokenVerificacion(String correoDestino, String nombreUsuario, String token) {
        if (!emailHabilitado || mailSender == null) {
            log.info("SCRUM-35 - Token de verificacion para {} ({}): {}", nombreUsuario, correoDestino, token);
            return true;
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(correoDestino);
            helper.setSubject("Tu código de verificación - UNISOF");

            String html = buildHtmlTokenEmail(nombreUsuario, token);
            helper.setText(html, true);

            mailSender.send(mensaje);
            log.info("Token enviado a {}", correoDestino);
            return true;
        } catch (MessagingException | MailException e) {
            log.error("Error enviando token a {}: {}", correoDestino, e.getMessage());
            if (fallbackLogOnError) {
                log.info(">>> TOKEN PARA PRUEBAS (red bloquea correo): {}", token);
                return true;
            }
            return false;
        }
    }

    private String buildHtmlTokenEmail(String nombreUsuario, String token) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;">
            <div style="max-width:480px;margin:0 auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.1);">
            <div style="background:#f5a623;color:#1a1a1a;padding:24px;text-align:center;">
            <span style="font-size:28px;font-weight:bold;display:inline-block;width:48px;height:48px;line-height:48px;background:#1a1a1a;color:#f5a623;border-radius:8px;margin:0 8px 0 0;">U</span>
            <span style="font-size:24px;font-weight:bold;letter-spacing:2px;">UNISOF</span>
            </div>
            <div style="padding:32px;">
            <p style="font-size:16px;color:#333;margin:0 0 16px;">Hola <strong>%s</strong>,</p>
            <p style="font-size:15px;color:#555;margin:0 0 24px;">Tu código de verificación para iniciar sesión es:</p>
            <div style="background:#f8f8f8;border:2px dashed #f5a623;border-radius:8px;padding:20px;text-align:center;margin:0 0 24px;">
            <span style="font-size:28px;font-weight:bold;letter-spacing:8px;color:#1a1a1a;">%s</span>
            </div>
            <p style="font-size:13px;color:#777;margin:0 0 8px;">⏱ Este código expira en 10 minutos.</p>
            <p style="font-size:13px;color:#777;margin:0;">No compartas este código con nadie. Si no solicitaste este código, ignora este mensaje.</p>
            </div>
            <div style="background:#f8f8f8;padding:16px;text-align:center;font-size:12px;color:#888;">
            Sistema de Insumos UNISOF
            </div>
            </div>
            </body>
            </html>
            """.formatted(nombreUsuario, token);
    }
}
