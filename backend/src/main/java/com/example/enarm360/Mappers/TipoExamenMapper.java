package com.example.enarm360.Mappers;

import com.example.enarm360.dtos.TipoExamenDTO;
import com.example.enarm360.entities.TipoExamen;

public class TipoExamenMapper {
    
    public static TipoExamenDTO toDTO(TipoExamen tipoExamen) {
        if (tipoExamen == null) return null;
        
        return TipoExamenDTO.builder()
                .id(tipoExamen.getId())
                .nombre(tipoExamen.getNombre())
                .descripcion(tipoExamen.getDescripcion())
                .codigo(tipoExamen.getCodigo())
                .permiteCasosClinicos(tipoExamen.getPermiteCasosClinicos())
                .permitePreguntasDirectas(tipoExamen.getPermitePreguntasDirectas())
                .tieneTiempoLimite(tipoExamen.getTieneTiempoLimite())
                .esCalificable(tipoExamen.getEsCalificable())
                .ordenVisualizacion(tipoExamen.getOrdenVisualizacion())
                .activo(tipoExamen.getActivo())
                .build();
    }
    
    public static TipoExamen toEntity(TipoExamenDTO dto) {
        if (dto == null) return null;
        
        return TipoExamen.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .codigo(dto.getCodigo())
                .permiteCasosClinicos(dto.getPermiteCasosClinicos())
                .permitePreguntasDirectas(dto.getPermitePreguntasDirectas())
                .tieneTiempoLimite(dto.getTieneTiempoLimite())
                .esCalificable(dto.getEsCalificable())
                .ordenVisualizacion(dto.getOrdenVisualizacion())
                .activo(dto.getActivo())
                .build();
    }
}