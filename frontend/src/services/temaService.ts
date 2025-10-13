import api from "./api";

export interface Tema {
  id: number;
  nombre: string;
  especialidadId: number;
}

const API_URL = "/api/temas";

export const temaService = {
  /**
   * Obtener todos los temas
   */
  listar: async (): Promise<Tema[]> => {
    const response = await api.get<Tema[]>(API_URL);
    return response.data;
  }
};

export default temaService;
