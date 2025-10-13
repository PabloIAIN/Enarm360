package com.example.enarm360.entities;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@Entity
@Table(name = "especialidades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "El nombre de la especialidad es obligatorio")
    private String nombre;

   

    // Relación con reactivos
    @OneToMany(mappedBy = "especialidad", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"especialidad", "clave", "dificultad", "usuario"})
    private List<Reactivo> reactivos;

    // Relación con preguntas de casos
    @OneToMany(mappedBy = "especialidad", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"especialidad", "clave", "dificultad", "usuario"})
    private List<PreguntaCaso> preguntasCasos;
}
