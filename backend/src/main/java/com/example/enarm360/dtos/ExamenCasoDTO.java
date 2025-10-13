package com.example.enarm360.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamenCasoDTO {
    private Long id;
    private Integer orden;
    private CasoEstudioDTO casoEstudio;
    private Integer cantidadPreguntas;
 
}