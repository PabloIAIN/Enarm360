import React, { useState, useEffect } from 'react';
import {
  Stack,
  TextInput,
  Button,
  Text,
  Alert,
  Group,
  Box,
  Paper,
  Center
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { notifications } from '@mantine/notifications';
import {
  IconMail,
  IconCheck,
  IconX,
  IconAlertCircle,
  IconArrowLeft,
  IconRefresh,
  IconClock
} from '@tabler/icons-react';

import { registroService } from '../../services/registroService';

interface Step3EmailVerificationProps {
  email: string;
  onVerified: () => void;
  onBack: () => void;
  loading: boolean;
  setLoading: (loading: boolean) => void;
}

const Step3EmailVerification: React.FC<Step3EmailVerificationProps> = ({
  email,
  onVerified,
  onBack,
  loading,
  setLoading
}) => {
  const [resendCooldown, setResendCooldown] = useState(0);
  const [attempts, setAttempts] = useState(0);
  const maxAttempts = 5;

  const form = useForm<{ codigo: string }>({
    initialValues: {
      codigo: ''
    },
    validate: {
      codigo: (value) => {
        if (!value) return 'El código es obligatorio';
        if (value.length !== 6) return 'El código debe tener 6 dígitos';
        if (!/^\d{6}$/.test(value)) return 'El código debe contener solo números';
        return null;
      }
    }
  });

  // Countdown para reenvío
  useEffect(() => {
    if (resendCooldown > 0) {
      const timer = setTimeout(() => {
        setResendCooldown(prev => prev - 1);
      }, 1000);
      return () => clearTimeout(timer);
    }
  }, [resendCooldown]);

  const handleVerification = async (values: { codigo: string }) => {
    if (attempts >= maxAttempts) {
      notifications.show({
        title: 'Demasiados intentos',
        message: 'Has excedido el número máximo de intentos. Solicita un nuevo código.',
        color: 'red',
        icon: <IconX size={16} />
      });
      return;
    }

    setLoading(true);
    try {
      const result = await registroService.verificarEmail({
        codigo: values.codigo,
        email: email
      });

      if (result.success) {
        notifications.show({
          title: '¡Email verificado!',
          message: 'Tu correo electrónico ha sido verificado exitosamente',
          color: 'green',
          icon: <IconCheck size={16} />
        });
        onVerified();
      } else {
        setAttempts(prev => prev + 1);
        const remainingAttempts = maxAttempts - attempts - 1;
        
        notifications.show({
          title: 'Código incorrecto',
          message: result.mensaje || `Código inválido. Te quedan ${remainingAttempts} intentos.`,
          color: 'red',
          icon: <IconX size={16} />
        });
        
        form.setFieldError('codigo', 'Código incorrecto');
        form.setFieldValue('codigo', '');
      }
    } catch (error: any) {
      console.error('Error verifying email:', error);
      setAttempts(prev => prev + 1);
      
      notifications.show({
        title: 'Error de verificación',
        message: 'Hubo un problema al verificar el código. Inténtalo de nuevo.',
        color: 'red',
        icon: <IconAlertCircle size={16} />
      });
    } finally {
      setLoading(false);
    }
  };

  const handleResendCode = async () => {
    if (resendCooldown > 0) return;

    setLoading(true);
    try {
      const result = await registroService.reenviarEmailVerificacion(email);
      
      if (result.success) {
        notifications.show({
          title: 'Código reenviado',
          message: 'Se ha enviado un nuevo código a tu email',
          color: 'green',
          icon: <IconCheck size={16} />
        });
        
        // Reset attempts and set cooldown
        setAttempts(0);
        setResendCooldown(60); // 60 segundos
        form.setFieldValue('codigo', '');
        form.clearErrors();

        // Log para desarrollo
        if (result.tokenEmail) {
          console.log('🔐 NUEVO CÓDIGO EMAIL (DESARROLLO):', result.tokenEmail);
        }
      } else {
        notifications.show({
          title: 'Error al reenviar',
          message: result.mensaje || 'No se pudo reenviar el código',
          color: 'red',
          icon: <IconX size={16} />
        });
      }
    } catch (error) {
      notifications.show({
        title: 'Error',
        message: 'Error al reenviar el código',
        color: 'red',
        icon: <IconAlertCircle size={16} />
      });
    } finally {
      setLoading(false);
    }
  };

  const formatEmail = (email: string) => {
    const [localPart, domain] = email.split('@');
    if (localPart.length <= 3) return email;
    
    const visibleStart = localPart.substring(0, 2);
    const visibleEnd = localPart.substring(localPart.length - 1);
    const maskedPart = '*'.repeat(Math.max(1, localPart.length - 3));
    
    return `${visibleStart}${maskedPart}${visibleEnd}@${domain}`;
  };

  return (
    <form onSubmit={form.onSubmit(handleVerification)}>
      <Stack gap="lg">
        
        {/* Información del email */}
        <Paper p="md" radius="md" withBorder style={{ backgroundColor: 'rgba(59, 130, 246, 0.05)' }}>
          <Group>
            <IconMail size={20} color="#3b82f6" />
            <Stack gap="xs" style={{ flex: 1 }}>
              <Text size="sm" fw={500}>Código enviado a:</Text>
              <Text size="sm" c="dimmed" ff="monospace">
                {formatEmail(email)}
              </Text>
            </Stack>
          </Group>
        </Paper>

        {/* Información importante */}
        <Alert color="blue" variant="light">
          <Stack gap="xs">
            <Text size="sm" fw={500}>📧 Revisa tu bandeja de entrada</Text>
            <Text size="sm">
              • El código tiene 6 dígitos numéricos
              • Válido por 15 minutos
              • Revisa también tu carpeta de spam
              • Si no lo encuentras, puedes solicitar un nuevo código
            </Text>
          </Stack>
        </Alert>

        {/* Campo de código */}
        <TextInput
          label="Código de verificación"
          placeholder="123456"
          required
          size="lg"
          style={{ textAlign: 'center' }}
          styles={{
            input: {
              textAlign: 'center',
              fontSize: '1.5rem',
              letterSpacing: '0.5rem',
              fontFamily: 'monospace'
            }
          }}
          maxLength={6}
          {...form.getInputProps('codigo')}
          onChange={(e) => {
            // Solo permitir números
            const value = e.target.value.replace(/\D/g, '');
            form.setFieldValue('codigo', value);
          }}
        />

        {/* Estado de intentos */}
        {attempts > 0 && (
          <Alert color="orange" variant="light">
            <Group>
              <IconAlertCircle size={16} />
              <Text size="sm">
                Intentos fallidos: {attempts}/{maxAttempts}
                {attempts >= maxAttempts && ' - Solicita un nuevo código'}
              </Text>
            </Group>
          </Alert>
        )}

        {/* Botón de reenvío */}
        <Center>
          <Button
            variant="subtle"
            size="sm"
            onClick={handleResendCode}
            disabled={resendCooldown > 0 || loading}
            leftSection={resendCooldown > 0 ? <IconClock size={14} /> : <IconRefresh size={14} />}
          >
            {resendCooldown > 0
              ? `Reenviar en ${resendCooldown}s`
              : 'Reenviar código'
            }
          </Button>
        </Center>

        {/* Botones de navegación */}
        <Group justify="space-between">
          <Button
            variant="light"
            leftSection={<IconArrowLeft size={16} />}
            onClick={onBack}
            disabled={loading}
          >
            Volver
          </Button>
          
          <Button
            type="submit"
            size="lg"
            loading={loading}
            disabled={!form.isValid() || attempts >= maxAttempts}
            leftSection={<IconCheck size={16} />}
            style={{ flex: 1, marginLeft: '1rem' }}
          >
            {loading ? 'Verificando...' : 'Verificar email'}
          </Button>
        </Group>

        {/* Debug info - solo en desarrollo */}
        {process.env.NODE_ENV === 'development' && (
          <Alert color="gray" variant="light">
            <Text size="xs" c="dimmed">
              🔧 DEV: Revisa la consola del navegador para ver el código de verificación
            </Text>
          </Alert>
        )}
      </Stack>
    </form>
  );
};

export default Step3EmailVerification;