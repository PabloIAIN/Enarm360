package com.example.enarm360.repositories;

import com.example.enarm360.entities.Dificultad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DificultadRepository extends JpaRepository<Dificultad, Long> {
}
