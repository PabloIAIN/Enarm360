package com.example.enarm360.services;

import com.example.enarm360.dtos.*;
import com.example.enarm360.entities.*;
import com.example.enarm360.repositories.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamenService {

    private static final Logger log = LoggerFactory.getLogger(ExamenService.class);

    private final ExamenRepository examenRepository;
    private final IntentoExamenRepository intentoExamenRepository;
    private final IntentoPreguntaRepository intentoPreguntaRepository;
    private final ReactivoRepository reactivoRepository;
    private final PreguntaCasoRepository preguntaCasoRepository;
    private final CasosEstudioRepository casosEstudioRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoExamenRepository tipoExamenRepository;
    private final ExamenPreguntaRepository examenPreguntaRepository;

    // ========================================
    // CREAR EXAMEN - Genera el examen según tipo
    // ========================================

    @Transactional
    public IniciarExamenResponseDTO crearExamen(CrearExamenRequestDTO request) {
        log.info("🎯 Creando examen tipo: {}", request.getTipoExamen());

        Usuario usuario = getUsuarioAutenticado();
        Examen examen;

        switch (request.getTipoExamen().toUpperCase()) {
            case "RAPIDO":
                examen = generarExamenRapido(usuario);
                break;
            case "FILTRADO":
                examen = generarExamenFiltrado(request, usuario);
                break;
            case "ENARM":
                examen = generarSimulacroENARM(usuario);
                break;
            default:
                throw new RuntimeException("Tipo de examen no válido: " + request.getTipoExamen());
        }

        // Crear el intento
        IntentoExamen intento = IntentoExamen.builder()
                .examen(examen)
                .usuario(usuario)
                .iniciadoEn(LocalDateTime.now())
                .correctas(0)
                .incorrectas(0)
                .enBlanco(0)
                .duracionSeg(0)
                .estado("EN_PROGRESO")
                .preguntaActual(0)
                .tiempoTranscurrido(0)
                .pausado(false)
                .build();

        intento = intentoExamenRepository.save(intento);
        log.info("✅ Intento creado con ID: {}", intento.getId());

        // Cargar preguntas para el response
        return construirIniciarExamenResponse(examen, intento);
    }

    // ========================================
    // EXAMEN RÁPIDO - 10 preguntas aleatorias
    // ========================================

    @Transactional
    public Examen generarExamenRapido(Usuario usuario) {
        log.info("📝 Generando examen rápido");

        TipoExamen tipoExamen = obtenerOCrearTipoExamen("RAPIDO", "Examen Rápido");

        Examen examen = Examen.builder()
                .nombre("Examen Rápido")
                .descripcion("10 preguntas aleatorias")
                .tipoExamen(tipoExamen)
                .creadoPor(usuario.getId())
                .creadoEn(LocalDateTime.now())
                .tiempoLimiteMin(15)
                .build();

        examen = examenRepository.save(examen);

        // 10 reactivos aleatorios para garantizar siempre 10 preguntas
        int numReactivos = 10;

        agregarReactivosAleatorios(examen, numReactivos, null, null, 1);

        return examen;
    }

    // ========================================
    // EXAMEN FILTRADO - Según filtros del usuario
    // ========================================

    @Transactional
    public Examen generarExamenFiltrado(CrearExamenRequestDTO request, Usuario usuario) {
        // Valores por defecto si son null
        int cantidadReactivos = request.getCantidadReactivos() != null ? request.getCantidadReactivos() : 0;
        int cantidadCasos = request.getCantidadCasos() != null ? request.getCantidadCasos() : 0;

        log.info("🔍 Generando examen filtrado - Reactivos: {}, Casos: {}", cantidadReactivos, cantidadCasos);

        TipoExamen tipoExamen = obtenerOCrearTipoExamen("FILTRADO", "Examen Filtrado");

        Examen examen = Examen.builder()
                .nombre("Examen Filtrado")
                .descripcion(String.format("%d reactivos + %d casos", cantidadReactivos, cantidadCasos))
                .tipoExamen(tipoExamen)
                .creadoPor(usuario.getId())
                .creadoEn(LocalDateTime.now())
                .tiempoLimiteMin(calcularTiempoLimite(cantidadReactivos + cantidadCasos * 5))
                .build();

        examen = examenRepository.save(examen);

        int orden = 1;

        // Agregar reactivos
        if (cantidadReactivos > 0) {
            orden = agregarReactivosAleatorios(
                    examen,
                    cantidadReactivos,
                    request.getEspecialidadIds(),
                    request.getDificultadId(),
                    orden
            );
        }

        // Agregar casos
        if (cantidadCasos > 0) {
            agregarCasosAleatorios(
                    examen,
                    cantidadCasos,
                    request.getEspecialidadIds(),
                    request.getDificultadId(),
                    orden
            );
        }

        return examen;
    }

    // ========================================
    // SIMULACRO ENARM - 280 preguntas
    // ========================================

    @Transactional
    public Examen generarSimulacroENARM(Usuario usuario) {
        log.info("🏥 Generando Simulacro ENARM");

        TipoExamen tipoExamen = obtenerOCrearTipoExamen("ENARM", "Simulacro ENARM");

        Examen examen = Examen.builder()
                .nombre("Simulacro ENARM")
                .descripcion("~280 preguntas tipo ENARM (250 reactivos + 30 casos mezclados)")
                .tipoExamen(tipoExamen)
                .creadoPor(usuario.getId())
                .creadoEn(LocalDateTime.now())
                .tiempoLimiteMin(240) // 4 horas
                .build();

        examen = examenRepository.save(examen);

        // 250 reactivos + ~30 preguntas de casos para llegar a 280 total
        // NOTA: Ajustado para usar los casos disponibles en BD
        int numReactivos = 250; // Reactivos aleatorios
        int numCasos = 30;      // Casos de estudio (~30 preguntas de casos)

        // Generar preguntas mezcladas
        generarPreguntasMezcladas(examen, numReactivos, numCasos);

        return examen;
    }

    // ========================================
    // MÉTODOS AUXILIARES PARA CREAR EXÁMENES
    // ========================================

    private int agregarReactivosAleatorios(Examen examen, int cantidad,
                                           List<Long> especialidadIds, Long dificultadId,
                                           int ordenInicial) {
        List<Reactivo> reactivos;

        if (especialidadIds != null && !especialidadIds.isEmpty() && dificultadId != null) {
            reactivos = reactivoRepository.findRandomByEspecialidadesAndDificultad(
                    especialidadIds, dificultadId, cantidad);
        } else if (especialidadIds != null && !especialidadIds.isEmpty()) {
            reactivos = reactivoRepository.findRandomByEspecialidades(especialidadIds, cantidad);
        } else if (dificultadId != null) {
            reactivos = reactivoRepository.findRandomByDificultad(dificultadId, cantidad);
        } else {
            reactivos = reactivoRepository.findRandomReactivos(cantidad);
        }

        log.info("📌 Reactivos encontrados: {}", reactivos.size());

        int orden = ordenInicial;
        for (Reactivo reactivo : reactivos) {
            ExamenPregunta ep = ExamenPregunta.builder()
                    .examen(examen)
                    .reactivo(reactivo)
                    .orden(orden++)
                    .puntaje(1.0)
                    .build();
            examenPreguntaRepository.save(ep);
        }

        return orden;
    }

    private void agregarCasosAleatorios(Examen examen, int cantidadCasos,
                                       List<Long> especialidadIds, Long dificultadId, int ordenInicial) {
        List<CasoEstudio> casos;

        if (especialidadIds != null && !especialidadIds.isEmpty() && dificultadId != null) {
            casos = casosEstudioRepository.findRandomByEspecialidadesAndDificultad(
                    especialidadIds, dificultadId, cantidadCasos);
        } else if (especialidadIds != null && !especialidadIds.isEmpty()) {
            casos = casosEstudioRepository.findRandomByEspecialidades(especialidadIds, cantidadCasos);
        } else if (dificultadId != null) {
            casos = casosEstudioRepository.findRandomByDificultad(dificultadId, cantidadCasos);
        } else {
            casos = casosEstudioRepository.findRandomCasos(cantidadCasos);
        }

        log.info("📚 Casos encontrados: {}", casos.size());

        int orden = ordenInicial;
        int preguntasCasosGuardadas = 0;
        for (CasoEstudio caso : casos) {
            List<PreguntaCaso> preguntas = preguntaCasoRepository.findByCasoEstudioId(caso.getId());
            log.info("  - Caso {}: {} preguntas", caso.getId(), preguntas.size());

            for (PreguntaCaso pregunta : preguntas) {
                ExamenPregunta ep = ExamenPregunta.builder()
                        .examen(examen)
                        .preguntaCaso(pregunta)
                        .orden(orden++)
                        .puntaje(1.0)
                        .build();
                ExamenPregunta saved = examenPreguntaRepository.save(ep);
                preguntasCasosGuardadas++;
                log.info("    ✅ ExamenPregunta guardada: ID={}, Orden={}, PreguntaCasoId={}", 
                    saved.getId(), saved.getOrden(), pregunta.getId());
            }
        }
        log.info("📊 Total preguntas de casos guardadas: {}", preguntasCasosGuardadas);
    }

    /**
     * Genera preguntas mezcladas para el simulacro ENARM
     */
    private void generarPreguntasMezcladas(Examen examen, int numReactivos, int numCasos) {
        log.info("🔀 Generando preguntas mezcladas: {} reactivos + {} casos", numReactivos, numCasos);
        
        // Obtener reactivos aleatorios
        List<Reactivo> reactivos = reactivoRepository.findRandomReactivos(numReactivos);
        log.info("📌 Reactivos encontrados: {}", reactivos.size());
        
        // Obtener casos aleatorios
        List<CasoEstudio> casos = casosEstudioRepository.findRandomCasos(numCasos);
        log.info("📚 Casos encontrados: {}", casos.size());
        
        // Crear lista de todas las preguntas con sus tipos
        List<Object> todasLasPreguntas = new ArrayList<>();
        
        // Agregar reactivos
        for (Reactivo reactivo : reactivos) {
            todasLasPreguntas.add(reactivo);
        }
        
        // Agregar preguntas de casos
        int preguntasCasosAgregadas = 0;
        for (CasoEstudio caso : casos) {
            List<PreguntaCaso> preguntasCaso = preguntaCasoRepository.findByCasoEstudioId(caso.getId());
            log.info("  - Caso {}: {} preguntas", caso.getId(), preguntasCaso.size());
            
            for (PreguntaCaso preguntaCaso : preguntasCaso) {
                todasLasPreguntas.add(preguntaCaso);
                preguntasCasosAgregadas++;
            }
        }
        
        log.info("📊 Total elementos para mezclar: {} (reactivos: {}, preguntas de casos: {})", 
                todasLasPreguntas.size(), reactivos.size(), preguntasCasosAgregadas);
        
        // Mezclar las preguntas aleatoriamente
        java.util.Collections.shuffle(todasLasPreguntas);
        
        // Guardar en orden mezclado
        int orden = 1;
        int reactivosGuardados = 0;
        int preguntasCasosGuardadas = 0;
        
        for (Object pregunta : todasLasPreguntas) {
            ExamenPregunta ep = ExamenPregunta.builder()
                    .examen(examen)
                    .orden(orden++)
                    .puntaje(1.0)
                    .build();
            
            if (pregunta instanceof Reactivo) {
                ep.setReactivo((Reactivo) pregunta);
                reactivosGuardados++;
            } else if (pregunta instanceof PreguntaCaso) {
                ep.setPreguntaCaso((PreguntaCaso) pregunta);
                preguntasCasosGuardadas++;
            }
            
            examenPreguntaRepository.save(ep);
        }
        
        log.info("✅ Preguntas mezcladas guardadas: {} reactivos, {} preguntas de casos, {} total", 
                reactivosGuardados, preguntasCasosGuardadas, orden - 1);
    }

    // ========================================
    // CONSTRUIR RESPONSE AL INICIAR EXAMEN
    // ========================================

    private IniciarExamenResponseDTO construirIniciarExamenResponse(Examen examen, IntentoExamen intento) {
        // Cargar preguntas
        List<ExamenPregunta> examenPreguntas = examenPreguntaRepository
                .findByExamenIdOrderByOrden(examen.getId());

        log.info("📋 Total preguntas cargadas: {}", examenPreguntas.size());
        
        // Contar tipos de preguntas
        int countReactivos = 0;
        int countCasos = 0;
        for (ExamenPregunta ep : examenPreguntas) {
            if (ep.getReactivo() != null) countReactivos++;
            if (ep.getPreguntaCaso() != null) countCasos++;
        }
        log.info("📈 Distribución: {} reactivos, {} preguntas de casos", countReactivos, countCasos);

        List<PreguntaExamenDTO> preguntas = new ArrayList<>();

        for (ExamenPregunta ep : examenPreguntas) {
            PreguntaExamenDTO dto = new PreguntaExamenDTO();
            dto.setId(ep.getId());
            dto.setOrden(ep.getOrden());

            if (ep.getReactivo() != null) {
                // Es un reactivo
                Reactivo r = ep.getReactivo();
                dto.setTipo("REACTIVO");
                dto.setReactivoId(r.getId());
                dto.setPregunta(r.getPregunta());
                dto.setRespuestaA(r.getRespuestaA());
                dto.setRespuestaB(r.getRespuestaB());
                dto.setRespuestaC(r.getRespuestaC());
                dto.setRespuestaD(r.getRespuestaD());
                // NO incluir respuesta correcta ni retroalimentación
            } else if (ep.getPreguntaCaso() != null) {
                // Es una pregunta de caso
                PreguntaCaso pc = ep.getPreguntaCaso();
                log.info("  📝 Procesando pregunta de caso: Orden={}, PreguntaCasoId={}", ep.getOrden(), pc.getId());
                dto.setTipo("CASO");
                dto.setPreguntaCasoId(pc.getId());
                dto.setPregunta(pc.getPregunta());
                dto.setRespuestaA(pc.getRespuestaA());
                dto.setRespuestaB(pc.getRespuestaB());
                dto.setRespuestaC(pc.getRespuestaC());
                dto.setRespuestaD(pc.getRespuestaD());
                dto.setImagenPregunta(pc.getImagen());

                // Incluir caso de estudio
                if (pc.getCasoEstudio() != null) {
                    dto.setCasoEstudioId(pc.getCasoEstudio().getId());
                    dto.setCasoEstudioContenido(pc.getCasoEstudio().getCaso());
                    dto.setImagenCaso(pc.getCasoEstudio().getImagen());
                    log.info("    ✅ Caso de estudio incluido: CasoId={}", pc.getCasoEstudio().getId());
                } else {
                    log.warn("    ⚠️ PreguntaCaso sin CasoEstudio asociado: {}", pc.getId());
                }
            }

            preguntas.add(dto);
        }

        return IniciarExamenResponseDTO.builder()
                .examenId(examen.getId())
                .intentoId(intento.getId())
                .tipoExamen(examen.getTipoExamen().getCodigo())
                .totalPreguntas(preguntas.size())
                .tiempoLimiteMin(examen.getTiempoLimiteMin())
                .preguntas(preguntas)
                .build();
    }

    // ========================================
    // FINALIZAR EXAMEN Y GUARDAR RESULTADOS
    // ========================================

    @Transactional
    public ResultadoExamenDTO finalizarExamen(FinalizarExamenRequestDTO request) {
        log.info("🏁 Finalizando examen - Intento ID: {}", request.getIntentoId());

        IntentoExamen intento = intentoExamenRepository.findById(request.getIntentoId())
                .orElseThrow(() -> new RuntimeException("Intento no encontrado"));

        Examen examen = intento.getExamen();

        // Cargar preguntas del examen
        List<ExamenPregunta> examenPreguntas = examenPreguntaRepository
                .findByExamenIdOrderByOrden(examen.getId());

        int correctas = 0;
        int incorrectas = 0;
        int enBlanco = 0;

        List<DetallePreguntaResultadoDTO> detallesPreguntas = new ArrayList<>();

        for (ExamenPregunta ep : examenPreguntas) {
            // Buscar respuesta del usuario
            RespuestaUsuarioDTO respuestaUsuario = request.getRespuestas().stream()
                    .filter(r -> r.getOrden().equals(ep.getOrden()))
                    .findFirst()
                    .orElse(null);

            String respuestaSeleccionada = respuestaUsuario != null ? respuestaUsuario.getRespuesta() : null;
            Integer tiempoSeg = respuestaUsuario != null ? respuestaUsuario.getTiempoSeg() : 0;

            DetallePreguntaResultadoDTO detalle = new DetallePreguntaResultadoDTO();
            detalle.setOrden(ep.getOrden());
            detalle.setRespuestaUsuario(respuestaSeleccionada);
            detalle.setTiempoSeg(tiempoSeg);

            String respuestaCorrecta = null;
            String retroalimentacion = null;
            boolean esCorrecta = false;

            if (ep.getReactivo() != null) {
                // Reactivo
                Reactivo r = ep.getReactivo();
                detalle.setTipo("REACTIVO");
                detalle.setPregunta(r.getPregunta());
                detalle.setRespuestaA(r.getRespuestaA());
                detalle.setRespuestaB(r.getRespuestaB());
                detalle.setRespuestaC(r.getRespuestaC());
                detalle.setRespuestaD(r.getRespuestaD());

                respuestaCorrecta = r.getRespuestaCorrecta();
                retroalimentacion = r.getRetroalimentacion();

                if (respuestaSeleccionada != null) {
                    esCorrecta = respuestaSeleccionada.equalsIgnoreCase(respuestaCorrecta);
                }

                // Guardar IntentoPregunta
                IntentoPregunta ip = IntentoPregunta.builder()
                        .intentoExamen(intento)
                        .reactivo(r)
                        .orden(ep.getOrden())
                        .respuesta(respuestaSeleccionada)
                        .respondida(respuestaSeleccionada != null)
                        .correcta(esCorrecta)
                        .tiempoSeg(tiempoSeg)
                        .enunciadoSnap(r.getPregunta())
                        .explicacionSnap(r.getRetroalimentacion())
                        .build();
                intentoPreguntaRepository.save(ip);

            } else if (ep.getPreguntaCaso() != null) {
                // Pregunta de caso
                PreguntaCaso pc = ep.getPreguntaCaso();
                detalle.setTipo("CASO");
                detalle.setPregunta(pc.getPregunta());
                detalle.setRespuestaA(pc.getRespuestaA());
                detalle.setRespuestaB(pc.getRespuestaB());
                detalle.setRespuestaC(pc.getRespuestaC());
                detalle.setRespuestaD(pc.getRespuestaD());
                detalle.setImagenPregunta(pc.getImagen());

                if (pc.getCasoEstudio() != null) {
                    detalle.setCasoEstudioContenido(pc.getCasoEstudio().getCaso());
                    detalle.setImagenCaso(pc.getCasoEstudio().getImagen());
                }

                respuestaCorrecta = pc.getRespuestaCorrecta();
                retroalimentacion = pc.getRetroalimentacion();

                if (respuestaSeleccionada != null) {
                    esCorrecta = respuestaSeleccionada.equalsIgnoreCase(respuestaCorrecta);
                }

                // Guardar IntentoPregunta
                IntentoPregunta ip = IntentoPregunta.builder()
                        .intentoExamen(intento)
                        .orden(ep.getOrden())
                        .respuesta(respuestaSeleccionada)
                        .respondida(respuestaSeleccionada != null)
                        .correcta(esCorrecta)
                        .tiempoSeg(tiempoSeg)
                        .enunciadoSnap(pc.getPregunta())
                        .explicacionSnap(pc.getRetroalimentacion())
                        .build();
                intentoPreguntaRepository.save(ip);
            }

            detalle.setRespuestaCorrecta(respuestaCorrecta);
            detalle.setRetroalimentacion(retroalimentacion);
            detalle.setEsCorrecta(esCorrecta);

            // Contadores
            if (respuestaSeleccionada == null) {
                enBlanco++;
            } else if (esCorrecta) {
                correctas++;
            } else {
                incorrectas++;
            }

            detallesPreguntas.add(detalle);
        }

        // Actualizar intento
        intento.setCorrectas(correctas);
        intento.setIncorrectas(incorrectas);
        intento.setEnBlanco(enBlanco);
        intento.setDuracionSeg(request.getDuracionSeg());
        intento.setFinalizadoEn(LocalDateTime.now());
        intento.setPuntajeTotal((double) correctas);
        intento.setEstado("FINALIZADO"); // Marcar como finalizado

        intentoExamenRepository.save(intento);

        log.info("✅ Examen finalizado - Correctas: {}, Incorrectas: {}, En blanco: {}",
                correctas, incorrectas, enBlanco);

        // Construir response
        double porcentaje = examenPreguntas.size() > 0
                ? (correctas * 100.0) / examenPreguntas.size()
                : 0;

        return ResultadoExamenDTO.builder()
                .intentoId(intento.getId())
                .examenId(examen.getId())
                .tipoExamen(examen.getTipoExamen().getCodigo())
                .totalPreguntas(examenPreguntas.size())
                .correctas(correctas)
                .incorrectas(incorrectas)
                .enBlanco(enBlanco)
                .porcentajeAciertos(porcentaje)
                .duracionSeg(request.getDuracionSeg())
                .iniciadoEn(intento.getIniciadoEn())
                .finalizadoEn(intento.getFinalizadoEn())
                .preguntas(detallesPreguntas)
                .build();
    }

    // ========================================
    // OBTENER RESULTADOS DE UN INTENTO
    // ========================================

    @Transactional(readOnly = true)
    public ResultadoExamenDTO obtenerResultados(Long intentoId) {
        log.info("📊 Obteniendo resultados del intento: {}", intentoId);

        IntentoExamen intento = intentoExamenRepository.findByIdWithPreguntas(intentoId)
                .orElseThrow(() -> new RuntimeException("Intento no encontrado"));

        Examen examen = intento.getExamen();
        List<IntentoPregunta> respuestas = intento.getPreguntas();

        // Cargar preguntas del examen en orden
        List<ExamenPregunta> examenPreguntas = examenPreguntaRepository
                .findByExamenIdOrderByOrden(examen.getId());

        List<DetallePreguntaResultadoDTO> detalles = new ArrayList<>();

        for (ExamenPregunta ep : examenPreguntas) {
            IntentoPregunta respuesta = respuestas.stream()
                    .filter(r -> r.getOrden().equals(ep.getOrden()))
                    .findFirst()
                    .orElse(null);

            DetallePreguntaResultadoDTO detalle = new DetallePreguntaResultadoDTO();
            detalle.setOrden(ep.getOrden());

            if (respuesta != null) {
                detalle.setRespuestaUsuario(respuesta.getRespuesta());
                detalle.setEsCorrecta(respuesta.isCorrecta());
                detalle.setTiempoSeg(respuesta.getTiempoSeg());
                detalle.setRetroalimentacion(respuesta.getExplicacionSnap());
            }

            if (ep.getReactivo() != null) {
                Reactivo r = ep.getReactivo();
                detalle.setTipo("REACTIVO");
                detalle.setPregunta(r.getPregunta());
                detalle.setRespuestaA(r.getRespuestaA());
                detalle.setRespuestaB(r.getRespuestaB());
                detalle.setRespuestaC(r.getRespuestaC());
                detalle.setRespuestaD(r.getRespuestaD());
                detalle.setRespuestaCorrecta(r.getRespuestaCorrecta());
            } else if (ep.getPreguntaCaso() != null) {
                PreguntaCaso pc = ep.getPreguntaCaso();
                detalle.setTipo("CASO");
                detalle.setPregunta(pc.getPregunta());
                detalle.setRespuestaA(pc.getRespuestaA());
                detalle.setRespuestaB(pc.getRespuestaB());
                detalle.setRespuestaC(pc.getRespuestaC());
                detalle.setRespuestaD(pc.getRespuestaD());
                detalle.setImagenPregunta(pc.getImagen());
                detalle.setRespuestaCorrecta(pc.getRespuestaCorrecta());

                if (pc.getCasoEstudio() != null) {
                    detalle.setCasoEstudioContenido(pc.getCasoEstudio().getCaso());
                    detalle.setImagenCaso(pc.getCasoEstudio().getImagen());
                }
            }

            detalles.add(detalle);
        }

        double porcentaje = intento.getCorrectas() != null && examenPreguntas.size() > 0
                ? (intento.getCorrectas() * 100.0) / examenPreguntas.size()
                : 0;

        return ResultadoExamenDTO.builder()
                .intentoId(intento.getId())
                .examenId(examen.getId())
                .tipoExamen(examen.getTipoExamen().getCodigo())
                .totalPreguntas(examenPreguntas.size())
                .correctas(intento.getCorrectas())
                .incorrectas(intento.getIncorrectas())
                .enBlanco(intento.getEnBlanco())
                .porcentajeAciertos(porcentaje)
                .duracionSeg(intento.getDuracionSeg())
                .iniciadoEn(intento.getIniciadoEn())
                .finalizadoEn(intento.getFinalizadoEn())
                .preguntas(detalles)
                .build();
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no autenticado"));
    }

    private TipoExamen obtenerOCrearTipoExamen(String codigo, String nombre) {
        Optional<TipoExamen> tipoExistente = tipoExamenRepository.findByCodigo(codigo);

        if (tipoExistente.isPresent()) {
            log.info("✅ Tipo de examen encontrado: {}", codigo);
            return tipoExistente.get();
        }

        // Verificar si ya existe antes de intentar crear
        if (tipoExamenRepository.existsByCodigo(codigo)) {
            log.warn("⚠️ El tipo de examen {} existe pero no se pudo recuperar con findByCodigo. Reintentando...", codigo);
            // Forzar flush y reintentar
            tipoExamenRepository.flush();
            return tipoExamenRepository.findByCodigo(codigo)
                    .orElseThrow(() -> new RuntimeException("Error al obtener tipo de examen existente: " + codigo));
        }

        log.info("🆕 Creando nuevo tipo de examen: {}", codigo);
        try {
            TipoExamen nuevo = TipoExamen.builder()
                    .codigo(codigo)
                    .nombre(nombre)
                    .descripcion(nombre)
                    .activo(true)
                    .build();
            return tipoExamenRepository.save(nuevo);
        } catch (Exception e) {
            // Si falla por duplicado, intentar recuperarlo una vez más
            log.error("❌ Error al crear tipo de examen {}: {}", codigo, e.getMessage());
            return tipoExamenRepository.findByCodigo(codigo)
                    .orElseThrow(() -> new RuntimeException("No se pudo crear ni recuperar el tipo de examen: " + codigo, e));
        }
    }

    private Integer calcularTiempoLimite(int totalPreguntas) {
        // 1.5 minutos por pregunta
        return (int) Math.ceil(totalPreguntas * 1.5);
    }

    // ========================================
    // MÉTODOS PARA PERSISTENCIA DE PROGRESO
    // ========================================

    /**
     * Guarda el progreso del examen
     */
    @Transactional
    public void guardarProgreso(GuardarProgresoDTO request) {
        log.info("🔎 [GUARDAR_PROGRESO] ====== INICIO ======");
        log.info("💾 [GUARDAR_PROGRESO] Guardando progreso del intento: {}", request.getIntentoId());
        log.info("📊 [GUARDAR_PROGRESO] Datos recibidos:");
        log.info("   - Pregunta actual: {}", request.getPreguntaActual());
        log.info("   - Tiempo transcurrido: {}s", request.getTiempoTranscurrido());
        log.info("   - Pausado: {}", request.getPausado());
        log.info("   - Respuestas a guardar: {}", request.getRespuestas() != null ? request.getRespuestas().size() : 0);

        IntentoExamen intento = intentoExamenRepository.findById(request.getIntentoId())
                .orElseThrow(() -> {
                    log.error("❌ [GUARDAR_PROGRESO] Intento {} no encontrado", request.getIntentoId());
                    return new RuntimeException("Intento no encontrado");
                });

        log.info("✅ [GUARDAR_PROGRESO] Intento encontrado - Estado actual: {}", intento.getEstado());

        // Permitir guardar progreso SOLO para exámenes FILTRADO
        Examen examen = intento.getExamen();
        if (examen == null || examen.getTipoExamen() == null ||
            !"FILTRADO".equalsIgnoreCase(examen.getTipoExamen().getCodigo())) {
            log.error("❌ [GUARDAR_PROGRESO] Tipo de examen no permitido: {}",
                    examen != null && examen.getTipoExamen() != null ? examen.getTipoExamen().getCodigo() : "NULL");
            throw new RuntimeException("Guardar progreso solo está permitido para exámenes de tipo FILTRADO");
        }
        log.info("✅ [GUARDAR_PROGRESO] Validación de tipo FILTRADO: OK");

        if (!"EN_PROGRESO".equalsIgnoreCase(intento.getEstado())) {
            log.error("❌ [GUARDAR_PROGRESO] Estado inválido: {}", intento.getEstado());
            throw new RuntimeException("No se puede guardar progreso: el intento no está en progreso");
        }
        log.info("✅ [GUARDAR_PROGRESO] Validación de estado EN_PROGRESO: OK");

        // Actualizar campos de progreso
        log.info("📝 [GUARDAR_PROGRESO] Actualizando campos de progreso...");
        intento.setPreguntaActual(request.getPreguntaActual());
        intento.setTiempoTranscurrido(request.getTiempoTranscurrido());
        intento.setPausado(request.getPausado());

        // Guardar respuestas temporales en IntentoPregunta
        if (request.getRespuestas() != null && !request.getRespuestas().isEmpty()) {
            log.info("💾 [GUARDAR_PROGRESO] Guardando {} respuestas...", request.getRespuestas().size());
             examen = intento.getExamen();
            List<ExamenPregunta> examenPreguntas = examenPreguntaRepository
                    .findByExamenIdOrderByOrden(examen.getId());

            int respuestasCreadas = 0;
            int respuestasActualizadas = 0;

            for (Map.Entry<Integer, String> entry : request.getRespuestas().entrySet()) {
                Integer orden = entry.getKey();
                String respuesta = entry.getValue();

                if (respuesta == null || respuesta.isEmpty()) continue;

                // Buscar si ya existe IntentoPregunta para este orden
                Optional<IntentoPregunta> intentoPreguntaOpt = intento.getPreguntas().stream()
                        .filter(ip -> ip.getOrden().equals(orden))
                        .findFirst();

                IntentoPregunta intentoPregunta;
                if (intentoPreguntaOpt.isPresent()) {
                    // Actualizar existente
                    intentoPregunta = intentoPreguntaOpt.get();
                    intentoPregunta.setRespuesta(respuesta);
                    respuestasActualizadas++;
                } else {
                    // Crear nuevo
                    ExamenPregunta ep = examenPreguntas.stream()
                            .filter(e -> e.getOrden().equals(orden))
                            .findFirst()
                            .orElse(null);

                    if (ep != null) {
                        intentoPregunta = IntentoPregunta.builder()
                                .intentoExamen(intento)
                                .orden(orden)
                                .respuesta(respuesta)
                                .respondida(true)
                                .enunciadoSnap(ep.getReactivo() != null ? ep.getReactivo().getPregunta() :
                                              ep.getPreguntaCaso().getPregunta())
                                .build();
                        intento.getPreguntas().add(intentoPregunta);
                        respuestasCreadas++;
                    }
                }
            }
            log.info("✅ [GUARDAR_PROGRESO] Respuestas procesadas: {} creadas, {} actualizadas",
                    respuestasCreadas, respuestasActualizadas);
        }

        log.info("💾 [GUARDAR_PROGRESO] Guardando en BD...");
        intentoExamenRepository.save(intento);
        log.info("🎉 [GUARDAR_PROGRESO] ====== ÉXITO ======");
    }

    /**
     * Verifica si el usuario tiene un examen en progreso
     */
    @Transactional
    public ExamenEnProgresoDTO verificarExamenEnProgreso(Long usuarioId) {
        log.info("🔎 [SERVICE_VERIFICAR] ====== INICIO ======");
        log.info("🔍 [SERVICE_VERIFICAR] Verificando examen en progreso para usuario: {}", usuarioId);

        // Limpiar intentos duplicados antes de verificar
        log.info("🧹 [SERVICE_VERIFICAR] Limpiando intentos duplicados...");
        limpiarIntentosEnProgresoDuplicados(usuarioId);

        log.info("🔎 [SERVICE_VERIFICAR] Buscando intento EN_PROGRESO en BD...");
        Optional<IntentoExamen> intentoOpt = intentoExamenRepository
                .findFirstExamenEnProgresoByUsuarioId(usuarioId);

        if (intentoOpt.isEmpty()) {
            log.info("📭 [SERVICE_VERIFICAR] No hay examen en progreso para usuario {}", usuarioId);
            log.info("🏁 [SERVICE_VERIFICAR] ====== FIN (Sin examen) ======");
            return null;
        }

        IntentoExamen intento = intentoOpt.get();
        log.info("✅ [SERVICE_VERIFICAR] Intento encontrado: ID={}, Estado={}",
                intento.getId(), intento.getEstado());

        Examen examen = intento.getExamen();

        // Validar que el examen existe
        if (examen == null) {
            log.warn("⚠️ [SERVICE_VERIFICAR] Se encontró intento {} pero el examen asociado no existe. Marcando como abandonado.",
                    intento.getId());
            intento.setEstado("ABANDONADO");
            intentoExamenRepository.save(intento);
            log.info("🏁 [SERVICE_VERIFICAR] ====== FIN (Examen nulo) ======");
            return null;
        }
        log.info("✅ [SERVICE_VERIFICAR] Examen asociado: ID={}, Nombre={}",
                examen.getId(), examen.getNombre());

        // Validar que tiene tipo de examen
        if (examen.getTipoExamen() == null) {
            log.warn("⚠️ [SERVICE_VERIFICAR] El examen {} no tiene tipo de examen asociado. Marcando intento como abandonado.",
                    examen.getId());
            intento.setEstado("ABANDONADO");
            intentoExamenRepository.save(intento);
            log.info("🏁 [SERVICE_VERIFICAR] ====== FIN (Tipo examen nulo) ======");
            return null;
        }
        log.info("✅ [SERVICE_VERIFICAR] Tipo de examen: {}", examen.getTipoExamen().getCodigo());

        // Solo retornar exámenes FILTRADO
        if (!"FILTRADO".equalsIgnoreCase(examen.getTipoExamen().getCodigo())) {
            log.info("⚠️ [SERVICE_VERIFICAR] Se encontró examen en progreso tipo '{}' pero no es FILTRADO; no se expone para reanudación",
                    examen.getTipoExamen().getCodigo());
            log.info("🏁 [SERVICE_VERIFICAR] ====== FIN (No es FILTRADO) ======");
            return null;
        }

        TipoExamen tipoExamen = examen.getTipoExamen();
        int totalPreguntas = (int) examenPreguntaRepository.countByExamenId(examen.getId());

        log.info("📊 [SERVICE_VERIFICAR] Construyendo DTO de examen en progreso:");
        log.info("   - IntentoId: {}", intento.getId());
        log.info("   - Tipo: {}", tipoExamen.getCodigo());
        log.info("   - Total preguntas: {}", totalPreguntas);
        log.info("   - Pregunta actual: {}", intento.getPreguntaActual());
        log.info("   - Tiempo: {}s", intento.getTiempoTranscurrido());
        log.info("   - Pausado: {}", intento.getPausado());

        ExamenEnProgresoDTO resultado = ExamenEnProgresoDTO.builder()
                .intentoId(intento.getId())
                .tipoExamen(tipoExamen.getCodigo())
                .nombreExamen(examen.getNombre())
                .totalPreguntas(totalPreguntas)
                .preguntaActual(intento.getPreguntaActual())
                .tiempoTranscurrido(intento.getTiempoTranscurrido())
                .pausado(intento.getPausado())
                .iniciadoEn(intento.getIniciadoEn())
                .build();

        log.info("🎉 [SERVICE_VERIFICAR] ====== ÉXITO ======");
        return resultado;
    }

    /**
     * Recupera el progreso completo de un examen
     */
    @Transactional(readOnly = true)
    public RecuperarProgresoDTO recuperarProgreso(Long intentoId) {
        log.info("🔎 [RECUPERAR_PROGRESO] ====== INICIO ======");
        log.info("📥 [RECUPERAR_PROGRESO] Recuperando progreso del intento: {}", intentoId);

        // Paso 1: Buscar intento
        IntentoExamen intento = intentoExamenRepository.findById(intentoId)
                .orElseThrow(() -> {
                    log.error("❌ [RECUPERAR_PROGRESO] Intento {} NO ENCONTRADO", intentoId);
                    return new RuntimeException("Intento no encontrado");
                });

        log.info("✅ [RECUPERAR_PROGRESO] Intento encontrado - Estado: {}, Pausado: {}",
                intento.getEstado(), intento.getPausado());
        log.info("📊 [RECUPERAR_PROGRESO] Pregunta actual: {}, Tiempo transcurrido: {}s",
                intento.getPreguntaActual(), intento.getTiempoTranscurrido());

        // Paso 2: Validar examen y tipo
        Examen examen = intento.getExamen();
        if (examen == null) {
            log.error("❌ [RECUPERAR_PROGRESO] El intento {} no tiene examen asociado", intentoId);
            throw new RuntimeException("El intento no tiene examen asociado");
        }
        log.info("✅ [RECUPERAR_PROGRESO] Examen asociado - ID: {}, Nombre: {}",
                examen.getId(), examen.getNombre());

        if (examen.getTipoExamen() == null) {
            log.error("❌ [RECUPERAR_PROGRESO] El examen {} no tiene tipo de examen", examen.getId());
            throw new RuntimeException("El examen no tiene tipo de examen asociado");
        }
        log.info("✅ [RECUPERAR_PROGRESO] Tipo de examen: {}", examen.getTipoExamen().getCodigo());

        if (!"FILTRADO".equalsIgnoreCase(examen.getTipoExamen().getCodigo())) {
            log.error("❌ [RECUPERAR_PROGRESO] Tipo de examen inválido: {}. Solo se permite FILTRADO",
                    examen.getTipoExamen().getCodigo());
            throw new RuntimeException("La reanudación solo está permitida para exámenes de tipo FILTRADO");
        }

        // Paso 3: Validar estado
        if (!"EN_PROGRESO".equals(intento.getEstado())) {
            log.error("❌ [RECUPERAR_PROGRESO] Estado inválido: {}. Se esperaba EN_PROGRESO",
                    intento.getEstado());
            throw new RuntimeException("El examen no está en progreso");
        }
        log.info("✅ [RECUPERAR_PROGRESO] Estado validado: EN_PROGRESO");

        // Paso 4: Construir response con datos del examen
        log.info("🏗️ [RECUPERAR_PROGRESO] Construyendo response de examen...");
        IniciarExamenResponseDTO examenData = construirIniciarExamenResponse(
                intento.getExamen(), intento);
        log.info("✅ [RECUPERAR_PROGRESO] ExamenData construido - {} preguntas cargadas",
                examenData.getPreguntas().size());

        // Paso 5: Recuperar respuestas guardadas
        log.info("📝 [RECUPERAR_PROGRESO] Recuperando respuestas guardadas...");
        Map<Integer, String> respuestas = new java.util.HashMap<>();
        int respuestasCount = 0;
        for (IntentoPregunta ip : intento.getPreguntas()) {
            if (ip.getRespuesta() != null && !ip.getRespuesta().isEmpty()) {
                respuestas.put(ip.getOrden(), ip.getRespuesta());
                respuestasCount++;
            }
        }
        log.info("✅ [RECUPERAR_PROGRESO] {} respuestas recuperadas", respuestasCount);

        // Paso 6: Construir DTO final
        RecuperarProgresoDTO resultado = RecuperarProgresoDTO.builder()
                .examenData(examenData)
                .preguntaActual(intento.getPreguntaActual())
                .tiempoTranscurrido(intento.getTiempoTranscurrido())
                .pausado(intento.getPausado())
                .respuestas(respuestas)
                .build();

        log.info("🎉 [RECUPERAR_PROGRESO] ====== ÉXITO ======");
        log.info("📦 [RECUPERAR_PROGRESO] Devolviendo progreso - Pregunta: {}/{}, Tiempo: {}s, Pausado: {}",
                resultado.getPreguntaActual(), examenData.getTotalPreguntas(),
                resultado.getTiempoTranscurrido(), resultado.getPausado());

        return resultado;
    }

    /**
     * Marca un examen como abandonado
     */
    @Transactional
    public void abandonarExamen(Long intentoId) {
        log.info("🚫 Abandonando examen: {}", intentoId);

        IntentoExamen intento = intentoExamenRepository.findById(intentoId)
                .orElseThrow(() -> new RuntimeException("Intento no encontrado"));

        intento.setEstado("ABANDONADO");
        intento.setFinalizadoEn(LocalDateTime.now());
        intentoExamenRepository.save(intento);

        log.info("✅ Examen marcado como abandonado");
    }

    /**
     * Limpia intentos de examen duplicados en progreso para un usuario
     * Mantiene solo el más reciente y marca los demás como abandonados
     */
    @Transactional
    public void limpiarIntentosEnProgresoDuplicados(Long usuarioId) {
        log.info("🧹 Limpiando intentos duplicados en progreso para usuario: {}", usuarioId);
        
        List<IntentoExamen> intentosEnProgreso = intentoExamenRepository
                .findExamenesEnProgresoByUsuarioId(usuarioId);
        
        if (intentosEnProgreso.size() <= 1) {
            log.info("No hay intentos duplicados para limpiar");
            return;
        }
        
        // Mantener solo el más reciente (primera posición ya que está ordenado por fecha DESC)
        IntentoExamen intentoReciente = intentosEnProgreso.get(0);
        log.info("Manteniendo intento más reciente: {} (iniciado: {})", 
                intentoReciente.getId(), intentoReciente.getIniciadoEn());
        
        // Marcar los demás como abandonados
        for (int i = 1; i < intentosEnProgreso.size(); i++) {
            IntentoExamen intento = intentosEnProgreso.get(i);
            intento.setEstado("ABANDONADO");
            intento.setFinalizadoEn(LocalDateTime.now());
            intentoExamenRepository.save(intento);
            log.info("Marcado como abandonado: {} (iniciado: {})", 
                    intento.getId(), intento.getIniciadoEn());
        }
        
        log.info("✅ Limpieza completada. {} intentos marcados como abandonados", 
                intentosEnProgreso.size() - 1);
    }
}
