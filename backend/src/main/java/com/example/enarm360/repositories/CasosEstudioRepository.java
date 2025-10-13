package com.example.enarm360.repositories;

import com.example.enarm360.entities.CasoEstudio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CasosEstudioRepository extends JpaRepository<CasoEstudio, Long> {

    // Query sin filtros
    @Query(value = "SELECT * FROM casos_de_estudio ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<CasoEstudio> findRandomCasos(@Param("limit") int limit);

    // Query con filtros de especialidad
    @Query(value = "SELECT c.* FROM casos_de_estudio c " +
                   "JOIN tema t ON c.tema_id = t.id " +
                   "WHERE t.especialidad_id IN :especialidadIds " +
                   "ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<CasoEstudio> findRandomByEspecialidades(
        @Param("especialidadIds") List<Long> especialidadIds,
        @Param("limit") int limit
    );

    // Query con filtros de especialidad y dificultad
    @Query(value = "SELECT c.* FROM casos_de_estudio c " +
                   "JOIN tema t ON c.tema_id = t.id " +
                   "WHERE t.especialidad_id IN :especialidadIds " +
                   "AND EXISTS (SELECT 1 FROM preguntas_casos pc " +
                   "            WHERE pc.caso_de_estudio_id = c.id " +
                   "            AND pc.dificultad = :dificultadId) " +
                   "ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<CasoEstudio> findRandomByEspecialidadesAndDificultad(
        @Param("especialidadIds") List<Long> especialidadIds,
        @Param("dificultadId") Long dificultadId,
        @Param("limit") int limit
    );

    // Query solo con filtro de dificultad
    @Query(value = "SELECT c.* FROM casos_de_estudio c " +
                   "WHERE EXISTS (SELECT 1 FROM preguntas_casos pc " +
                   "              WHERE pc.caso_de_estudio_id = c.id " +
                   "              AND pc.dificultad = :dificultadId) " +
                   "ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<CasoEstudio> findRandomByDificultad(
        @Param("dificultadId") Long dificultadId,
        @Param("limit") int limit
    );
}