// src/models/IntentoExamen.ts

import { IntentoPregunta } from "./IntentoPregunta";
import { Examen } from "./examen";  // ya lo tienes


export interface IntentoExamen {
  id: number;

  correctas: number;
  incorrectas: number;
  enBlanco: number;

  duracionSeg: number;
  iniciadoEn: string;   // LocalDateTime → string ISO
  finalizadoEn: string; // LocalDateTime → string ISO

  puntajeTotal: number;

  examen?: Examen;              // 🔹 depende si tu backend manda objeto o solo id
  usuarioId?: number;           // opcional si lo quieres simplificado
  preguntas: IntentoPregunta[]; // 🔹 relación 1–N
}
