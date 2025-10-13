package com.example.enarm360.controllers;

import com.example.enarm360.dtos.QuejaSugerenciaDTO;
import com.example.enarm360.services.QuejaSugerenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quejas-sugerencias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuejaSugerenciasController {

    private final QuejaSugerenciaService quejaSugerenciaService;

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody QuejaSugerenciaDTO dto) {
        System.out.println("🔔 INICIO - Endpoint /api/quejas-sugerencias POST llamado");
        System.out.println("📦 DTO Recibido: " + dto);
        System.out.println("   - usuarioId: " + dto.getUsuarioId());
        System.out.println("   - tipoObjetivo: " + dto.getTipoObjetivo());
        System.out.println("   - referenciaId: " + dto.getReferenciaId());
        System.out.println("   - rating: " + dto.getRating());
        System.out.println("   - comentario: " + dto.getComentario());

        try {
            QuejaSugerenciaDTO created = quejaSugerenciaService.crearQuejaSugerencia(dto);
            System.out.println("✅ Queja/sugerencia creada exitosamente: " + created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ ERROR al crear queja: " + e.getClass().getName() + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuejaSugerenciaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody QuejaSugerenciaDTO dto) {
        try {
            QuejaSugerenciaDTO updated = quejaSugerenciaService.actualizarQuejaSugerencia(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<QuejaSugerenciaDTO>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        List<QuejaSugerenciaDTO> quejas = quejaSugerenciaService.obtenerPorUsuario(usuarioId);
        return ResponseEntity.ok(quejas);
    }

    @GetMapping("/reactivo/{reactivoId}")
    public ResponseEntity<List<QuejaSugerenciaDTO>> obtenerPorReactivo(@PathVariable Long reactivoId) {
        List<QuejaSugerenciaDTO> quejas = quejaSugerenciaService.obtenerPorReactivo(reactivoId);
        return ResponseEntity.ok(quejas);
    }

    @GetMapping("/pregunta-caso/{preguntaCasoId}")
    public ResponseEntity<List<QuejaSugerenciaDTO>> obtenerPorPreguntaCaso(@PathVariable Long preguntaCasoId) {
        List<QuejaSugerenciaDTO> quejas = quejaSugerenciaService.obtenerPorPreguntaCaso(preguntaCasoId);
        return ResponseEntity.ok(quejas);
    }

    @GetMapping("/usuario/{usuarioId}/review")
    public ResponseEntity<QuejaSugerenciaDTO> obtenerReviewUsuario(
            @PathVariable Long usuarioId,
            @RequestParam String tipoObjetivo,
            @RequestParam Long referenciaId) {
        QuejaSugerenciaDTO review = quejaSugerenciaService.obtenerReviewUsuario(usuarioId, tipoObjetivo, referenciaId);
        if (review != null) {
            return ResponseEntity.ok(review);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/promedio-rating")
    public ResponseEntity<Double> obtenerPromedioRating(
            @RequestParam String tipoObjetivo,
            @RequestParam Long referenciaId) {
        Double promedio = quejaSugerenciaService.obtenerPromedioRating(tipoObjetivo, referenciaId);
        return ResponseEntity.ok(promedio != null ? promedio : 0.0);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            quejaSugerenciaService.eliminarQuejaSugerencia(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
