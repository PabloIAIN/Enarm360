package com.example.enarm360.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CasoEstudioDTO {
    private Long id;
    private String idCaso;
    private String caso;
    private String imagen;
    private Long temaId;
    private LocalDateTime fechaHora;
    private List<PreguntaCasoDTO> preguntas;
    private Integer cantidadPreguntas;
}