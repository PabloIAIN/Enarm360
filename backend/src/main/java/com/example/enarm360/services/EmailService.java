package com.example.enarm360.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.mail.from:no-reply@enarm360.com}")
    private String fromEmail;
    
    @Value("${app.name:ENARM360}")
    private String appName;
    
    @Value("${app.url:https://enarm360.com}")
    private String appUrl;
    
    /**
     * Enviar email de verificación
     */
    public boolean enviarEmailVerificacion(String email, String nombre, String codigo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            
            helper.setFrom(fromEmail, appName);
            helper.setTo(email);
            helper.setSubject("✅ Verifica tu cuenta en " + appName);
            
            String htmlContent = construirEmailVerificacion(nombre, codigo);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("Email de verificación enviado a: {}", email);
            return true;
            
        } catch (Exception e) {
            logger.error("Error al enviar email de verificación a {}: {}", email, e.getMessage());
            return false;
        }
    }
    
    /**
     * Enviar email de bienvenida
     */
    public boolean enviarEmailBienvenida(String email, String nombre) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            
            helper.setFrom(fromEmail, appName);
            helper.setTo(email);
            helper.setSubject("🎉 ¡Bienvenido a " + appName + "!");
            
            String htmlContent = construirEmailBienvenida(nombre);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("Email de bienvenida enviado a: {}", email);
            return true;
            
        } catch (Exception e) {
            logger.error("Error al enviar email de bienvenida a {}: {}", email, e.getMessage());
            return false;
        }
    }
    
    private String construirEmailVerificacion(String nombre, String codigo) {
        String verificationUrl = appUrl + "/verify-email?codigo=" + codigo;
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>Verifica tu cuenta</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 0; background-color: #f4f4f4; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .email-content { background: white; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                "        .header { text-align: center; margin-bottom: 30px; }" +
                "        .logo { font-size: 28px; font-weight: bold; color: #2563eb; margin-bottom: 10px; }" +
                "        .verification-code { background: #f8fafc; border: 2px dashed #cbd5e1; border-radius: 8px; padding: 20px; text-align: center; margin: 25px 0; }" +
                "        .code { font-size: 24px; font-weight: bold; color: #1e40af; letter-spacing: 3px; font-family: monospace; }" +
                "        .button { display: inline-block; background: #2563eb; color: white; padding: 12px 30px; text-decoration: none; border-radius: 8px; font-weight: bold; margin: 20px 0; }" +
                "        .button:hover { background: #1d4ed8; }" +
                "        .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='email-content'>" +
                "            <div class='header'>" +
                "                <div class='logo'>🩺 " + appName + "</div>" +
                "                <h1 style='color: #1f2937; margin: 0;'>Verifica tu cuenta</h1>" +
                "            </div>" +
                "            <p>Hola <strong>" + nombre + "</strong>,</p>" +
                "            <p>Gracias por unirte a " + appName + ". Para completar tu registro, necesitamos verificar tu dirección de email.</p>" +
                "            <div class='verification-code'>" +
                "                <p style='margin: 0 0 10px 0; font-weight: bold;'>Tu código de verificación es:</p>" +
                "                <div class='code'>" + codigo + "</div>" +
                "                <p style='margin: 10px 0 0 0; font-size: 12px; color: #6b7280;'>Válido por 24 horas</p>" +
                "            </div>" +
                "            <p>También puedes hacer clic en el siguiente enlace para verificar automáticamente:</p>" +
                "            <div style='text-align: center;'>" +
                "                <a href='" + verificationUrl + "' class='button'>Verificar mi cuenta</a>" +
                "            </div>" +
                "            <p style='font-size: 14px; color: #6b7280;'>" +
                "                Si no creaste esta cuenta, puedes ignorar este email de forma segura." +
                "            </p>" +
                "            <div class='footer'>" +
                "                <p>" + appName + " - Tu plataforma de preparación para el ENARM</p>" +
                "                <p>© " + LocalDateTime.now().getYear() + " " + appName + ". Todos los derechos reservados.</p>" +
                "            </div>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
    
    private String construirEmailBienvenida(String nombre) {
        String dashboardUrl = appUrl + "/estudiante/dashboard";
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>¡Bienvenido!</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 0; background-color: #f4f4f4; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .email-content { background: white; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                "        .header { text-align: center; margin-bottom: 30px; }" +
                "        .logo { font-size: 28px; font-weight: bold; color: #059669; margin-bottom: 10px; }" +
                "        .welcome-banner { background: linear-gradient(135deg, #10b981, #059669); color: white; padding: 25px; border-radius: 8px; text-align: center; margin: 20px 0; }" +
                "        .button { display: inline-block; background: #059669; color: white; padding: 12px 30px; text-decoration: none; border-radius: 8px; font-weight: bold; margin: 20px 0; }" +
                "        .features { margin: 25px 0; }" +
                "        .feature { margin: 15px 0; padding: 10px 0; }" +
                "        .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='email-content'>" +
                "            <div class='header'>" +
                "                <div class='logo'>🩺 " + appName + "</div>" +
                "            </div>" +
                "            <div class='welcome-banner'>" +
                "                <h1 style='margin: 0 0 10px 0;'>¡Bienvenido, " + nombre + "! 🎉</h1>" +
                "                <p style='margin: 0; opacity: 0.9;'>Tu cuenta ha sido verificada exitosamente</p>" +
                "            </div>" +
                "            <p>Estamos emocionados de tenerte en " + appName + ", la plataforma más completa para tu preparación al ENARM.</p>" +
                "            <div class='features'>" +
                "                <div class='feature'>✅ <strong>Miles de preguntas actualizadas</strong> - Banco completo tipo ENARM</div>" +
                "                <div class='feature'>📊 <strong>Estadísticas detalladas</strong> - Seguimiento de tu progreso</div>" +
                "                <div class='feature'>🎯 <strong>Simulacros personalizados</strong> - Práctica por especialidades</div>" +
                "                <div class='feature'>👥 <strong>Comunidad activa</strong> - Conecta con otros estudiantes</div>" +
                "            </div>" +
                "            <div style='text-align: center;'>" +
                "                <a href='" + dashboardUrl + "' class='button'>Comenzar mi preparación</a>" +
                "            </div>" +
                "            <p style='font-size: 14px; color: #6b7280;'>" +
                "                ¿Necesitas ayuda? Responde a este email o visita nuestro centro de ayuda." +
                "            </p>" +
                "            <div class='footer'>" +
                "                <p>¡Que tengas mucho éxito en tu preparación! 💪</p>" +
                "                <p>© " + LocalDateTime.now().getYear() + " " + appName + ". Todos los derechos reservados.</p>" +
                "            </div>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}