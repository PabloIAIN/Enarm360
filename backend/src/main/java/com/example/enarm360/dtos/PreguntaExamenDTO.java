package com.example.enarm360.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreguntaExamenDTO {

    private Long id;
    private Integer orden;
    private String tipo; // "REACTIVO" o "CASO"

    // Para reactivos
    private Long reactivoId;

    // Para preguntas de caso
    private Long preguntaCasoId;
    private Long casoEstudioId;
    private String casoEstudioContenido;
    private String imagenCaso;

    // Contenido de la pregunta
    private String pregunta;
    private String respuestaA;
    private String respuestaB;
    private String respuestaC;
    private String respuestaD;
    private String imagenPregunta;

    // NO se incluye la respuesta correcta ni retroalimentación al iniciar
}
