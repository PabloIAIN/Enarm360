package com.example.enarm360.Mappers;

import com.example.enarm360.dtos.CasoEstudioDTO;
import com.example.enarm360.entities.CasoEstudio;

import java.util.Collections;
import java.util.stream.Collectors;

public class CasoEstudioMapper {
    
    /**
     * Para mostrar en examen (sin respuestas correctas)
     */
    public static CasoEstudioDTO toDTOParaExamen(CasoEstudio caso) {
        if (caso == null) return null;
        
        return CasoEstudioDTO.builder()
                .id(caso.getId())
                // .idCaso(caso.getIdCaso())
                .caso(caso.getCaso())
                .imagen(caso.getImagen())
              
                .fechaHora(caso.getFechaHora())
                .preguntas(caso.getPreguntas() == null 
                    ? Collections.emptyList()
                    : caso.getPreguntas().stream()
                        .map(PreguntaCasoMapper::toDTOParaExamen)
                        .collect(Collectors.toList()))
                .cantidadPreguntas(caso.getCantidadPreguntas())
                .build();
    }
    
    /**
     * Para revisión (con respuestas correctas)
     */
    public static CasoEstudioDTO toDTOParaRevision(CasoEstudio caso) {
        if (caso == null) return null;
        
        return CasoEstudioDTO.builder()
                .id(caso.getId())
                // .idCaso(caso.getIdCaso())
                .caso(caso.getCaso())
                .imagen(caso.getImagen())
               
                .fechaHora(caso.getFechaHora())
                .preguntas(caso.getPreguntas() == null 
                    ? Collections.emptyList()
                    : caso.getPreguntas().stream()
                        .map(PreguntaCasoMapper::toDTOParaRevision)
                        .collect(Collectors.toList()))
                .cantidadPreguntas(caso.getCantidadPreguntas())
                .build();
    }
    
    /**
     * Para gestión/admin (incluye todo)
     */
    public static CasoEstudioDTO toDTO(CasoEstudio caso) {
        return toDTOParaRevision(caso);
    }
    
    /**
     * Básico sin preguntas (para listados)
     */
    public static CasoEstudioDTO toDTOBasico(CasoEstudio caso) {
        if (caso == null) return null;
        
        return CasoEstudioDTO.builder()
                .id(caso.getId())
                // .idCaso(caso.getIdCaso())
                .caso(caso.getCaso())
                .imagen(caso.getImagen())
                .fechaHora(caso.getFechaHora())
                .cantidadPreguntas(caso.getCantidadPreguntas())
                .build();
    }
}