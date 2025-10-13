package com.example.enarm360.entities;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "intento_examen")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntentoExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer correctas = 0;
    private Integer incorrectas=0;
    private Integer enBlanco=0;

    @Column(name = "duracion_seg")
    private Integer duracionSeg;

    @Column(name = "iniciado_en")
    private LocalDateTime iniciadoEn;

    @Column(name = "finalizado_en")
    private LocalDateTime finalizadoEn;

    @Column(name = "puntaje_total")
    private Double puntajeTotal;

    // Nuevos campos para persistencia de progreso
    @Column(name = "estado")
    @Builder.Default
    private String estado = "EN_PROGRESO"; // EN_PROGRESO, FINALIZADO, ABANDONADO

    @Column(name = "pregunta_actual")
    @Builder.Default
    private Integer preguntaActual = 0;

    @Column(name = "tiempo_transcurrido")
    @Builder.Default
    private Integer tiempoTranscurrido = 0;

    @Column(name = "pausado")
    @Builder.Default
    private Boolean pausado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examen_id")
    private Examen examen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

     @OneToMany(mappedBy = "intentoExamen", cascade = CascadeType.ALL, orphanRemoval = true)
     @Builder.Default
    private java.util.List<IntentoPregunta> preguntas = new java.util.ArrayList<>();
}
