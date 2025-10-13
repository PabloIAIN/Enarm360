import axios from 'axios';

const API_URL = 'http://localhost:8080/api/quejas-sugerencias';

export interface QuejaSugerenciaDTO {
  id?: number;
  usuarioId: number;
  tipoObjetivo: string; // 'reactivo' o 'pregunta_caso'
  referenciaId: number; // ID del reactivo o pregunta_caso
  rating: number; // 1-5
  comentario?: string;
  fechaCreacion?: string;
}

class QuejaSugerenciaService {
  async crear(data: QuejaSugerenciaDTO): Promise<QuejaSugerenciaDTO> {
    const response = await axios.post(API_URL, data);
    return response.data;
  }

  async actualizar(id: number, data: QuejaSugerenciaDTO): Promise<QuejaSugerenciaDTO> {
    const response = await axios.put(`${API_URL}/${id}`, data);
    return response.data;
  }

  async obtenerPorUsuario(usuarioId: number): Promise<QuejaSugerenciaDTO[]> {
    const response = await axios.get(`${API_URL}/usuario/${usuarioId}`);
    return response.data;
  }

  async obtenerPorReactivo(reactivoId: number): Promise<QuejaSugerenciaDTO[]> {
    const response = await axios.get(`${API_URL}/reactivo/${reactivoId}`);
    return response.data;
  }

  async obtenerReviewUsuario(
    usuarioId: number,
    tipoObjetivo: string,
    referenciaId: number
  ): Promise<QuejaSugerenciaDTO | null> {
    try {
      const response = await axios.get(`${API_URL}/usuario/${usuarioId}/review`, {
        params: { tipoObjetivo, referenciaId }
      });
      return response.data;
    } catch (error) {
      return null;
    }
  }

  async obtenerPromedioRating(tipoObjetivo: string, referenciaId: number): Promise<number> {
    const response = await axios.get(`${API_URL}/promedio-rating`, {
      params: { tipoObjetivo, referenciaId }
    });
    return response.data;
  }

  async eliminar(id: number): Promise<void> {
    await axios.delete(`${API_URL}/${id}`);
  }
}

export const quejaSugerenciaService = new QuejaSugerenciaService();
