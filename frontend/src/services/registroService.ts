import axios from 'axios';
import {
  CheckFieldResponse,
  RegistroRequest,
  RegistroResponse,
  PasswordValidationRequest,
  PasswordValidationResponse,
  RegistroInfoResponse,
  // Nuevos tipos para wizard
  Paso1DatosPersonales,
  Paso2Contacto,
  Paso1Response,
  Paso2Response,
  RegistroInfo,
  DisponibilidadResponse,
  VerificacionEmailRequest,
  VerificacionTelefonoRequest,
  VerificacionResponse
} from '../types/registro';

const API_BASE = '/api/registro';

// Los interceptors de axios están configurados globalmente en services/index.ts

class RegistroService {
  // ==========================================================
  // REGISTRO DE USUARIOS
  // ==========================================================

  async crearCuenta(registroData: RegistroRequest): Promise<RegistroResponse> {
    try {
      const response = await axios.post<RegistroResponse>(`${API_BASE}/crear-cuenta`, registroData);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  async getRegistroInfo(): Promise<RegistroInfoResponse> {
    try {
      const response = await axios.get<RegistroInfoResponse>(`${API_BASE}/info`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // ==========================================================
  // VALIDACIONES
  // ==========================================================

  async validarPassword(passwordData: PasswordValidationRequest): Promise<PasswordValidationResponse> {
    try {
      const response = await axios.post<PasswordValidationResponse>(`${API_BASE}/validar-password`, passwordData);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  async checkAvailability(field: string, value: string): Promise<CheckFieldResponse> {
    try {
      const response = await axios.get<CheckFieldResponse>(`${API_BASE}/check-availability`, {
        params: { field, value }
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  async checkEmailAvailability(email: string): Promise<CheckFieldResponse> {
    try {
      const response = await axios.get<CheckFieldResponse>(`${API_BASE}/check-email`, {
        params: { email }
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  async checkUsernameAvailability(username: string): Promise<CheckFieldResponse> {
    try {
      const response = await axios.get<CheckFieldResponse>(`${API_BASE}/check-username`, {
        params: { username }
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // ==========================================================
  // MÉTODOS PARA WIZARD DE REGISTRO
  // ==========================================================

  async validarPaso1(data: Paso1DatosPersonales): Promise<Paso1Response> {
    try {
      const response = await axios.post<Paso1Response>(`${API_BASE}/paso1-validar`, data);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  async crearCuentaPaso2(data: Paso2Contacto): Promise<Paso2Response> {
    try {
      const response = await axios.post<Paso2Response>(`${API_BASE}/paso2-crear-cuenta`, data);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  async getInfoParaPasos(): Promise<RegistroInfo> {
    try {
      const response = await axios.get<RegistroInfo>(`${API_BASE}/paso-info`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  async checkUsernameDisponibilidad(username: string): Promise<DisponibilidadResponse> {
    try {
      const response = await axios.get<DisponibilidadResponse>(`${API_BASE}/check-username`, {
        params: { username }
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // ==========================================================
  // MÉTODOS DE VERIFICACIÓN
  // ==========================================================

  async verificarEmail(data: VerificacionEmailRequest): Promise<VerificacionResponse> {
    try {
      const response = await axios.post<VerificacionResponse>(`${API_BASE}/verificar-email`, data);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  async verificarTelefono(data: VerificacionTelefonoRequest): Promise<VerificacionResponse> {
    try {
      const response = await axios.post<VerificacionResponse>(`${API_BASE}/verificar-telefono`, data);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  async reenviarEmailVerificacion(email: string): Promise<any> {
    try {
      const response = await axios.post(`${API_BASE}/reenviar-email`, { email });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  async reenviarTelefonoVerificacion(telefono: string): Promise<any> {
    try {
      const response = await axios.post(`${API_BASE}/reenviar-telefono`, { telefono });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // ==========================================================
  // UTILIDADES
  // ==========================================================

  validateEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  validateUsername(username: string): boolean {
    // Username debe tener entre 3-20 caracteres, solo letras, números y guiones bajos
    const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/;
    return usernameRegex.test(username);
  }

  validatePasswordStrength(password: string): {
    isValid: boolean;
    score: number;
    feedback: string[];
  } {
    const feedback: string[] = [];
    let score = 0;

    if (password.length >= 8) {
      score += 1;
    } else {
      feedback.push('Debe tener al menos 8 caracteres');
    }

    if (/[a-z]/.test(password)) {
      score += 1;
    } else {
      feedback.push('Debe contener al menos una letra minúscula');
    }

    if (/[A-Z]/.test(password)) {
      score += 1;
    } else {
      feedback.push('Debe contener al menos una letra mayúscula');
    }

    if (/[0-9]/.test(password)) {
      score += 1;
    } else {
      feedback.push('Debe contener al menos un número');
    }

    if (/[^a-zA-Z0-9]/.test(password)) {
      score += 1;
    } else {
      feedback.push('Debe contener al menos un carácter especial');
    }

    return {
      isValid: score >= 4,
      score,
      feedback
    };
  }
}

export const registroService = new RegistroService();
export default registroService;