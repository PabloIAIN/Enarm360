package com.example.enarm360.dtos.registrer;

import lombok.*;
import java.time.LocalDateTime;

public class VerificacionResponse {
    
    // ============================================================
    // RESPUESTA GENERAL DE VERIFICACIÓN
    // ============================================================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class General {
        
        private boolean success;
        private String mensaje;
        private boolean verificado;
        private LocalDateTime verificadoEn;
        private int intentosRestantes;
    }
    
    // ============================================================
    // RESPUESTA DE ENVÍO DE VERIFICACIÓN
    // ============================================================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Envio {
        
        private boolean success;
        private String mensaje;
        private String tipoEnvio; // "email" o "sms"
        private LocalDateTime enviadoEn;
        private LocalDateTime expiraEn;
        private boolean puedeReenviar;
        private int tiempoEsperaMinutos; // Para reenvío
        private int enviosRestantes;
    }
    
    // ============================================================
    // RESPUESTA DE ESTADO DE VERIFICACIONES DE USUARIO
    // ============================================================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Estado {
        
        private boolean emailVerificado;
        private boolean telefonoVerificado;
        private boolean puedeAccederCuenta;
        private String siguientePaso;
        
        // Detalles de verificación de email
        private VerificacionEmailDetalle emailDetalle;
        
        // Detalles de verificación de teléfono
        private VerificacionTelefonoDetalle telefonoDetalle;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class VerificacionEmailDetalle {
            private boolean pendiente;
            private LocalDateTime enviadoEn;
            private LocalDateTime expiraEn;
            private int intentosRestantes;
            private boolean puedeReenviar;
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class VerificacionTelefonoDetalle {
            private boolean pendiente;
            private LocalDateTime enviadoEn;
            private LocalDateTime expiraEn;
            private int intentosRestantes;
            private boolean puedeReenviar;
            private int tiempoEsperaMinutos;
        }
    }
}