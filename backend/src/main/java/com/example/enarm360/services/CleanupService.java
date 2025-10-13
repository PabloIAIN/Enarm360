package com.example.enarm360.services;

import com.example.enarm360.entities.Usuario;
import com.example.enarm360.repositories.UsuarioRepository;
import com.example.enarm360.repositories.VerificacionEmailRepository;
import com.example.enarm360.repositories.VerificacionTelefonoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(CleanupService.class);
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private VerificacionEmailRepository verificacionEmailRepository;
    
    @Autowired
    private VerificacionTelefonoRepository verificacionTelefonoRepository;
    
    /**
     * Limpiar usuarios no verificados después de 24 horas
     * Se ejecuta cada hora
     */
    // TEMPORALMENTE DESACTIVADO - usar RegistroTemporalService en su lugar
    // @Scheduled(fixedRate = 3600000) // Cada hora (3600000 ms = 1 hora)
    public void limpiarUsuariosNoVerificados() {
        logger.info("Iniciando limpieza de usuarios no verificados...");
        
        try {
            LocalDateTime limite = LocalDateTime.now().minusHours(24);
            
            // Buscar usuarios creados hace más de 24 horas y no verificados
            List<Usuario> usuariosNoVerificados = usuarioRepository.findAll().stream()
                .filter(usuario -> 
                    usuario.getCreadoEn().isBefore(limite) && 
                    (!usuario.getEmailVerificado() || !usuario.getTelefonoVerificado())
                )
                .toList();
            
            int usuariosEliminados = 0;
            
            for (Usuario usuario : usuariosNoVerificados) {
                logger.info("Eliminando usuario no verificado: {} (ID: {}, creado: {})", 
                    usuario.getEmail(), usuario.getId(), usuario.getCreadoEn());
                
                // Eliminar verificaciones asociadas
                verificacionEmailRepository.deleteByUsuario(usuario);
                verificacionTelefonoRepository.deleteByUsuario(usuario);
                
                // Eliminar usuario
                usuarioRepository.delete(usuario);
                usuariosEliminados++;
            }
            
            if (usuariosEliminados > 0) {
                logger.info("Se eliminaron {} usuarios no verificados", usuariosEliminados);
            } else {
                logger.debug("No se encontraron usuarios no verificados para eliminar");
            }
            
        } catch (Exception e) {
            logger.error("Error durante la limpieza de usuarios no verificados: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Limpiar verificaciones de email expiradas
     * Se ejecuta cada 30 minutos
     */
    @Scheduled(fixedRate = 1800000) // Cada 30 minutos (1800000 ms = 30 min)
    public void limpiarVerificacionesEmailExpiradas() {
        logger.debug("Limpiando verificaciones de email expiradas...");
        
        try {
            LocalDateTime ahora = LocalDateTime.now();
            
            int verificacionesEliminadas = verificacionEmailRepository.deleteExpired(ahora);
            
            if (verificacionesEliminadas > 0) {
                logger.info("Se eliminaron {} verificaciones de email expiradas", verificacionesEliminadas);
            }
            
        } catch (Exception e) {
            logger.error("Error al limpiar verificaciones de email expiradas: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Limpiar verificaciones de teléfono expiradas
     * Se ejecuta cada 10 minutos
     */
    @Scheduled(fixedRate = 600000) // Cada 10 minutos (600000 ms = 10 min)
    public void limpiarVerificacionesTelefonoExpiradas() {
        logger.debug("Limpiando verificaciones de teléfono expiradas...");
        
        try {
            LocalDateTime ahora = LocalDateTime.now();
            
            int verificacionesEliminadas = verificacionTelefonoRepository.deleteExpired(ahora);
            
            if (verificacionesEliminadas > 0) {
                logger.info("Se eliminaron {} verificaciones de teléfono expiradas", verificacionesEliminadas);
            }
            
        } catch (Exception e) {
            logger.error("Error al limpiar verificaciones de teléfono expiradas: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Método manual para limpiar usuarios no verificados (útil para testing)
     */
    public int limpiarUsuariosNoVerificadosManual() {
        logger.info("Ejecutando limpieza manual de usuarios no verificados...");
        
        try {
            LocalDateTime limite = LocalDateTime.now().minusHours(24);
            
            List<Usuario> usuariosNoVerificados = usuarioRepository.findAll().stream()
                .filter(usuario -> 
                    usuario.getCreadoEn().isBefore(limite) && 
                    (!usuario.getEmailVerificado() || !usuario.getTelefonoVerificado())
                )
                .toList();
            
            int usuariosEliminados = 0;
            
            for (Usuario usuario : usuariosNoVerificados) {
                verificacionEmailRepository.deleteByUsuario(usuario);
                verificacionTelefonoRepository.deleteByUsuario(usuario);
                usuarioRepository.delete(usuario);
                usuariosEliminados++;
            }
            
            logger.info("Limpieza manual completada: {} usuarios eliminados", usuariosEliminados);
            return usuariosEliminados;
            
        } catch (Exception e) {
            logger.error("Error en limpieza manual: {}", e.getMessage(), e);
            return 0;
        }
    }
}