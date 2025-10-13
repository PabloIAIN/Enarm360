package com.example.enarm360.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "examen")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Examen {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "tiempo_limite_min")
    private Integer tiempoLimiteMin;
    
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
    
    @Column(name = "creado_por")
    private Long creadoPor;
    
    @Column(name = "especialidad_id")
    private Long especialidadId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_examen_id")
    @JsonIgnoreProperties("examenes")
    private TipoExamen tipoExamen;
    
    @OneToMany(mappedBy = "examen", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    @JsonManagedReference
    @Builder.Default
    private List<ExamenPregunta> examenPreguntas = new ArrayList<>();
    
    @OneToMany(mappedBy = "examen", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    @JsonIgnoreProperties("examen")
    @Builder.Default
    private List<ExamenCaso> examenCasos = new ArrayList<>();
    
    @OneToMany(mappedBy = "examen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("examen")
    @Builder.Default
    private List<IntentoExamen> intentos = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
    }
    
    // ========================================
    // MÉTODOS TRANSIENT REQUERIDOS
    // ========================================
    
    /**
     * Total de reactivos (preguntas directas)
     */
    @Transient
    public Integer getTotalReactivos() {
        return examenPreguntas != null ? examenPreguntas.size() : 0;
    }
    
    /**
     * Total de casos clínicos
     */
    @Transient
    public Integer getTotalCasos() {
        return examenCasos != null ? examenCasos.size() : 0;
    }
    
    /**
     * Total de preguntas individuales (reactivos + preguntas de casos)
     */
    @Transient
    public Integer getTotalPreguntas() {
        int preguntasReactivos = getTotalReactivos();
        int preguntasCasos = 0;
        
        if (examenCasos != null) {
            for (ExamenCaso ec : examenCasos) {
                preguntasCasos += ec.getCantidadPreguntas();
            }
        }
        
        return preguntasReactivos + preguntasCasos;
    }
}
