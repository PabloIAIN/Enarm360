// ==========================================================
// TIPOS PARA REGISTRO POR PASOS
// ==========================================================

// Paso 1: Datos Personales + Contraseña
export interface Paso1DatosPersonales {
  nombre: string;
  apellidos?: string;
  username: string;
  paisNacimiento: string;
  genero: 'M' | 'F' | 'Otro';
  fechaNacimiento: string; // ISO format
  contrasena: string;
  confirmarContrasena: string;
}

// Paso 2: Email + Teléfono (incluye datos del paso 1)
export interface Paso2Contacto extends Paso1DatosPersonales {
  email: string;
  telefono: string;
}

// Respuestas del backend
export interface Paso1Response {
  success: boolean;
  mensaje: string;
  usernameDisponible: boolean;
  siguientePaso: string;
}

export interface Paso2Response {
  success: boolean;
  mensaje: string;
  usuarioId: number;
  tokenEmail?: string; // Solo para debug
  codigoTelefono?: string; // Solo para debug
  siguientePaso: string;
  requiereVerificacionEmail: boolean;
  requiereVerificacionTelefono: boolean;
}

// Tipos legacy (mantener por compatibilidad)
export interface RegistroRequest {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  nombre: string;
  apellido: string;
  fechaNacimiento?: string;
  telefono?: string;
  genero?: 'M' | 'F' | 'Otro';
  universidad?: string;
  anioGraduacion?: number;
  especialidadInteres?: string;
  terminosAceptados: boolean;
  recibirNewsletters?: boolean;
}

export interface RegistroResponse {
  success: boolean;
  message: string;
  usuario?: {
    id: number;
    username: string;
    email: string;
    nombre: string;
    apellido: string;
    fechaRegistro: string;
  };
  tempToken?: string; // Para verificación de email si es necesario
}

export interface RegistroInfoResponse {
  success: boolean;
  message: string;
  universidades: string[];
  especialidades: string[];
  config: {
    minPasswordLength: number;
    maxUsernameLength: number;
    emailVerificationRequired: boolean;
    allowedFileTypes: string[];
    maxFileSize: number;
  };
}

export interface PasswordValidationRequest {
  password: string;
  username?: string;
  email?: string;
}

export interface PasswordValidationResponse {
  success: boolean;
  isValid: boolean;
  strength: {
    score: number; // 0-5
    label: 'Muy débil' | 'Débil' | 'Regular' | 'Buena' | 'Fuerte' | 'Muy fuerte';
    suggestions: string[];
  };
  errors: string[];
}

export interface CheckFieldResponse {
  success: boolean;
  available: boolean;
  field: string;
  value: string;
  message: string;
  suggestions?: string[]; // Para usernames alternativos
}

// ==========================================================
// TIPOS PARA VALIDACIONES
// ==========================================================

export interface ValidationResult {
  isValid: boolean;
  errors: string[];
  warnings?: string[];
}

export interface PasswordStrength {
  score: number;
  label: string;
  color: string;
  suggestions: string[];
}

// ==========================================================
// TIPOS PARA FORMULARIOS
// ==========================================================

export interface RegistroFormData extends Omit<RegistroRequest, 'confirmPassword'> {
  confirmPassword: string;
  currentStep: number;
  validations: {
    username: ValidationResult;
    email: ValidationResult;
    password: ValidationResult;
    confirmPassword: ValidationResult;
    nombre: ValidationResult;
    apellido: ValidationResult;
  };
}

export interface RegistroStep {
  id: number;
  title: string;
  description: string;
  fields: string[];
  isCompleted: boolean;
  isActive: boolean;
}

// ==========================================================
// CONSTANTES
// ==========================================================

export const GENEROS = [
  { value: 'M', label: 'Masculino' },
  { value: 'F', label: 'Femenino' },
  { value: 'Otro', label: 'Otro' }
] as const;

export const PASSWORD_STRENGTH_CONFIG = {
  minLength: 8,
  maxLength: 128,
  requireUppercase: true,
  requireLowercase: true,
  requireNumbers: true,
  requireSpecialChars: true,
  forbiddenPasswords: [
    'password', '123456', 'password123', 'admin', 'enarm360'
  ]
};

export const USERNAME_CONFIG = {
  minLength: 3,
  maxLength: 20,
  allowedChars: /^[a-zA-Z0-9_]+$/,
  forbiddenUsernames: [
    'admin', 'root', 'system', 'api', 'www', 'mail', 'test', 'enarm360'
  ]
};

export const EMAIL_CONFIG = {
  maxLength: 100,
  allowedDomains: [], // Vacío = todos los dominios permitidos
  blockedDomains: [
    '10minutemail.com', 'tempmail.org', 'guerrillamail.com'
  ]
};

// ==========================================================
// TIPOS PARA WIZARD DE REGISTRO
// ==========================================================

export interface RegistroWizardStep {
  id: number;
  title: string;
  description: string;
  isCompleted: boolean;
  isActive: boolean;
  isAccessible: boolean;
}

export interface RegistroWizardData {
  currentStep: number;
  totalSteps: number;
  steps: RegistroWizardStep[];
  // Datos acumulados
  paso1?: Paso1DatosPersonales;
  paso2?: Paso2Contacto;
  usuarioCreado?: {
    id: number;
    email: string;
    telefono: string;
  };
}

// Info del backend para formularios
export interface RegistroInfo {
  paises: string[];
  generos: Record<string, string>;
  passwordRequirements: {
    minLength: number;
    requireUppercase: boolean;
    requireLowercase: boolean;
    requireNumbers: boolean;
    mensaje: string;
  };
}

// Verificaciones
export interface VerificacionEmailRequest {
  codigo: string;
  email: string;
}

export interface VerificacionTelefonoRequest {
  codigo: string;
  telefono: string;
}

export interface VerificacionResponse {
  success: boolean;
  mensaje: string;
  verificado: boolean;
  intentosRestantes?: number;
}

// Disponibilidad de campos
export interface DisponibilidadResponse {
  available: boolean;
  message: string;
}

// Constantes para el wizard
export const WIZARD_STEPS = [
  {
    id: 1,
    title: 'Datos Personales',
    description: 'Información básica y username'
  },
  {
    id: 2, 
    title: 'Contacto',
    description: 'Email y teléfono'
  },
  {
    id: 3,
    title: 'Verificar Email',
    description: 'Confirma tu correo electrónico'
  },
  {
    id: 4,
    title: 'Verificar Teléfono', 
    description: 'Confirma tu número telefónico'
  },
  {
    id: 5,
    title: 'Elegir Plan',
    description: 'Selecciona tu plan de estudio'
  }
];

export const GENEROS_OPTIONS = [
  { value: 'M', label: 'Masculino' },
  { value: 'F', label: 'Femenino' },
  { value: 'Otro', label: 'Otro' }
] as const;
