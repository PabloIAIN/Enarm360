package com.example.enarm360.controllers;

import com.example.enarm360.dtos.registrer.*;
import com.example.enarm360.entities.Usuario;
import com.example.enarm360.entities.RegistroTemporal;
import com.example.enarm360.services.AuthService;
import com.example.enarm360.services.RegistroService;
import com.example.enarm360.services.VerificacionService;
import com.example.enarm360.services.RegistroPasosService;
import com.example.enarm360.services.RegistroTemporalService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/registro")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RegistroController {

    private static final Logger logger = LoggerFactory.getLogger(RegistroController.class);

    @Autowired
    private RegistroService registroService;
    
    @Autowired
    private VerificacionService verificacionService;
    
    @Autowired
    private RegistroPasosService registroPasosService;

    @Autowired
    private AuthService authService;
    
    @Autowired
    private RegistroTemporalService registroTemporalService;

    // ==========================================================
    // REGISTRO PRINCIPAL
    // ==========================================================

    @PostMapping("/crear-cuenta")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroRequest request) {
        logger.info("Solicitud de registro temporal para email: {}", request.getEmail());
        
        try {
            // Usar el nuevo servicio de registro temporal
            RegistroResponse response = registroTemporalService.iniciarRegistroTemporal(request);
            
            logger.info("Registro temporal creado exitosamente: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación en registro temporal: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message", e.getMessage(),
                    "success", false
                ));
        } catch (Exception e) {
            logger.error("Error en registro temporal para {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "message", "Error del servidor al crear cuenta",
                    "success", false
                ));
        }
    }

    // ==========================================================
    // INFORMACIÓN PARA FORMULARIO
    // ==========================================================

    @GetMapping("/info")
    public ResponseEntity<?> getRegistroInfo() {
        try {
            RegistroInfoResponse info = registroService.getInformacionRegistro();
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            logger.error("Error al obtener info de registro: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message", "Error al cargar información",
                    "success", false
                ));
        }
    }

    // ==========================================================
    // VALIDACIONES
    // ==========================================================

    @PostMapping("/validar-password")
    public ResponseEntity<?> validarPassword(@RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "valido", false,
                    "mensaje", "Contraseña es requerida"
                ));
            }
            
            PasswordValidationResponse validation = registroService.validarPassword(password);
            return ResponseEntity.ok(validation);
            
        } catch (Exception e) {
            logger.error("Error al validar password: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message", "Error al validar contraseña",
                    "success", false
                ));
        }
    }

    // ==========================================================
    // VERIFICACIONES DE DISPONIBILIDAD
    // ==========================================================

    @GetMapping("/check-availability")
    public ResponseEntity<?> checkAvailability(
            @RequestParam String field, 
            @RequestParam String value) {
        try {
            AuthService.CheckAvailabilityResponse response = 
                authService.checkAvailability(field, value);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al verificar disponibilidad: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "message", "Error al verificar disponibilidad",
                        "success", false
                    ));
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        try {
            boolean exists = authService.existsByEmail(email);
            return ResponseEntity.ok(new CheckFieldResponse(!exists, 
                exists ? "Email ya está en uso" : "Email disponible"));
        } catch (Exception e) {
            logger.error("Error al verificar email: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "message", "Error al verificar email",
                        "success", false
                    ));
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        try {
            // Validaciones adicionales para el frontend
            if (username == null || username.trim().length() < 3) {
                return ResponseEntity.ok(new CheckFieldResponse(false, 
                    "Username debe tener al menos 3 caracteres"));
            }
            
            if (username.trim().length() > 20) {
                return ResponseEntity.ok(new CheckFieldResponse(false, 
                    "Username no puede exceder 20 caracteres"));
            }
            
            if (!username.matches("^[a-zA-Z0-9._-]+$")) {
                return ResponseEntity.ok(new CheckFieldResponse(false, 
                    "Username solo puede contener letras, números, puntos, guiones y guiones bajos"));
            }
            
            boolean exists = authService.existsByUsername(username.toLowerCase().trim());
            return ResponseEntity.ok(new CheckFieldResponse(!exists, 
                exists ? "Username ya está en uso" : "Username disponible"));
        } catch (Exception e) {
            logger.error("Error al verificar username: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "message", "Error al verificar username",
                        "success", false
                    ));
        }
    }

    // ==========================================================
    // DTO INTERNO
    // ==========================================================

    public static class CheckFieldResponse {
        private boolean available;
        private String message;

        public CheckFieldResponse(boolean available, String message) {
            this.available = available;
            this.message = message;
        }

        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    // ==========================================================
    // ENDPOINTS DE VERIFICACIÓN
    // ==========================================================
    
    @PostMapping("/verificar-email")
    public ResponseEntity<?> verificarEmail(@Valid @RequestBody VerificacionRequest.Email request) {
        logger.info("Verificando email con código: {} para: {}", request.getCodigo(), request.getEmail());
        
        try {
            // Intentar verificar desde registro temporal usando token
            // Necesitamos obtener el token desde el email primero
            boolean verificado = false;
            String tokenFromEmail = null; // En producción esto debe venir del frontend
            
            if (tokenFromEmail != null) {
                verificado = registroTemporalService.verificarCodigoEmail(tokenFromEmail, request.getCodigo());
            }
            
            if (verificado) {
                logger.info("Email verificado exitosamente desde registro temporal");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email verificado correctamente"
                ));
            } else {
                // Si no funciona, intentar con el servicio tradicional
                VerificacionResponse.General legacyResponse = verificacionService.verificarEmail(request);
                return legacyResponse.isSuccess() ? ResponseEntity.ok(legacyResponse) : ResponseEntity.badRequest().body(legacyResponse);
            }
            
        } catch (Exception e) {
            logger.error("Error al verificar email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error del servidor al verificar email"
                ));
        }
    }
    
    @PostMapping("/reenviar-email")
    public ResponseEntity<?> reenviarVerificacionEmail(@Valid @RequestBody VerificacionRequest.ReenviarEmail request) {
        logger.info("Reenviando verificación de email para: {}", request.getEmail());
        
        try {
            // Intentar reenviar desde registro temporal usando token
            boolean reenviado = false;
            String tokenFromEmail = null; // En producción esto debe venir del frontend
            
            if (tokenFromEmail != null) {
                reenviado = registroTemporalService.reenviarCodigoEmail(tokenFromEmail);
            }
            
            if (reenviado) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Código de email reenviado correctamente"
                ));
            } else {
                // Si no funciona, intentar con el servicio tradicional
                VerificacionResponse.Envio legacyResponse = verificacionService.reenviarVerificacionEmail(request);
                return legacyResponse.isSuccess() ? ResponseEntity.ok(legacyResponse) : ResponseEntity.badRequest().body(legacyResponse);
            }
            
        } catch (Exception e) {
            logger.error("Error al reenviar verificación de email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error del servidor al reenviar verificación"
                ));
        }
    }
    
    @PostMapping("/verificar-telefono")
    public ResponseEntity<?> verificarTelefono(@Valid @RequestBody VerificacionRequest.Telefono request) {
        logger.info("Verificando teléfono: {}", request.getTelefono());
        
        try {
            // Intentar verificar desde registro temporal usando token
            boolean verificado = false;
            String tokenFromTelefono = null; // En producción esto debe venir del frontend
            
            if (tokenFromTelefono != null) {
                verificado = registroTemporalService.verificarCodigoTelefono(tokenFromTelefono, request.getCodigo());
            }
            
            if (verificado) {
                logger.info("Teléfono verificado exitosamente desde registro temporal: {}", request.getTelefono());
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Teléfono verificado correctamente"
                ));
            } else {
                // Si no funciona, intentar con el servicio tradicional
                VerificacionResponse.General legacyResponse = verificacionService.verificarTelefono(request);
                return legacyResponse.isSuccess() ? ResponseEntity.ok(legacyResponse) : ResponseEntity.badRequest().body(legacyResponse);
            }
            
        } catch (Exception e) {
            logger.error("Error al verificar teléfono: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error del servidor al verificar teléfono"
                ));
        }
    }
    
    @PostMapping("/reenviar-telefono")
    public ResponseEntity<?> reenviarVerificacionTelefono(@Valid @RequestBody VerificacionRequest.ReenviarTelefono request) {
        logger.info("Reenviando verificación de teléfono para: {}", request.getTelefono());
        
        try {
            // Intentar reenviar desde registro temporal usando token
            boolean reenviado = false;
            String tokenFromTelefono = null; // En producción esto debe venir del frontend
            
            if (tokenFromTelefono != null) {
                reenviado = registroTemporalService.reenviarCodigoTelefono(tokenFromTelefono);
            }
            
            if (reenviado) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Código de teléfono reenviado correctamente"
                ));
            } else {
                // Si no funciona, intentar con el servicio tradicional
                VerificacionResponse.Envio legacyResponse = verificacionService.reenviarVerificacionTelefono(request);
                return legacyResponse.isSuccess() ? ResponseEntity.ok(legacyResponse) : ResponseEntity.badRequest().body(legacyResponse);
            }
            
        } catch (Exception e) {
            logger.error("Error al reenviar verificación de teléfono: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error del servidor al reenviar verificación"
                ));
        }
    }
    
    // ==========================================================
    // ENDPOINTS ADICIONALES PARA GESTIÓN DE REGISTROS
    // ==========================================================
    
    @GetMapping("/temporal/info/{tokenVerificacion}")
    public ResponseEntity<?> getRegistroTemporalInfo(@PathVariable String tokenVerificacion) {
        logger.info("Consultando información de registro temporal con token: {}", tokenVerificacion);
        
        try {
            RegistroTemporal registro = registroTemporalService.obtenerRegistroPorToken(tokenVerificacion);
            
            if (registro != null && !registro.isExpired()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", registro.getToken(),
                    "email", registro.getEmail(),
                    "telefono", registro.getTelefono(),
                    "emailVerificado", registro.getEmailVerificado(),
                    "telefonoVerificado", registro.getTelefonoVerificado(),
                    "completo", registro.isCompletelyVerified(),
                    "expiraEn", registro.getExpiraEn().toString()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Token no válido o registro expirado"
                ));
            }
            
        } catch (Exception e) {
            logger.error("Error al consultar registro temporal: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error del servidor al consultar registro temporal"
                ));
        }
    }
    
    @PostMapping("/continuar-registro")
    public ResponseEntity<?> continuarRegistro(@RequestParam String telefono) {
        logger.info("Solicitando continuación de registro para teléfono: {}", telefono);
        
        try {
            // Buscar usuario por teléfono
            Usuario usuario = authService.findByTelefono(telefono);
            if (usuario == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "message", "No se encontró un registro con este teléfono",
                        "success", false
                    ));
            }
            
            if (usuario.getTelefonoVerificado() && usuario.getEmailVerificado()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "message", "Este usuario ya está completamente verificado",
                        "success", false
                    ));
            }
            
            // Reenviar códigos de verificación
            VerificacionResponse.Envio emailResponse = null;
            VerificacionResponse.Envio telefonoResponse = null;
            
            if (!usuario.getEmailVerificado()) {
                VerificacionRequest.ReenviarEmail emailRequest = new VerificacionRequest.ReenviarEmail();
                emailRequest.setEmail(usuario.getEmail());
                emailResponse = verificacionService.reenviarVerificacionEmail(emailRequest);
            }
            
            if (!usuario.getTelefonoVerificado()) {
                VerificacionRequest.ReenviarTelefono telefonoRequest = new VerificacionRequest.ReenviarTelefono();
                telefonoRequest.setTelefono(usuario.getTelefono());
                telefonoResponse = verificacionService.reenviarVerificacionTelefono(telefonoRequest);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Se han reenviado los códigos de verificación pendientes",
                "usuario", Map.of(
                    "id", usuario.getId(),
                    "email", usuario.getEmail(),
                    "telefono", usuario.getTelefono(),
                    "emailVerificado", usuario.getEmailVerificado(),
                    "telefonoVerificado", usuario.getTelefonoVerificado()
                )
            ));
            
        } catch (Exception e) {
            logger.error("Error al continuar registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "message", "Error del servidor al procesar solicitud",
                    "success", false
                ));
        }
    }
    
    // ==========================================================
    // ENDPOINTS DE REGISTRO POR PASOS
    // ==========================================================
    
    @PostMapping("/paso1-validar")
    public ResponseEntity<?> validarPaso1(@Valid @RequestBody RegistroPasosRequest.Paso1DatosPersonales request) {
        logger.info("Validando paso 1 para username: {}", request.getUsername());
        
        try {
            RegistroPasosRequest.Paso1Response response = registroPasosService.validarPaso1(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error al validar paso 1: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error del servidor al validar datos"
                ));
        }
    }
    
    @PostMapping("/paso2-crear-cuenta")
    public ResponseEntity<?> crearCuentaPaso2(@Valid @RequestBody RegistroPasosRequest.Paso2Contacto request) {
        logger.info("Creando cuenta paso 2 para: {} ({})", request.getEmail(), request.getUsername());
        
        try {
            RegistroPasosRequest.Paso2Response response = registroPasosService.crearCuentaYEnviarVerificaciones(request);
            
            if (response.isSuccess()) {
                logger.info("Cuenta creada exitosamente: {} (ID: {})", request.getEmail(), response.getUsuarioId());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error al crear cuenta paso 2: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error del servidor al crear cuenta"
                ));
        }
    }
    
    @GetMapping("/paso-info")
    public ResponseEntity<?> getInfoParaPasos() {
        try {
            return ResponseEntity.ok(Map.of(
                "paises", RegistroPasosRequest.PAISES,
                "generos", Map.of(
                    "M", "Masculino",
                    "F", "Femenino", 
                    "Otro", "Otro"
                ),
                "passwordRequirements", Map.of(
                    "minLength", 8,
                    "requireUppercase", true,
                    "requireLowercase", true,
                    "requireNumbers", true,
                    "mensaje", "Mínimo 8 caracteres, una mayúscula, una minúscula y un número"
                )
            ));
        } catch (Exception e) {
            logger.error("Error al obtener info de pasos: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message", "Error al cargar información",
                    "success", false
                ));
        }
    }
}
