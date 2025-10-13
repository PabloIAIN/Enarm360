/**
 * Utilidad para guardar y recuperar progreso de examen en localStorage
 * Sirve como backup local del progreso
 */

export interface ProgresoLocalExamen {
  intentoId: number;
  preguntaActual: number;
  tiempoTranscurrido: number;
  pausado: boolean;
  respuestas: Record<number, string>;
  guardadoEn: string;
}

export type ProgresoLocalInput = Omit<ProgresoLocalExamen, 'guardadoEn'>;

const STORAGE_KEY = 'examen_progreso';

export const examenStorage = {
  /**
   * Guarda el progreso en localStorage
   */
  guardarProgreso: (progreso: ProgresoLocalInput): void => {
    try {
      const data = {
        ...progreso,
        guardadoEn: new Date().toISOString()
      };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
      console.log('💾 Progreso guardado en localStorage');
    } catch (error) {
      console.error('Error guardando progreso en localStorage:', error);
    }
  },

  /**
   * Recupera el progreso de localStorage
   */
  recuperarProgreso: (): ProgresoLocalExamen | null => {
    try {
      const data = localStorage.getItem(STORAGE_KEY);
      if (!data) return null;

      const progreso = JSON.parse(data) as ProgresoLocalExamen;
      console.log('📥 Progreso recuperado de localStorage');
      return progreso;
    } catch (error) {
      console.error('Error recuperando progreso de localStorage:', error);
      return null;
    }
  },

  /**
   * Limpia el progreso guardado
   */
  limpiarProgreso: (): void => {
    try {
      localStorage.removeItem(STORAGE_KEY);
      console.log('🗑️ Progreso limpiado de localStorage');
    } catch (error) {
      console.error('Error limpiando progreso:', error);
    }
  },

  /**
   * Verifica si hay progreso guardado
   */
  tieneProgreso: (): boolean => {
    return localStorage.getItem(STORAGE_KEY) !== null;
  }
};
