package com.example.enarm360.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "examen_pregunta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ExamenPregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer orden;
    private Double puntaje;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "examen_id", nullable = false)
    @JsonBackReference
    private Examen examen;

    // Ahora opcional
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "reactivo_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Reactivo reactivo;

    // También opcional
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "pregunta_caso_id", nullable = true)
    @JsonIgnoreProperties({"casoEstudio", "hibernateLazyInitializer", "handler"})
    private PreguntaCaso preguntaCaso;

    @PrePersist
    @PreUpdate
    private void validarContenido() {
        boolean tieneReactivo = this.reactivo != null;
        boolean tieneCaso = this.preguntaCaso != null;
        if (tieneReactivo == tieneCaso) {
            throw new IllegalStateException(
                "ExamenPregunta debe tener exactamente uno: reactivo o preguntaCaso"
            );
        }
    }
}