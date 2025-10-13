package com.example.enarm360.dtos.registrer;

import jakarta.validation.constraints.*;
import lombok.*;

public class VerificacionRequest {
    
    // ============================================================
    // VERIFICACIÓN DE EMAIL
    // ============================================================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Email {
        
        @NotBlank(message = "El código es obligatorio")
        @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos")
        @Pattern(regexp = "^[0-9]{6}$", message = "El código debe contener solo números")
        private String codigo;
        
        @NotBlank(message = "El email es obligatorio")
        @jakarta.validation.constraints.Email(message = "Formato de email inválido")
        @Size(max = 150, message = "El email no puede exceder 150 caracteres")
        private String email;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReenviarEmail {
        
        @NotBlank(message = "El email es obligatorio")
        @jakarta.validation.constraints.Email(message = "Formato de email inválido")
        @Size(max = 150, message = "El email no puede exceder 150 caracteres")
        private String email;
    }
    
    // ============================================================
    // VERIFICACIÓN DE TELÉFONO
    // ============================================================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Telefono {
        
        @NotBlank(message = "El código es obligatorio")
        @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos")
        @Pattern(regexp = "^[0-9]{6}$", message = "El código debe contener solo números")
        private String codigo;
        
        @NotBlank(message = "El teléfono es obligatorio")
        @Size(min = 10, max = 20, message = "El teléfono debe tener entre 10 y 20 caracteres")
        @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]+$", message = "Formato de teléfono inválido")
        private String telefono;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReenviarTelefono {
        
        @NotBlank(message = "El teléfono es obligatorio")
        @Size(min = 10, max = 20, message = "El teléfono debe tener entre 10 y 20 caracteres")
        @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]+$", message = "Formato de teléfono inválido")
        private String telefono;
    }
}