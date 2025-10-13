package com.example.enarm360.services;

import com.example.enarm360.repositories.RegistroTemporalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LimpiezaRegistrosService {
    
    private static final Logger logger = LoggerFactory.getLogger(LimpiezaRegistrosService.class);
    
    @Autowired
    private RegistroTemporalRepository registroTemporalRepository;
    
    /**
     * Ejecutar limpieza automática cada hora
     */
    @Scheduled(fixedRate = 3600000) // 1 hora = 3600000 ms
    public void limpiarRegistrosExpirados() {
        logger.info("Iniciando limpieza automática de registros temporales expirados");
        
        try {
            int registrosEliminados = registroTemporalRepository.eliminarExpirados();
            
            if (registrosEliminados > 0) {
                logger.info("Limpieza completada: {} registros temporales expirados eliminados", registrosEliminados);
            } else {
                logger.debug("No se encontraron registros temporales expirados para eliminar");
            }
            
        } catch (Exception e) {
            logger.error("Error durante la limpieza automática de registros temporales: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Limpieza manual - puede ser llamada desde un endpoint de administración
     */
    public int limpiezaManual() {
        logger.info("Ejecutando limpieza manual de registros temporales expirados");
        
        try {
            int registrosEliminados = registroTemporalRepository.eliminarExpirados();
            logger.info("Limpieza manual completada: {} registros eliminados", registrosEliminados);
            return registrosEliminados;
            
        } catch (Exception e) {
            logger.error("Error durante la limpieza manual: {}", e.getMessage(), e);
            throw new RuntimeException("Error en limpieza manual: " + e.getMessage());
        }
    }
    
    /**
     * Obtener estadísticas de registros temporales
     */
    public EstadisticasLimpieza getEstadisticas() {
        long totalActivos = registroTemporalRepository.count();
        long pendientesEmail = registroTemporalRepository.countByCodigoEmailVerificadoFalse();
        long pendientesTelefono = registroTemporalRepository.countByCodigoTelefonoVerificadoFalse();
        long expirados = registroTemporalRepository.countExpirados();
        
        return EstadisticasLimpieza.builder()
                .totalRegistrosTempporales(totalActivos)
                .pendientesVerificacionEmail(pendientesEmail)
                .pendientesVerificacionTelefono(pendientesTelefono)
                .registrosExpirados(expirados)
                .build();
    }
    
    // DTO para estadísticas
    @lombok.Builder
    @lombok.Data
    public static class EstadisticasLimpieza {
        private long totalRegistrosTempporales;
        private long pendientesVerificacionEmail;
        private long pendientesVerificacionTelefono;
        private long registrosExpirados;
    }
}