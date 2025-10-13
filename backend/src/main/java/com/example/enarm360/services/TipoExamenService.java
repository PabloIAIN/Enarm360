package com.example.enarm360.services;

import com.example.enarm360.dtos.TipoExamenDTO;
import com.example.enarm360.entities.TipoExamen;
import com.example.enarm360.repositories.TipoExamenRepository;
import com.example.enarm360.Mappers.TipoExamenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TipoExamenService {
    
    private final TipoExamenRepository tipoExamenRepository;
    
    @Transactional(readOnly = true)
    public List<TipoExamenDTO> listarTodos() {
        return tipoExamenRepository.findAll().stream()
                .map(TipoExamenMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TipoExamenDTO> listarActivos() {
        return tipoExamenRepository.findByActivoTrueOrderByOrdenVisualizacion().stream()
                .map(TipoExamenMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public TipoExamenDTO obtenerPorId(Long id) {
        return tipoExamenRepository.findById(id)
                .map(TipoExamenMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Tipo de examen no encontrado"));
    }
    
    @Transactional(readOnly = true)
    public TipoExamenDTO obtenerPorCodigo(String codigo) {
        return tipoExamenRepository.findByCodigo(codigo)
                .map(TipoExamenMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Tipo de examen no encontrado"));
    }
    
    @Transactional
    public TipoExamenDTO crear(TipoExamenDTO dto) {
        if (tipoExamenRepository.existsByCodigo(dto.getCodigo())) {
            throw new RuntimeException("Ya existe un tipo de examen con ese código");
        }
        
        TipoExamen tipoExamen = TipoExamenMapper.toEntity(dto);
        TipoExamen guardado = tipoExamenRepository.save(tipoExamen);
        return TipoExamenMapper.toDTO(guardado);
    }
    
    @Transactional
    public TipoExamenDTO actualizar(Long id, TipoExamenDTO dto) {
        TipoExamen existente = tipoExamenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de examen no encontrado"));
        
        existente.setNombre(dto.getNombre());
        existente.setDescripcion(dto.getDescripcion());
        existente.setPermiteCasosClinicos(dto.getPermiteCasosClinicos());
        existente.setPermitePreguntasDirectas(dto.getPermitePreguntasDirectas());
        existente.setTieneTiempoLimite(dto.getTieneTiempoLimite());
        existente.setEsCalificable(dto.getEsCalificable());
        existente.setOrdenVisualizacion(dto.getOrdenVisualizacion());
        existente.setActivo(dto.getActivo());
        
        TipoExamen actualizado = tipoExamenRepository.save(existente);
        return TipoExamenMapper.toDTO(actualizado);
    }
    
    @Transactional
    public void eliminar(Long id) {
        tipoExamenRepository.deleteById(id);
    }
    
    @Transactional
    public void desactivar(Long id) {
        TipoExamen tipoExamen = tipoExamenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de examen no encontrado"));
        tipoExamen.setActivo(false);
        tipoExamenRepository.save(tipoExamen);
    }
}