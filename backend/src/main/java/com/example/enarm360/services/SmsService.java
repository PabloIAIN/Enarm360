package com.example.enarm360.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class SmsService {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    
    @Value("${app.sms.provider:console}")
    private String smsProvider;
    
    @Value("${app.sms.twilio.accountSid:}")
    private String twilioAccountSid;
    
    @Value("${app.sms.twilio.authToken:}")
    private String twilioAuthToken;
    
    @Value("${app.sms.twilio.fromNumber:}")
    private String twilioFromNumber;
    
    @Value("${app.name:ENARM360}")
    private String appName;
    
    private boolean twilioInitialized = false;
    
    /**
     * Enviar SMS de verificación
     */
    public boolean enviarSmsVerificacion(String telefono, String codigo) {
        String mensaje = construirMensajeVerificacion(codigo);
        return enviarSms(telefono, mensaje);
    }
    
    /**
     * Enviar SMS de bienvenida
     */
    public boolean enviarSmsBienvenida(String telefono, String nombre) {
        String mensaje = construirMensajeBienvenida(nombre);
        return enviarSms(telefono, mensaje);
    }
    
    private boolean enviarSms(String telefono, String mensaje) {
        switch (smsProvider.toLowerCase()) {
            case "twilio":
                return enviarConTwilio(telefono, mensaje);
            case "console":
            default:
                return enviarConsole(telefono, mensaje);
        }
    }
    
    /**
     * Enviar SMS usando Twilio SDK
     */
    private boolean enviarConTwilio(String telefono, String mensaje) {
        try {
            if (twilioAccountSid.isEmpty() || twilioAuthToken.isEmpty() || twilioFromNumber.isEmpty()) {
                logger.warn("Credenciales de Twilio no configuradas, usando modo consola");
                return enviarConsole(telefono, mensaje);
            }
            
            // Inicializar Twilio si no se ha hecho
            if (!twilioInitialized) {
                Twilio.init(twilioAccountSid, twilioAuthToken);
                twilioInitialized = true;
                logger.info("Twilio inicializado correctamente para envío de SMS");
            }
            
            String telefonoFormateado = formatearTelefono(telefono);
            
            Message message = Message.creator(
                new PhoneNumber(telefonoFormateado),
                new PhoneNumber(twilioFromNumber),
                mensaje
            ).create();
            
            logger.info("SMS enviado exitosamente a: {} via Twilio. MessageSID: {}, Status: {}", 
                       telefonoFormateado, message.getSid(), message.getStatus());
            return true;
            
        } catch (Exception e) {
            logger.error("Error al enviar SMS a {} via Twilio: {}", telefono, e.getMessage());
            return false;
        }
    }
    
    /**
     * Modo consola para desarrollo
     */
    private boolean enviarConsole(String telefono, String mensaje) {
        logger.info("=".repeat(60));
        logger.info("📱 SMS SIMULADO - MODO DESARROLLO");
        logger.info("=".repeat(60));
        logger.info("📞 Para: {}", formatearTelefono(telefono));
        logger.info("💬 Mensaje:");
        logger.info("{}", mensaje);
        logger.info("=".repeat(60));
        
        return true;
    }
    
    private String construirMensajeVerificacion(String codigo) {
        return String.format(
            "🩺 %s\n\n" +
            "Tu código de verificación es: %s\n\n" +
            "Válido por 10 minutos.\n" +
            "Si no solicitaste este código, ignora este mensaje.",
            appName, codigo
        );
    }
    
    private String construirMensajeBienvenida(String nombre) {
        return String.format(
            "¡Hola %s! 🎉\n\n" +
            "Bienvenido a %s. Tu cuenta ha sido verificada exitosamente.\n\n" +
            "¡Comienza tu preparación para el ENARM ahora!",
            nombre, appName
        );
    }
    
    /**
     * Formatear número de teléfono para envío internacional
     */
    private String formatearTelefono(String telefono) {
        // Limpiar el número
        String numeroLimpio = telefono.replaceAll("[^0-9+]", "");
        
        // Si no empieza con +, asumimos que es número mexicano
        if (!numeroLimpio.startsWith("+")) {
            if (numeroLimpio.startsWith("52")) {
                numeroLimpio = "+" + numeroLimpio;
            } else if (numeroLimpio.length() == 10) {
                numeroLimpio = "+52" + numeroLimpio;
            } else {
                numeroLimpio = "+52" + numeroLimpio;
            }
        }
        
        return numeroLimpio;
    }
    
    /**
     * Validar formato de número de teléfono
     */
    public boolean esNumeroValido(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return false;
        }
        
        String numeroLimpio = telefono.replaceAll("[^0-9+]", "");
        
        // Validar longitud básica (mínimo 10 dígitos)
        if (numeroLimpio.replaceAll("[^0-9]", "").length() < 10) {
            return false;
        }
        
        return true;
    }
}