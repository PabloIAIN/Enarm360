package com.example.enarm360.controllers;

import com.example.enarm360.dtos.TipoExamenDTO;
import com.example.enarm360.services.TipoExamenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-examen")
@RequiredArgsConstructor
public class TipoExamenController {
    
    private final TipoExamenService tipoExamenService;
    
    @GetMapping
    public ResponseEntity<List<TipoExamenDTO>> listarTodos() {
        return ResponseEntity.ok(tipoExamenService.listarTodos());
    }
    
    @GetMapping("/activos")
    public ResponseEntity<List<TipoExamenDTO>> listarActivos() {
        return ResponseEntity.ok(tipoExamenService.listarActivos());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TipoExamenDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tipoExamenService.obtenerPorId(id));
    }
    
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<TipoExamenDTO> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(tipoExamenService.obtenerPorCodigo(codigo));
    }
    
    @PostMapping
    public ResponseEntity<TipoExamenDTO> crear(@RequestBody TipoExamenDTO dto) {
        return ResponseEntity.ok(tipoExamenService.crear(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TipoExamenDTO> actualizar(
            @PathVariable Long id,
            @RequestBody TipoExamenDTO dto
    ) {
        return ResponseEntity.ok(tipoExamenService.actualizar(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tipoExamenService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        tipoExamenService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}