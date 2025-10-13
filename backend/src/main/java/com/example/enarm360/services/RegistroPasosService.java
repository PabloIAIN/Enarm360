package com.example.enarm360.services;

import com.example.enarm360.dtos.registrer.RegistroPasosRequest;
import com.example.enarm360.entities.*;
import com.example.enarm360.repositories.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class RegistroPasosService {
    
    private static final Logger logger = LoggerFactory.getLogger(RegistroPasosService.class);
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PerfilUsuarioRepository perfilUsuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private VerificacionEmailRepository verificacionEmailRepository;
    
    @Autowired
    private VerificacionTelefonoRepository verificacionTelefonoRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    // ==========================================================
    // PASO 1: VALIDAR DATOS PERSONALES
    // ==========================================================
    
    /**
     * Valida el paso 1 sin crear la cuenta
     */
    public RegistroPasosRequest.Paso1Response validarPaso1(RegistroPasosRequest.Paso1DatosPersonales request) {
        logger.info("Validando paso 1 para username: {}", request.getUsername());
        
        try {
            // Validar disponibilidad de username
            if (authService.existsByUsername(request.getUsername())) {
                return RegistroPasosRequest.Paso1Response.builder()
                    .success(false)
                    .mensaje("Username ya está en uso")
                    .usernameDisponible(false)
                    .siguientePaso("paso1")
                    .build();
            }
            
            // Validar edad mínima (ejemplo: 16 años)
            if (request.getFechaNacimiento().isAfter(LocalDateTime.now().minusYears(16).toLocalDate())) {
                return RegistroPasosRequest.Paso1Response.builder()
                    .success(false)
                    .mensaje("Debes ser mayor de 16 años para registrarte")
                    .usernameDisponible(true)
                    .siguientePaso("paso1")
                    .build();
            }
            
            // Todo válido
            return RegistroPasosRequest.Paso1Response.builder()
                .success(true)
                .mensaje("Datos personales válidos")
                .usernameDisponible(true)
                .siguientePaso("paso2")
                .build();
                
        } catch (Exception e) {
            logger.error("Error al validar paso 1: {}", e.getMessage(), e);
            return RegistroPasosRequest.Paso1Response.builder()
                .success(false)
                .mensaje("Error interno al validar datos")
                .usernameDisponible(false)
                .siguientePaso("paso1")
                .build();
        }
    }
    
    // ==========================================================
    // PASO 2: CREAR CUENTA Y ENVIAR VERIFICACIONES
    // ==========================================================
    
    /**
     * Crea la cuenta completa y envía verificaciones
     */
    public RegistroPasosRequest.Paso2Response crearCuentaYEnviarVerificaciones(RegistroPasosRequest.Paso2Contacto request) {
        logger.info("Creando cuenta para: {} ({})", request.getEmail(), request.getUsername());
        
        try {
            // 1. Validar disponibilidad final
            if (authService.existsByEmail(request.getEmail())) {
                return RegistroPasosRequest.Paso2Response.builder()
                    .success(false)
                    .mensaje("El email ya está registrado")
                    .siguientePaso("paso2")
                    .build();
            }
            
            if (authService.existsByTelefono(request.getTelefono())) {
                return RegistroPasosRequest.Paso2Response.builder()
                    .success(false)
                    .mensaje("El teléfono ya está registrado")
                    .siguientePaso("paso2")
                    .build();
            }
            
            if (authService.existsByUsername(request.getUsername())) {
                return RegistroPasosRequest.Paso2Response.builder()
                    .success(false)
                    .mensaje("El username ya está en uso")
                    .siguientePaso("paso1")
                    .build();
            }
            
            // 2. Crear Usuario
            Usuario usuario = Usuario.builder()
                .email(request.getEmail().toLowerCase().trim())
                .username(request.getUsername().toLowerCase().trim())
                .contrasenaHash(passwordEncoder.encode(request.getContrasena()))
                .nombre(request.getNombre().trim())
                .apellidos(request.getApellidos() != null && !request.getApellidos().trim().isEmpty() 
                           ? request.getApellidos().trim() : null)
                .telefono(request.getTelefono().trim())
                .activo(true)
                .emailVerificado(false)
                .telefonoVerificado(false)
                .build();
            
            // 3. Asignar rol ESTUDIANTE
            Rol rolEstudiante = rolRepository.findByNombre("ESTUDIANTE")
                .orElseThrow(() -> new RuntimeException("Rol ESTUDIANTE no encontrado"));
            usuario.getRoles().add(rolEstudiante);
            
            // 4. Guardar usuario
            usuario = usuarioRepository.save(usuario);
            
            // 5. Crear PerfilUsuario con los datos del paso 1
            PerfilUsuario perfil = PerfilUsuario.builder()
                .usuario(usuario)
                .pais(request.getPaisNacimiento().trim())
                .genero(request.getGenero())
                .fechaNacimiento(request.getFechaNacimiento())
                .bio("Estudiante de medicina preparándose para el ENARM. ¡Listo para alcanzar mis metas académicas!")
                .tz("America/Monterrey")
                .build();
            
            perfilUsuarioRepository.save(perfil);
            
            // 6. Actualizar relación bidireccional
            usuario.setPerfil(perfil);
            usuarioRepository.save(usuario);
            
            // 7. Crear verificaciones
            String tokenEmail = crearVerificacionEmail(usuario);
            String codigoTelefono = crearVerificacionTelefono(usuario);
            
            logger.info("Cuenta creada exitosamente: {} (ID: {})", usuario.getEmail(), usuario.getId());
            
            return RegistroPasosRequest.Paso2Response.builder()
                .success(true)
                .mensaje("Cuenta creada exitosamente. Se han enviado códigos de verificación.")
                .usuarioId(usuario.getId())
                .tokenEmail(tokenEmail) // Solo para debug
                .codigoTelefono(codigoTelefono) // Solo para debug
                .siguientePaso("verificar-email")
                .requiereVerificacionEmail(true)
                .requiereVerificacionTelefono(true)
                .build();
                
        } catch (Exception e) {
            logger.error("Error al crear cuenta: {}", e.getMessage(), e);
            return RegistroPasosRequest.Paso2Response.builder()
                .success(false)
                .mensaje("Error interno al crear cuenta: " + e.getMessage())
                .siguientePaso("paso2")
                .build();
        }
    }
    
    // ==========================================================
    // MÉTODOS PRIVADOS DE VERIFICACIÓN
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
            
            logger.info("[DESARROLLO] Email verificación para {}: {}", usuario.getEmail(), codigo);
            
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
            
            logger.info("[DESARROLLO] SMS verificación para {}: {}", usuario.getTelefono(), codigo);
            
            return codigo;
        } catch (Exception e) {
            logger.error("Error al crear verificación de teléfono: {}", e.getMessage());
            return null;
        }
    }
}