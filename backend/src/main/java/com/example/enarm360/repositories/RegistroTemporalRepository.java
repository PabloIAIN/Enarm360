package com.example.enarm360.repositories;

import com.example.enarm360.entities.RegistroTemporal;
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
public interface RegistroTemporalRepository extends JpaRepository<RegistroTemporal, Long> {
    
    /**
     * Buscar registro temporal por token
     */
    Optional<RegistroTemporal> findByToken(String token);
    
    /**
     * Buscar registro temporal por email
     */
    Optional<RegistroTemporal> findByEmail(String email);
    
    /**
     * Buscar registro temporal por username
     */
    Optional<RegistroTemporal> findByUsername(String username);
    
    /**
     * Buscar registro temporal por teléfono
     */
    Optional<RegistroTemporal> findByTelefono(String telefono);
    
    /**
     * Verificar si existe un email en registros temporales
     */
    boolean existsByEmail(String email);
    
    /**
     * Verificar si existe un username en registros temporales
     */
    boolean existsByUsername(String username);
    
    /**
     * Verificar si existe un teléfono en registros temporales
     */
    boolean existsByTelefono(String telefono);
    
    /**
     * Buscar registros temporales expirados
     */
    @Query("SELECT rt FROM RegistroTemporal rt WHERE rt.expiraEn < :now")
    List<RegistroTemporal> findExpiredRegistrations(@Param("now") LocalDateTime now);
    
    /**
     * Buscar registros completamente verificados listos para migrar
     */
    @Query("SELECT rt FROM RegistroTemporal rt WHERE rt.emailVerificado = true AND rt.telefonoVerificado = true")
    List<RegistroTemporal> findCompletelyVerified();
    
    /**
     * Buscar registros con códigos de email expirados
     */
    @Query("SELECT rt FROM RegistroTemporal rt WHERE rt.codigoEmailExpira < :now AND rt.codigoEmail IS NOT NULL")
    List<RegistroTemporal> findWithExpiredEmailCodes(@Param("now") LocalDateTime now);
    
    /**
     * Buscar registros con códigos de teléfono expirados
     */
    @Query("SELECT rt FROM RegistroTemporal rt WHERE rt.codigoTelefonoExpira < :now AND rt.codigoTelefono IS NOT NULL")
    List<RegistroTemporal> findWithExpiredTelefonoCodes(@Param("now") LocalDateTime now);
    
    /**
     * Eliminar registros temporales expirados
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RegistroTemporal rt WHERE rt.expiraEn < :now")
    int deleteExpiredRegistrations(@Param("now") LocalDateTime now);
    
    /**
     * Limpiar códigos de email expirados
     */
    @Modifying
    @Transactional
    @Query("UPDATE RegistroTemporal rt SET rt.codigoEmail = NULL, rt.codigoEmailExpira = NULL " +
           "WHERE rt.codigoEmailExpira < :now AND rt.codigoEmail IS NOT NULL")
    int clearExpiredEmailCodes(@Param("now") LocalDateTime now);
    
    /**
     * Limpiar códigos de teléfono expirados
     */
    @Modifying
    @Transactional
    @Query("UPDATE RegistroTemporal rt SET rt.codigoTelefono = NULL, rt.codigoTelefonoExpira = NULL " +
           "WHERE rt.codigoTelefonoExpira < :now AND rt.codigoTelefono IS NOT NULL")
    int clearExpiredTelefonoCodes(@Param("now") LocalDateTime now);
    
    /**
     * Contar registros temporales activos
     */
    @Query("SELECT COUNT(rt) FROM RegistroTemporal rt WHERE rt.expiraEn > :now")
    long countActiveRegistrations(@Param("now") LocalDateTime now);
    
    /**
     * Contar registros por estado de verificación
     */
    @Query("SELECT COUNT(rt) FROM RegistroTemporal rt WHERE rt.emailVerificado = :emailVerified AND rt.telefonoVerificado = :telefonoVerified")
    long countByVerificationStatus(@Param("emailVerified") Boolean emailVerified, @Param("telefonoVerified") Boolean telefonoVerified);
    
    /**
     * Buscar registros creados en las últimas horas para estadísticas
     */
    @Query("SELECT rt FROM RegistroTemporal rt WHERE rt.creadoEn > :since ORDER BY rt.creadoEn DESC")
    List<RegistroTemporal> findRecentRegistrations(@Param("since") LocalDateTime since);
    
    /**
     * Buscar registros por email parcialmente verificado
     */
    @Query("SELECT rt FROM RegistroTemporal rt WHERE rt.email = :email AND rt.emailVerificado = false")
    Optional<RegistroTemporal> findByEmailNotVerified(@Param("email") String email);
    
    /**
     * Buscar registros por teléfono parcialmente verificado  
     */
    @Query("SELECT rt FROM RegistroTemporal rt WHERE rt.telefono = :telefono AND rt.telefonoVerificado = false")
    Optional<RegistroTemporal> findByTelefonoNotVerified(@Param("telefono") String telefono);
    
    /**
     * Método simplificado para eliminar expirados
     */
    @Modifying
    @Transactional
    default int eliminarExpirados() {
        return deleteExpiredRegistrations(LocalDateTime.now());
    }
    
    /**
     * Contar registros con email no verificado
     */
    @Query("SELECT COUNT(rt) FROM RegistroTemporal rt WHERE rt.emailVerificado = false")
    long countByCodigoEmailVerificadoFalse();
    
    /**
     * Contar registros con teléfono no verificado
     */
    @Query("SELECT COUNT(rt) FROM RegistroTemporal rt WHERE rt.telefonoVerificado = false")
    long countByCodigoTelefonoVerificadoFalse();
    
    /**
     * Contar registros expirados
     */
    @Query("SELECT COUNT(rt) FROM RegistroTemporal rt WHERE rt.expiraEn < :now")
    default long countExpirados() {
        return countExpiredRegistrations(LocalDateTime.now());
    }
    
    @Query("SELECT COUNT(rt) FROM RegistroTemporal rt WHERE rt.expiraEn < :now")
    long countExpiredRegistrations(@Param("now") LocalDateTime now);
}