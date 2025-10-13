package com.example.enarm360.Mappers;

import com.example.enarm360.dtos.*;
import com.example.enarm360.entities.*;

public class ExamenPreguntaMapper {

    /**
     * Mapper para mostrar examen (sin respuestas correctas)
     */
    public static ExamenPreguntaDTO toDTOParaExamen(ExamenPregunta ep) {
        if (ep == null) return null;

        ExamenPreguntaDTO.ExamenPreguntaDTOBuilder dto = ExamenPreguntaDTO.builder()
                .id(ep.getId())
                .orden(ep.getOrden())
                .puntaje(ep.getPuntaje())
                .examenId(ep.getExamen() != null ? ep.getExamen().getId() : null);

        if (ep.getReactivo() != null) {
            dto.tipo("REACTIVO")
               .reactivo(ReactivoDTO.builder()
                        .id(ep.getReactivo().getId())
                        .pregunta(ep.getReactivo().getPregunta())
                        .respuestaA(ep.getReactivo().getRespuestaA())
                        .respuestaB(ep.getReactivo().getRespuestaB())
                        .respuestaC(ep.getReactivo().getRespuestaC())
                        .respuestaD(ep.getReactivo().getRespuestaD())
                        .retroalimentacion(null) // 🔒 no se incluye en examen normal
                        .build()
               )
               .enunciado(ep.getReactivo().getPregunta());
        } else if (ep.getPreguntaCaso() != null) {
            dto.tipo("CASO")
               .preguntaCaso(PreguntaCasoDTO.builder()
                        .id(ep.getPreguntaCaso().getId())
                        .pregunta(ep.getPreguntaCaso().getPregunta())
                        .respuestaA(ep.getPreguntaCaso().getRespuestaA())
                        .respuestaB(ep.getPreguntaCaso().getRespuestaB())
                        .respuestaC(ep.getPreguntaCaso().getRespuestaC())
                        .respuestaD(ep.getPreguntaCaso().getRespuestaD())
                        .respuestaCorrecta(null)   // 🔒 ocultar en examen normal
                        .retroalimentacion(null)   // 🔒 ocultar en examen normal
                        .imagen(ep.getPreguntaCaso().getImagen())
                        .especialidadId(null)  // Don't access lazy entity
                        .dificultad(null)  // Don't access lazy entity
                        .fechaHora(ep.getPreguntaCaso().getFechaHora())
                        .build()
               )
               .enunciado(ep.getPreguntaCaso().getPregunta());
        }

        return dto.build();
    }

    /**
     * Mapper para revisión (incluye respuestas correctas y retroalimentación)
     */
    public static ExamenPreguntaDTO toDTOParaRevision(ExamenPregunta ep) {
        if (ep == null) return null;

        ExamenPreguntaDTO.ExamenPreguntaDTOBuilder dto = ExamenPreguntaDTO.builder()
                .id(ep.getId())
                .orden(ep.getOrden())
                .puntaje(ep.getPuntaje())
                .examenId(ep.getExamen() != null ? ep.getExamen().getId() : null);

        if (ep.getReactivo() != null) {
            dto.tipo("REACTIVO")
               .reactivo(ReactivoDTO.builder()
                        .id(ep.getReactivo().getId())
                        .pregunta(ep.getReactivo().getPregunta())
                        .respuestaA(ep.getReactivo().getRespuestaA())
                        .respuestaB(ep.getReactivo().getRespuestaB())
                        .respuestaC(ep.getReactivo().getRespuestaC())
                        .respuestaD(ep.getReactivo().getRespuestaD())
                        .retroalimentacion(ep.getReactivo().getRetroalimentacion()) // ✅ en revisión sí se incluye
                        .build()
               )
               .enunciado(ep.getReactivo().getPregunta())
               .respuestaCorrecta(ep.getReactivo().getRespuestaCorrecta())
               .retroalimentacion(ep.getReactivo().getRetroalimentacion());
        } else if (ep.getPreguntaCaso() != null) {
            dto.tipo("CASO")
               .preguntaCaso(PreguntaCasoDTO.builder()
                        .id(ep.getPreguntaCaso().getId())
                        .pregunta(ep.getPreguntaCaso().getPregunta())
                        .respuestaA(ep.getPreguntaCaso().getRespuestaA())
                        .respuestaB(ep.getPreguntaCaso().getRespuestaB())
                        .respuestaC(ep.getPreguntaCaso().getRespuestaC())
                        .respuestaD(ep.getPreguntaCaso().getRespuestaD())
                        .respuestaCorrecta(ep.getPreguntaCaso().getRespuestaCorrecta())
                        .retroalimentacion(ep.getPreguntaCaso().getRetroalimentacion())
                        .imagen(ep.getPreguntaCaso().getImagen())
                        .especialidadId(null)  // Don't access lazy entity
                        .dificultad(null)  // Don't access lazy entity
                        .fechaHora(ep.getPreguntaCaso().getFechaHora())
                        .build()
               )
               .enunciado(ep.getPreguntaCaso().getPregunta())
               .respuestaCorrecta(ep.getPreguntaCaso().getRespuestaCorrecta())
               .retroalimentacion(ep.getPreguntaCaso().getRetroalimentacion());
        }

        return dto.build();
    }

    // ----------------------------------------------------------------------
    // Métodos auxiliares usados en ExamenMapper
    // ----------------------------------------------------------------------
    public static ExamenPreguntaDTO toDTODesdeCaso(PreguntaCaso pc, Integer orden, Long examenId) {
        if (pc == null) return null;

        return ExamenPreguntaDTO.builder()
                .id(pc.getId())
                .orden(orden)
                .examenId(examenId)
                .tipo("CASO")
                .preguntaCaso(PreguntaCasoDTO.builder()
                        .id(pc.getId())
                        .pregunta(pc.getPregunta())
                        .respuestaA(pc.getRespuestaA())
                        .respuestaB(pc.getRespuestaB())
                        .respuestaC(pc.getRespuestaC())
                        .respuestaD(pc.getRespuestaD())
                        .build()
                )
                .enunciado(pc.getPregunta())
                .build();
    }

    public static ExamenPreguntaDTO toDTODesdeCasoRevision(PreguntaCaso pc, Integer orden, Long examenId) {
        if (pc == null) return null;

        return ExamenPreguntaDTO.builder()
                .id(pc.getId())
                .orden(orden)
                .examenId(examenId)
                .tipo("CASO")
                .preguntaCaso(PreguntaCasoDTO.builder()
                        .id(pc.getId())
                        .pregunta(pc.getPregunta())
                        .respuestaA(pc.getRespuestaA())
                        .respuestaB(pc.getRespuestaB())
                        .respuestaC(pc.getRespuestaC())
                        .respuestaD(pc.getRespuestaD())
                        .respuestaCorrecta(pc.getRespuestaCorrecta())
                        .retroalimentacion(pc.getRetroalimentacion())
                        .imagen(pc.getImagen())
                        .especialidadId(null)  // Don't access lazy entity
                        .dificultad(null)  // Don't access lazy entity
                        .fechaHora(pc.getFechaHora())
                        .build()
                )
                .enunciado(pc.getPregunta())
                .respuestaCorrecta(pc.getRespuestaCorrecta())
                .retroalimentacion(pc.getRetroalimentacion())
                .build();
    }
}
