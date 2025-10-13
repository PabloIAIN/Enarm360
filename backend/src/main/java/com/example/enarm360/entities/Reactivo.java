package com.example.enarm360.entities;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;



@Entity
@Table(name = "reactivos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reactivo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La pregunta es obligatoria")
    private String pregunta;
    
    @Column(name = "respuesta_a", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La respuesta A es obligatoria")
    private String respuestaA;
    
    @Column(name = "respuesta_b", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La respuesta B es obligatoria")
    private String respuestaB;
    
    @Column(name = "respuesta_c", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La respuesta C es obligatoria")
    private String respuestaC;
    
    @Column(name = "respuesta_d", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La respuesta D es obligatoria")
    private String respuestaD;
    
    @Column(columnDefinition = "TEXT")
    private String retroalimentacion;
    
    @Column(name = "respuesta_correcta", nullable = false, length = 1)
    @Pattern(regexp = "[abcd]", message = "La respuesta correcta debe ser a, b, c o d")
    private String respuestaCorrecta;
    
    // Relaciones
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dificultad_id", nullable = false)
    @JsonIgnoreProperties({"reactivos", "preguntasCasos"})
    private Dificultad dificultad;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id", nullable = false)
    @JsonIgnoreProperties({"reactivos", "claves", "preguntasCasos"})
    private Especialidad especialidad;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnoreProperties({"reactivos", "casosEstudio", "preguntasCasos"})
    private Usuario usuario;
    
    @Column(name = "fecha_creacion")
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

     @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examen_id")
    private Examen examen;


}
