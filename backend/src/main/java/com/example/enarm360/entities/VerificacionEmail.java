package com.example.enarm360.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "verificacion_email")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionEmail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 6)
    private String codigo;
    
    @Column(nullable = false, length = 150)
    private String email;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @CreationTimestamp
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
    
    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;
    
    @Builder.Default
    @Column(name = "verificado", nullable = false)
    private Boolean verificado = false;
    
    @Column(name = "verificado_en")
    private LocalDateTime verificadoEn;
    
    @Builder.Default
    @Column(name = "intentos", nullable = false)
    private Integer intentos = 0;
    
    // Métodos de utilidad
    
    /**
     * Verifica si el código ha expirado
     */
    public boolean hasExpirado() {
        return LocalDateTime.now().isAfter(expiraEn);
    }
    
    /**
     * Verifica si el código es válido (no expirado y no verificado ya)
     */
    public boolean isValido() {
        return !hasExpirado() && !verificado;
    }
    
    /**
     * Marca el código como verificado
     */
    public void marcarComoVerificado() {
        this.verificado = true;
        this.verificadoEn = LocalDateTime.now();
    }
    
    /**
     * Incrementa el contador de intentos
     */
    public void incrementarIntentos() {
        this.intentos++;
    }
    
    /**
     * Verifica si se han excedido los intentos máximos
     */
    public boolean hasExcedidoIntentos() {
        return intentos >= 3; // Máximo 3 intentos
    }
    
    /**
     * Valida que el código ingresado coincida
     */
    public boolean validarCodigo(String codigoIngresado) {
        return this.codigo.equals(codigoIngresado);
    }
}
