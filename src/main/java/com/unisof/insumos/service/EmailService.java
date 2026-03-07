package com.unisof.insumos.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * SCRUM-35: Servicio de envío de correos electrónicos.
 * <p>
 * Usa Resend API (cuando RESEND_API_KEY está configurado) para entornos cloud donde SMTP está bloqueado.
 * Fallback a JavaMailSender (Gmail SMTP) para desarrollo local.
 * </p>
 *
 * @see TokenVerificacionService
 */
@Service
@Slf4j
public class EmailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.mail.username:no-reply@unisof.com}")
    private String remitente;

    @Value("${app.mail.enabled:true}")
    private boolean emailHabilitado;

    @Value("${app.mail.fallback-log-on-error:false}")
    private boolean fallbackLogOnError;

    @Value("${RESEND_API_KEY:}")
    private String resendApiKey;

    @Value("${app.resend.from:UNISOF <onboarding@resend.dev>}")
    private String resendFrom;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    /**
     * Envía el token de verificación 2FA por correo con diseño HTML y logo UNISOF.
     * Prioridad: Resend (si API key configurada) > JavaMailSender > log fallback.
     */
    public boolean enviarTokenVerificacion(String correoDestino, String nombreUsuario, String token) {
        if (!emailHabilitado) {
            log.info("SCRUM-35 - Token de verificacion para {} ({}): {}", nombreUsuario, correoDestino, token);
            return true;
        }

        // 1. Intentar Resend (funciona en Render y otros cloud)
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            if (enviarViaResend(correoDestino, "Tu código de verificación - UNISOF", buildHtmlTokenEmail(nombreUsuario, token))) {
                log.info("Token enviado a {} (Resend)", correoDestino);
                return true;
            }
        }

        // 2. Intentar JavaMailSender (SMTP - funciona en local)
        if (mailSender != null) {
            try {
                MimeMessage mensaje = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
                helper.setFrom(remitente);
                helper.setTo(correoDestino);
                helper.setSubject("Tu código de verificación - UNISOF");
                helper.setText(buildHtmlTokenEmail(nombreUsuario, token), true);
                mailSender.send(mensaje);
                log.info("Token enviado a {} (SMTP)", correoDestino);
                return true;
            } catch (MessagingException | MailException e) {
                log.error("Error enviando token a {} (SMTP): {}", correoDestino, e.getMessage());
            }
        }

        // 3. Fallback: mostrar token en logs
        if (fallbackLogOnError) {
            log.info(">>> TOKEN PARA PRUEBAS (red bloquea correo): {}", token);
            return true;
        }
        log.warn("No se pudo enviar el token por correo a {}", correoDestino);
        return false;
    }

    /**
     * Envía el enlace para restablecer contraseña por correo.
     * El enlace expira en 1 hora.
     */
    public boolean enviarLinkRecuperacionContrasena(String correoDestino, String nombreUsuario, String token) {
        if (!emailHabilitado) {
            log.info("Link recuperación para {} ({}): {}", nombreUsuario, correoDestino, token);
            return true;
        }
        String link = String.format("%s/restablecer-contrasena.html?token=%s", appBaseUrl.replaceAll("/$", ""), token);
        String html = buildHtmlRecuperacionContrasenaEmail(nombreUsuario, link);

        if (resendApiKey != null && !resendApiKey.isBlank()) {
            if (enviarViaResend(correoDestino, "Restablece tu contraseña - UNISOF", html)) {
                log.info("Link recuperación enviado a {} (Resend)", correoDestino);
                return true;
            }
        }
        if (mailSender != null) {
            try {
                MimeMessage mensaje = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
                helper.setFrom(remitente);
                helper.setTo(correoDestino);
                helper.setSubject("Restablece tu contraseña - UNISOF");
                helper.setText(html, true);
                mailSender.send(mensaje);
                log.info("Link recuperación enviado a {} (SMTP)", correoDestino);
                return true;
            } catch (MessagingException | MailException e) {
                log.error("Error enviando link recuperación a {} (SMTP): {}", correoDestino, e.getMessage());
            }
        }
        if (fallbackLogOnError) {
            log.info(">>> LINK RECUPERACIÓN PARA PRUEBAS: {}", link);
            return true;
        }
        return false;
    }

    private String buildHtmlRecuperacionContrasenaEmail(String nombreUsuario, String link) {
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
            <p style="font-size:15px;color:#555;margin:0 0 24px;">Solicitaste restablecer tu contraseña. Haz clic en el botón para crear una nueva:</p>
            <p style="text-align:center;margin:0 0 24px;">
            <a href="%s" style="display:inline-block;background:#f5a623;color:#1a1a1a;padding:14px 28px;text-decoration:none;font-weight:bold;border-radius:8px;">Restablecer contraseña</a>
            </p>
            <p style="font-size:13px;color:#777;margin:0 0 8px;">⏱ Este enlace expira en 1 hora.</p>
            <p style="font-size:13px;color:#777;margin:0;">Si no solicitaste esto, ignora este mensaje. Tu contraseña no cambiará.</p>
            </div>
            <div style="background:#f8f8f8;padding:16px;text-align:center;font-size:12px;color:#888;">
            Sistema de Insumos UNISOF
            </div>
            </div>
            </body>
            </html>
            """.formatted(nombreUsuario, link);
    }

    /**
     * Envía correo usando la API REST de Resend (HTTP, no bloqueado en cloud).
     */
    private boolean enviarViaResend(String correoDestino, String subject, String html) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey.trim());

            Map<String, Object> body = Map.of(
                    "from", resendFrom,
                    "to", new String[]{correoDestino},
                    "subject", subject,
                    "html", html
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(RESEND_API_URL, request, Map.class);
            return true;
        } catch (Exception e) {
            log.error("Error enviando correo via Resend a {}: {}", correoDestino, e.getMessage());
            return false;
        }
    }

    /**
     * Envía el recibo por correo al cliente.
     * @param nombreVendedor nombre completo del vendedor que realizó la venta (puede ser null)
     */
    public boolean enviarRecibo(String correoDestino, String nombreCliente, String cedula, String correoCliente,
                               String telefono, String direccion, String numeroRecibo, String itemsHtml,
                               String totalFormateado, String estado, String fechaFormateada, String fechaEntregaFormateada,
                               String nombreVendedor) {
        if (!emailHabilitado) {
            log.info("Recibo {} para {} ({}): total {} - {}", numeroRecibo, nombreCliente, correoDestino, totalFormateado, estado);
            return true;
        }

        String html = buildHtmlReciboEmail(nombreCliente, cedula, correoCliente, telefono, direccion,
                numeroRecibo, itemsHtml, totalFormateado, estado, fechaFormateada, fechaEntregaFormateada, nombreVendedor);
        String subject = "Recibo Nº " + numeroRecibo + " - UNISOF";

        // 1. Intentar Resend
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            if (enviarViaResend(correoDestino, subject, html)) {
                log.info("Recibo enviado a {} (Resend)", correoDestino);
                return true;
            }
        }

        // 2. Intentar JavaMailSender
        if (mailSender != null) {
            try {
                MimeMessage mensaje = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
                helper.setFrom(remitente);
                helper.setTo(correoDestino);
                helper.setSubject(subject);
                helper.setText(html, true);
                mailSender.send(mensaje);
                log.info("Recibo enviado a {} (SMTP)", correoDestino);
                return true;
            } catch (MessagingException | MailException e) {
                log.error("Error enviando recibo a {} (SMTP): {}", correoDestino, e.getMessage());
            }
        }

        if (fallbackLogOnError) {
            log.info(">>> RECIBO {} PARA PRUEBAS: total {}", numeroRecibo, totalFormateado);
            return true;
        }
        return false;
    }

    private String buildHtmlReciboEmail(String nombreCliente, String cedula, String correoCliente,
                                        String telefono, String direccion, String numeroRecibo,
                                        String itemsHtml, String totalFormateado, String estado,
                                        String fechaFormateada, String fechaEntregaFormateada, String nombreVendedor) {
        String clienteHtml = """
            <p style="margin:4px 0;font-size:13px;">Nombre: %s</p>
            <p style="margin:4px 0;font-size:13px;">Cédula: %s</p>
            <p style="margin:4px 0;font-size:13px;">Correo: %s</p>
            <p style="margin:4px 0;font-size:13px;">Teléfono: %s</p>
            <p style="margin:4px 0;font-size:13px;">Dirección: %s</p>
            """.formatted(nombreCliente != null ? nombreCliente : "—",
                    cedula != null && !cedula.isBlank() ? cedula : "—",
                    correoCliente != null && !correoCliente.isBlank() ? correoCliente : "—",
                    telefono != null && !telefono.isBlank() ? telefono : "—",
                    direccion != null && !direccion.isBlank() ? direccion : "—");
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
            <div style="padding:24px;">
            <p style="text-align:center;color:#666;font-size:12px;margin:0 0 8px;">Orden de compra</p>
            <p style="text-align:center;font-size:18px;font-weight:bold;margin:0 0 20px;">Nº %s</p>
            <p style="font-size:12px;color:#555;margin:0 0 4px;"><strong>Fecha:</strong> %s</p>
            <p style="font-size:12px;color:#555;margin:0 0 16px;"><strong>Fecha de entrega:</strong> %s</p>
            <h3 style="font-size:14px;margin:0 0 8px;">Cliente</h3>
            %s
            <h3 style="font-size:14px;margin:16px 0 8px;">Productos</h3>
            <table style="width:100%%;border-collapse:collapse;font-size:12px;">
            <thead><tr style="border-bottom:1px solid #ddd;"><th style="text-align:left;padding:6px 0;">Prendas de vestir</th><th>Talla</th><th>Cant.</th><th>P.Unit</th><th>Subtotal</th></tr></thead>
            <tbody>%s</tbody>
            </table>
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-top:16px;flex-wrap:wrap;gap:12px;">
            <p style="margin:0;font-size:13px;color:#555;">%s</p>
            <p style="text-align:right;font-size:16px;font-weight:bold;margin:0;color:#f5a623;">Total: %s COP</p>
            </div>
            </div>
            <div style="background:#f8f8f8;padding:16px;text-align:center;font-size:12px;color:#888;">
            Sistema de Insumos UNISOF
            </div>
            </div>
            </body>
            </html>
            """.formatted(numeroRecibo, fechaFormateada, fechaEntregaFormateada, clienteHtml, itemsHtml,
                    nombreVendedor != null && !nombreVendedor.isBlank() ? "Vendedor: " + nombreVendedor : "Vendedor: —",
                    totalFormateado);
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
