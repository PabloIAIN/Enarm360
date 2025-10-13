import React, { useState, useEffect } from 'react';
import {
  Stack,
  TextInput,
  Button,
  Text,
  Alert,
  Group,
  Paper,
  Center
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { notifications } from '@mantine/notifications';
import {
  IconPhone,
  IconCheck,
  IconX,
  IconAlertCircle,
  IconArrowLeft,
  IconRefresh,
  IconClock,
  IconMessage
} from '@tabler/icons-react';

import { registroService } from '../../services/registroService';

interface Step4PhoneVerificationProps {
  telefono: string;
  onVerified: () => void;
  onBack: () => void;
  loading: boolean;
  setLoading: (loading: boolean) => void;
}

const Step4PhoneVerification: React.FC<Step4PhoneVerificationProps> = ({
  telefono,
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
      const result = await registroService.verificarTelefono({
        codigo: values.codigo,
        telefono: telefono
      });

      if (result.success) {
        notifications.show({
          title: '¡Teléfono verificado!',
          message: 'Tu número telefónico ha sido verificado exitosamente',
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
      console.error('Error verifying phone:', error);
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
      const result = await registroService.reenviarTelefonoVerificacion(telefono);
      
      if (result.success) {
        notifications.show({
          title: 'SMS enviado',
          message: 'Se ha enviado un nuevo código a tu teléfono',
          color: 'green',
          icon: <IconCheck size={16} />
        });
        
        // Reset attempts and set cooldown
        setAttempts(0);
        setResendCooldown(60); // 60 segundos
        form.setFieldValue('codigo', '');
        form.clearErrors();

        // Log para desarrollo
        if (result.codigoTelefono) {
          console.log('🔐 NUEVO CÓDIGO SMS (DESARROLLO):', result.codigoTelefono);
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

  const formatPhone = (phone: string) => {
    // Ocultar dígitos del medio del teléfono
    if (phone.length <= 6) return phone;
    
    const visible = 3; // Mostrar primeros y últimos 3 dígitos
    const start = phone.substring(0, visible);
    const end = phone.substring(phone.length - visible);
    const masked = '*'.repeat(Math.max(1, phone.length - (visible * 2)));
    
    return `${start}${masked}${end}`;
  };

  return (
    <form onSubmit={form.onSubmit(handleVerification)}>
      <Stack gap="lg">
        
        {/* Información del teléfono */}
        <Paper p="md" radius="md" withBorder style={{ backgroundColor: 'rgba(34, 197, 94, 0.05)' }}>
          <Group>
            <IconMessage size={20} color="#22c55e" />
            <Stack gap="xs" style={{ flex: 1 }}>
              <Text size="sm" fw={500}>SMS enviado a:</Text>
              <Text size="sm" c="dimmed" ff="monospace">
                {formatPhone(telefono)}
              </Text>
            </Stack>
          </Group>
        </Paper>

        {/* Información importante */}
        <Alert color="green" variant="light">
          <Stack gap="xs">
            <Text size="sm" fw={500}>📱 Revisa tus mensajes de texto</Text>
            <Text size="sm">
              • El código tiene 6 dígitos numéricos
              • Válido por 10 minutos
              • Puede tardar hasta 2 minutos en llegar
              • Si no llega, verifica que tengas señal
            </Text>
          </Stack>
        </Alert>

        {/* Campo de código */}
        <TextInput
          label="Código SMS"
          placeholder="123456"
          required
          size="lg"
          style={{ textAlign: 'center' }}
          styles={{
            input: {
              textAlign: 'center',
              fontSize: '1.8rem',
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

        {/* Problema con el SMS? */}
        <Alert color="blue" variant="light">
          <Stack gap="xs">
            <Text size="sm" fw={500}>📞 ¿Problemas con el SMS?</Text>
            <Text size="sm">
              • Verifica que tu teléfono tenga señal
              • Algunos operadores pueden demorar
              • El código puede llegar como mensaje de texto normal
              • Si tienes bloqueados mensajes de números desconocidos, desactívalo temporalmente
            </Text>
          </Stack>
        </Alert>

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
              : 'Reenviar SMS'
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
            {loading ? 'Verificando...' : 'Verificar teléfono'}
          </Button>
        </Group>

        {/* Debug info - solo en desarrollo */}
        {process.env.NODE_ENV === 'development' && (
          <Alert color="gray" variant="light">
            <Text size="xs" c="dimmed">
              🔧 DEV: Revisa la consola del navegador para ver el código SMS
            </Text>
          </Alert>
        )}
      </Stack>
    </form>
  );
};

export default Step4PhoneVerification;