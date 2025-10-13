// src/Models/examenPregunta.ts
import { PreguntaCaso } from "./PreguntaCaso";
import { Reactivo } from "./Reactivo";

export type TipoPregunta = "REACTIVO" | "CASO";

export interface ExamenPregunta {
  id: number;
  orden: number;
  puntaje: number;
  examenId: number;

  tipo: TipoPregunta;

  // Si es reactivo normal
  reactivoId?: number;
  reactivo?: Reactivo;

  // Si es de caso clínico
  preguntaCasoId?: number;
  preguntaCaso?: PreguntaCaso;

  // Datos planos que ya trae el backend
  enunciado?: string;
  imagen?: string;

  respuestaA?: string;
  respuestaB?: string;
  respuestaC?: string;
  respuestaD?: string;

  opciones?: { id: number; texto: string }[];

  // Solo si es revisión
  respuestaCorrecta?: string;
  retroalimentacion?: string;

  // Para casos clínicos - texto del caso separado de la pregunta
  casoClinico?: string;
}
