package com.example.enarm360.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.enarm360.entities.ExamenPregunta;

import java.util.List;

@Repository
public interface   ExamenPreguntaRepository  extends JpaRepository<ExamenPregunta, Long> {
    List<ExamenPregunta> findByExamen_Id(Long examenId);

    List<ExamenPregunta> findByExamenIdOrderByOrden(Long examenId);

    long countByExamenId(Long examenId);
}




