package com.example.enarm360.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_temporal")
@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroTemporal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 36)
    private String token; // UUID para identificar la sesión
    
    // Datos del usuario
    @Column(nullable = false, length = 120)
    private String nombre;
    
    @Column(length = 120)
    private String apellidos;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    
    @Column(nullable = false, unique = true, length = 20)
    private String telefono;
    
    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;
    
    @Column(name = "pais_nacimiento", nullable = false, length = 100)
    private String paisNacimiento;
    
    @Column(nullable = false, length = 10)
    private String genero;
    
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;
    
    // Estado de verificación
    @Builder.Default
    @Column(name = "email_verificado", nullable = false)
    private Boolean emailVerificado = false;
    
    @Builder.Default
    @Column(name = "telefono_verificado", nullable = false)
    private Boolean telefonoVerificado = false;
    
    // Códigos de verificación embebidos
    @Column(name = "codigo_email", length = 6)
    private String codigoEmail;
    
    @Column(name = "codigo_telefono", length = 6)
    private String codigoTelefono;
    
    @Column(name = "codigo_email_expira")
    private LocalDateTime codigoEmailExpira;
    
    @Column(name = "codigo_telefono_expira")
    private LocalDateTime codigoTelefonoExpira;
    
    // Control temporal
    @CreationTimestamp
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
    
    @Builder.Default
    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn = LocalDateTime.now().plusHours(24);
    
    // Métodos de utilidad
    
    /**
     * Verifica si el registro temporal ha expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiraEn);
    }
    
    /**
     * Verifica si ambas verificaciones están completadas
     */
    public boolean isCompletelyVerified() {
        return emailVerificado && telefonoVerificado;
    }
    
    /**
     * Verifica si el código de email es válido y no ha expirado
     */
    public boolean isEmailCodeValid(String codigo) {
        return codigoEmail != null && 
               codigoEmail.equals(codigo) && 
               codigoEmailExpira != null && 
               LocalDateTime.now().isBefore(codigoEmailExpira);
    }
    
    /**
     * Verifica si el código de teléfono es válido y no ha expirado
     */
    public boolean isTelefonoCodeValid(String codigo) {
        return codigoTelefono != null && 
               codigoTelefono.equals(codigo) && 
               codigoTelefonoExpira != null && 
               LocalDateTime.now().isBefore(codigoTelefonoExpira);
    }
    
    /**
     * Genera y establece un nuevo código de email
     */
    public void generateEmailCode() {
        this.codigoEmail = String.format("%06d", (int)(Math.random() * 1000000));
        this.codigoEmailExpira = LocalDateTime.now().plusHours(24);
    }
    
    /**
     * Genera y establece un nuevo código de teléfono
     */
    public void generateTelefonoCode() {
        this.codigoTelefono = String.format("%06d", (int)(Math.random() * 1000000));
        this.codigoTelefonoExpira = LocalDateTime.now().plusMinutes(10);
    }
    
    /**
     * Marca el email como verificado
     */
    public void markEmailAsVerified() {
        this.emailVerificado = true;
        this.codigoEmail = null;
        this.codigoEmailExpira = null;
    }
    
    /**
     * Marca el teléfono como verificado
     */
    public void markTelefonoAsVerified() {
        this.telefonoVerificado = true;
        this.codigoTelefono = null;
        this.codigoTelefonoExpira = null;
    }
}