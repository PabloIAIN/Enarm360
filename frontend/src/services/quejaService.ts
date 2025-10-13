import api from './api';

const API_URL = '/api/quejas-sugerencias';

export interface QuejaSugerenciaDTO {
  id?: number;
  usuarioId: number;
  tipoObjetivo: 'REACTIVO' | 'PREGUNTA_CASO';
  referenciaId: number;
  rating: number;
  comentario?: string;
  fechaCreacion?: string;
  reactivoId?: number;
  preguntaCasoId?: number;
  ratingPromedio?: number;
  totalReviews?: number;
}

export const quejaService = {
  // Crear una nueva queja/sugerencia
  async crear(data: QuejaSugerenciaDTO): Promise<QuejaSugerenciaDTO> {
    const response = await api.post(API_URL, data);
    return response.data;
  },

  // Actualizar una queja/sugerencia existente
  async actualizar(id: number, data: QuejaSugerenciaDTO): Promise<QuejaSugerenciaDTO> {
    const response = await api.put(`${API_URL}/${id}`, data);
    return response.data;
  },

  // Obtener quejas/sugerencias por usuario
  async obtenerPorUsuario(usuarioId: number): Promise<QuejaSugerenciaDTO[]> {
    const response = await api.get(`${API_URL}/usuario/${usuarioId}`);
    return response.data;
  },

  // Obtener quejas/sugerencias por reactivo
  async obtenerPorReactivo(reactivoId: number): Promise<QuejaSugerenciaDTO[]> {
    const response = await api.get(`${API_URL}/reactivo/${reactivoId}`);
    return response.data;
  },

  // Obtener quejas/sugerencias por pregunta de caso
  async obtenerPorPreguntaCaso(preguntaCasoId: number): Promise<QuejaSugerenciaDTO[]> {
    const response = await api.get(`${API_URL}/pregunta-caso/${preguntaCasoId}`);
    return response.data;
  },

  // Obtener el review de un usuario para una pregunta específica
  async obtenerReviewUsuario(
    usuarioId: number,
    tipoObjetivo: 'REACTIVO' | 'PREGUNTA_CASO',
    referenciaId: number
  ): Promise<QuejaSugerenciaDTO | null> {
    try {
      const response = await api.get(`${API_URL}/usuario/${usuarioId}/review`, {
        params: { tipoObjetivo, referenciaId }
      });
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  // Obtener promedio de rating
  async obtenerPromedioRating(
    tipoObjetivo: 'REACTIVO' | 'PREGUNTA_CASO',
    referenciaId: number
  ): Promise<number> {
    const response = await api.get(`${API_URL}/promedio-rating`, {
      params: { tipoObjetivo, referenciaId }
    });
    return response.data;
  },

  // Eliminar una queja/sugerencia
  async eliminar(id: number): Promise<void> {
    await api.delete(`${API_URL}/${id}`);
  }
};
