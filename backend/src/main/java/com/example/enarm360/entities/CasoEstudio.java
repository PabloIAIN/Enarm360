package com.example.enarm360.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "casos_de_estudio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CasoEstudio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "El contenido del caso es obligatorio")
    private String caso;
    
    @Column(length = 255)
    private String imagen;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_id")
    @JsonIgnoreProperties({"casosEstudio", "hibernateLazyInitializer", "handler"})
    private Tema tema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"casosEstudio", "reactivos", "preguntasCasos", "hibernateLazyInitializer", "handler"})
    private Usuario usuario;
    
    @Column(name = "fecha_hora", nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaHora;
    
    @OneToMany(mappedBy = "casoEstudio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"casoEstudio", "hibernateLazyInitializer", "handler"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<PreguntaCaso> preguntas = new ArrayList<>();

    @OneToMany(mappedBy = "casoEstudio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"casoEstudio", "examen", "hibernateLazyInitializer", "handler"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<ExamenCaso> examenesQueUsan = new ArrayList<>();
    
    @Transient
    public int getCantidadPreguntas() {
        return preguntas != null ? preguntas.size() : 0;
    }
    
    @Transient
    public Long getTemaIdDesdePreguntas() {
        if (preguntas != null && !preguntas.isEmpty() && preguntas.get(0).getEspecialidad() != null) {
            return preguntas.get(0).getEspecialidad().getId();
        }
        return null;
    }
}