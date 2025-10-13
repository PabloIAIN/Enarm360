import api from "./api";
import {
  CrearExamenRequest,
  FinalizarExamenRequest,
  IniciarExamenResponse,
  ResultadoExamen,
  Dificultad,
  GuardarProgresoRequest,
  ExamenEnProgreso,
  RecuperarProgresoResponse
} from '../types/examen.types';

const API_URL = "/api/examenes";

// ========================================
// SERVICIO DE EXÁMENES - NUEVO SISTEMA
// ========================================

// Variables para evitar múltiples llamadas simultáneas
let verificandoProgreso = false;
let ultimaVerificacion: Promise<ExamenEnProgreso | null> | null = null;

export const examenService = {
  /**
   * Crear e iniciar un examen (cualquier tipo)
   */
  crearExamen: async (request: CrearExamenRequest): Promise<IniciarExamenResponse> => {
    console.log('🎯 Creando examen:', request);
    const response = await api.post<IniciarExamenResponse>(`${API_URL}/crear`, request);
    console.log('✅ Examen creado:', response.data);
    return response.data;
  },

  /**
   * Crear examen rápido (10 preguntas aleatorias)
   */
  crearExamenRapido: async (usuarioId: number): Promise<IniciarExamenResponse> => {
    return examenService.crearExamen({
      tipoExamen: 'RAPIDO',
      usuarioId
    });
  },

  /**
   * Crear examen filtrado (personalizado)
   */
  crearExamenFiltrado: async (
    usuarioId: number,
    especialidadIds?: number[],
    cantidadReactivos?: number,
    cantidadCasos?: number,
    dificultad?: Dificultad
  ): Promise<IniciarExamenResponse> => {
    const request: CrearExamenRequest = {
      tipoExamen: 'FILTRADO',
      usuarioId,
      especialidadIds,
      cantidadReactivos,
      cantidadCasos,
      dificultad
    };

    const response = await api.post(`${API_URL}/crear`, request);
    return response.data;
  },

  /**
   * Crear simulacro ENARM (280 preguntas)
   */
  crearSimulacroENARM: async (usuarioId: number): Promise<IniciarExamenResponse> => {
    return examenService.crearExamen({
      tipoExamen: 'ENARM',
      usuarioId
    });
  },

  /**
   * Finalizar examen y obtener resultados
   */
  finalizarExamen: async (request: FinalizarExamenRequest): Promise<ResultadoExamen> => {
    console.log('🏁 Finalizando examen:', request);
    const response = await api.post<ResultadoExamen>(`${API_URL}/finalizar`, request);
    console.log('✅ Resultados obtenidos:', response.data);
    return response.data;
  },

  /**
   * Obtener resultados de un intento previo
   */
  obtenerResultados: async (intentoId: number): Promise<ResultadoExamen> => {
    console.log('📊 Obteniendo resultados del intento:', intentoId);
    const response = await api.get<ResultadoExamen>(`${API_URL}/resultados/${intentoId}`);
    return response.data;
  },

  /**
   * Health check
   */
  healthCheck: async (): Promise<string> => {
    const response = await api.get<string>(`${API_URL}/health`);
    return response.data;
  },

  // ========================================
  // PERSISTENCIA DE PROGRESO
  // ========================================

  /**
   * Guardar progreso del examen
   */
  guardarProgreso: async (request: GuardarProgresoRequest): Promise<void> => {
    console.log('💾 Guardando progreso:', request);
    await api.post(`${API_URL}/guardar-progreso`, request);
    console.log('✅ Progreso guardado');
  },

  /**
   * Verificar si hay examen en progreso
   */
  verificarExamenEnProgreso: async (usuarioId: number): Promise<ExamenEnProgreso | null> => {
    // Agregar stack trace para identificar quién está llamando
    const stack = new Error().stack;
    const caller = stack?.split('\n')[2]?.trim() || 'unknown';
    console.log('🔎 [FRONTEND_VERIFICAR] ====== INICIO ======');
    console.log('🔍 [FRONTEND_VERIFICAR] Verificando examen en progreso para usuario:', usuarioId);
    console.log('📍 [FRONTEND_VERIFICAR] Llamado desde:', caller);

    // Evitar múltiples llamadas simultáneas: reusar la última si está en curso
    if (verificandoProgreso && ultimaVerificacion) {
      console.log('⚠️ [FRONTEND_VERIFICAR] Reutilizando verificación en curso');
      return ultimaVerificacion;
    }

    verificandoProgreso = true;
    ultimaVerificacion = (async () => {
      try {
        console.log('🌐 [FRONTEND_VERIFICAR] Llamando GET /api/examenes/en-progreso/' + usuarioId);
        const response = await api.get<ExamenEnProgreso>(`${API_URL}/en-progreso/${usuarioId}`);

        console.log('✅ [FRONTEND_VERIFICAR] Respuesta recibida - Status:', response.status);
        console.log('📦 [FRONTEND_VERIFICAR] Examen en progreso encontrado:');
        console.log('   - IntentoId:', response.data.intentoId);
        console.log('   - Tipo:', response.data.tipoExamen);
        console.log('   - Nombre:', response.data.nombreExamen);
        console.log('   - Pregunta:', response.data.preguntaActual + 1, '/', response.data.totalPreguntas);
        console.log('   - Tiempo:', response.data.tiempoTranscurrido, 's');
        console.log('   - Pausado:', response.data.pausado);
        console.log('🎉 [FRONTEND_VERIFICAR] ====== ÉXITO ======');

        return response.data;
      } catch (error: any) {
        if (error.response?.status === 204) {
          console.log('📭 [FRONTEND_VERIFICAR] No hay examen en progreso (204 No Content)');
          console.log('🏁 [FRONTEND_VERIFICAR] ====== FIN (Sin examen) ======');
          return null;
        }

        console.error('❌ [FRONTEND_VERIFICAR] ERROR:');
        console.error('   - Status:', error.response?.status);
        console.error('   - Message:', error.message);
        console.error('   - Data:', error.response?.data);
        console.log('🚨 [FRONTEND_VERIFICAR] ====== ERROR ======');
        throw error;
      } finally {
        verificandoProgreso = false;
        // Resetear después de un corto tiempo para permitir futuras verificaciones
        setTimeout(() => { ultimaVerificacion = null; }, 300);
      }
    })();

    return ultimaVerificacion;
  },

  /**
   * Recuperar progreso de un examen
   */
  recuperarProgreso: async (intentoId: number): Promise<RecuperarProgresoResponse> => {
    console.log('🔎 [FRONTEND_RECUPERAR] ====== INICIO ======');
    console.log('📥 [FRONTEND_RECUPERAR] Recuperando progreso del intento:', intentoId);

    try {
      console.log('🌐 [FRONTEND_RECUPERAR] Llamando GET /api/examenes/recuperar-progreso/' + intentoId);
      const response = await api.get<RecuperarProgresoResponse>(`${API_URL}/recuperar-progreso/${intentoId}`);

      console.log('✅ [FRONTEND_RECUPERAR] Respuesta recibida - Status:', response.status);
      console.log('📦 [FRONTEND_RECUPERAR] Progreso recuperado:');
      console.log('   - Pregunta actual:', response.data.preguntaActual);
      console.log('   - Tiempo transcurrido:', response.data.tiempoTranscurrido, 's');
      console.log('   - Pausado:', response.data.pausado);
      console.log('   - Respuestas guardadas:', Object.keys(response.data.respuestas).length);
      console.log('   - Total preguntas:', response.data.examenData.totalPreguntas);
      console.log('   - IntentoId:', response.data.examenData.intentoId);
      console.log('🎉 [FRONTEND_RECUPERAR] ====== ÉXITO ======');

      return response.data;
    } catch (error: any) {
      console.error('❌ [FRONTEND_RECUPERAR] ERROR:');
      console.error('   - Status:', error.response?.status);
      console.error('   - Message:', error.message);
      console.error('   - Data:', error.response?.data);
      console.log('🚨 [FRONTEND_RECUPERAR] ====== ERROR ======');
      throw error;
    }
  },

  /**
   * Abandonar examen en progreso
   */
  abandonarExamen: async (intentoId: number): Promise<void> => {
    console.log('🚫 Abandonando examen:', intentoId);
    await api.post(`${API_URL}/abandonar/${intentoId}`);
    console.log('✅ Examen abandonado');
  }
};

export default examenService;
