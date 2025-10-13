package com.example.enarm360.repositories;

import com.example.enarm360.entities.VerificacionTelefono;
import com.example.enarm360.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificacionTelefonoRepository extends JpaRepository<VerificacionTelefono, Long> {

    /**
     * Buscar verificación por código y teléfono
     */
    Optional<VerificacionTelefono> findByCodigoAndTelefono(String codigo, String telefono);

    /**
     * Buscar verificación por código, teléfono y que no esté verificada
     */
    Optional<VerificacionTelefono> findByCodigoAndTelefonoAndVerificadoFalse(String codigo, String telefono);

    /**
     * Buscar verificaciones activas (no verificadas y no expiradas) de un usuario
     */
    @Query("SELECT v FROM VerificacionTelefono v WHERE v.usuario = :usuario " +
           "AND v.verificado = false AND v.expiraEn > :now")
    List<VerificacionTelefono> findActiveVerificationsByUsuario(
            @Param("usuario") Usuario usuario, 
            @Param("now") LocalDateTime now);

    /**
     * Buscar verificaciones activas por teléfono
     */
    @Query("SELECT v FROM VerificacionTelefono v WHERE v.telefono = :telefono " +
           "AND v.verificado = false AND v.expiraEn > :now")
    List<VerificacionTelefono> findActiveVerificationsByTelefono(
            @Param("telefono") String telefono, 
            @Param("now") LocalDateTime now);

    /**
     * Buscar última verificación de un usuario
     */
    @Query("SELECT v FROM VerificacionTelefono v WHERE v.usuario = :usuario " +
           "ORDER BY v.creadoEn DESC LIMIT 1")
    Optional<VerificacionTelefono> findLatestByUsuario(@Param("usuario") Usuario usuario);

    /**
     * Buscar última verificación activa de un teléfono
     */
    @Query("SELECT v FROM VerificacionTelefono v WHERE v.telefono = :telefono " +
           "AND v.verificado = false AND v.expiraEn > :now " +
           "ORDER BY v.creadoEn DESC LIMIT 1")
    Optional<VerificacionTelefono> findLatestActiveByTelefono(@Param("telefono") String telefono);

    /**
     * Buscar verificaciones expiradas para limpiar
     */
    @Query("SELECT v FROM VerificacionTelefono v WHERE v.expiraEn < :now " +
           "AND v.verificado = false")
    List<VerificacionTelefono> findExpiredVerifications(@Param("now") LocalDateTime now);

    /**
     * Eliminar todas las verificaciones no verificadas de un usuario
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificacionTelefono v WHERE v.usuario = :usuario " +
           "AND v.verificado = false")
    void deleteUnverifiedByUsuario(@Param("usuario") Usuario usuario);

    /**
     * Eliminar verificaciones expiradas
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificacionTelefono v WHERE v.expiraEn < :now " +
           "AND v.verificado = false")
    void deleteExpiredVerifications(@Param("now") LocalDateTime now);

    /**
     * Contar verificaciones activas de un usuario
     */
    @Query("SELECT COUNT(v) FROM VerificacionTelefono v WHERE v.usuario = :usuario " +
           "AND v.verificado = false AND v.expiraEn > :now")
    long countActiveVerificationsByUsuario(
            @Param("usuario") Usuario usuario, 
            @Param("now") LocalDateTime now);

    /**
     * Verificar si existe una verificación válida para un teléfono
     */
    @Query("SELECT COUNT(v) > 0 FROM VerificacionTelefono v WHERE v.telefono = :telefono " +
           "AND v.verificado = false AND v.expiraEn > :now")
    boolean hasActiveVerificationForTelefono(
            @Param("telefono") String telefono, 
            @Param("now") LocalDateTime now);

    /**
     * Contar intentos de verificaciones en las últimas horas para un teléfono
     */
    @Query("SELECT COUNT(v) FROM VerificacionTelefono v WHERE v.telefono = :telefono " +
           "AND v.creadoEn > :since")
    long countRecentVerificationsByTelefono(
            @Param("telefono") String telefono, 
            @Param("since") LocalDateTime since);

    /**
     * Buscar verificaciones por usuario ordenadas por fecha
     */
    List<VerificacionTelefono> findByUsuarioOrderByCreadoEnDesc(Usuario usuario);

    /**
     * Buscar verificaciones por teléfono ordenadas por fecha
     */
    List<VerificacionTelefono> findByTelefonoOrderByCreadoEnDesc(String telefono);
    
    /**
     * Eliminar todas las verificaciones de un usuario (incluidas las verificadas)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificacionTelefono v WHERE v.usuario = :usuario")
    void deleteByUsuario(@Param("usuario") Usuario usuario);
    
    /**
     * Eliminar verificaciones expiradas y retornar el número de registros eliminados
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificacionTelefono v WHERE v.expiraEn < :now AND v.verificado = false")
    int deleteExpired(@Param("now") LocalDateTime now);
}
