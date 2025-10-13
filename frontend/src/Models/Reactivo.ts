// src/Models/reactivo.ts
export interface Reactivo {
  id: number;
  pregunta: string;
  respuestaA: string;
  respuestaB: string;
  respuestaC: string;
  respuestaD: string;
  retroalimentacion?: string;
}
