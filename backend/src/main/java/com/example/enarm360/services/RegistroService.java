package com.example.enarm360.services;
import com.example.enarm360.dtos.registrer.PasswordValidationResponse;
import com.example.enarm360.dtos.registrer.RegistroInfoResponse;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RegistroService {
    
    private static final Logger logger = LoggerFactory.getLogger(RegistroService.class);
    
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
    private SmsService smsService;
    
    /**
 * Registrar nuevo usuario (solo estudiantes)
 */
public RegistroResponse registrarUsuario(RegistroRequest request) {
    logger.info("Iniciando registro para: {}", request.getEmail());
    
    try {
        // 1. Validar disponibilidad de telefono - permitir re-registro si no está verificado
        Usuario usuarioExistentePorTelefono = usuarioRepository.findByTelefono(request.getTelefono()).orElse(null);
        if (usuarioExistentePorTelefono != null) {
            if (usuarioExistentePorTelefono.getTelefonoVerificado()) {
                throw new IllegalArgumentException("El teléfono ya está registrado y verificado");
            } else {
                // Usuario existe pero no ha verificado - permitir continuar con el registro
                logger.info("Usuario con teléfono {} existe pero no verificado, permitiendo re-registro", request.getTelefono());
                return continuarRegistroExistente(usuarioExistentePorTelefono, request);
            }
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
            
        // 3. Asignar rol ESTUDIANTE antes de guardar
        Rol rolEstudiante = rolRepository.findByNombre("ESTUDIANTE")
            .orElseThrow(() -> new RuntimeException("Rol ESTUDIANTE no encontrado"));
        
        usuario.getRoles().add(rolEstudiante);
        
        // 4. Guardar usuario PRIMERO
        usuario = usuarioRepository.save(usuario);
        
        // 5. Crear PerfilUsuario con los nuevos datos obligatorios
        PerfilUsuario perfil = PerfilUsuario.builder()
            .usuario(usuario)
            .pais(request.getPaisNacimiento().trim())  // Ahora obligatorio
            .genero(request.getGenero())               // Nuevo campo obligatorio
            .fechaNacimiento(request.getFechaNacimiento()) // Nuevo campo obligatorio
            // BIOGRAFÍA POR DEFECTO
            .bio("Estudiante de medicina preparándose para el ENARM. ¡Listo para alcanzar mis metas académicas!")
            .tz("America/Monterrey")
            .build();
            
        perfilUsuarioRepository.save(perfil);
        
        // 6. Actualizar la relación bidireccional
        usuario.setPerfil(perfil);
        usuarioRepository.save(usuario);
        
        // 7. Crear verificaciones de email y teléfono
        String tokenEmail = crearVerificacionEmail(usuario);
        String codigoTelefono = crearVerificacionTelefono(usuario);
        
        logger.info("Usuario registrado: {} (ID: {})", usuario.getEmail(), usuario.getId());
        logger.info("Verificaciones creadas - Email: {}, Teléfono: {}", tokenEmail != null, codigoTelefono != null);
        
        return RegistroResponse.builder()
            .id(usuario.getId())
            .nombre(usuario.getNombre())
            .apellidos(usuario.getApellidos())
            .email(usuario.getEmail())
            .username(usuario.getUsername())
            .telefono(usuario.getTelefono())
            .activo(usuario.getActivo())
            .creadoEn(usuario.getCreadoEn())
            .emailVerificado(usuario.getEmailVerificado())
            .telefonoVerificado(usuario.getTelefonoVerificado())
            .requiereVerificacion(true)
            .tokenEmail(tokenEmail) // Solo para debug - remover en producción
            .mensaje("Usuario registrado exitosamente. Se han enviado códigos de verificación a tu email y teléfono.")
            .success(true)
            .build();
            
    } catch (Exception e) {
        logger.error("Error al registrar usuario: {}", e.getMessage(), e);
        throw new RuntimeException("Error al crear cuenta: " + e.getMessage());
    }
}

/**
 * Info básica para formulario
 */
public RegistroInfoResponse getInformacionRegistro() {
    
    List<String> paises = Arrays.asList(
        "México", "Estados Unidos", "España", "Colombia", "Argentina", 
        "Chile", "Perú", "Venezuela", "Ecuador", "Guatemala", "Otro"
    );
    
    return RegistroInfoResponse.builder()
        .paises(paises)
        .passwordRequirements(RegistroInfoResponse.PasswordRequirementsDTO.getDefault())
        .build();
}
    
    /**
     * Validar fortaleza de contraseña
     */
    public PasswordValidationResponse validarPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return PasswordValidationResponse.builder()
                .valido(false)
                .mensaje("Contraseña es requerida")
                .fortaleza("ninguna")
                .build();
        }
        
        boolean tieneMinimo8 = password.length() >= 8;
        boolean tieneMayuscula = password.matches(".*[A-Z].*");
        boolean tieneMinuscula = password.matches(".*[a-z].*");
        boolean tieneNumero = password.matches(".*\\d.*");
        
        int puntaje = 0;
        if (tieneMinimo8) puntaje++;
        if (tieneMayuscula) puntaje++;
        if (tieneMinuscula) puntaje++;
        if (tieneNumero) puntaje++;
        
        boolean esValida = puntaje >= 4; // Requiere todos los criterios
        
        String fortaleza;
        String mensaje;
        
        if (puntaje >= 4) {
            fortaleza = "fuerte";
            mensaje = "Contraseña fuerte";
        } else if (puntaje >= 3) {
            fortaleza = "media";
            mensaje = "Contraseña media - falta algún criterio";
        } else {
            fortaleza = "debil";
            mensaje = "Contraseña débil - cumple pocos criterios";
        }
        
        PasswordValidationResponse.PasswordChecks checks = 
            PasswordValidationResponse.PasswordChecks.builder()
                .tieneMinimo8Caracteres(tieneMinimo8)
                .tieneMayuscula(tieneMayuscula)
                .tieneMinuscula(tieneMinuscula)
                .tieneNumero(tieneNumero)
                .puntaje(puntaje)
                .build();
        
        return PasswordValidationResponse.builder()
            .valido(esValida)
            .mensaje(mensaje)
            .fortaleza(fortaleza)
            .checks(checks)
            .build();
    }
    
    // ==========================================================
    // MÉTODOS DE VERIFICACIÓN
    // ==========================================================
    
    /**
     * Crear verificación de email
     */
    private String crearVerificacionEmail(Usuario usuario) {
        try {
            // Eliminar verificaciones anteriores no verificadas
            verificacionEmailRepository.deleteUnverifiedByUsuario(usuario);
            
            // Generar código de 6 dígitos igual que el SMS
            String codigo = String.format("%06d", (int)(Math.random() * 1000000));
            
            // Crear verificación
            VerificacionEmail verificacion = VerificacionEmail.builder()
                .codigo(codigo)
                .email(usuario.getEmail())
                .usuario(usuario)
                .expiraEn(LocalDateTime.now().plusHours(24)) // Expira en 24 horas
                .build();
                
            verificacionEmailRepository.save(verificacion);
            
            // TODO: Enviar email con el código
            enviarEmailVerificacion(usuario.getEmail(), codigo);
            
            return codigo;
        } catch (Exception e) {
            logger.error("Error al crear verificación de email para {}: {}", usuario.getEmail(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Crear verificación de teléfono
     */
    private String crearVerificacionTelefono(Usuario usuario) {
        try {
            // Eliminar verificaciones anteriores no verificadas
            verificacionTelefonoRepository.deleteUnverifiedByUsuario(usuario);
            
            // Generar código de 6 dígitos
            String codigo = String.format("%06d", (int)(Math.random() * 1000000));
            
            // Crear verificación
            VerificacionTelefono verificacion = VerificacionTelefono.builder()
                .codigo(codigo)
                .telefono(usuario.getTelefono())
                .usuario(usuario)
                .expiraEn(LocalDateTime.now().plusMinutes(10)) // Expira en 10 minutos
                .build();
                
            verificacionTelefonoRepository.save(verificacion);
            
            // TODO: Enviar SMS con el código
            enviarSMSVerificacion(usuario.getTelefono(), codigo);
            
            return codigo;
        } catch (Exception e) {
            logger.error("Error al crear verificación de teléfono para {}: {}", usuario.getTelefono(), e.getMessage());
            return null;
        }
    }
    
    // ==========================================================
    // MÉTODOS DE ENVÍO (TEMPORALES - IMPLEMENTAR MÁS TARDE)
    // ==========================================================
    
    /**
     * Enviar email de verificación (implementación temporal)
     */
    private void enviarEmailVerificacion(String email, String codigo) {
        // TODO: Implementar envío real de email
        logger.info("[DESARROLLO] Email de verificación enviado a {}: codigo={}", email, codigo);
        logger.info("[DESARROLLO] Código de verificación: {}", codigo);
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
    
    /**
     * Continuar con un registro existente no verificado
     */
    private RegistroResponse continuarRegistroExistente(Usuario usuarioExistente, RegistroRequest request) {
        logger.info("Continuando registro existente para usuario ID: {}", usuarioExistente.getId());
        
        try {
            // Actualizar datos del usuario si es necesario
            usuarioExistente.setNombre(request.getNombre().trim());
            usuarioExistente.setApellidos(request.getApellidos() != null && !request.getApellidos().trim().isEmpty() 
                       ? request.getApellidos().trim() : null);
            
            // Actualizar contraseña si es diferente
            if (!passwordEncoder.matches(request.getContrasena(), usuarioExistente.getContrasenaHash())) {
                usuarioExistente.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
            }
            
            // Actualizar perfil si existe
            if (usuarioExistente.getPerfil() != null) {
                PerfilUsuario perfil = usuarioExistente.getPerfil();
                perfil.setPais(request.getPaisNacimiento().trim());
                perfil.setGenero(request.getGenero());
                perfil.setFechaNacimiento(request.getFechaNacimiento());
                perfilUsuarioRepository.save(perfil);
            }
            
            usuarioRepository.save(usuarioExistente);
            
            // Reenviar códigos de verificación
            String tokenEmail = crearVerificacionEmail(usuarioExistente);
            String codigoTelefono = crearVerificacionTelefono(usuarioExistente);
            
            logger.info("Registro existente actualizado para: {} (ID: {})", usuarioExistente.getEmail(), usuarioExistente.getId());
            
            return RegistroResponse.builder()
                .id(usuarioExistente.getId())
                .nombre(usuarioExistente.getNombre())
                .apellidos(usuarioExistente.getApellidos())
                .email(usuarioExistente.getEmail())
                .username(usuarioExistente.getUsername())
                .telefono(usuarioExistente.getTelefono())
                .activo(usuarioExistente.getActivo())
                .creadoEn(usuarioExistente.getCreadoEn())
                .emailVerificado(usuarioExistente.getEmailVerificado())
                .telefonoVerificado(usuarioExistente.getTelefonoVerificado())
                .requiereVerificacion(true)
                .mensaje("Se han reenviado los códigos de verificación. Completa tu registro verificando tu email y teléfono.")
                .success(true)
                .build();
                
        } catch (Exception e) {
            logger.error("Error al continuar registro existente: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar registro: " + e.getMessage());
        }
    }
}
