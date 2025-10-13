package com.example.enarm360.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreguntaCasoDTO {
    private Long id;
    private String pregunta;
    private String respuestaA;
    private String respuestaB;
    private String respuestaC;
    private String respuestaD;
    private String respuestaCorrecta; // Solo se incluye en revisión
    private String retroalimentacion; // Solo se incluye en revisión
    private String imagen;

    private Long especialidadId;
    private DificultadDTO dificultad;  // ✅ DTO, no entidad

    private LocalDateTime fechaHora;

    // Caso clínico al que pertenece esta pregunta
    private CasoEstudioDTO caso;
}