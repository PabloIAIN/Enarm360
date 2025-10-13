package com.example.enarm360.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuejaSugerenciaDTO {

    private Long id;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @NotBlank(message = "El tipo de objetivo es obligatorio")
    private String tipoObjetivo; // 'reactivo' o 'pregunta_caso'

    @NotNull(message = "La referencia ID es obligatoria")
    private Long referenciaId; // ID del reactivo o pregunta_caso

    @NotNull(message = "El rating es obligatorio")
    @Min(value = 1, message = "El rating debe ser entre 1 y 5")
    @Max(value = 5, message = "El rating debe ser entre 1 y 5")
    private Integer rating;

    private String comentario;

    private LocalDateTime fechaCreacion;

    private Long reactivoId;

    private Long preguntaCasoId;

    // Rating promedio de la pregunta
    private Double ratingPromedio;

    // Total de reviews
    private Long totalReviews;
}
