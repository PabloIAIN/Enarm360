// ========================================
// TIPOS DE EXAMEN
// ========================================

export type TipoExamen = 'RAPIDO' | 'FILTRADO' | 'ENARM';

export type TipoPregunta = 'REACTIVO' | 'CASO';

// ========================================
// REQUEST DTOs
// ========================================

export interface CrearExamenRequest {
  tipoExamen: TipoExamen;
  usuarioId: number;

  // Campos opcionales para examen FILTRADO
  especialidadIds?: number[];
  cantidadReactivos?: number;
  cantidadCasos?: number;
  dificultad?: Dificultad;
}

export interface RespuestaUsuario {
  orden: number;
  respuesta: string | null; // 'a', 'b', 'c', 'd' o null
  tiempoSeg: number;
}

export interface FinalizarExamenRequest {
  intentoId: number;
  duracionSeg: number;
  respuestas: RespuestaUsuario[];
}

// ========================================
// RESPONSE DTOs
// ========================================

export interface PreguntaExamen {
  id: number;
  orden: number;
  tipo: TipoPregunta;

  // Para reactivos
  reactivoId?: number;

  // Para preguntas de caso
  preguntaCasoId?: number;
  casoEstudioId?: number;
  casoEstudioContenido?: string;
  imagenCaso?: string;

  // Contenido de la pregunta
  pregunta: string;
  respuestaA: string;
  respuestaB: string;
  respuestaC: string;
  respuestaD: string;
  imagenPregunta?: string;
}

export interface IniciarExamenResponse {
  examenId: number;
  intentoId: number;
  tipoExamen: string;
  totalPreguntas: number;
  tiempoLimiteMin: number;
  preguntas: PreguntaExamen[];
}

export interface DetallePreguntaResultado {
  orden: number;
  tipo: TipoPregunta;

  // Contenido de la pregunta
  pregunta: string;
  respuestaA: string;
  respuestaB: string;
  respuestaC: string;
  respuestaD: string;
  imagenPregunta?: string;

  // Caso de estudio (si aplica)
  casoEstudioContenido?: string;
  imagenCaso?: string;

  // Respuestas y resultados
  respuestaUsuario: string | null;
  respuestaCorrecta: string;
  esCorrecta: boolean;
  retroalimentacion: string;

  // Tiempo
  tiempoSeg: number;
}

export interface ResultadoExamen {
  intentoId: number;
  examenId: number;
  tipoExamen: string;

  // Estadísticas generales
  totalPreguntas: number;
  correctas: number;
  incorrectas: number;
  enBlanco: number;
  porcentajeAciertos: number;
  duracionSeg: number;
  iniciadoEn: string;
  finalizadoEn: string;

  // Detalles de cada pregunta con retroalimentación
  preguntas: DetallePreguntaResultado[];
}

// ========================================
// ESTADO DEL EXAMEN EN EL FRONTEND
// ========================================

export interface EstadoExamen {
  examenId: number;
  intentoId: number;
  tipoExamen: TipoExamen;
  totalPreguntas: number;
  tiempoLimiteMin: number;
  preguntas: PreguntaExamen[];
  preguntaActual: number;
  respuestas: Map<number, string | null>; // orden -> respuesta
  tiemposPregunta: Map<number, number>; // orden -> segundos
  tiempoInicio: Date;
  tiempoTranscurrido: number; // en segundos
}

// ========================================
// ESPECIALIDADES Y DIFICULTADES
// ========================================

export interface Especialidad {
  id: number;
  nombre: string;
}

export interface Dificultad {
  id: number;
  nombre: string;
}

// ========================================
// PERSISTENCIA DE PROGRESO
// ========================================

export interface GuardarProgresoRequest {
  intentoId: number;
  preguntaActual: number;
  tiempoTranscurrido: number;
  pausado: boolean;
  respuestas: Record<number, string>; // orden -> respuesta (a,b,c,d)
}

export interface ExamenEnProgreso {
  intentoId: number;
  tipoExamen: string;
  nombreExamen: string;
  totalPreguntas: number;
  preguntaActual: number;
  tiempoTranscurrido: number;
  pausado: boolean;
  iniciadoEn: string;
}

export interface RecuperarProgresoResponse {
  examenData: IniciarExamenResponse;
  preguntaActual: number;
  tiempoTranscurrido: number;
  pausado: boolean;
  respuestas: Record<number, string>; // orden -> respuesta
}
