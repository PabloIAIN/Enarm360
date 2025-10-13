package com.example.enarm360.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quejas_sugerencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuejaSugerencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario usuario;

    @Column(name = "tipo_objetivo", nullable = false, length = 50)
    @NotBlank(message = "El tipo de objeto es obligatorio")
    private String tipoObjetivo; // 'reactivo' o 'pregunta_caso'

    @Column(name = "referencia_id", nullable = false)
    @NotNull(message = "La referencia ID es obligatoria")
    private Long referenciaId; // ID del reactivo o pregunta_caso

    @Column(nullable = false)
    @Min(value = 1, message = "El rating debe ser entre 1 y 5")
    @Max(value = 5, message = "El rating debe ser entre 1 y 5")
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha_creacion", nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reactivo_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Reactivo reactivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_caso_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PreguntaCaso preguntaCaso;
}
