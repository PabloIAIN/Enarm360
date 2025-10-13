package com.example.enarm360.repositories;

import com.example.enarm360.entities.TipoExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface TipoExamenRepository extends JpaRepository<TipoExamen, Long> {
    
    Optional<TipoExamen> findByCodigo(String codigo);
    
    List<TipoExamen> findByActivoTrueOrderByOrdenVisualizacion();
    
    boolean existsByCodigo(String codigo);
}