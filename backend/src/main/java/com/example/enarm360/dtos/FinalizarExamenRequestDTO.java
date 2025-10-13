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
public class FinalizarExamenRequestDTO {

    private Long intentoId;
    private Integer duracionSeg; // Tiempo total en segundos
    private List<RespuestaUsuarioDTO> respuestas;
}
