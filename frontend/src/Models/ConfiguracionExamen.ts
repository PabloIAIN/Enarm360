import { Especialidad } from "./Especialidad";
import { Dificultad } from "./Dificultad";

export interface ConfiguracionExamen {
  especialidades: Especialidad[];
  dificultades: Dificultad[];
}
