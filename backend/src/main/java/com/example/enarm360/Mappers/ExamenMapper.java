package com.example.enarm360.Mappers;

import com.example.enarm360.dtos.*;
import com.example.enarm360.entities.*;

import java.util.List;
import java.util.stream.Collectors;

public class ExamenMapper {

    /**
     * Para mostrar examen (sin respuestas correctas).
     */
    public static ExamenDTO toDTOParaExamen(Examen examen) {
        if (examen == null) return null;

        List<ExamenPreguntaDTO> preguntas = examen.getExamenPreguntas() == null
                ? List.of()
                : examen.getExamenPreguntas().stream()
                        .map(ep -> mapExamenPreguntaParaExamen(ep, examen.getId())) // ✅ Pasar ID
                        .collect(Collectors.toList());

        Long especialidadId = getEspecialidadIdDelExamen(examen);

        return ExamenDTO.builder()
                .id(examen.getId())
                .nombre(examen.getNombre())
                .descripcion(examen.getDescripcion())
                .creadoEn(examen.getCreadoEn())
                .creadoPor(examen.getCreadoPor())
                .tiempoLimiteMin(examen.getTiempoLimiteMin())
                .tipoExamen(TipoExamenMapper.toDTO(examen.getTipoExamen()))
                .preguntas(preguntas)
                .totalReactivos(examen.getTotalReactivos())
                .totalCasos(examen.getTotalCasos())
                .totalPreguntas(examen.getTotalPreguntas())
                .especialidadId(especialidadId)
                .build();
    }

    /**
     * Para revisión (incluye respuestas correctas y retroalimentación).
     */
    public static ExamenDTO toDTOParaRevision(Examen examen) {
        if (examen == null) return null;

        List<ExamenPreguntaDTO> preguntas = examen.getExamenPreguntas() == null
                ? List.of()
                : examen.getExamenPreguntas().stream()
                        .map(ep -> mapExamenPreguntaParaRevision(ep, examen.getId())) // ✅ Pasar ID
                        .collect(Collectors.toList());

        Long especialidadId = getEspecialidadIdDelExamen(examen);

        return ExamenDTO.builder()
                .id(examen.getId())
                .nombre(examen.getNombre())
                .descripcion(examen.getDescripcion())
                .creadoEn(examen.getCreadoEn())
                .creadoPor(examen.getCreadoPor())
                .tiempoLimiteMin(examen.getTiempoLimiteMin())
                .tipoExamen(TipoExamenMapper.toDTO(examen.getTipoExamen()))
                .preguntas(preguntas)
                .totalReactivos(examen.getTotalReactivos())
                .totalCasos(examen.getTotalCasos())
                .totalPreguntas(examen.getTotalPreguntas())
                .especialidadId(especialidadId)
                .build();
    }

    public static ExamenDTO toDTOBasico(Examen examen) {
        if (examen == null) return null;

        Long especialidadId = getEspecialidadIdDelExamen(examen);

        return ExamenDTO.builder()
                .id(examen.getId())
                .nombre(examen.getNombre())
                .descripcion(examen.getDescripcion())
                .creadoEn(examen.getCreadoEn())
                .creadoPor(examen.getCreadoPor())
                .tiempoLimiteMin(examen.getTiempoLimiteMin())
                .tipoExamen(TipoExamenMapper.toDTO(examen.getTipoExamen()))
                .totalReactivos(examen.getTotalReactivos())
                .totalCasos(examen.getTotalCasos())
                .totalPreguntas(examen.getTotalPreguntas())
                .especialidadId(especialidadId)
                .build();
    }

    public static ExamenDTO toDTO(Examen examen) {
        return toDTOParaExamen(examen);
    }

    // ==========================================================
    // Mappers auxiliares - VERSIÓN CORREGIDA
    // ==========================================================

    private static ExamenPreguntaDTO mapExamenPreguntaParaExamen(ExamenPregunta ep, Long examenId) {
        ExamenPreguntaDTO dto = new ExamenPreguntaDTO();
        dto.setId(ep.getId());
        dto.setOrden(ep.getOrden());
        dto.setPuntaje(ep.getPuntaje());
        dto.setExamenId(examenId); // ✅ Usar el ID pasado como parámetro

        if (ep.getReactivo() != null) {
            dto.setTipo("REACTIVO");
            Reactivo r = ep.getReactivo();
            dto.setReactivo(
                new ReactivoDTO(
                    r.getId(),
                    r.getPregunta(),
                    r.getRespuestaA(),
                    r.getRespuestaB(),
                    r.getRespuestaC(),
                    r.getRespuestaD(),
                    null // no incluir retroalimentación en examen normal
                )
            );
        }

        if (ep.getPreguntaCaso() != null) {
            dto.setTipo("CASO");
            PreguntaCaso pc = ep.getPreguntaCaso();

            // Construir el DTO del caso clínico (sin preguntas para evitar recursión)
            CasoEstudioDTO casoDTO = null;
            if (pc.getCasoEstudio() != null) {
                CasoEstudio caso = pc.getCasoEstudio();
                casoDTO = CasoEstudioDTO.builder()
                    .id(caso.getId())
                    .caso(caso.getCaso())
                    .imagen(caso.getImagen())
                    .build();
            }

            dto.setPreguntaCaso(
                PreguntaCasoDTO.builder()
                    .id(pc.getId())
                    .pregunta(pc.getPregunta())
                    .respuestaA(pc.getRespuestaA())
                    .respuestaB(pc.getRespuestaB())
                    .respuestaC(pc.getRespuestaC())
                    .respuestaD(pc.getRespuestaD())
                    .respuestaCorrecta(null)   // no en examen normal
                    .retroalimentacion(null)   // no en examen normal
                    .imagen(pc.getImagen())
                    .especialidadId(null)  // Don't access lazy entity
                    .dificultad(null)  // Don't access lazy entity
                    .fechaHora(pc.getFechaHora())
                    .caso(casoDTO)
                    .build()
            );
        }

        return dto;
    }

    private static ExamenPreguntaDTO mapExamenPreguntaParaRevision(ExamenPregunta ep, Long examenId) {
        ExamenPreguntaDTO dto = new ExamenPreguntaDTO();
        dto.setId(ep.getId());
        dto.setOrden(ep.getOrden());
        dto.setPuntaje(ep.getPuntaje());
        dto.setExamenId(examenId); // ✅ Usar el ID pasado como parámetro

        if (ep.getReactivo() != null) {
            dto.setTipo("REACTIVO");
            Reactivo r = ep.getReactivo();
            dto.setReactivo(
                new ReactivoDTO(
                    r.getId(),
                    r.getPregunta(),
                    r.getRespuestaA(),
                    r.getRespuestaB(),
                    r.getRespuestaC(),
                    r.getRespuestaD(),
                    r.getRetroalimentacion() // incluir en revisión
                )
            );
        }

        if (ep.getPreguntaCaso() != null) {
            dto.setTipo("CASO");
            PreguntaCaso pc = ep.getPreguntaCaso();

            // Construir el DTO del caso clínico (sin preguntas para evitar recursión)
            CasoEstudioDTO casoDTO = null;
            if (pc.getCasoEstudio() != null) {
                CasoEstudio caso = pc.getCasoEstudio();
                casoDTO = CasoEstudioDTO.builder()
                    .id(caso.getId())
                    .caso(caso.getCaso())
                    .imagen(caso.getImagen())
                    .build();
            }

            dto.setPreguntaCaso(
                PreguntaCasoDTO.builder()
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
                    .caso(casoDTO)
                    .build()
            );
        }

        return dto;
    }

    // ==========================================================
    // Extraer la especialidad del examen
    // ==========================================================
    private static Long getEspecialidadIdDelExamen(Examen examen) {
        return examen.getEspecialidadId();
    }
}