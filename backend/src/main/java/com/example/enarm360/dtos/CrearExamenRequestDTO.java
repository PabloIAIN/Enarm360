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
public class CrearExamenRequestDTO {

    private String tipoExamen; // "RAPIDO", "FILTRADO", "ENARM"

    // Para examen filtrado
    private Integer cantidadReactivos;
    private Integer cantidadCasos;
    private List<Long> especialidadIds;
    private Long dificultadId;
}
