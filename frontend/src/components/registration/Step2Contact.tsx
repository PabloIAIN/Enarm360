import React, { useState } from 'react';
import {
  Stack,
  Grid,
  TextInput,
  Button,
  Text,
  Alert,
  Group,
  useMantineColorScheme
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { notifications } from '@mantine/notifications';
import {
  IconMail,
  IconPhone,
  IconCheck,
  IconX,
  IconAlertCircle,
  IconArrowLeft,
  IconUserPlus
} from '@tabler/icons-react';

import { registroService } from '../../services/registroService';
import { 
  Paso1DatosPersonales,
  Paso2Contacto
} from '../../types/registro';

interface Step2ContactProps {
  paso1Data: Paso1DatosPersonales;
  onComplete: (data: Paso2Contacto, usuarioId: number) => void;
  onBack: () => void;
  loading: boolean;
  setLoading: (loading: boolean) => void;
}

const Step2Contact: React.FC<Step2ContactProps> = ({
  paso1Data,
  onComplete,
  onBack,
  loading,
  setLoading
}) => {
  const { colorScheme } = useMantineColorScheme();

  const form = useForm<{ email: string; telefono: string }>({
    initialValues: {
      email: '',
      telefono: ''
    },
    validate: {
      email: (value) => {
        if (!value) return 'El email es obligatorio';
        if (!/^\S+@\S+\.\S+$/.test(value)) return 'Formato de email inválido';
        return null;
      },
      telefono: (value) => {
        if (!value) return 'El teléfono es obligatorio';
        if (value.length < 10) return 'El teléfono debe tener al menos 10 dígitos';
        if (!/^[+]?[0-9\s\-\(\)]+$/.test(value)) return 'Formato de teléfono inválido';
        return null;
      }
    }
  });

  const handleSubmit = async (values: { email: string; telefono: string }) => {
    setLoading(true);

    // Combinar datos del paso 1 con los del paso 2
    const datosCompletos: Paso2Contacto = {
      ...paso1Data,
      email: values.email.toLowerCase().trim(),
      telefono: values.telefono.trim()
    };

    try {
      const result = await registroService.crearCuentaPaso2(datosCompletos);
      
      if (result.success) {
        notifications.show({
          title: '¡Cuenta creada!',
          message: 'Tu cuenta ha sido creada exitosamente. Se han enviado códigos de verificación.',
          color: 'green',
          icon: <IconCheck size={16} />
        });

        // Log para desarrollo - mostrar los códigos
        if (result.tokenEmail || result.codigoTelefono) {
          console.log('🔐 CÓDIGOS DE VERIFICACIÓN (DESARROLLO):');
          if (result.tokenEmail) {
            console.log('📧 Token Email:', result.tokenEmail);
          }
          if (result.codigoTelefono) {
            console.log('📱 Código SMS:', result.codigoTelefono);
          }
        }

        onComplete(datosCompletos, result.usuarioId);
      } else {
        notifications.show({
          title: 'Error al crear cuenta',
          message: result.mensaje,
          color: 'red',
          icon: <IconX size={16} />
        });

        // Si el error es de duplicados, mostrar campo específico
        if (result.mensaje.includes('email')) {
          form.setFieldError('email', 'Este email ya está registrado');
        }
        if (result.mensaje.includes('teléfono')) {
          form.setFieldError('telefono', 'Este teléfono ya está registrado');
        }
        if (result.mensaje.includes('username')) {
          notifications.show({
            title: 'Username no disponible',
            message: 'El username ya está en uso. Por favor regresa y elige otro.',
            color: 'orange',
            icon: <IconAlertCircle size={16} />
          });
        }
      }
    } catch (error: any) {
      console.error('Error creating account:', error);
      
      let errorMessage = 'Error al crear cuenta';
      
      if (error.response?.data?.mensaje) {
        errorMessage = error.response.data.mensaje;
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      }

      notifications.show({
        title: 'Error',
        message: errorMessage,
        color: 'red',
        icon: <IconAlertCircle size={16} />
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={form.onSubmit(handleSubmit)}>
      <Stack gap="lg">
        
        {/* Resumen de datos del paso anterior */}
        <Alert color="blue" variant="light">
          <Stack gap="xs">
            <Text size="sm" fw={500}>Resumen de tu información:</Text>
            <Group gap="md">
              <Text size="sm">
                <strong>{paso1Data.nombre} {paso1Data.apellidos}</strong>
              </Text>
              <Text size="sm" c="dimmed">@{paso1Data.username}</Text>
            </Group>
            <Text size="xs" c="dimmed">
              {paso1Data.paisNacimiento} • {new Date(paso1Data.fechaNacimiento).toLocaleDateString()}
            </Text>
          </Stack>
        </Alert>

        {/* Email */}
        <TextInput
          label="Correo electrónico"
          placeholder="tu@email.com"
          leftSection={<IconMail size={16} />}
          required
          {...form.getInputProps('email')}
          description="Aquí recibirás el código de verificación"
        />

        {/* Teléfono */}
        <TextInput
          label="Número de teléfono"
          placeholder="+52 55 1234 5678"
          leftSection={<IconPhone size={16} />}
          required
          {...form.getInputProps('telefono')}
          description="Incluye el código de país (ej: +52 para México)"
        />

        {/* Información importante */}
        <Alert color="yellow" variant="light">
          <Stack gap="xs">
            <Text size="sm" fw={500}>📱 Importante:</Text>
            <Text size="sm">
              • Te enviaremos un código por email y otro por SMS
              • Necesitas verificar ambos para activar tu cuenta
              • Los códigos expiran en poco tiempo por seguridad
            </Text>
          </Stack>
        </Alert>

        {/* Botones */}
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
            disabled={!form.isValid()}
            leftSection={<IconUserPlus size={16} />}
            style={{ flex: 1, marginLeft: '1rem' }}
          >
            {loading ? 'Creando cuenta...' : 'Crear cuenta'}
          </Button>
        </Group>
      </Stack>
    </form>
  );
};

export default Step2Contact;