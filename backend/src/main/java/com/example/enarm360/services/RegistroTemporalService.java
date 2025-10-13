package com.example.enarm360.services;

import com.example.enarm360.dtos.registrer.RegistroRequest;
import com.example.enarm360.dtos.registrer.RegistroResponse;
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
public class RegistroTemporalService {
    
    private static final Logger logger = LoggerFactory.getLogger(RegistroTemporalService.class);
    
    @Autowired
    private RegistroTemporalRepository registroTemporalRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PerfilUsuarioRepository perfilUsuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private SmsService smsService;
    
    /**
     * Crear registro temporal y enviar códigos de verificación
     */
    public RegistroResponse iniciarRegistroTemporal(RegistroRequest request) {
        logger.info("Iniciando registro temporal para: {}", request.getEmail());
        
        try {
            // Verificar si ya existe registro temporal con estos datos
            RegistroTemporal existente = buscarRegistroExistente(request);
            
            RegistroTemporal registroTemporal;
            
            if (existente != null && !existente.isExpired()) {
                // Actualizar registro existente
                logger.info("Actualizando registro temporal existente: {}", existente.getToken());
                registroTemporal = actualizarRegistroTemporal(existente, request);
            } else {
                // Crear nuevo registro temporal
                registroTemporal = crearNuevoRegistroTemporal(request);
            }
            
            // Generar y enviar códigos
            generarYEnviarCodigos(registroTemporal);
            
            // Guardar
            registroTemporalRepository.save(registroTemporal);
            
            logger.info("Registro temporal creado/actualizado: {} para {}", 
                registroTemporal.getToken(), registroTemporal.getEmail());
            
            return RegistroResponse.builder()
                .id(null) // No hay usuario real todavía
                .nombre(registroTemporal.getNombre())
                .apellidos(registroTemporal.getApellidos())
                .email(registroTemporal.getEmail())
                .username(registroTemporal.getUsername())
                .telefono(registroTemporal.getTelefono())
                .activo(true)
                .creadoEn(registroTemporal.getCreadoEn())
                .emailVerificado(false)
                .telefonoVerificado(false)
                .requiereVerificacion(true)
                .tokenVerificacion(registroTemporal.getToken()) // Token para verificaciones
                .mensaje("¡Registro iniciado! Se han enviado códigos de verificación a tu email y teléfono. Tienes 24 horas para completar la verificación.")
                .success(true)
                .build();
                
        } catch (Exception e) {
            logger.error("Error al crear registro temporal: {}", e.getMessage(), e);
            throw new RuntimeException("Error al iniciar registro: " + e.getMessage());
        }
    }
    
    /**
     * Verificar código de email
     */
    public boolean verificarCodigoEmail(String token, String codigo) {
        logger.info("Verificando código de email para token: {}", token);
        
        try {
            RegistroTemporal registro = registroTemporalRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de registro no válido"));
            
            if (registro.isExpired()) {
                throw new IllegalArgumentException("El registro ha expirado");
            }
            
            if (registro.isEmailCodeValid(codigo)) {
                registro.markEmailAsVerified();
                registroTemporalRepository.save(registro);
                
                logger.info("Email verificado correctamente para token: {}", token);
                
                // Verificar si está completamente verificado para migrar
                if (registro.isCompletelyVerified()) {
                    migrarAUsuarioReal(registro);
                }
                
                return true;
            } else {
                logger.warn("Código de email inválido o expirado para token: {}", token);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error al verificar código de email: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Verificar código de teléfono
     */
    public boolean verificarCodigoTelefono(String token, String codigo) {
        logger.info("Verificando código de teléfono para token: {}", token);
        
        try {
            RegistroTemporal registro = registroTemporalRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de registro no válido"));
            
            if (registro.isExpired()) {
                throw new IllegalArgumentException("El registro ha expirado");
            }
            
            if (registro.isTelefonoCodeValid(codigo)) {
                registro.markTelefonoAsVerified();
                registroTemporalRepository.save(registro);
                
                logger.info("Teléfono verificado correctamente para token: {}", token);
                
                // Verificar si está completamente verificado para migrar
                if (registro.isCompletelyVerified()) {
                    migrarAUsuarioReal(registro);
                }
                
                return true;
            } else {
                logger.warn("Código de teléfono inválido o expirado para token: {}", token);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error al verificar código de teléfono: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Reenviar código de email
     */
    public boolean reenviarCodigoEmail(String token) {
        logger.info("Reenviando código de email para token: {}", token);
        
        try {
            RegistroTemporal registro = registroTemporalRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de registro no válido"));
            
            if (registro.isExpired()) {
                throw new IllegalArgumentException("El registro ha expirado");
            }
            
            if (registro.getEmailVerificado()) {
                logger.info("Email ya verificado para token: {}", token);
                return true;
            }
            
            // Generar nuevo código
            registro.generateEmailCode();
            registroTemporalRepository.save(registro);
            
            // Enviar email
            enviarEmailVerificacion(registro.getEmail(), registro.getCodigoEmail());
            
            logger.info("Código de email reenviado para token: {}", token);
            return true;
            
        } catch (Exception e) {
            logger.error("Error al reenviar código de email: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Reenviar código de teléfono
     */
    public boolean reenviarCodigoTelefono(String token) {
        logger.info("Reenviando código de teléfono para token: {}", token);
        
        try {
            RegistroTemporal registro = registroTemporalRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de registro no válido"));
            
            if (registro.isExpired()) {
                throw new IllegalArgumentException("El registro ha expirado");
            }
            
            if (registro.getTelefonoVerificado()) {
                logger.info("Teléfono ya verificado para token: {}", token);
                return true;
            }
            
            // Generar nuevo código
            registro.generateTelefonoCode();
            registroTemporalRepository.save(registro);
            
            // Enviar SMS
            enviarSMSVerificacion(registro.getTelefono(), registro.getCodigoTelefono());
            
            logger.info("Código de teléfono reenviado para token: {}", token);
            return true;
            
        } catch (Exception e) {
            logger.error("Error al reenviar código de teléfono: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Obtener estado del registro temporal
     */
    public RegistroTemporal obtenerRegistroPorToken(String token) {
        return registroTemporalRepository.findByToken(token).orElse(null);
    }
    
    // ===================== MÉTODOS PRIVADOS =====================
    
    /**
     * Buscar registro temporal existente
     */
    private RegistroTemporal buscarRegistroExistente(RegistroRequest request) {
        // Buscar por email primero
        if (registroTemporalRepository.existsByEmail(request.getEmail())) {
            return registroTemporalRepository.findByEmail(request.getEmail()).orElse(null);
        }
        // Buscar por username
        if (registroTemporalRepository.existsByUsername(request.getUsername())) {
            return registroTemporalRepository.findByUsername(request.getUsername()).orElse(null);
        }
        // Buscar por teléfono
        if (registroTemporalRepository.existsByTelefono(request.getTelefono())) {
            return registroTemporalRepository.findByTelefono(request.getTelefono()).orElse(null);
        }
        
        return null;
    }
    
    /**
     * Crear nuevo registro temporal
     */
    private RegistroTemporal crearNuevoRegistroTemporal(RegistroRequest request) {
        return RegistroTemporal.builder()
            .token(UUID.randomUUID().toString())
            .nombre(request.getNombre().trim())
            .apellidos(request.getApellidos() != null && !request.getApellidos().trim().isEmpty() 
                       ? request.getApellidos().trim() : null)
            .username(request.getUsername().toLowerCase().trim())
            .email(request.getEmail().toLowerCase().trim())
            .telefono(request.getTelefono().trim())
            .contrasenaHash(passwordEncoder.encode(request.getContrasena()))
            .paisNacimiento(request.getPaisNacimiento().trim())
            .genero(request.getGenero())
            .fechaNacimiento(request.getFechaNacimiento())
            .expiraEn(LocalDateTime.now().plusHours(24))
            .build();
    }
    
    /**
     * Actualizar registro temporal existente
     */
    private RegistroTemporal actualizarRegistroTemporal(RegistroTemporal existente, RegistroRequest request) {
        // Actualizar datos (mantener verificaciones ya completadas)
        existente.setNombre(request.getNombre().trim());
        existente.setApellidos(request.getApellidos() != null && !request.getApellidos().trim().isEmpty() 
                   ? request.getApellidos().trim() : null);
        existente.setUsername(request.getUsername().toLowerCase().trim());
        existente.setEmail(request.getEmail().toLowerCase().trim());
        existente.setTelefono(request.getTelefono().trim());
        existente.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
        existente.setPaisNacimiento(request.getPaisNacimiento().trim());
        existente.setGenero(request.getGenero());
        existente.setFechaNacimiento(request.getFechaNacimiento());
        
        // Extender tiempo de expiración
        existente.setExpiraEn(LocalDateTime.now().plusHours(24));
        
        return existente;
    }
    
    /**
     * Generar y enviar códigos de verificación
     */
    private void generarYEnviarCodigos(RegistroTemporal registro) {
        // Generar código de email si no está verificado
        if (!registro.getEmailVerificado()) {
            registro.generateEmailCode();
            enviarEmailVerificacion(registro.getEmail(), registro.getCodigoEmail());
        }
        
        // Generar código de teléfono si no está verificado
        if (!registro.getTelefonoVerificado()) {
            registro.generateTelefonoCode();
            enviarSMSVerificacion(registro.getTelefono(), registro.getCodigoTelefono());
        }
    }
    
    /**
     * Migrar registro temporal a usuario real una vez completamente verificado
     */
    private void migrarAUsuarioReal(RegistroTemporal registroTemporal) {
        logger.info("Migrando registro temporal a usuario real: {}", registroTemporal.getToken());
        
        try {
            // 1. Crear Usuario con campos correspondientes a la tabla usuario
            Usuario usuario = Usuario.builder()
                .email(registroTemporal.getEmail())
                .username(registroTemporal.getUsername())
                .contrasenaHash(registroTemporal.getContrasenaHash())
                .nombre(registroTemporal.getNombre())
                .apellidos(registroTemporal.getApellidos())
                .telefono(registroTemporal.getTelefono())
                .activo(true)
                .emailVerificado(true)   // ✅ Ya verificado
                .telefonoVerificado(true) // ✅ Ya verificado
                .build();
            
            // 2. Asignar rol ESTUDIANTE
            Rol rolEstudiante = rolRepository.findByNombre("ESTUDIANTE")
                .orElseThrow(() -> new RuntimeException("Rol ESTUDIANTE no encontrado"));
            usuario.getRoles().add(rolEstudiante);
            
            // 3. Guardar usuario PRIMERO
            usuario = usuarioRepository.save(usuario);
            
            // 4. Crear PerfilUsuario con campos correspondientes a perfil_usuario
            PerfilUsuario perfil = PerfilUsuario.builder()
                .usuario(usuario)
                .pais(registroTemporal.getPaisNacimiento())  // país nacimiento va a perfil
                .genero(registroTemporal.getGenero())        // genero va a perfil
                .fechaNacimiento(registroTemporal.getFechaNacimiento()) // fecha nacimiento va a perfil
                .bio("Estudiante de medicina preparándose para el ENARM. ¡Listo para alcanzar mis metas académicas!")
                .tz("America/Monterrey")
                // Configuraciones por defecto
                .recibirNotificaciones(true)
                .recibirNewsletters(false)
                .perfilPublico(true)
                .mostrarEmail(false)
                .mostrarTelefono(false)
                .mostrarUniversidad(true)
                .permitirMensajes(true)
                .mostrarEstadisticas(true)
                .build();
            
            // 5. Guardar perfil
            perfilUsuarioRepository.save(perfil);
            
            // 6. Actualizar relación bidireccional
            usuario.setPerfil(perfil);
            usuarioRepository.save(usuario);
            
            // 7. Eliminar registro temporal
            registroTemporalRepository.delete(registroTemporal);
            
            logger.info("Usuario migrado exitosamente: {} (ID: {})", usuario.getEmail(), usuario.getId());
            
        } catch (Exception e) {
            logger.error("Error al migrar registro temporal: {}", e.getMessage(), e);
            throw new RuntimeException("Error al completar registro: " + e.getMessage());
        }
    }
    
    /**
     * Enviar email de verificación (implementación temporal)
     */
    private void enviarEmailVerificacion(String email, String codigo) {
        // TODO: Implementar envío real de email
        logger.info("[DESARROLLO] Email de verificación enviado a {}: codigo={}", email, codigo);
    }
    
    /**
     * Enviar SMS de verificación usando SmsService
     */
    private void enviarSMSVerificacion(String telefono, String codigo) {
        try {
            boolean enviado = smsService.enviarSmsVerificacion(telefono, codigo);
            if (enviado) {
                logger.info("SMS de verificación enviado exitosamente a: {}", telefono);
            } else {
                logger.warn("Error al enviar SMS de verificación a: {}", telefono);
            }
        } catch (Exception e) {
            logger.error("Error al enviar SMS de verificación a {}: {}", telefono, e.getMessage());
        }
    }
}