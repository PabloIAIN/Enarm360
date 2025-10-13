package com.example.enarm360.repositories;

import com.example.enarm360.entities.VerificacionEmail;
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
public interface VerificacionEmailRepository extends JpaRepository<VerificacionEmail, Long> {

    /**
     * Buscar verificación por código
     */
    Optional<VerificacionEmail> findByCodigo(String codigo);

    /**
     * Buscar verificación por código y que no esté verificada
     */
    Optional<VerificacionEmail> findByCodigoAndVerificadoFalse(String codigo);
    
    /**
     * Buscar verificación por código, email y que no esté verificada
     */
    Optional<VerificacionEmail> findByCodigoAndEmailAndVerificadoFalse(String codigo, String email);

    /**
     * Buscar verificaciones activas (no verificadas y no expiradas) de un usuario
     */
    @Query("SELECT v FROM VerificacionEmail v WHERE v.usuario = :usuario " +
           "AND v.verificado = false AND v.expiraEn > :now")
    List<VerificacionEmail> findActiveVerificationsByUsuario(
            @Param("usuario") Usuario usuario, 
            @Param("now") LocalDateTime now);

    /**
     * Buscar verificaciones activas por email
     */
    @Query("SELECT v FROM VerificacionEmail v WHERE v.email = :email " +
           "AND v.verificado = false AND v.expiraEn > :now")
    List<VerificacionEmail> findActiveVerificationsByEmail(
            @Param("email") String email, 
            @Param("now") LocalDateTime now);

    /**
     * Buscar última verificación de un usuario
     */
    @Query("SELECT v FROM VerificacionEmail v WHERE v.usuario = :usuario " +
           "ORDER BY v.creadoEn DESC LIMIT 1")
    Optional<VerificacionEmail> findLatestByUsuario(@Param("usuario") Usuario usuario);

    /**
     * Buscar verificaciones expiradas para limpiar
     */
    @Query("SELECT v FROM VerificacionEmail v WHERE v.expiraEn < :now " +
           "AND v.verificado = false")
    List<VerificacionEmail> findExpiredVerifications(@Param("now") LocalDateTime now);

    /**
     * Eliminar todas las verificaciones no verificadas de un usuario
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificacionEmail v WHERE v.usuario = :usuario " +
           "AND v.verificado = false")
    void deleteUnverifiedByUsuario(@Param("usuario") Usuario usuario);

    /**
     * Eliminar verificaciones expiradas
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificacionEmail v WHERE v.expiraEn < :now " +
           "AND v.verificado = false")
    void deleteExpiredVerifications(@Param("now") LocalDateTime now);

    /**
     * Contar verificaciones activas de un usuario
     */
    @Query("SELECT COUNT(v) FROM VerificacionEmail v WHERE v.usuario = :usuario " +
           "AND v.verificado = false AND v.expiraEn > :now")
    long countActiveVerificationsByUsuario(
            @Param("usuario") Usuario usuario, 
            @Param("now") LocalDateTime now);

    /**
     * Verificar si existe una verificación válida para un email
     */
    @Query("SELECT COUNT(v) > 0 FROM VerificacionEmail v WHERE v.email = :email " +
           "AND v.verificado = false AND v.expiraEn > :now")
    boolean hasActiveVerificationForEmail(
            @Param("email") String email, 
            @Param("now") LocalDateTime now);

    /**
     * Buscar verificaciones por usuario ordenadas por fecha
     */
    List<VerificacionEmail> findByUsuarioOrderByCreadoEnDesc(Usuario usuario);
    
    /**
     * Eliminar todas las verificaciones de un usuario (incluidas las verificadas)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificacionEmail v WHERE v.usuario = :usuario")
    void deleteByUsuario(@Param("usuario") Usuario usuario);
    
    /**
     * Eliminar verificaciones expiradas y retornar el número de registros eliminados
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificacionEmail v WHERE v.expiraEn < :now AND v.verificado = false")
    int deleteExpired(@Param("now") LocalDateTime now);
}
