package com.example.enarm360.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IniciarExamenResponseDTO {

    private Long examenId;
    private Long intentoId;
    private String tipoExamen;
    private Integer totalPreguntas;
    private Integer tiempoLimiteMin;
    private List<PreguntaExamenDTO> preguntas;
}
