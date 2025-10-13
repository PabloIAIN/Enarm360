package com.example.enarm360.services;

import com.example.enarm360.dtos.registrer.*;
import com.example.enarm360.entities.*;
import com.example.enarm360.repositories.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class VerificacionService {
    
    private static final Logger logger = LoggerFactory.getLogger(VerificacionService.class);
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private VerificacionEmailRepository verificacionEmailRepository;
    
    @Autowired
    private VerificacionTelefonoRepository verificacionTelefonoRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    // ==========================================================
    // VERIFICACIÓN DE EMAIL
    // ==========================================================
    
    /**
     * Verificar email con token
     */
    public VerificacionResponse.General verificarEmail(VerificacionRequest.Email request) {
        try {
            Optional<VerificacionEmail> verificacionOpt = verificacionEmailRepository.findByCodigoAndEmailAndVerificadoFalse(request.getCodigo(), request.getEmail());
            
            if (verificacionOpt.isEmpty()) {
                return VerificacionResponse.General.builder()
                    .success(false)
                    .mensaje("Código de verificación inválido o ya utilizado")
                    .verificado(false)
                    .intentosRestantes(0)
                    .build();
            }
            
            VerificacionEmail verificacion = verificacionOpt.get();
            
            // Verificar si no ha expirado
            if (verificacion.hasExpirado()) {
                return VerificacionResponse.General.builder()
                    .success(false)
                    .mensaje("El código de verificación ha expirado")
                    .verificado(false)
                    .intentosRestantes(0)
                    .build();
            }
            
            // Incrementar intentos
            verificacion.incrementarIntentos();
            
            // Verificar si ha excedido los intentos
            if (verificacion.hasExcedidoIntentos()) {
                verificacionEmailRepository.save(verificacion);
                return VerificacionResponse.General.builder()
                    .success(false)
                    .mensaje("Has excedido el número máximo de intentos")
                    .verificado(false)
                    .intentosRestantes(0)
                    .build();
            }
            
            // Marcar como verificado
            verificacion.marcarComoVerificado();
            verificacionEmailRepository.save(verificacion);
            
            // Actualizar usuario
            Usuario usuario = verificacion.getUsuario();
            usuario.setEmailVerificado(true);
            usuarioRepository.save(usuario);
            
            logger.info("Email verificado exitosamente para usuario: {}", usuario.getEmail());
            
            return VerificacionResponse.General.builder()
                .success(true)
                .mensaje("Email verificado exitosamente")
                .verificado(true)
                .verificadoEn(verificacion.getVerificadoEn())
                .intentosRestantes(0)
                .build();
                
        } catch (Exception e) {
            logger.error("Error al verificar email: {}", e.getMessage(), e);
            return VerificacionResponse.General.builder()
                .success(false)
                .mensaje("Error interno al verificar email")
                .verificado(false)
                .intentosRestantes(0)
                .build();
        }
    }
    
    /**
     * Reenviar verificación de email
     */
    public VerificacionResponse.Envio reenviarVerificacionEmail(VerificacionRequest.ReenviarEmail request) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailAndActivoTrue(request.getEmail());
            
            if (usuarioOpt.isEmpty()) {
                return VerificacionResponse.Envio.builder()
                    .success(false)
                    .mensaje("No se encontró usuario con ese email")
                    .tipoEnvio("email")
                    .build();
            }
            
            Usuario usuario = usuarioOpt.get();
            
            if (usuario.getEmailVerificado()) {
                return VerificacionResponse.Envio.builder()
                    .success(false)
                    .mensaje("El email ya está verificado")
                    .tipoEnvio("email")
                    .build();
            }
            
            // Verificar si ya existe una verificación activa reciente
            long verificacionesActivas = verificacionEmailRepository.countActiveVerificationsByUsuario(usuario, LocalDateTime.now());
            if (verificacionesActivas > 0) {
                return VerificacionResponse.Envio.builder()
                    .success(false)
                    .mensaje("Ya existe una verificación pendiente. Revisa tu email.")
                    .tipoEnvio("email")
                    .puedeReenviar(false)
                    .build();
            }
            
            // Crear nueva verificación
            String token = crearVerificacionEmail(usuario);
            
            if (token != null) {
                return VerificacionResponse.Envio.builder()
                    .success(true)
                    .mensaje("Se ha reenviado el email de verificación")
                    .tipoEnvio("email")
                    .enviadoEn(LocalDateTime.now())
                    .expiraEn(LocalDateTime.now().plusHours(24))
                    .puedeReenviar(false)
                    .tiempoEsperaMinutos(5)
                    .build();
            } else {
                return VerificacionResponse.Envio.builder()
                    .success(false)
                    .mensaje("Error al crear verificación de email")
                    .tipoEnvio("email")
                    .build();
            }
            
        } catch (Exception e) {
            logger.error("Error al reenviar verificación de email: {}", e.getMessage(), e);
            return VerificacionResponse.Envio.builder()
                .success(false)
                .mensaje("Error interno al reenviar verificación")
                .tipoEnvio("email")
                .build();
        }
    }
    
    // ==========================================================
    // VERIFICACIÓN DE TELÉFONO
    // ==========================================================
    
    /**
     * Verificar teléfono con código
     */
    public VerificacionResponse.General verificarTelefono(VerificacionRequest.Telefono request) {
        try {
            Optional<VerificacionTelefono> verificacionOpt = verificacionTelefonoRepository
                .findByCodigoAndTelefonoAndVerificadoFalse(request.getCodigo(), request.getTelefono());
            
            if (verificacionOpt.isEmpty()) {
                return VerificacionResponse.General.builder()
                    .success(false)
                    .mensaje("Código de verificación inválido")
                    .verificado(false)
                    .intentosRestantes(2)
                    .build();
            }
            
            VerificacionTelefono verificacion = verificacionOpt.get();
            
            // Verificar si no ha expirado
            if (verificacion.hasExpirado()) {
                return VerificacionResponse.General.builder()
                    .success(false)
                    .mensaje("El código de verificación ha expirado")
                    .verificado(false)
                    .intentosRestantes(0)
                    .build();
            }
            
            // Incrementar intentos
            verificacion.incrementarIntentos();
            
            // Verificar si ha excedido los intentos
            if (verificacion.hasExcedidoIntentos()) {
                verificacionTelefonoRepository.save(verificacion);
                return VerificacionResponse.General.builder()
                    .success(false)
                    .mensaje("Has excedido el número máximo de intentos")
                    .verificado(false)
                    .intentosRestantes(0)
                    .build();
            }
            
            // Marcar como verificado
            verificacion.marcarComoVerificado();
            verificacionTelefonoRepository.save(verificacion);
            
            // Actualizar usuario
            Usuario usuario = verificacion.getUsuario();
            usuario.setTelefonoVerificado(true);
            usuarioRepository.save(usuario);
            
            logger.info("Teléfono verificado exitosamente para usuario: {}", usuario.getTelefono());
            
            return VerificacionResponse.General.builder()
                .success(true)
                .mensaje("Teléfono verificado exitosamente")
                .verificado(true)
                .verificadoEn(verificacion.getVerificadoEn())
                .intentosRestantes(0)
                .build();
                
        } catch (Exception e) {
            logger.error("Error al verificar teléfono: {}", e.getMessage(), e);
            return VerificacionResponse.General.builder()
                .success(false)
                .mensaje("Error interno al verificar teléfono")
                .verificado(false)
                .intentosRestantes(0)
                .build();
        }
    }
    
    /**
     * Reenviar código de verificación de teléfono
     */
    public VerificacionResponse.Envio reenviarVerificacionTelefono(VerificacionRequest.ReenviarTelefono request) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByTelefonoAndActivoTrue(request.getTelefono());
            
            if (usuarioOpt.isEmpty()) {
                return VerificacionResponse.Envio.builder()
                    .success(false)
                    .mensaje("No se encontró usuario con ese teléfono")
                    .tipoEnvio("sms")
                    .build();
            }
            
            Usuario usuario = usuarioOpt.get();
            
            if (usuario.getTelefonoVerificado()) {
                return VerificacionResponse.Envio.builder()
                    .success(false)
                    .mensaje("El teléfono ya está verificado")
                    .tipoEnvio("sms")
                    .build();
            }
            
            // Verificar límites de reenvío
            Optional<VerificacionTelefono> ultimaVerificacion = verificacionTelefonoRepository
                .findLatestActiveByTelefono(request.getTelefono());
                
            if (ultimaVerificacion.isPresent() && !ultimaVerificacion.get().puedeReenviar()) {
                long minutosEspera = ChronoUnit.MINUTES.between(
                    ultimaVerificacion.get().getUltimoEnvio(), 
                    LocalDateTime.now().plusMinutes(1)
                );
                
                return VerificacionResponse.Envio.builder()
                    .success(false)
                    .mensaje("Debes esperar antes de solicitar un nuevo código")
                    .tipoEnvio("sms")
                    .puedeReenviar(false)
                    .tiempoEsperaMinutos((int)minutosEspera)
                    .build();
            }
            
            // Crear nueva verificación
            String codigo = crearVerificacionTelefono(usuario);
            
            if (codigo != null) {
                return VerificacionResponse.Envio.builder()
                    .success(true)
                    .mensaje("Se ha reenviado el código de verificación")
                    .tipoEnvio("sms")
                    .enviadoEn(LocalDateTime.now())
                    .expiraEn(LocalDateTime.now().plusMinutes(10))
                    .puedeReenviar(false)
                    .tiempoEsperaMinutos(1)
                    .enviosRestantes(2)
                    .build();
            } else {
                return VerificacionResponse.Envio.builder()
                    .success(false)
                    .mensaje("Error al crear código de verificación")
                    .tipoEnvio("sms")
                    .build();
            }
            
        } catch (Exception e) {
            logger.error("Error al reenviar verificación de teléfono: {}", e.getMessage(), e);
            return VerificacionResponse.Envio.builder()
                .success(false)
                .mensaje("Error interno al reenviar verificación")
                .tipoEnvio("sms")
                .build();
        }
    }
    
    // ==========================================================
    // MÉTODOS PRIVADOS
    // ==========================================================
    
    private String crearVerificacionEmail(Usuario usuario) {
        try {
            verificacionEmailRepository.deleteUnverifiedByUsuario(usuario);
            
            // Generar código de 6 dígitos igual que el SMS
            String codigo = String.format("%06d", (int)(Math.random() * 1000000));
            VerificacionEmail verificacion = VerificacionEmail.builder()
                .codigo(codigo)
                .email(usuario.getEmail())
                .usuario(usuario)
                .expiraEn(LocalDateTime.now().plusHours(24))
                .build();
                
            verificacionEmailRepository.save(verificacion);
            
            // Enviar email de verificación
            boolean emailEnviado = emailService.enviarEmailVerificacion(
                usuario.getEmail(), 
                usuario.getNombre(), 
                codigo
            );
            
            if (!emailEnviado) {
                logger.warn("No se pudo enviar email de verificación a: {}", usuario.getEmail());
            }
            
            logger.info("[DESARROLLO] Código de verificación: {}", codigo);
            
            return codigo;
        } catch (Exception e) {
            logger.error("Error al crear verificación de email: {}", e.getMessage());
            return null;
        }
    }
    
    private String crearVerificacionTelefono(Usuario usuario) {
        try {
            verificacionTelefonoRepository.deleteUnverifiedByUsuario(usuario);
            
            String codigo = String.format("%06d", (int)(Math.random() * 1000000));
            VerificacionTelefono verificacion = VerificacionTelefono.builder()
                .codigo(codigo)
                .telefono(usuario.getTelefono())
                .usuario(usuario)
                .expiraEn(LocalDateTime.now().plusMinutes(10))
                .build();
            
            verificacion.registrarEnvio();
            verificacionTelefonoRepository.save(verificacion);
            
            // Enviar SMS de verificación
            boolean smsEnviado = smsService.enviarSmsVerificacion(
                usuario.getTelefono(), 
                codigo
            );
            
            if (!smsEnviado) {
                logger.warn("No se pudo enviar SMS de verificación a: {}", usuario.getTelefono());
            }
            
            logger.info("[DESARROLLO] Código SMS: {}", codigo);
            
            return codigo;
        } catch (Exception e) {
            logger.error("Error al crear verificación de teléfono: {}", e.getMessage());
            return null;
        }
    }
}