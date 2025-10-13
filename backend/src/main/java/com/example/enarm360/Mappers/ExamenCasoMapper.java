package com.example.enarm360.Mappers;

import com.example.enarm360.dtos.ExamenCasoDTO;
import com.example.enarm360.entities.ExamenCaso;

public class ExamenCasoMapper {
    
    /**
     * Para mostrar en examen (sin respuestas correctas)
     */
    public static ExamenCasoDTO toDTOParaExamen(ExamenCaso examenCaso) {
        if (examenCaso == null) return null;
        
        return ExamenCasoDTO.builder()
                .id(examenCaso.getId())
                .orden(examenCaso.getOrden())
               
                .casoEstudio(CasoEstudioMapper.toDTOParaExamen(examenCaso.getCasoEstudio()))
                .cantidadPreguntas(examenCaso.getCantidadPreguntas())
                .build();
    }
    
    /**
     * Para revisión (con respuestas correctas)
     */
    public static ExamenCasoDTO toDTOParaRevision(ExamenCaso examenCaso) {
        if (examenCaso == null) return null;
        
        return ExamenCasoDTO.builder()
                .id(examenCaso.getId())
                .orden(examenCaso.getOrden())
               
                .casoEstudio(CasoEstudioMapper.toDTOParaRevision(examenCaso.getCasoEstudio()))
                .cantidadPreguntas(examenCaso.getCantidadPreguntas())
                .build();
    }
    
    /**
     * Para gestión/admin
     */
    public static ExamenCasoDTO toDTO(ExamenCaso examenCaso) {
        return toDTOParaRevision(examenCaso);
    }
}