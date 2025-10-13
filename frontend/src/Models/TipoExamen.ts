// src/Models/tipoExamen.ts
export interface TipoExamen {
  id: number;
  nombre: string;
  descripcion?: string;
  codigo: string;
  permiteCasosClinicos?: boolean;
  permitePreguntasDirectas?: boolean;
  tieneTiempoLimite?: boolean;
  esCalificable?: boolean;
  ordenVisualizacion?: number;
  activo: boolean;
}
