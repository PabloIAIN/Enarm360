package com.example.enarm360.controllers;

import com.example.enarm360.dtos.*;
import com.example.enarm360.services.ExamenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/examenes")
@RequiredArgsConstructor
public class ExamenController {

    private static final Logger log = LoggerFactory.getLogger(ExamenController.class);
    private final ExamenService examenService;

    // ========================================
    // CREAR E INICIAR EXAMEN
    // ========================================

    /**
     * Endpoint único para crear cualquier tipo de examen
     *
     * Tipos de examen:
     * - RAPIDO: 10 preguntas aleatorias
     * - FILTRADO: Según filtros especificados
     * - ENARM: 280 preguntas tipo simulacro
     *
     * @param request Configuración del examen
     * @return Examen creado con todas las preguntas e intentoId para comenzar
     */
    @PostMapping("/crear")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IniciarExamenResponseDTO> crearExamen(
            @RequestBody CrearExamenRequestDTO request
    ) {
        log.info("📝 Solicitud para crear examen tipo: {}", request.getTipoExamen());

        try {
            IniciarExamenResponseDTO response = examenService.crearExamen(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error al crear examen: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear examen: " + e.getMessage());
        }
    }

    // ========================================
    // FINALIZAR EXAMEN Y OBTENER RESULTADOS
    // ========================================

    /**
     * Finalizar examen y obtener resultados completos
     *
     * Este endpoint:
     * 1. Recibe todas las respuestas del usuario
     * 2. Calcula correctas, incorrectas y en blanco
     * 3. Guarda el tiempo
     * 4. Retorna todas las preguntas con retroalimentación
     *
     * @param request Datos del intento y respuestas
     * @return Resultados completos con retroalimentación
     */
    @PostMapping("/finalizar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResultadoExamenDTO> finalizarExamen(
            @RequestBody FinalizarExamenRequestDTO request
    ) {
        log.info("Finalizando examen - Intento: {}", request.getIntentoId());

        try {
            ResultadoExamenDTO resultado = examenService.finalizarExamen(request);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("❌ Error al finalizar examen: {}", e.getMessage(), e);
            throw new RuntimeException("Error al finalizar examen: " + e.getMessage());
        }
    }

    // ========================================
    // CONSULTAR RESULTADOS PREVIOS
    // ========================================

    /**
     * Obtener resultados de un intento previo
     *
     * @param intentoId ID del intento
     * @return Resultados completos con retroalimentación
     */
    @GetMapping("/resultados/{intentoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResultadoExamenDTO> obtenerResultados(
            @PathVariable Long intentoId
    ) {
        log.info("📊 Obteniendo resultados del intento: {}", intentoId);

        try {
            ResultadoExamenDTO resultado = examenService.obtenerResultados(intentoId);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("❌ Error al obtener resultados: {}", e.getMessage(), e);
            throw new RuntimeException("Intento no encontrado");
        }
    }

    // ========================================
    // PERSISTENCIA DE PROGRESO
    // ========================================

    /**
     * Guardar progreso del examen
     */
    @PostMapping("/guardar-progreso")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> guardarProgreso(@RequestBody GuardarProgresoDTO request) {
        log.info("💾 Guardando progreso del intento: {}", request.getIntentoId());
        try {
            examenService.guardarProgreso(request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ Error al guardar progreso: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verificar si hay examen en progreso
     */
    @GetMapping("/en-progreso/{usuarioId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExamenEnProgresoDTO> verificarExamenEnProgreso(@PathVariable Long usuarioId) {
        log.info("🔎 [VERIFICAR_PROGRESO] ====== INICIO ======");
        log.info("🔍 [VERIFICAR_PROGRESO] Verificando examen en progreso para usuario: {}", usuarioId);

        try {
            ExamenEnProgresoDTO examen = examenService.verificarExamenEnProgreso(usuarioId);

            if (examen == null) {
                log.info("📭 [VERIFICAR_PROGRESO] No hay examen en progreso. Devolviendo 204 (No Content)");
                return ResponseEntity.noContent().build();
            }

            log.info("✅ [VERIFICAR_PROGRESO] Examen encontrado:");
            log.info("   - IntentoId: {}", examen.getIntentoId());
            log.info("   - Tipo: {}", examen.getTipoExamen());
            log.info("   - Nombre: {}", examen.getNombreExamen());
            log.info("   - Pregunta actual: {}/{}", examen.getPreguntaActual() + 1, examen.getTotalPreguntas());
            log.info("   - Tiempo: {}s", examen.getTiempoTranscurrido());
            log.info("   - Pausado: {}", examen.getPausado());
            log.info("🎉 [VERIFICAR_PROGRESO] Devolviendo 200 (OK) con datos del examen");

            return ResponseEntity.ok(examen);
        } catch (Exception e) {
            log.error("❌ [VERIFICAR_PROGRESO] ERROR CAPTURADO:");
            log.error("   - Tipo: {}", e.getClass().getName());
            log.error("   - Mensaje: {}", e.getMessage());
            log.error("   - Stack trace:", e);
            log.info("🚨 [VERIFICAR_PROGRESO] Devolviendo 500 (Internal Server Error)");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recuperar progreso de un examen
     */
    @GetMapping("/recuperar-progreso/{intentoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecuperarProgresoDTO> recuperarProgreso(@PathVariable Long intentoId) {
        log.info("🔎 [CONTROLLER_RECUPERAR] ====== INICIO ======");
        log.info("📥 [CONTROLLER_RECUPERAR] Intentando recuperar progreso del intento: {}", intentoId);

        try {
            RecuperarProgresoDTO progreso = examenService.recuperarProgreso(intentoId);

            log.info("✅ [CONTROLLER_RECUPERAR] Progreso recuperado exitosamente:");
            log.info("   - Pregunta actual: {}", progreso.getPreguntaActual());
            log.info("   - Tiempo transcurrido: {}s", progreso.getTiempoTranscurrido());
            log.info("   - Pausado: {}", progreso.getPausado());
            log.info("   - Respuestas guardadas: {}", progreso.getRespuestas().size());
            log.info("   - Total preguntas: {}", progreso.getExamenData().getTotalPreguntas());
            log.info("🎉 [CONTROLLER_RECUPERAR] Devolviendo 200 (OK)");

            return ResponseEntity.ok(progreso);
        } catch (Exception e) {
            log.error("❌ [CONTROLLER_RECUPERAR] ERROR CAPTURADO:");
            log.error("   - Tipo: {}", e.getClass().getName());
            log.error("   - Mensaje: {}", e.getMessage());
            log.error("   - Stack trace:", e);
            log.info("🚨 [CONTROLLER_RECUPERAR] Devolviendo 404 (Not Found)");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Abandonar examen en progreso
     */
    @PostMapping("/abandonar/{intentoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> abandonarExamen(@PathVariable Long intentoId) {
        log.info("🚫 Abandonando examen: {}", intentoId);
        try {
            examenService.abandonarExamen(intentoId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ Error al abandonar examen: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Limpiar intentos de examen duplicados en progreso
     */
    @PostMapping("/limpiar-duplicados/{usuarioId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> limpiarDuplicados(@PathVariable Long usuarioId) {
        log.info("🧹 Limpiando duplicados para usuario: {}", usuarioId);
        try {
            examenService.limpiarIntentosEnProgresoDuplicados(usuarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ Error al limpiar duplicados: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================
    // HEALTH CHECK
    // ========================================

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Examen API está funcionando correctamente");
    }
}
