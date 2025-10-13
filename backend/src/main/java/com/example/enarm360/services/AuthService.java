package com.example.enarm360.services;

import com.example.enarm360.dtos.auth.*;
import com.example.enarm360.entities.SesionAuth;
import com.example.enarm360.entities.Usuario;
import com.example.enarm360.repositories.SesionAuthRepository;
import com.example.enarm360.repositories.UsuarioRepository;
import com.example.enarm360.security.JwtUtils;
import com.example.enarm360.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SesionAuthRepository sesionAuthRepository;


    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Autenticar usuario con email O username y generar tokens
     */
    public LoginResponse login(LoginRequest loginRequest) {
        logger.info("Intento de login para: {}", loginRequest.getLogin());
        
        try {
            // Autenticar usuario (puede ser email o username)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getLogin(), 
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Generar tokens usando el username (no el login)
            String accessToken = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());

            // Limpiar tokens expirados del usuario
            limpiarTokensExpirados(userDetails.getId());

            // Guardar refresh token en base de datos
            SesionAuth sesion = SesionAuth.builder()
                    .usuario(usuarioRepository.findById(userDetails.getId()).orElseThrow())
                    .tokenRefresh(refreshToken)
                    .emitidoEn(LocalDateTime.now())
                    .expiraEn(LocalDateTime.now().plusSeconds(jwtUtils.getRefreshExpirationMs() / 1000))
                    .build();

            sesionAuthRepository.save(sesion);

            // Obtener información del usuario
            Usuario usuario = usuarioRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            UsuarioInfo usuarioInfo = buildUsuarioInfo(usuario);

            logger.info("Login exitoso para usuario: {} (username: {}, email: {})", 
                    usuario.getNombre(), usuario.getUsername(), usuario.getEmail());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn((long) jwtUtils.getJwtExpirationMs() / 1000)
                    .usuario(usuarioInfo)
                    .build();

        } catch (Exception e) {
            logger.error("Error en login para: {}: {}", loginRequest.getLogin(), e.getMessage());
            throw new RuntimeException("Credenciales inválidas");
        }
    }

    /**
     * Renovar access token usando refresh token
     */
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        logger.debug("Intentando renovar token");

        // Verificar si el refresh token es válido
        if (!jwtUtils.validateJwtToken(requestRefreshToken)) {
            logger.warn("Refresh token inválido");
            throw new RuntimeException("Refresh token inválido");
        }

        // Buscar el token en la base de datos
        Optional<SesionAuth> sesionOpt = sesionAuthRepository.findByTokenRefresh(requestRefreshToken);
        if (sesionOpt.isEmpty()) {
            logger.warn("Refresh token no encontrado en BD");
            throw new RuntimeException("Refresh token no encontrado");
        }

        SesionAuth sesion = sesionOpt.get();

        // Verificar si ha expirado
        if (sesion.getExpiraEn().isBefore(LocalDateTime.now())) {
            logger.warn("Refresh token expirado para usuario: {}", sesion.getUsuario().getUsername());
            sesionAuthRepository.delete(sesion);
            throw new RuntimeException("Refresh token expirado");
        }

        // Generar nuevo access token usando username
        String username = jwtUtils.getUsernameFromJwtToken(requestRefreshToken);
        String newAccessToken = jwtUtils.generateTokenFromUsername(username, jwtUtils.getJwtExpirationMs());

        logger.debug("Token renovado exitosamente para usuario: {}", username);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn((long) jwtUtils.getJwtExpirationMs() / 1000)
                .build();
    }

    /**
     * Cerrar sesión (eliminar refresh token)
     */
    public void logout(String refreshToken) {
        logger.debug("Cerrando sesión");
        
        sesionAuthRepository.findByTokenRefresh(refreshToken)
                .ifPresent(sesion -> {
                    logger.info("Logout para usuario: {}", sesion.getUsuario().getUsername());
                    sesionAuthRepository.delete(sesion);
                });
    }

    /**
     * Cerrar todas las sesiones de un usuario
     */
    public void logoutAll(Long usuarioId) {
        logger.info("Cerrando todas las sesiones para usuario ID: {}", usuarioId);
        sesionAuthRepository.deleteAllByUsuarioId(usuarioId);
    }

    /**
     * Obtener información del usuario actual
     */
    public UsuarioInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Usuario usuario = usuarioRepository.findByUsernameAndActivoTrue(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return buildUsuarioInfo(usuario);
    }

    /**
     * Verificar si un email existe
     */
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    /**
     * Verificar si un username existe
     */
    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }
    
    /**
     * Verificar si un teléfono existe
     */
    public boolean existsByTelefono(String telefono) {
        return usuarioRepository.existsByTelefono(telefono);
    }
    
    /**
     * Buscar usuario por teléfono
     */
    public Usuario findByTelefono(String telefono) {
        return usuarioRepository.findByTelefono(telefono).orElse(null);
    }

    /**
     * Verificar disponibilidad de username o email
     */
    public CheckAvailabilityResponse checkAvailability(String field, String value) {
        boolean available = false;
        String message = "";
        
        switch (field.toLowerCase()) {
            case "username":
                available = !existsByUsername(value);
                message = available ? "Username disponible" : "Username ya está en uso";
                break;
            case "email":
                available = !existsByEmail(value);
                message = available ? "Email disponible" : "Email ya está en uso";
                break;
            case "telefono":
                available = !existsByTelefono(value);
                message = available ? "Teléfono disponible" : "Teléfono ya está en uso";
                break;
            default:
                message = "Campo no válido";
        }
        
        return new CheckAvailabilityResponse(available, message);
    }

    /**
     * Limpiar tokens expirados de un usuario
     */
    private void limpiarTokensExpirados(Long usuarioId) {
        sesionAuthRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    /**
     * Construir información del usuario para el frontend
     */
    private UsuarioInfo buildUsuarioInfo(Usuario usuario) {
        Set<String> roles = usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .collect(Collectors.toSet());

        Set<String> permisos = new HashSet<>();
        
        // Permisos de roles
        usuario.getRoles().forEach(rol -> 
            rol.getPermisos().forEach(permiso -> 
                permisos.add(permiso.getCodigo())
            )
        );
        
        // Permisos directos del usuario
        usuario.getPermisos().forEach(permiso -> 
            permisos.add(permiso.getCodigo())
        );

        return UsuarioInfo.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellidos(usuario.getApellidos())
                .roles(roles)
                .permisos(permisos)
                .activo(usuario.getActivo())
                .emailVerificado(usuario.getEmailVerificado())
                .telefonoVerificado(usuario.getTelefonoVerificado())
                .requiereVerificacion(!usuario.getEmailVerificado() || !usuario.getTelefonoVerificado())
                .build();
    }

    /**
 * Verificar si usuario actual tiene un rol específico
 */
public boolean hasRole(String role) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
        return false;
    }
    return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
}


/**
 * Obtener roles del usuario actual
 */
public Set<String> getCurrentUserRoles() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
        return Collections.emptySet();
    }
    return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
}

    /**
     * Obtener conteo de sesiones activas
     */
    public Long getActiveSessionsCount(Long usuarioId) {
        return sesionAuthRepository.countActiveSessionsByUsuario(usuarioId);
    }

    // DTO para response de disponibilidad
    public static class CheckAvailabilityResponse {
        private boolean available;
        private String message;

        public CheckAvailabilityResponse(boolean available, String message) {
            this.available = available;
            this.message = message;
        }

        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}