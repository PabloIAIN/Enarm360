package com.example.enarm360.controllers;

import com.example.enarm360.dtos.DificultadDTO;
import com.example.enarm360.repositories.DificultadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dificultades")
@RequiredArgsConstructor
public class DificultadController {

    private final DificultadRepository dificultadRepository;

    @GetMapping
    public ResponseEntity<List<DificultadDTO>> listar() {
        List<DificultadDTO> dificultades = dificultadRepository.findAll()
                .stream()
                .map(d -> new DificultadDTO(d.getId(), d.getNombre()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dificultades);
    }
}
