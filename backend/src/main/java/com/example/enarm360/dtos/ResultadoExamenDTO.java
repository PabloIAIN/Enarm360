package com.example.enarm360.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoExamenDTO {

    private Long intentoId;
    private Long examenId;
    private String tipoExamen;

    // Estadísticas generales
    private Integer totalPreguntas;
    private Integer correctas;
    private Integer incorrectas;
    private Integer enBlanco;
    private Double porcentajeAciertos;
    private Integer duracionSeg;
    private LocalDateTime iniciadoEn;
    private LocalDateTime finalizadoEn;

    // Detalles de cada pregunta con retroalimentación
    private List<DetallePreguntaResultadoDTO> preguntas;
}
