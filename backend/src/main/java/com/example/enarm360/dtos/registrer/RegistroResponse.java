package com.example.enarm360.dtos.registrer;

import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroResponse {
    
    private Long id;
    private String nombre;
    private String apellidos;
    private String email;
    private String username;
    private String telefono;
    private boolean activo;
    private LocalDateTime creadoEn;
    private String mensaje;
    private boolean success;
    
    // Estados de verificación
    private boolean emailVerificado;
    private boolean telefonoVerificado;
    
    // Información adicional para el proceso de verificación
    private boolean requiereVerificacion;
    private String tokenEmail; // Solo para desarrollo/debug, no enviar en producción
    private String tokenVerificacion; // Token para el sistema de registro temporal
}
