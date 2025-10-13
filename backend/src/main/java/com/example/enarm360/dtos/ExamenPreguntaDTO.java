package com.example.enarm360.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamenPreguntaDTO {
    private Long id;
    private Integer orden;
    private Double puntaje;
    private Long examenId;
    

    // Identificadores de origen
   
    private ReactivoDTO reactivo;        
    private PreguntaCasoDTO preguntaCaso; 

    // Tipo de pregunta (REACTIVO o CASO)
    private String tipo;

    // Datos comunes
    private String enunciado;
    private String imagen;

    // Opciones (A–D, para reactivos o preguntas de caso clínico)
    private String respuestaA;
    private String respuestaB;
    private String respuestaC;
    private String respuestaD;

    // Datos extra (solo revisión)
    private String respuestaCorrecta;
    private String retroalimentacion;
}
