package com.example.enarm360.dtos.registrer;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * DTOs para el registro por pasos
 */
public class RegistroPasosRequest {
    
    // ============================================================
    // PASO 1: DATOS PERSONALES + CONTRASEÑA
    // ============================================================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Paso1DatosPersonales {
        
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
        private String nombre;
        
        @Size(max = 120, message = "Los apellidos no pueden exceder 120 caracteres")
        private String apellidos;
        
        @NotBlank(message = "El username es obligatorio")
        @Size(min = 3, max = 20, message = "El username debe tener entre 3 y 20 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username solo puede contener letras, números, puntos, guiones y guiones bajos")
        private String username;
        
        @NotBlank(message = "El país de nacimiento es obligatorio")
        @Size(max = 80, message = "El país no puede exceder 80 caracteres")
        private String paisNacimiento;
        
        @NotBlank(message = "El género es obligatorio")
        @Pattern(regexp = "^(M|F|Otro)$", message = "Género debe ser M, F u Otro")
        private String genero;
        
        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser en el pasado")
        private LocalDate fechaNacimiento;
        
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
        private String contrasena;
        
        @NotBlank(message = "Confirmar contraseña es obligatorio")
        private String confirmarContrasena;
        
        @AssertTrue(message = "Las contraseñas no coinciden")
        public boolean isPasswordMatching() {
            return contrasena != null && contrasena.equals(confirmarContrasena);
        }
    }
    
    // ============================================================
    // PASO 2: EMAIL + TELÉFONO
    // ============================================================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Paso2Contacto {
        
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        @Size(max = 150, message = "El email no puede exceder 150 caracteres")
        private String email;
        
        @NotBlank(message = "El teléfono es obligatorio")
        @Size(min = 10, max = 20, message = "El teléfono debe tener entre 10 y 20 caracteres")
        @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]+$", message = "Formato de teléfono inválido")
        private String telefono;
        
        // Datos del paso anterior (para crear cuenta completa)
        private String nombre;
        private String apellidos;
        private String username;
        private String paisNacimiento;
        private String genero;
        private LocalDate fechaNacimiento;
        private String contrasena;
    }
    
    // ============================================================
    // RESPUESTAS POR PASO
    // ============================================================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Paso1Response {
        private boolean success;
        private String mensaje;
        private boolean usernameDisponible;
        private String siguientePaso;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Paso2Response {
        private boolean success;
        private String mensaje;
        private Long usuarioId;
        private String tokenEmail;
        private String codigoTelefono;
        private String siguientePaso;
        private boolean requiereVerificacionEmail;
        private boolean requiereVerificacionTelefono;
    }
    
    // ============================================================
    // CONSTANTES PARA EL FRONTEND
    // ============================================================
    
    public static final String[] GENEROS = {"M", "F", "Otro"};
    
    public static final String[] PAISES = {
        "México", "Estados Unidos", "España", "Colombia", "Argentina",
        "Chile", "Perú", "Venezuela", "Ecuador", "Guatemala", "Costa Rica",
        "Panamá", "Uruguay", "Paraguay", "Bolivia", "República Dominicana",
        "Cuba", "Puerto Rico", "El Salvador", "Honduras", "Nicaragua", "Otro"
    };
}