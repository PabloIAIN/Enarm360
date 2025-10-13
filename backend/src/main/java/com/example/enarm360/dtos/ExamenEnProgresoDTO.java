package com.example.enarm360.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamenEnProgresoDTO {
    private Long intentoId;
    private String tipoExamen;
    private String nombreExamen;
    private Integer totalPreguntas;
    private Integer preguntaActual;
    private Integer tiempoTranscurrido;
    private Boolean pausado;
    private LocalDateTime iniciadoEn;
}
