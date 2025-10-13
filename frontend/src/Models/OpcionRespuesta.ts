// src/models/OpcionRespuesta.ts
export interface OpcionRespuesta {
  id: number;
  texto: string;
  orden: number;
  peso?: number;
  porcentajeCorrecto?: number;
  explicacion?: string;
  justificacion?: string;
  esCorrecta: boolean;
}
