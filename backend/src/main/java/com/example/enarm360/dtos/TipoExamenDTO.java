package com.example.enarm360.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoExamenDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String codigo;
    private Boolean permiteCasosClinicos;
    private Boolean permitePreguntasDirectas;
    private Boolean tieneTiempoLimite;
    private Boolean esCalificable;
    private Integer ordenVisualizacion;
    private Boolean activo;
}