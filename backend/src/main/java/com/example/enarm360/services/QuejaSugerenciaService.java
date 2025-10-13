package com.example.enarm360.services;

import com.example.enarm360.dtos.QuejaSugerenciaDTO;
import com.example.enarm360.entities.*;
import com.example.enarm360.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuejaSugerenciaService {

    private final QuejaSugerenciaRepository quejaSugerenciaRepository;
    private final ReactivoRepository reactivoRepository;
    private final PreguntaCasoRepository preguntaCasoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public QuejaSugerenciaDTO crearQuejaSugerencia(QuejaSugerenciaDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        QuejaSugerencia quejaSugerencia = QuejaSugerencia.builder()
                .usuario(usuario)
                .tipoObjetivo(dto.getTipoObjetivo())
                .referenciaId(dto.getReferenciaId())
                .rating(dto.getRating())
                .comentario(dto.getComentario())
                .build();

        // Asignar la relación específica según el tipo
        if ("reactivo".equalsIgnoreCase(dto.getTipoObjetivo())) {
            Reactivo reactivo = reactivoRepository.findById(dto.getReferenciaId())
                    .orElseThrow(() -> new RuntimeException("Reactivo no encontrado"));
            quejaSugerencia.setReactivo(reactivo);
        } else if ("pregunta_caso".equalsIgnoreCase(dto.getTipoObjetivo())) {
            PreguntaCaso preguntaCaso = preguntaCasoRepository.findById(dto.getReferenciaId())
                    .orElseThrow(() -> new RuntimeException("Pregunta de caso no encontrada"));
            quejaSugerencia.setPreguntaCaso(preguntaCaso);
        }

        QuejaSugerencia saved = quejaSugerenciaRepository.save(quejaSugerencia);
        return convertirADTO(saved);
    }

    @Transactional
    public QuejaSugerenciaDTO actualizarQuejaSugerencia(Long id, QuejaSugerenciaDTO dto) {
        QuejaSugerencia quejaSugerencia = quejaSugerenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Queja/Sugerencia no encontrada"));

        quejaSugerencia.setRating(dto.getRating());
        quejaSugerencia.setComentario(dto.getComentario());

        QuejaSugerencia updated = quejaSugerenciaRepository.save(quejaSugerencia);
        return convertirADTO(updated);
    }

    public List<QuejaSugerenciaDTO> obtenerPorUsuario(Long usuarioId) {
        return quejaSugerenciaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<QuejaSugerenciaDTO> obtenerPorReactivo(Long reactivoId) {
        return quejaSugerenciaRepository.findByReactivoId(reactivoId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<QuejaSugerenciaDTO> obtenerPorPreguntaCaso(Long preguntaCasoId) {
        return quejaSugerenciaRepository.findByPreguntaCasoId(preguntaCasoId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public QuejaSugerenciaDTO obtenerReviewUsuario(Long usuarioId, String tipoObjetivo, Long referenciaId) {
        QuejaSugerencia queja = null;

        if ("reactivo".equalsIgnoreCase(tipoObjetivo)) {
            queja = quejaSugerenciaRepository.findByUsuarioIdAndReactivoId(usuarioId, referenciaId)
                    .orElse(null);
        } else if ("pregunta_caso".equalsIgnoreCase(tipoObjetivo)) {
            queja = quejaSugerenciaRepository.findByUsuarioIdAndPreguntaCasoId(usuarioId, referenciaId)
                    .orElse(null);
        }

        return queja != null ? convertirADTO(queja) : null;
    }

    public Double obtenerPromedioRating(String tipoObjetivo, Long referenciaId) {
        if ("reactivo".equalsIgnoreCase(tipoObjetivo)) {
            return quejaSugerenciaRepository.getPromedioRatingReactivo(referenciaId);
        } else if ("pregunta_caso".equalsIgnoreCase(tipoObjetivo)) {
            return quejaSugerenciaRepository.getPromedioRatingPreguntaCaso(referenciaId);
        }
        return 0.0;
    }

    @Transactional
    public void eliminarQuejaSugerencia(Long id) {
        quejaSugerenciaRepository.deleteById(id);
    }

    private QuejaSugerenciaDTO convertirADTO(QuejaSugerencia quejaSugerencia) {
        return QuejaSugerenciaDTO.builder()
                .id(quejaSugerencia.getId())
                .usuarioId(quejaSugerencia.getUsuario().getId())
                .tipoObjetivo(quejaSugerencia.getTipoObjetivo())
                .referenciaId(quejaSugerencia.getReferenciaId())
                .rating(quejaSugerencia.getRating())
                .comentario(quejaSugerencia.getComentario())
                .fechaCreacion(quejaSugerencia.getFechaCreacion())
                .reactivoId(quejaSugerencia.getReactivo() != null ? quejaSugerencia.getReactivo().getId() : null)
                .preguntaCasoId(quejaSugerencia.getPreguntaCaso() != null ? quejaSugerencia.getPreguntaCaso().getId() : null)
                .build();
    }
}
