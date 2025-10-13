import React, { useState, useEffect } from 'react';
import {
  Stack,
  Grid,
  TextInput,
  PasswordInput,
  Select,
  Button,
  Text,
  Alert,
  Group,
  Progress,
  useMantineColorScheme
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { useDebouncedValue } from '@mantine/hooks';
import { notifications } from '@mantine/notifications';
import {
  IconUser,
  IconLock,
  IconCalendar,
  IconWorld,
  IconGenderBigender,
  IconCheck,
  IconX,
  IconLoader,
  IconAlertCircle
} from '@tabler/icons-react';

import { registroService } from '../../services/registroService';
import { COUNTRIES, getPopularCountries, searchCountries } from '../../data/countries';
import { 
  Paso1DatosPersonales,
  RegistroInfo,
  GENEROS_OPTIONS
} from '../../types/registro';

interface Step1PersonalDataProps {
  onComplete: (data: Paso1DatosPersonales) => void;
  loading: boolean;
  setLoading: (loading: boolean) => void;
}

const Step1PersonalData: React.FC<Step1PersonalDataProps> = ({
  onComplete,
  loading,
  setLoading
}) => {
  const { colorScheme } = useMantineColorScheme();
  
  const [checkingUsername, setCheckingUsername] = useState(false);
  const [usernameStatus, setUsernameStatus] = useState<'idle' | 'checking' | 'available' | 'taken'>('idle');
  const [registroInfo, setRegistroInfo] = useState<RegistroInfo | null>(null);
  const [passwordStrength, setPasswordStrength] = useState(0);
  // Preparar lista de países con populares primero
  const countryData = (() => {
    const popularCountries = getPopularCountries();
    const otherCountries = COUNTRIES.filter(
      country => !popularCountries.find(popular => popular.value === country.value)
    ).sort((a, b) => a.label.localeCompare(b.label));
    
    return [...popularCountries, ...otherCountries];
  })();

  const form = useForm<Paso1DatosPersonales>({
    initialValues: {
      nombre: '',
      apellidos: '',
      username: '',
      paisNacimiento: 'MX', // México por defecto
      genero: 'M',
      fechaNacimiento: '',
      contrasena: '',
      confirmarContrasena: ''
    },
    validate: {
      nombre: (value) => {
        if (!value || value.trim().length < 2) {
          return 'El nombre debe tener al menos 2 caracteres';
        }
        return null;
      },
      username: (value) => {
        if (!value || value.length < 3) {
          return 'El username debe tener al menos 3 caracteres';
        }
        if (value.length > 20) {
          return 'El username no puede exceder 20 caracteres';
        }
        if (!/^[a-zA-Z0-9._-]+$/.test(value)) {
          return 'Solo letras, números, puntos, guiones y guiones bajos';
        }
        return null;
      },
      paisNacimiento: (value) => !value ? 'Selecciona tu país de nacimiento' : null,
      genero: (value) => !value ? 'Selecciona tu género' : null,
      fechaNacimiento: (value) => {
        if (!value) return 'La fecha de nacimiento es obligatoria';
        
        const fecha = new Date(value);
        const hoy = new Date();
        const edad = hoy.getFullYear() - fecha.getFullYear();
        
        if (edad < 16) {
          return 'Debes ser mayor de 16 años';
        }
        if (edad > 100) {
          return 'Fecha de nacimiento inválida';
        }
        return null;
      },
      contrasena: (value) => {
        if (!value || value.length < 8) {
          return 'La contraseña debe tener al menos 8 caracteres';
        }
        
        let fuerza = 0;
        if (/[a-z]/.test(value)) fuerza++;
        if (/[A-Z]/.test(value)) fuerza++;
        if (/[0-9]/.test(value)) fuerza++;
        if (/[^A-Za-z0-9]/.test(value)) fuerza++;
        
        if (fuerza < 3) {
          return 'Contraseña muy débil. Incluye mayúsculas, minúsculas y números';
        }
        return null;
      },
      confirmarContrasena: (value, values) => {
        if (value !== values.contrasena) {
          return 'Las contraseñas no coinciden';
        }
        return null;
      }
    }
  });

  // Debounce del username para verificación
  const [debouncedUsername] = useDebouncedValue(form.values.username, 500);

  // ==========================================================
  // EFECTOS
  // ==========================================================

  useEffect(() => {
    loadRegistroInfo();
  }, []);

  useEffect(() => {
    if (debouncedUsername && debouncedUsername.length >= 3 && !form.errors.username) {
      checkUsernameAvailability(debouncedUsername);
    } else {
      setUsernameStatus('idle');
    }
  }, [debouncedUsername]);

  useEffect(() => {
    calculatePasswordStrength(form.values.contrasena);
  }, [form.values.contrasena]);

  // ==========================================================
  // FUNCIONES
  // ==========================================================

  const loadRegistroInfo = async () => {
    try {
      const info = await registroService.getInfoParaPasos();
      setRegistroInfo(info);
    } catch (error) {
      console.error('Error loading registro info:', error);
    }
  };

  const checkUsernameAvailability = async (username: string) => {
    setCheckingUsername(true);
    setUsernameStatus('checking');

    try {
      const result = await registroService.checkUsernameDisponibilidad(username);
      
      if (result.available) {
        setUsernameStatus('available');
      } else {
        setUsernameStatus('taken');
        form.setFieldError('username', result.message);
      }
    } catch (error) {
      console.error('Error checking username:', error);
      setUsernameStatus('idle');
    } finally {
      setCheckingUsername(false);
    }
  };

  const calculatePasswordStrength = (password: string) => {
    let strength = 0;
    
    if (password.length >= 8) strength += 25;
    if (/[a-z]/.test(password)) strength += 25;
    if (/[A-Z]/.test(password)) strength += 25;
    if (/[0-9]/.test(password)) strength += 15;
    if (/[^A-Za-z0-9]/.test(password)) strength += 10;
    
    setPasswordStrength(Math.min(strength, 100));
  };

  const getPasswordStrengthColor = () => {
    if (passwordStrength < 50) return 'red';
    if (passwordStrength < 75) return 'yellow';
    return 'green';
  };

  const getPasswordStrengthLabel = () => {
    if (passwordStrength < 25) return 'Muy débil';
    if (passwordStrength < 50) return 'Débil';
    if (passwordStrength < 75) return 'Media';
    return 'Fuerte';
  };

  const handleSubmit = async (values: Paso1DatosPersonales) => {
    if (usernameStatus !== 'available') {
      form.setFieldError('username', 'Verifica que el username esté disponible');
      return;
    }

    setLoading(true);

    try {
      const result = await registroService.validarPaso1(values);
      
      if (result.success) {
        notifications.show({
          title: 'Datos validados',
          message: 'Información personal verificada correctamente',
          color: 'green',
          icon: <IconCheck size={16} />
        });
        onComplete(values);
      } else {
        notifications.show({
          title: 'Error de validación',
          message: result.mensaje,
          color: 'red',
          icon: <IconX size={16} />
        });
        
        if (!result.usernameDisponible) {
          setUsernameStatus('taken');
          form.setFieldError('username', 'Username no disponible');
        }
      }
    } catch (error: any) {
      console.error('Error validating step 1:', error);
      notifications.show({
        title: 'Error',
        message: error.response?.data?.mensaje || 'Error al validar información',
        color: 'red',
        icon: <IconAlertCircle size={16} />
      });
    } finally {
      setLoading(false);
    }
  };

  const getUsernameRightSection = () => {
    if (checkingUsername) {
      return <IconLoader size={16} />;
    }
    
    switch (usernameStatus) {
      case 'available':
        return <IconCheck size={16} color="green" />;
      case 'taken':
        return <IconX size={16} color="red" />;
      default:
        return null;
    }
  };

  return (
    <form onSubmit={form.onSubmit(handleSubmit)}>
      <Stack gap="lg">
        
        {/* Información básica */}
        <Grid>
          <Grid.Col span={6}>
            <TextInput
              label="Nombre"
              placeholder="Tu nombre"
              leftSection={<IconUser size={16} />}
              required
              {...form.getInputProps('nombre')}
            />
          </Grid.Col>
          
          <Grid.Col span={6}>
            <TextInput
              label="Apellidos"
              placeholder="Tus apellidos (opcional)"
              leftSection={<IconUser size={16} />}
              {...form.getInputProps('apellidos')}
            />
          </Grid.Col>
        </Grid>

        {/* Username con validación */}
        <TextInput
          label="Nombre de usuario"
          placeholder="Tu username único"
          leftSection={<IconUser size={16} />}
          rightSection={getUsernameRightSection()}
          required
          {...form.getInputProps('username')}
          styles={{
            input: {
              borderColor: usernameStatus === 'available' ? 'green' : 
                          usernameStatus === 'taken' ? 'red' : undefined
            }
          }}
        />

        {usernameStatus === 'available' && (
          <Alert color="green" variant="light">
            <Group gap="xs">
              <IconCheck size={16} />
              <Text size="sm">Username disponible</Text>
            </Group>
          </Alert>
        )}

        {/* Información personal */}
        <Grid>
          <Grid.Col span={6}>
            <Select
              label="País de nacimiento"
              placeholder="Buscar o seleccionar país..."
              leftSection={<IconWorld size={16} />}
              data={countryData}
              searchable
              maxDropdownHeight={250}
              required
              {...form.getInputProps('paisNacimiento')}
              description="Empieza a escribir para buscar tu país"
            />
          </Grid.Col>

          <Grid.Col span={6}>
            <Select
              label="Género"
              placeholder="Selecciona tu género"
              leftSection={<IconGenderBigender size={16} />}
              data={GENEROS_OPTIONS}
              required
              {...form.getInputProps('genero')}
            />
          </Grid.Col>
        </Grid>

        <TextInput
          label="Fecha de nacimiento"
          type="date"
          leftSection={<IconCalendar size={16} />}
          required
          {...form.getInputProps('fechaNacimiento')}
        />

        {/* Contraseñas */}
        <Stack gap="xs">
          <PasswordInput
            label="Contraseña"
            placeholder="Crea una contraseña segura"
            leftSection={<IconLock size={16} />}
            required
            {...form.getInputProps('contrasena')}
          />
          
          {form.values.contrasena && (
            <Stack gap="xs">
              <Group justify="space-between">
                <Text size="sm" c="dimmed">Fortaleza:</Text>
                <Text size="sm" c={getPasswordStrengthColor()}>
                  {getPasswordStrengthLabel()}
                </Text>
              </Group>
              <Progress 
                value={passwordStrength} 
                color={getPasswordStrengthColor()}
                size="sm"
                radius="xl"
              />
            </Stack>
          )}
        </Stack>

        <PasswordInput
          label="Confirmar contraseña"
          placeholder="Repite tu contraseña"
          leftSection={<IconLock size={16} />}
          required
          {...form.getInputProps('confirmarContrasena')}
        />

        {/* Información de requisitos */}
        {registroInfo?.passwordRequirements && (
          <Alert color="blue" variant="light">
            <Text size="sm">
              <strong>Requisitos de contraseña:</strong><br />
              {registroInfo.passwordRequirements.mensaje}
            </Text>
          </Alert>
        )}

        {/* Botón continuar */}
        <Button
          type="submit"
          fullWidth
          size="lg"
          loading={loading}
          disabled={usernameStatus !== 'available' || !form.isValid()}
          leftSection={<IconCheck size={16} />}
        >
          Continuar
        </Button>
      </Stack>
    </form>
  );
};

export default Step1PersonalData;