package com.example.enarm360.repositories;

import com.example.enarm360.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar por username O email (acepta ambos)
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permisos LEFT JOIN FETCH u.permisos " +
           "WHERE (u.username = :login OR u.email = :login) AND u.activo = true")
    Optional<Usuario> findByUsernameOrEmailAndActivoTrueWithRolesAndPermisos(@Param("login") String login);

    // Métodos individuales por username
    Optional<Usuario> findByUsernameAndActivoTrue(String username);
    Boolean existsByUsername(String username);

    // Métodos individuales por email
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailAndActivoTrue(String email);
    Boolean existsByEmail(String email);
    
    // Métodos individuales por telefono
    Optional<Usuario> findByTelefono(String telefono);
    Optional<Usuario> findByTelefonoAndActivoTrue(String telefono);
    Boolean existsByTelefono(String telefono);

    // Método que busca por username O email
    @Query("SELECT u FROM Usuario u WHERE (u.username = :login OR u.email = :login) AND u.activo = true")
    Optional<Usuario> findByUsernameOrEmailAndActivoTrue(@Param("login") String login);

    @Query("SELECT u FROM Usuario u WHERE u.activo = true")
    List<Usuario> findAllActiveUsers();

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<Usuario> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<Usuario> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail, @Param("usernameOrEmail") String usernameOrEmail2);

    // 🔎 Búsqueda de usuarios por término (paginada)
    @Query("SELECT u FROM Usuario u WHERE u.activo = true AND (LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :q, '%'))) ORDER BY u.nombre ASC")
    Page<Usuario> searchUsers(@Param("q") String q, Pageable pageable);

    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN u.roles r LEFT JOIN u.permisos up LEFT JOIN r.permisos rp " +
           "WHERE (:activo IS NULL OR u.activo = :activo) " +
           "AND (:rol IS NULL OR r.nombre = :rol) " +
           "AND (:permiso IS NULL OR (CASE WHEN :effective = true THEN (rp.codigo = :permiso OR up.codigo = :permiso) ELSE (up.codigo = :permiso) END) ) " +
           "AND (:q IS NULL OR :q = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "ORDER BY u.nombre ASC")
    Page<Usuario> searchUsersAdvanced(@Param("q") String q,
                                      @Param("rol") String rol,
                                      @Param("activo") Boolean activo,
                                      @Param("permiso") String permiso,
                                      @Param("effective") Boolean effective,
                                      Pageable pageable);

    // 🔔 Métodos relacionados con suscripciones
    @Query("SELECT u FROM Usuario u WHERE u.currentSubscription.endDate IS NOT NULL " +
           "AND u.currentSubscription.endDate BETWEEN :start AND :end " +
           "AND u.currentSubscription.status = 'ACTIVE'")
    List<Usuario> findUsersWithSubscriptionsExpiringBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT u FROM Usuario u WHERE u.subscriptionStatus = :status")
    List<Usuario> findBySubscriptionStatus(@Param("status") Usuario.SubscriptionStatus status);

    @Query("SELECT u FROM Usuario u WHERE u.currentSubscription IS NOT NULL " +
           "AND u.currentSubscription.status = 'ACTIVE' " +
           "AND (u.currentSubscription.endDate IS NULL OR u.currentSubscription.endDate > CURRENT_TIMESTAMP)")
    List<Usuario> findUsersWithActiveSubscriptions();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.subscriptionStatus = :status")
    Integer countBySubscriptionStatus(@Param("status") Usuario.SubscriptionStatus status);

    Integer countByActivo(Boolean activo);

    @Query("SELECT u FROM Usuario u WHERE u.currentSubscription.plan.id = :planId " +
           "AND u.currentSubscription.status = 'ACTIVE'")
    List<Usuario> findUsersWithActivePlan(@Param("planId") Long planId);

    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.nombre) LIKE :searchTerm OR " +
           "LOWER(u.apellidos) LIKE :searchTerm OR " +
           "LOWER(u.email) LIKE :searchTerm OR " +
           "LOWER(u.username) LIKE :searchTerm")
    List<Usuario> findBySearchTerm(@Param("searchTerm") String searchTerm);

    @Query("SELECT u FROM Usuario u WHERE u.currentSubscription IS NULL " +
           "AND u.subscriptionStatus = 'FREE'")
    List<Usuario> findUsersWithoutSubscription();

    @Query("SELECT u FROM Usuario u WHERE u.subscriptionStatus = 'CANCELLED'")
    List<Usuario> findUsersWithCancelledSubscriptions();

    @Query("SELECT u FROM Usuario u WHERE u.subscriptionStatus = 'EXPIRED'")
    List<Usuario> findUsersWithExpiredSubscriptions();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.creadoEn BETWEEN :start AND :end")
    Integer countUsersRegisteredBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombre = :roleName")
    List<Usuario> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT u FROM Usuario u WHERE u.activo = true " +
           "AND u.creadoEn >= :since ORDER BY u.creadoEn DESC")
    List<Usuario> findRecentActiveUsers(@Param("since") LocalDateTime since);

    @Query("SELECT u FROM Usuario u WHERE u.currentSubscription IS NOT NULL " +
           "AND u.currentSubscription.status = 'ACTIVE' " +
           "AND u.currentSubscription.endDate BETWEEN CURRENT_TIMESTAMP AND :renewalDate " +
           "AND u.currentSubscription.autoRenew = true")
    List<Usuario> findUsersForRenewal(@Param("renewalDate") LocalDateTime renewalDate);
}
