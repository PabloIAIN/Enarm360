package com.example.enarm360.repositories;
import com.example.enarm360.entities.IntentoExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface IntentoExamenRepository extends JpaRepository<IntentoExamen, Long> {

    // Intentos por usuario
    List<IntentoExamen> findByUsuarioId(Long usuarioId);

    // Intentos de un examen específico
    List<IntentoExamen> findByExamenId(Long examenId);

    // Último intento de un usuario
    Optional<IntentoExamen> findTopByUsuarioIdOrderByIniciadoEnDesc(Long usuarioId);

    // Obtener intento con sus preguntas cargadas
    @Query("SELECT i FROM IntentoExamen i " +
           "LEFT JOIN FETCH i.preguntas " +
           "WHERE i.id = :id")
    Optional<IntentoExamen> findByIdWithPreguntas(@Param("id") Long id);

    // Buscar examen en progreso de un usuario
    @Query("SELECT i FROM IntentoExamen i " +
           "WHERE i.usuario.id = :usuarioId " +
           "AND i.estado = 'EN_PROGRESO' " +
           "ORDER BY i.iniciadoEn DESC")
    List<IntentoExamen> findExamenesEnProgresoByUsuarioId(@Param("usuarioId") Long usuarioId);

    // Buscar examen en progreso más reciente de un usuario
    @Query(value = "SELECT * FROM intento_examen i " +
           "WHERE i.usuario_id = :usuarioId " +
           "AND i.estado = 'EN_PROGRESO' " +
           "ORDER BY i.iniciado_en DESC " +
           "LIMIT 1", nativeQuery = true)
    Optional<IntentoExamen> findFirstExamenEnProgresoByUsuarioId(@Param("usuarioId") Long usuarioId);

    // Obtener intento con examen y preguntas cargadas
    @Query("SELECT i FROM IntentoExamen i " +
           "LEFT JOIN FETCH i.examen e " +
           "LEFT JOIN FETCH e.examenPreguntas " +
           "WHERE i.id = :id")
    Optional<IntentoExamen> findByIdWithExamenAndPreguntas(@Param("id") Long id);
}
