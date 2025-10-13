package com.example.enarm360.Mappers;

import com.example.enarm360.dtos.PreguntaCasoDTO;
import com.example.enarm360.entities.PreguntaCaso;

public class PreguntaCasoMapper {

    /**
     * Para mostrar en examen (SIN respuesta correcta ni retroalimentación)
     */
    public static PreguntaCasoDTO toDTOParaExamen(PreguntaCaso pregunta) {
        if (pregunta == null) return null;

        return PreguntaCasoDTO.builder()
                .id(pregunta.getId())
                .pregunta(pregunta.getPregunta())
                .respuestaA(pregunta.getRespuestaA())
                .respuestaB(pregunta.getRespuestaB())
                .respuestaC(pregunta.getRespuestaC())
                .respuestaD(pregunta.getRespuestaD())
                // 🔒 No incluimos respuestaCorrecta ni retroalimentacion
                .imagen(pregunta.getImagen())
                .especialidadId(null)  // Don't access lazy entity
                .dificultad(null)  // Don't access lazy entity
                .fechaHora(pregunta.getFechaHora())
                .build();
    }

    /**
     * Para revisión (CON respuesta correcta y retroalimentación)
     */
    public static PreguntaCasoDTO toDTOParaRevision(PreguntaCaso pregunta) {
        if (pregunta == null) return null;

        return PreguntaCasoDTO.builder()
                .id(pregunta.getId())
                .pregunta(pregunta.getPregunta())
                .respuestaA(pregunta.getRespuestaA())
                .respuestaB(pregunta.getRespuestaB())
                .respuestaC(pregunta.getRespuestaC())
                .respuestaD(pregunta.getRespuestaD())
                .respuestaCorrecta(pregunta.getRespuestaCorrecta())
                .retroalimentacion(pregunta.getRetroalimentacion())
                .imagen(pregunta.getImagen())
                .especialidadId(null)  // Don't access lazy entity
                .dificultad(null)  // Don't access lazy entity
                .fechaHora(pregunta.getFechaHora())
                .build();
    }

    /**
     * Para gestión/admin (incluye todo)
     */
    public static PreguntaCasoDTO toDTO(PreguntaCaso pregunta) {
        return toDTOParaRevision(pregunta);
    }
}
