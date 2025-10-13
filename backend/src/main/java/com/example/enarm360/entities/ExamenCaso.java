package com.example.enarm360.entities;
import com.example.enarm360.entities.CasoEstudio;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "examen_caso")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamenCaso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Relación con Examen
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examen_id", nullable = false)
    private Examen examen;
    
    // Relación con CasosDeEstudio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caso_id", nullable = false)
    private CasoEstudio casoEstudio;
    
    @Column(nullable = false)
    private Integer orden;
    
   @Column
    private Double puntaje;
    
    // Método de utilidad para obtener cantidad de preguntas
    @Transient
    public Integer getCantidadPreguntas() {
        return casoEstudio != null && casoEstudio.getPreguntas() != null 
            ? casoEstudio.getPreguntas().size() 
            : 0;
    }
}