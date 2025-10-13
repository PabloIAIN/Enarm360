package com.example.enarm360.controllers;

import com.example.enarm360.dtos.TemaDTO;
import com.example.enarm360.entities.Tema;
import com.example.enarm360.repositories.TemaRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/temas")
@RequiredArgsConstructor
public class TemaController {

    private final TemaRepositoryRepository temaRepository;

    @GetMapping
    public ResponseEntity<List<TemaDTO>> listar() {
        List<TemaDTO> temas = temaRepository.findAll()
                .stream()
                .map(t -> new TemaDTO(
                    t.getId(),
                    t.getNombre(),
                    t.getEspecialidad() != null ? t.getEspecialidad().getId() : null
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(temas);
    }
}
