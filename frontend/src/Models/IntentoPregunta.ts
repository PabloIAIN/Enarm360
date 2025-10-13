// src/models/IntentoPregunta.ts

import { Reactivo } from "./Reactivo";
import { PreguntaCaso } from "./PreguntaCaso";
import { OpcionRespuesta } from "./OpcionRespuesta";

export interface IntentoPregunta {
  id: number;

  correcta: boolean;
  respondida: boolean;

  orden: number;
  tiempoSeg: number;

  enunciadoSnap: string;      // 🔹 copia de la pregunta en el momento del intento
  explicacionSnap?: string;   // 🔹 retroalimentación guardada

  intentoExamenId?: number;   // relación con IntentoExamen

  // Origen de la pregunta
  preguntaId?: number;
  reactivoId?: number;

  // Si tu backend devuelve objetos completos:
  reactivo?: Reactivo;
  preguntaCaso?: PreguntaCaso;

  // Opción seleccionada
  opcionSeleccionadaId?: number;
  opcionSeleccionada?: OpcionRespuesta;

  // Respuesta marcada (a/b/c/d)
  respuesta?: string;

  // Respuestas para revisión
  respuestaSeleccionada: string;  // a, b, c, d
  respuestaCorrecta: string;      // a, b, c, d
  opciones?: Record<string, string>; // { a: "...", b: "...", c: "...", d: "..." }
  numeroSecuencial?: number;      // Numeración 1, 2, 3...
}
