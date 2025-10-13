package com.example.enarm360.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tipo_examen")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoExamen {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(unique = true, length = 50)
    private String codigo;
    
    @Column(name = "permite_casos_clinicos")
    private Boolean permiteCasosClinicos = true;
    
    @Column(name = "permite_preguntas_directas")
    private Boolean permitePreguntasDirectas = true;
    
    @Column(name = "tiene_tiempo_limite")
    private Boolean tieneTiempoLimite = true;
    
    @Column(name = "es_calificable")
    private Boolean esCalificable = true;
    
    @Column(name = "orden_visualizacion")
    private Integer ordenVisualizacion;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Column(name = "creado_en")
    private LocalDateTime creadoEn;
    
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
    
    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
        actualizadoEn = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}