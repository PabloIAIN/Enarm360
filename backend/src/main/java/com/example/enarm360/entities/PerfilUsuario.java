package com.example.enarm360.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "perfil_usuario")
@DynamicUpdate
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilUsuario {
    
    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;
    
    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(length = 80)
    private String pais;
    
    @Builder.Default
    @Column(length = 64)
    private String tz = "America/Monterrey";
    
    // Nuevos campos de información personal
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
    
    @Column(length = 10)
    private String genero; // 'M', 'F', 'Otro'
    
    // Información académica
    @Column(length = 150)
    private String universidad;
    
    @Column(name = "anio_graduacion")
    private Integer anioGraduacion;
    
    @Column(name = "numero_titulo", length = 100)
    private String numeroTitulo;
    
    @Column(name = "especialidad_interes", length = 100)
    private String especialidadInteres;
    
    // Configuraciones de usuario
    @Builder.Default
    @Column(name = "recibir_notificaciones")
    private Boolean recibirNotificaciones = true;
    
    @Builder.Default
    @Column(name = "recibir_newsletters")
    private Boolean recibirNewsletters = false;
    
    @Builder.Default
    @Column(name = "perfil_publico")
    private Boolean perfilPublico = true;
    
    // Configuración de privacidad
    @Builder.Default
    @Column(name = "mostrar_email")
    private Boolean mostrarEmail = false;
    
    @Builder.Default
    @Column(name = "mostrar_telefono")
    private Boolean mostrarTelefono = false;
    
    @Builder.Default
    @Column(name = "mostrar_universidad")
    private Boolean mostrarUniversidad = true;
    
    @Builder.Default
    @Column(name = "permitir_mensajes")
    private Boolean permitirMensajes = true;
    
    @Builder.Default
    @Column(name = "mostrar_estadisticas")
    private Boolean mostrarEstadisticas = true;
    
    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
    
    // Relación con Usuario
    @OneToOne
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}