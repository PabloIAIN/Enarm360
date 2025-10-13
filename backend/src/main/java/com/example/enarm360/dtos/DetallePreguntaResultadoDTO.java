package com.example.enarm360.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePreguntaResultadoDTO {

    private Integer orden;
    private String tipo; // "REACTIVO" o "CASO"

    // Contenido de la pregunta
    private String pregunta;
    private String respuestaA;
    private String respuestaB;
    private String respuestaC;
    private String respuestaD;
    private String imagenPregunta;

    // Caso de estudio (si aplica)
    private String casoEstudioContenido;
    private String imagenCaso;

    // Respuestas y resultados
    private String respuestaUsuario; // "a", "b", "c", "d" o null
    private String respuestaCorrecta;
    private Boolean esCorrecta;
    private String retroalimentacion;

    // Tiempo
    private Integer tiempoSeg;
}
