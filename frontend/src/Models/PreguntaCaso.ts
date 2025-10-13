// src/Models/preguntaCaso.ts
export type Dificultad = "FACIL" | "MEDIO" | "DIFICIL";

export interface CasoEstudio {
  id: number;
  caso: string;
  imagen?: string | null;
}

export interface PreguntaCaso {
  id: number;
  pregunta: string;
  respuestaA: string;
  respuestaB: string;
  respuestaC: string;
  respuestaD: string;
  respuestaCorrecta?: string;   // solo viene en revisión
  retroalimentacion?: string;   // solo viene en revisión
  imagen?: string | null;
  especialidadId?: number;
  dificultad: Dificultad;       // 👈 ya viene como enum string
  fechaHora: string;
  caso?: CasoEstudio;           // ✅ Caso clínico al que pertenece
}
