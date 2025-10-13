package com.example.enarm360.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder   
@NoArgsConstructor
@AllArgsConstructor
public class ReactivoDTO {
    private Long id;
    private String pregunta;
    private String respuestaA;
    private String respuestaB;
    private String respuestaC;
    private String respuestaD;
    private String retroalimentacion;
}
