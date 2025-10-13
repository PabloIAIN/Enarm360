package com.example.enarm360.repositories;

import com.example.enarm360.entities.QuejaSugerencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuejaSugerenciaRepository extends JpaRepository<QuejaSugerencia, Long> {

    // Buscar quejas/sugerencias por usuario
    List<QuejaSugerencia> findByUsuarioId(Long usuarioId);

    // Buscar quejas/sugerencias por reactivo
    List<QuejaSugerencia> findByReactivoId(Long reactivoId);

    // Buscar quejas/sugerencias por pregunta de caso
    List<QuejaSugerencia> findByPreguntaCasoId(Long preguntaCasoId);

    // Buscar por tipo de objetivo y referencia ID
    List<QuejaSugerencia> findByTipoObjetivoAndReferenciaId(String tipoObjetivo, Long referenciaId);

    // Buscar si un usuario ya dejó review para un reactivo específico
    Optional<QuejaSugerencia> findByUsuarioIdAndReactivoId(Long usuarioId, Long reactivoId);

    // Buscar si un usuario ya dejó review para una pregunta de caso específica
    Optional<QuejaSugerencia> findByUsuarioIdAndPreguntaCasoId(Long usuarioId, Long preguntaCasoId);

    // Obtener rating promedio de un reactivo
    @Query("SELECT AVG(qs.rating) FROM QuejaSugerencia qs WHERE qs.reactivo.id = :reactivoId")
    Double getPromedioRatingReactivo(@Param("reactivoId") Long reactivoId);

    // Obtener rating promedio de una pregunta de caso
    @Query("SELECT AVG(qs.rating) FROM QuejaSugerencia qs WHERE qs.preguntaCaso.id = :preguntaCasoId")
    Double getPromedioRatingPreguntaCaso(@Param("preguntaCasoId") Long preguntaCasoId);
}
