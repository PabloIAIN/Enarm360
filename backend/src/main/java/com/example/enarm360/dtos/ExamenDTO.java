package com.example.enarm360.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamenDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer tiempoLimiteMin;
    private LocalDateTime creadoEn;
    private Long creadoPor;
    
    // Tipo correcto: Long (no Especialidad)
    
     private Long especialidadId;
    // Tipo de examen
    private TipoExamenDTO tipoExamen;
    
    // Contenido del examen
    private List<ExamenPreguntaDTO> preguntas;
    private List<ExamenCasoDTO> casos;
    
    // Estadísticas
    private Integer totalReactivos;
    private Integer totalCasos;
    private Integer totalPreguntas;
}