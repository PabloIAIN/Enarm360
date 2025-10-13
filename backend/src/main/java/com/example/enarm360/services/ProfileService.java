package com.example.enarm360.services;

import com.example.enarm360.dtos.profile.ProfileDto;
import com.example.enarm360.dtos.profile.UpdateProfileDto;
import com.example.enarm360.entities.PerfilUsuario;
import com.example.enarm360.entities.Usuario;
import com.example.enarm360.repositories.PerfilUsuarioRepository;
import com.example.enarm360.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UsuarioRepository usuarioRepo;
    private final PerfilUsuarioRepository perfilRepo;

    private Usuario currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new RuntimeException("No autenticado");
        String login = auth.getName(); // username o email
        return usuarioRepo.findByUsernameOrEmailAndActivoTrue(login)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado/activo: " + login));
    }

    private static ProfileDto toDTO(Usuario u, PerfilUsuario p) {
        return ProfileDto.builder()
                .id(u.getId())
                .email(u.getEmail())
                .username(u.getUsername())
                .nombre(u.getNombre())
                .apellido(u.getApellidos())
                .avatar(p != null ? p.getAvatarUrl() : null)
                .bio(p != null ? p.getBio() : null)

                .pais(p != null ? p.getPais() : null)
                .tz(p != null ? p.getTz() : "America/Monterrey")
                // Información personal adicional
                .fechaNacimiento(p != null ? p.getFechaNacimiento() : null)
                .genero(p != null ? p.getGenero() : null)
                // Información académica
                .universidad(p != null ? p.getUniversidad() : null)
                .anioGraduacion(p != null ? p.getAnioGraduacion() : null)
                .numeroTitulo(p != null ? p.getNumeroTitulo() : null)
                .especialidadInteres(p != null ? p.getEspecialidadInteres() : null)
                // Configuraciones
                .recibirNotificaciones(p != null ? p.getRecibirNotificaciones() : true)
                .recibirNewsletters(p != null ? p.getRecibirNewsletters() : false)
                .perfilPublico(p != null ? p.getPerfilPublico() : true)

                // Metadata del sistema
                .fechaRegistro(u.getCreadoEn())
                .ultimaActividad(u.getActualizadoEn())
                .estado(u.getActivo() ? "activo" : "inactivo")
                .roles(u.getRoles() != null ? 
                    u.getRoles().stream().map(rol -> rol.getNombre()).collect(java.util.stream.Collectors.toSet()) : 
                    new java.util.HashSet<>())
                // Configuración de privacidad
                .privacy(ProfileDto.PrivacySettingsDto.builder()
                    .mostrarEmail(p != null ? p.getMostrarEmail() : false)
                    .mostrarTelefono(p != null ? p.getMostrarTelefono() : false)
                    .mostrarUniversidad(p != null ? p.getMostrarUniversidad() : true)
                    .permitirMensajes(p != null ? p.getPermitirMensajes() : true)
                    .mostrarEstadisticas(p != null ? p.getMostrarEstadisticas() : true)
                    .build())
                .build();
    }

    @Transactional(readOnly = true)
    public ProfileDto me() {
        Usuario u = currentUser();
        PerfilUsuario p = perfilRepo.findById(u.getId()).orElse(null);
        return toDTO(u, p);
    }

    public ProfileDto upsertMyProfile(UpdateProfileDto req) {
        return upsertMyProfileWithRetry(req, 3);
    }
    
    @Transactional(rollbackFor = Exception.class)
    private ProfileDto upsertMyProfileWithRetry(UpdateProfileDto req, int maxRetries) {
        log.debug("Iniciando actualización de perfil para usuario: {}, intentos restantes: {}", 
            SecurityContextHolder.getContext().getAuthentication().getName(), maxRetries);
        
        try {
            Usuario u = currentUser();
            log.debug("Usuario encontrado: id={}, username={}", u.getId(), u.getUsername());
            
            // Actualizar campos del usuario directamente en la entidad managed
            boolean usuarioChanged = false;
            if (req.getNombre() != null && !req.getNombre().equals(u.getNombre())) {
                log.debug("Actualizando nombre: {} -> {}", u.getNombre(), req.getNombre());
                u.setNombre(req.getNombre());
                usuarioChanged = true;
            }
            if (req.getApellido() != null && !req.getApellido().equals(u.getApellidos())) {
                log.debug("Actualizando apellido: {} -> {}", u.getApellidos(), req.getApellido());
                u.setApellidos(req.getApellido());
                usuarioChanged = true;
            }
            
            // Buscar o crear perfil - usando estrategia diferente para nuevos perfiles
            PerfilUsuario p = perfilRepo.findById(u.getId()).orElse(null);
            log.debug("Perfil existente encontrado: {}", p != null ? "sí" : "no");
            
            boolean perfilChanged = false;
            boolean isNewProfile = false;
            
            if (p == null) {
                // Crear nuevo perfil
                log.debug("Creando nuevo perfil para usuario {}", u.getId());
                p = new PerfilUsuario();
                p.setUsuarioId(u.getId());
                p.setUsuario(u);
                
                // Inicializar valores por defecto para nuevo perfil
                p.setTz("America/Monterrey");
                p.setRecibirNotificaciones(true);
                p.setRecibirNewsletters(false);
                p.setPerfilPublico(true);
                p.setMostrarEmail(false);
                p.setMostrarTelefono(false);
                p.setMostrarUniversidad(true);
                p.setPermitirMensajes(true);
                p.setMostrarEstadisticas(true);
                
                perfilChanged = true;
                isNewProfile = true;
            }
            
            // Actualizar campos del perfil solo si cambiaron
            if (req.getBio() != null && !req.getBio().equals(p.getBio())) {
                p.setBio(req.getBio());
                perfilChanged = true;
            }
            if (req.getPais() != null && !req.getPais().equals(p.getPais())) {
                p.setPais(req.getPais());
                perfilChanged = true;
            }
            if (req.getTz() != null && !req.getTz().equals(p.getTz())) {
                p.setTz(req.getTz());
                perfilChanged = true;
            }
            
            // Nuevos campos de información personal
            if (req.getFechaNacimiento() != null && !req.getFechaNacimiento().equals(p.getFechaNacimiento())) {
                p.setFechaNacimiento(req.getFechaNacimiento());
                perfilChanged = true;
            }
            if (req.getGenero() != null && !req.getGenero().equals(p.getGenero())) {
                p.setGenero(req.getGenero());
                perfilChanged = true;
            }
            
            // Campos académicos
            if (req.getUniversidad() != null && !req.getUniversidad().equals(p.getUniversidad())) {
                p.setUniversidad(req.getUniversidad());
                perfilChanged = true;
            }
            if (req.getAnioGraduacion() != null && !req.getAnioGraduacion().equals(p.getAnioGraduacion())) {
                p.setAnioGraduacion(req.getAnioGraduacion());
                perfilChanged = true;
            }
            if (req.getNumeroTitulo() != null && !req.getNumeroTitulo().equals(p.getNumeroTitulo())) {
                p.setNumeroTitulo(req.getNumeroTitulo());
                perfilChanged = true;
            }
            if (req.getEspecialidadInteres() != null && !req.getEspecialidadInteres().equals(p.getEspecialidadInteres())) {
                p.setEspecialidadInteres(req.getEspecialidadInteres());
                perfilChanged = true;
            }
            
            // Configuraciones
            if (req.getRecibirNotificaciones() != null && !req.getRecibirNotificaciones().equals(p.getRecibirNotificaciones())) {
                p.setRecibirNotificaciones(req.getRecibirNotificaciones());
                perfilChanged = true;
            }
            if (req.getRecibirNewsletters() != null && !req.getRecibirNewsletters().equals(p.getRecibirNewsletters())) {
                p.setRecibirNewsletters(req.getRecibirNewsletters());
                perfilChanged = true;
            }
            if (req.getPerfilPublico() != null && !req.getPerfilPublico().equals(p.getPerfilPublico())) {
                p.setPerfilPublico(req.getPerfilPublico());
                perfilChanged = true;
            }
            
            // Configuración de privacidad
            if (req.getPrivacy() != null) {
                var privacy = req.getPrivacy();
                if (privacy.getMostrarEmail() != null && !privacy.getMostrarEmail().equals(p.getMostrarEmail())) {
                    p.setMostrarEmail(privacy.getMostrarEmail());
                    perfilChanged = true;
                }
                if (privacy.getMostrarTelefono() != null && !privacy.getMostrarTelefono().equals(p.getMostrarTelefono())) {
                    p.setMostrarTelefono(privacy.getMostrarTelefono());
                    perfilChanged = true;
                }
                if (privacy.getMostrarUniversidad() != null && !privacy.getMostrarUniversidad().equals(p.getMostrarUniversidad())) {
                    p.setMostrarUniversidad(privacy.getMostrarUniversidad());
                    perfilChanged = true;
                }
                if (privacy.getPermitirMensajes() != null && !privacy.getPermitirMensajes().equals(p.getPermitirMensajes())) {
                    p.setPermitirMensajes(privacy.getPermitirMensajes());
                    perfilChanged = true;
                }
                if (privacy.getMostrarEstadisticas() != null && !privacy.getMostrarEstadisticas().equals(p.getMostrarEstadisticas())) {
                    p.setMostrarEstadisticas(privacy.getMostrarEstadisticas());
                    perfilChanged = true;
                }
            }

            log.debug("Cambios detectados - Usuario: {}, Perfil: {}, Nuevo perfil: {}", usuarioChanged, perfilChanged, isNewProfile);

            // Guardar solo si hay cambios
            if (usuarioChanged) {
                log.debug("Guardando cambios de usuario");
                u = usuarioRepo.save(u);
            }
            if (perfilChanged) {
                if (isNewProfile) {
                    log.debug("Guardando nuevo perfil");
                    p = perfilRepo.save(p);
                } else {
                    log.debug("Actualizando perfil existente");
                    p = perfilRepo.save(p);
                }
            }
            
            log.debug("Actualización completada exitosamente");
            return toDTO(u, p);
            
        } catch (OptimisticLockingFailureException e) {
            log.warn("Error de optimistic locking detectado - esto indica concurrencia de requests", e);
            if (maxRetries > 0) {
                log.warn("Error de optimistic locking, reintentando... Intentos restantes: {}", maxRetries - 1);
                // Pausa más larga para resolver conflictos de concurrencia
                try {
                    Thread.sleep(200 + (4 - maxRetries) * 100); // Backoff exponencial
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Operación interrumpida", ie);
                }
                // Forzar una nueva transacción para el reintento
                return upsertMyProfileWithRetry(req, maxRetries - 1);
            } else {
                log.error("Error de optimistic locking después de todos los reintentos", e);
                throw new RuntimeException("Error actualizando perfil: detectamos múltiples actualizaciones simultáneas. Intente nuevamente en unos segundos.", e);
            }
        } catch (Exception e) {
            log.error("Error inesperado actualizando perfil", e);
            throw new RuntimeException("Error actualizando perfil: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ProfileDto setAvatar(String avatarUrl) {
        Usuario u = currentUser();
        PerfilUsuario p = perfilRepo.findById(u.getId()).orElseGet(() -> {
            var x = new PerfilUsuario();
            x.setUsuario(u);
            x.setUsuarioId(u.getId());
            return x;
        });
        p.setAvatarUrl(avatarUrl);
        perfilRepo.save(p);
        return toDTO(u, p);
    }

    @Transactional(readOnly = true)
    public ProfileDto byUserId(Long id) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        PerfilUsuario p = perfilRepo.findById(id).orElse(null);
        return toDTO(u, p);
    }
}
