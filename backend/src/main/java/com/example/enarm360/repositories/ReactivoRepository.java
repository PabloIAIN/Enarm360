package com.example.enarm360.repositories;

import com.example.enarm360.entities.Reactivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReactivoRepository extends JpaRepository<Reactivo, Long> {

    // Query sin filtros
    @Query(value = "SELECT * FROM reactivos ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Reactivo> findRandomReactivos(@Param("limit") int limit);

    // Query con filtros de especialidad
    @Query(value = "SELECT * FROM reactivos WHERE especialidad_id IN :especialidadIds ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Reactivo> findRandomByEspecialidades(
        @Param("especialidadIds") List<Long> especialidadIds,
        @Param("limit") int limit
    );

    // Query con filtros de especialidad y dificultad
    @Query(value = "SELECT * FROM reactivos WHERE especialidad_id IN :especialidadIds AND dificultad_id = :dificultadId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Reactivo> findRandomByEspecialidadesAndDificultad(
        @Param("especialidadIds") List<Long> especialidadIds,
        @Param("dificultadId") Long dificultadId,
        @Param("limit") int limit
    );

    // Query solo con filtro de dificultad
    @Query(value = "SELECT * FROM reactivos WHERE dificultad_id = :dificultadId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Reactivo> findRandomByDificultad(
        @Param("dificultadId") Long dificultadId,
        @Param("limit") int limit
    );
}