package com.example.enarm360.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecuperarProgresoDTO {
    private IniciarExamenResponseDTO examenData;
    private Integer preguntaActual;
    private Integer tiempoTranscurrido;
    private Boolean pausado;
    private Map<Integer, String> respuestas; // orden -> respuesta
}
