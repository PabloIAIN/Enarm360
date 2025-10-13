package com.example.enarm360.repositories;

import com.example.enarm360.entities.PreguntaCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreguntaCasoRepository extends JpaRepository<PreguntaCaso, Long> {
    
    /**
     * Encuentra todas las preguntas asociadas a un caso de estudio específico.
     * Spring Data JPA genera automáticamente la query basándose en el nombre del método.
     * 
     * Asume que en la entidad PreguntaCaso existe una relación ManyToOne con CasoEstudio:
     * @ManyToOne
     * @JoinColumn(name = "caso_estudio_id")
     * private CasoEstudio casoEstudio;
     */
    List<PreguntaCaso> findByCasoEstudioId(Long casoEstudioId);
    
    /**
     * Alternativa: Si la relación se llama diferente en tu entidad,
     * puedes usar @Query para ser más explícito:
     */
    // @Query("SELECT p FROM PreguntaCaso p WHERE p.casoEstudio.id = :casoEstudioId")
    // List<PreguntaCaso> findByCasoEstudioId(@Param("casoEstudioId") Long casoEstudioId);
}