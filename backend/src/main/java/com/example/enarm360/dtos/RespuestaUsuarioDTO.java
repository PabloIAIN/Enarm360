package com.example.enarm360.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaUsuarioDTO {

    private Integer orden; // Orden de la pregunta en el examen
    private String respuesta; // "a", "b", "c", "d" o null si no respondió
    private Integer tiempoSeg; // Tiempo que tardó en esta pregunta
}
