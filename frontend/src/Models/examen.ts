import { ExamenPregunta } from "./ExamenPregunta";
import { TipoExamen } from "./TipoExamen";
import { Especialidad } from "./Especialidad";

export interface Examen {
  examenPreguntas: any;
  id: number;
  nombre: string;
  descripcion: string;
  creadoEn: string;
  creadoPor: number;
  tiempoLimiteMin?: number;

  tipoExamen: TipoExamen;

  preguntas: ExamenPregunta[];
  totalReactivos: number;
  totalCasos: number;
  totalPreguntas: number;

  especialidadId?: number;
  especialidades?: Especialidad[];
}


export function getTipoPregunta(pregunta: ExamenPregunta): "REACTIVO" | "CASO" {
  return pregunta.tipo || (pregunta.reactivoId ? "REACTIVO" : "CASO");
}

export function esReactivo(pregunta: ExamenPregunta): boolean {
  return getTipoPregunta(pregunta) === "REACTIVO";
}

export function esCaso(pregunta: ExamenPregunta): boolean {
  return getTipoPregunta(pregunta) === "CASO";
}
