import api from "./api";

export interface Especialidad {
  id: number;
  nombre: string;
}

const API_URL = "/api/especialidades";

export const especialidadService = {
  /**
   * Obtener todas las especialidades
   */
  listar: async (): Promise<Especialidad[]> => {
    const response = await api.get<Especialidad[]>(API_URL);
    return response.data;
  }
};

export default especialidadService;
