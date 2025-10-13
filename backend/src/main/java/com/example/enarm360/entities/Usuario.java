package com.example.enarm360.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

@Entity
@Table(name = "usuario")
@Data
@EqualsAndHashCode(exclude = {"roles", "permisos", "sesiones", "eventosAuditoria", "subscriptions", "paymentHistory", "couponUsages", "createdCoupons"})
@ToString(exclude = {"roles", "permisos", "sesiones", "subscriptions", "paymentHistory", "couponUsages", "createdCoupons"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(unique = true, nullable = false, length = 150)
    private String email;
    
    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;
    
    @Column(nullable = false, length = 120)
    private String nombre;
    
    @Column(length = 120)
    private String apellidos;
    
    @Column(unique = true, nullable = false, length = 20)
    private String telefono;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
    
    // Campos de verificación
    @Builder.Default
    @Column(name = "email_verificado", nullable = false)
    private Boolean emailVerificado = false;
    
    @Builder.Default
    @Column(name = "telefono_verificado", nullable = false)
    private Boolean telefonoVerificado = false;
    
    @CreationTimestamp
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
    
    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
    
    // ============================================================
    // NUEVAS PROPIEDADES PARA SISTEMA DE SUSCRIPCIONES
    // ============================================================
    
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", length = 20)
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.FREE;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_subscription_id")
    private UserSubscription currentSubscription;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<UserSubscription> subscriptions = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<PaymentHistory> paymentHistory = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<CouponUsage> couponUsages = new ArrayList<>();
    
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<DiscountCoupon> createdCoupons = new ArrayList<>();
    
    // ============================================================
    // RELACIONES ORIGINALES DEL SISTEMA
    // ============================================================
    
    // Relaciones Many-to-Many
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuario_rol",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuario_permiso",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    @Builder.Default
    private Set<Permiso> permisos = new HashSet<>();
    
    // Relaciones One-to-One y One-to-Many
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PerfilUsuario perfil;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @Builder.Default
    private List<SesionAuth> sesiones = new ArrayList<>();
    
    @OneToMany(mappedBy = "actor")
    @Builder.Default
    private List<EventoAuditoria> eventosAuditoria = new ArrayList<>();
    
    // ============================================================
    // ENUMS PARA SISTEMA DE SUSCRIPCIONES
    // ============================================================
    
    public enum SubscriptionStatus {
        FREE, TRIAL, STANDARD, PREMIUM, EXPIRED, CANCELLED
    }
    
    // ============================================================
    // MÉTODOS DE UTILIDAD PARA SUSCRIPCIONES
    // ============================================================
    
    /**
     * Verifica si el usuario tiene una suscripción activa
     */
    public boolean hasActiveSubscription() {
        return currentSubscription != null && currentSubscription.isActive();
    }
    
    /**
     * Verifica si el usuario tiene acceso a una característica específica
     */
    public boolean hasFeature(String feature) {
        if (!hasActiveSubscription()) {
            // Usuarios gratuitos tienen acceso básico
            return switch (feature) {
                case "basic_questions" -> true;
                case "limited_attempts" -> true;
                case "basic_statistics" -> true;
                default -> false;
            };
        }
        
        SubscriptionPlan plan = currentSubscription.getPlan();
        return switch (feature) {
            case "clinical_cases" -> plan.getHasClinicalCases();
            case "expert_explanations" -> plan.getHasExpertExplanations();
            case "detailed_analytics" -> plan.getHasDetailedAnalytics();
            case "progress_tracking" -> plan.getHasProgressTracking();
            case "priority_support" -> plan.getHasPrioritySupport();
            case "unlimited_attempts" -> plan.getMaxAttempts() == null;
            case "all_questions" -> plan.getMaxAttempts() != null || plan.getHasClinicalCases();
            default -> false;
        };
    }
    
    /**
     * Verifica si el usuario puede tomar un examen
     */
    public boolean canTakeExam() {
        if (!hasActiveSubscription()) {
            // Los usuarios gratuitos pueden tomar exámenes con limitaciones
            return true; // Implementar lógica de límites según tus reglas de negocio
        }
        
        if (currentSubscription.getPlan().getMaxAttempts() == null) {
            return true; // Intentos ilimitados
        }
        
        // Aquí podrías agregar lógica para contar intentos del período actual
        // Por ejemplo, contar intentos del mes actual desde intento_examen
        return true; // Implementar lógica de conteo real
    }
    
    /**
     * Obtiene el número de intentos restantes del usuario
     */
    public int getRemainingAttempts() {
        if (!hasActiveSubscription()) {
            // Implementar lógica para usuarios gratuitos
            return 10; // Ejemplo: 10 intentos gratuitos por mes
        }
        
        SubscriptionPlan plan = currentSubscription.getPlan();
        if (plan.getMaxAttempts() == null) {
            return Integer.MAX_VALUE; // Ilimitados
        }
        
        // Implementar lógica para contar intentos usados y calcular restantes
        // Ejemplo: consultar tabla intento_examen para contar intentos del mes actual
        return plan.getMaxAttempts(); // Placeholder
    }
    
    /**
     * Obtiene el nombre del plan de suscripción para mostrar
     */
    public String getSubscriptionDisplayName() {
        if (!hasActiveSubscription()) {
            return "Plan Gratuito";
        }
        return currentSubscription.getPlan().getName();
    }
    
    /**
     * Verifica si la suscripción está próxima a expirar
     */
    public boolean isSubscriptionExpiringSoon() {
        if (!hasActiveSubscription() || currentSubscription.getEndDate() == null) {
            return false;
        }
        
        LocalDateTime expirationDate = currentSubscription.getEndDate();
        LocalDateTime warningDate = LocalDateTime.now().plusDays(7); // 7 días de advertencia
        
        return expirationDate.isBefore(warningDate);
    }
    
    /**
     * Actualiza el estado de suscripción basado en el plan actual
     */
    public void updateSubscriptionStatus() {
        if (currentSubscription == null || !currentSubscription.isActive()) {
            this.subscriptionStatus = SubscriptionStatus.FREE;
            return;
        }
        
        String planName = currentSubscription.getPlan().getName().toLowerCase();
        this.subscriptionStatus = switch (planName) {
            case "free trial" -> SubscriptionStatus.TRIAL;
            case "standard" -> SubscriptionStatus.STANDARD;
            case "premium" -> SubscriptionStatus.PREMIUM;
            default -> SubscriptionStatus.FREE;
        };
        
        // Verificar si está expirada
        if (currentSubscription.isExpired()) {
            this.subscriptionStatus = SubscriptionStatus.EXPIRED;
        }
        
        // Verificar si está cancelada
        if (currentSubscription.getStatus() == UserSubscription.SubscriptionStatus.CANCELLED) {
            this.subscriptionStatus = SubscriptionStatus.CANCELLED;
        }
    }
    
    /**
     * Obtiene información resumida de la suscripción
     */
    public String getSubscriptionSummary() {
        if (!hasActiveSubscription()) {
            return "Plan Gratuito - Acceso básico";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append(currentSubscription.getPlan().getName());
        
        if (currentSubscription.getEndDate() != null) {
            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDateTime.now(), 
                currentSubscription.getEndDate()
            );
            if (daysRemaining > 0) {
                summary.append(" (").append(daysRemaining).append(" días restantes)");
            } else {
                summary.append(" (Expirada)");
            }
        }
        
        return summary.toString();
    }
    
    /**
     * Verifica si el usuario tiene un rol específico
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(rol -> rol.getNombre().equalsIgnoreCase(roleName));
    }
    
    /**
     * Verifica si el usuario tiene un permiso específico
     */
    public boolean hasPermission(String permissionCode) {
        // Verificar permisos directos
        boolean hasDirectPermission = permisos.stream()
                .anyMatch(permiso -> permiso.getCodigo().equalsIgnoreCase(permissionCode));
        
        if (hasDirectPermission) {
            return true;
        }
        
        // Verificar permisos a través de roles
        return roles.stream()
                .flatMap(rol -> rol.getPermisos().stream())
                .anyMatch(permiso -> permiso.getCodigo().equalsIgnoreCase(permissionCode));
    }
    
    /**
     * Método de conveniencia para obtener el nombre completo
     */
    public String getNombreCompleto() {
        StringBuilder nombreCompleto = new StringBuilder(nombre);
        if (apellidos != null && !apellidos.trim().isEmpty()) {
            nombreCompleto.append(" ").append(apellidos);
        }
        return nombreCompleto.toString();
    }
    
    // ============================================================
    // MÉTODOS PARA INTEGRACIÓN CON SISTEMA DE EXÁMENES
    // ============================================================
    
    /**
     * Verifica si puede acceder a un caso clínico específico
     */
    public boolean canAccessClinicalCase() {
        return hasFeature("clinical_cases");
    }
    
    /**
     * Verifica si puede ver explicaciones de expertos
     */
    public boolean canViewExpertExplanations() {
        return hasFeature("expert_explanations");
    }
    
    /**
     * Verifica si puede acceder a análisis detallados
     */
    public boolean canViewDetailedAnalytics() {
        return hasFeature("detailed_analytics");
    }
    
    /**
     * Verifica si puede hacer seguimiento de progreso
     */
    public boolean canTrackProgress() {
        return hasFeature("progress_tracking");
    }
}