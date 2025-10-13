import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Text,
  Title,
  Stack,
  Box,
  useMantineColorScheme,
  SimpleGrid,
  Paper,
  Group,
  Badge,
  Button,
  Loader,
} from '@mantine/core';
import {
  IconRun,
  IconSettings,
  IconUsers,
  IconTrophy,
  IconPhoto,
  IconWorld,
  IconBrain,
  IconClock,
  IconPlayerPlay,
} from '@tabler/icons-react';
import examenService from '../services/examenService';
import { authService } from '../services/authService';
import { ExamenEnProgreso } from '../types/examen.types';

const SimuladorPage: React.FC = () => {
  const navigate = useNavigate();
  const { colorScheme } = useMantineColorScheme();
  const currentUser = authService.getCurrentUserFromStorage();

  const [examenEnProgreso, setExamenEnProgreso] = useState<ExamenEnProgreso | null>(null);
  const [cargandoProgreso, setCargandoProgreso] = useState(true);

  // Cargar examen en progreso al montar el componente - SOLO UNA VEZ
  useEffect(() => {
    const cargarExamenEnProgreso = async () => {
      console.log('🔎 [SIMULADOR_LOAD] ====== INICIO ======');
      console.log('🎯 [SIMULADOR_LOAD] SimuladorPage: Cargando examen en progreso');

      const user = authService.getCurrentUserFromStorage();
      if (!user) {
        console.log('⚠️ [SIMULADOR_LOAD] No hay usuario autenticado');
        setCargandoProgreso(false);
        console.log('🏁 [SIMULADOR_LOAD] ====== FIN (Sin usuario) ======');
        return;
      }

      console.log('✅ [SIMULADOR_LOAD] Usuario autenticado:', user.id, '-', user.username);

      try {
        console.log('🌐 [SIMULADOR_LOAD] Llamando verificarExamenEnProgreso...');
        const examen = await examenService.verificarExamenEnProgreso(user.id);

        if (examen) {
          console.log('✅ [SIMULADOR_LOAD] Examen en progreso encontrado:');
          console.log('   - IntentoId:', examen.intentoId);
          console.log('   - Tipo:', examen.tipoExamen);
          console.log('   - Nombre:', examen.nombreExamen);
          console.log('   - Pregunta:', examen.preguntaActual + 1, '/', examen.totalPreguntas);
          setExamenEnProgreso(examen);
        } else {
          console.log('📭 [SIMULADOR_LOAD] No hay examen en progreso');
        }

        console.log('🎉 [SIMULADOR_LOAD] ====== ÉXITO ======');
      } catch (error: any) {
        console.error('❌ [SIMULADOR_LOAD] ERROR:');
        console.error('   - Tipo:', error.constructor.name);
        console.error('   - Mensaje:', error.message);
        console.log('🚨 [SIMULADOR_LOAD] ====== ERROR ======');
      } finally {
        setCargandoProgreso(false);
      }
    };

    cargarExamenEnProgreso();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // SIN DEPENDENCIAS para evitar loops

  const continuarExamen = () => {
    console.log('🔎 [SIMULADOR_CONTINUAR] ====== INICIO ======');
    console.log('🚀 [SIMULADOR_CONTINUAR] Usuario hace clic en Continuar Examen');

    if (!examenEnProgreso) {
      console.error('❌ [SIMULADOR_CONTINUAR] No hay examen en progreso');
      return;
    }

    console.log('📦 [SIMULADOR_CONTINUAR] Datos del examen a continuar:');
    console.log('   - IntentoId:', examenEnProgreso.intentoId);
    console.log('   - Tipo:', examenEnProgreso.tipoExamen);
    console.log('   - Nombre:', examenEnProgreso.nombreExamen);
    console.log('   - Pregunta actual:', examenEnProgreso.preguntaActual + 1);
    console.log('   - Total preguntas:', examenEnProgreso.totalPreguntas);

    // Redirigir según el tipo de examen
    if (examenEnProgreso.tipoExamen === 'RAPIDO') {
      console.log('🔀 [SIMULADOR_CONTINUAR] Navegando a examen-rapido');
      navigate('/estudiante/examen-rapido', {
        state: {
          continuandoExamen: true,
          examenEnProgreso: examenEnProgreso
        }
      });
    } else if (examenEnProgreso.tipoExamen === 'ENARM') {
      console.log('🔀 [SIMULADOR_CONTINUAR] Navegando a simulacro');
      navigate('/estudiante/simulacro', {
        state: {
          continuandoExamen: true,
          examenEnProgreso: examenEnProgreso
        }
      });
    } else if (examenEnProgreso.tipoExamen === 'FILTRADO') {
      console.log('🔀 [SIMULADOR_CONTINUAR] Navegando a examen-filtrado/' + examenEnProgreso.intentoId);
      // Pasar el examen en progreso para que no haga verificaciones adicionales
      navigate(`/estudiante/examen-filtrado/${examenEnProgreso.intentoId}`, {
        state: {
          continuandoExamen: true,
          examenEnProgreso: examenEnProgreso
        }
      });
    }

    console.log('🎉 [SIMULADOR_CONTINUAR] ====== NAVEGACIÓN INICIADA ======');
  };

  const formatearTiempo = (segundos: number): string => {
    const horas = Math.floor(segundos / 3600);
    const mins = Math.floor((segundos % 3600) / 60);
    const secs = segundos % 60;

    if (horas > 0) {
      return `${horas}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  // Configuración de las 4 opciones principales
  const opcionesSimulador = [
    {
      id: 'express',
      title: 'Quiz Express',
      description: '10 preguntas al azar',
      status: null,
      icon: IconRun,
      color: '#60a5fa', // azul
      bgColor: colorScheme === 'dark' ? 'rgba(96, 165, 250, 0.1)' : 'rgba(96, 165, 250, 0.05)',
      borderColor: 'rgba(96, 165, 250, 0.3)',
      route: '/estudiante/simulador/rapido',
      disabled: false
    },
    {
      id: 'filtros',
      title: 'Arma tu Examen',
      description: 'Tú eliges las especialidades\ny la cantidad de preguntas',
      status: null,
      icon: IconSettings,
      color: '#f59e0b', // naranja/amarillo
      bgColor: colorScheme === 'dark' ? 'rgba(245, 158, 11, 0.1)' : 'rgba(245, 158, 11, 0.05)',
      borderColor: 'rgba(245, 158, 11, 0.3)',
      route: '/estudiante/simulador/filtros',
      disabled: false
    },
    {
      id: 'multiplayer',
      title: 'Multiplayer',
      description: 'Únete a un examen\ncompartido y compite por\n1er lugar',
      status: null,
      icon: IconUsers,
      color: '#34d399', // verde
      bgColor: colorScheme === 'dark' ? 'rgba(52, 211, 153, 0.1)' : 'rgba(52, 211, 153, 0.05)',
      borderColor: 'rgba(52, 211, 153, 0.3)',
      route: '/estudiante/simulador/multiplayer',
      disabled: false
    },
    {
      id: 'completo',
      title: 'Completo',
      description: 'Simula la experiencia de\nun ENARM completo',
      status: null,
      icon: IconTrophy,
      color: '#f87171', // rojo/coral
      bgColor: colorScheme === 'dark' ? 'rgba(248, 113, 113, 0.1)' : 'rgba(248, 113, 113, 0.05)',
      borderColor: 'rgba(248, 113, 113, 0.3)',
      route: '/estudiante/simulador/completo',
      disabled: false
    }
  ];

  // Exámenes especiales (sección inferior)
  const examenesEspeciales = [
    {
      id: 'casos-clinicos',
      title: 'Casos Clínicos',
      icon: IconPhoto,
      color: '#f87171', // rosa/rojo
      bgColor: colorScheme === 'dark' ? 'rgba(248, 113, 113, 0.1)' : 'rgba(248, 113, 113, 0.05)',
      disabled: true
    },
    {
      id: 'internacionales',
      title: 'Internacionales',
      icon: IconWorld,
      color: '#64748b', // gris
      bgColor: colorScheme === 'dark' ? 'rgba(100, 116, 139, 0.1)' : 'rgba(100, 116, 139, 0.05)',
      disabled: true
    },
    {
      id: 'ia-personalizado',
      title: 'IA Personalizado',
      icon: IconBrain,
      color: '#a855f7', // púrpura
      bgColor: colorScheme === 'dark' ? 'rgba(168, 85, 247, 0.1)' : 'rgba(168, 85, 247, 0.05)',
      disabled: true
    }
  ];

  const handleOpcionClick = (opcion: any) => {
    // Permite navegar incluso si está marcado como disabled
    navigate(opcion.route);
  };

  return (
    <Box
      style={{
        padding: '2rem',
        minHeight: '100vh',
        backgroundColor: colorScheme === 'dark' 
          ? 'rgba(15, 23, 42, 0.5)' 
          : 'rgba(247, 243, 238, 0.3)',
      }}
    >
      {/* Header */}
      <Box mb="3rem" ta="center">
        <Title
          order={1}
          size="2.5rem"
          fw={700}
          style={{
            color: colorScheme === 'dark' ? '#e2e8f0' : '#1e293b',
            fontFamily: 'Space Grotesk, Inter, sans-serif',
            marginBottom: '1rem',
          }}
        >
          Elige tu cuestionario
        </Title>
      </Box>

      {/* Sección de Examen en Progreso */}
      {!cargandoProgreso && examenEnProgreso && (
        <Box mb="3rem">
          <Title
            order={2}
            size="1.5rem"
            fw={600}
            mb="1.5rem"
            style={{
              color: colorScheme === 'dark' ? '#e2e8f0' : '#1e293b',
              fontFamily: 'Space Grotesk, Inter, sans-serif',
            }}
          >
            📝 Examen por completar
          </Title>

          <Paper
            style={{
              backgroundColor: colorScheme === 'dark'
                ? 'rgba(34, 211, 238, 0.1)'
                : 'rgba(34, 211, 238, 0.05)',
              border: `2px solid ${colorScheme === 'dark' ? 'rgba(34, 211, 238, 0.3)' : 'rgba(34, 211, 238, 0.4)'}`,
              borderRadius: '20px',
              padding: '2rem',
              cursor: 'pointer',
              transition: 'all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94)',
              backdropFilter: 'blur(20px) saturate(180%)',
              WebkitBackdropFilter: 'blur(20px) saturate(180%)',
            }}
            onClick={continuarExamen}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = 'translateY(-4px)';
              e.currentTarget.style.boxShadow = '0 20px 40px rgba(34, 211, 238, 0.2)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = 'translateY(0)';
              e.currentTarget.style.boxShadow = 'none';
            }}
          >
            <Group justify="space-between" align="center" wrap="nowrap">
              <Group align="center" gap="xl">
                {/* Icono */}
                <Box
                  style={{
                    width: '80px',
                    height: '80px',
                    borderRadius: '16px',
                    backgroundColor: 'rgba(34, 211, 238, 0.2)',
                    border: '2px solid rgba(34, 211, 238, 0.4)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <IconClock size={40} style={{ color: '#22d3ee' }} />
                </Box>

                {/* Info del examen */}
                <Stack gap="xs">
                  <Title
                    order={3}
                    size="1.5rem"
                    fw={600}
                    style={{
                      color: colorScheme === 'dark' ? '#e2e8f0' : '#1e293b',
                      fontFamily: 'Space Grotesk, Inter, sans-serif',
                    }}
                  >
                    {examenEnProgreso.nombreExamen}
                  </Title>
                  <Group gap="md">
                    <Badge
                      size="lg"
                      radius="md"
                      style={{
                        backgroundColor: examenEnProgreso.pausado
                          ? 'rgba(251, 146, 60, 0.2)'
                          : 'rgba(34, 197, 94, 0.2)',
                        color: examenEnProgreso.pausado ? '#fb923c' : '#22c55e',
                        border: `1px solid ${examenEnProgreso.pausado ? '#fb923c' : '#22c55e'}`,
                      }}
                    >
                      {examenEnProgreso.pausado ? '⏸ Pausado' : '▶️ En Progreso'}
                    </Badge>
                    <Text
                      size="sm"
                      style={{
                        color: colorScheme === 'dark' ? '#94a3b8' : '#64748b',
                        fontFamily: 'Inter, sans-serif',
                      }}
                    >
                      Pregunta {examenEnProgreso.preguntaActual + 1} de {examenEnProgreso.totalPreguntas}
                    </Text>
                    <Text
                      size="sm"
                      style={{
                        color: colorScheme === 'dark' ? '#94a3b8' : '#64748b',
                        fontFamily: 'Inter, sans-serif',
                      }}
                    >
                      ⏱ {formatearTiempo(examenEnProgreso.tiempoTranscurrido)}
                    </Text>
                  </Group>
                </Stack>
              </Group>

              {/* Botón de continuar */}
              <Button
                size="lg"
                radius="md"
                leftSection={<IconPlayerPlay size={20} />}
                style={{
                  backgroundColor: '#22d3ee',
                  color: '#0f172a',
                  fontWeight: 600,
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = '#06b6d4';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = '#22d3ee';
                }}
              >
                Continuar
              </Button>
            </Group>
          </Paper>
        </Box>
      )}

      {/* Loader mientras carga */}
      {cargandoProgreso && (
        <Box mb="3rem" ta="center">
          <Loader size="sm" />
        </Box>
      )}

      {/* Opciones principales - 4 tarjetas */}
      <SimpleGrid
        cols={{ base: 1, sm: 2, lg: 4 }}
        spacing="xl"
        mb="4rem"
      >
        {opcionesSimulador.map((opcion) => {
          const IconComponent = opcion.icon;
          return (
            <Paper
              key={opcion.id}
              onClick={() => handleOpcionClick(opcion)}
              style={{
                backgroundColor: opcion.bgColor,
                border: `2px solid ${opcion.borderColor}`,
                borderRadius: '24px',
                padding: '2rem',
                minHeight: '280px',
                cursor: 'pointer',
                opacity: opcion.disabled ? 0.6 : 1,
                transition: 'all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94)',
                position: 'relative',
                overflow: 'hidden',
                backdropFilter: 'blur(20px) saturate(180%)',
                WebkitBackdropFilter: 'blur(20px) saturate(180%)',
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'translateY(-8px)';
                e.currentTarget.style.boxShadow = `0 20px 40px rgba(0, 0, 0, 0.1), 0 0 0 1px ${opcion.color}20`;
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'translateY(0)';
                e.currentTarget.style.boxShadow = 'none';
              }}
            >
              {/* Status badge */}
              {opcion.status && (
                <Badge
                  size="sm"
                  radius="xl"
                  style={{
                    position: 'absolute',
                    top: '1rem',
                    right: '1rem',
                    backgroundColor: colorScheme === 'dark' ? 'rgba(100, 116, 139, 0.8)' : 'rgba(148, 163, 184, 0.8)',
                    color: colorScheme === 'dark' ? '#e2e8f0' : '#1e293b',
                    fontFamily: 'Inter, sans-serif',
                    fontSize: '0.75rem',
                  }}
                >
                  {opcion.status}
                </Badge>
              )}

              <Stack align="center" gap="lg" style={{ height: '100%', justifyContent: 'center' }}>
                {/* Icon */}
                <Box
                  style={{
                    width: '80px',
                    height: '80px',
                    borderRadius: '20px',
                    backgroundColor: `${opcion.color}20`,
                    border: `2px solid ${opcion.color}40`,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <IconComponent size={40} style={{ color: opcion.color }} />
                </Box>

                {/* Content */}
                <Stack align="center" gap="sm">
                  <Title
                    order={3}
                    size="1.25rem"
                    fw={600}
                    ta="center"
                    style={{
                      color: colorScheme === 'dark' ? '#e2e8f0' : '#1e293b',
                      fontFamily: 'Space Grotesk, Inter, sans-serif',
                    }}
                  >
                    {opcion.title}
                  </Title>
                  <Text
                    size="sm"
                    ta="center"
                    style={{
                      color: colorScheme === 'dark' ? '#94a3b8' : '#64748b',
                      fontFamily: 'Inter, sans-serif',
                      lineHeight: 1.4,
                      whiteSpace: 'pre-line',
                    }}
                  >
                    {opcion.description}
                  </Text>
                </Stack>
              </Stack>
            </Paper>
          );
        })}
      </SimpleGrid>

      {/* Sección de Exámenes Especiales */}
      <Box>
        <Title
          order={2}
          size="1.5rem"
          fw={600}
          ta="center"
          mb="2rem"
          style={{
            color: colorScheme === 'dark' ? '#e2e8f0' : '#1e293b',
            fontFamily: 'Space Grotesk, Inter, sans-serif',
          }}
        >
          Exámenes especiales
        </Title>

        <SimpleGrid
          cols={{ base: 1, sm: 3 }}
          spacing="xl"
        >
          {examenesEspeciales.map((examen) => {
            const IconComponent = examen.icon;
            return (
              <Paper
                key={examen.id}
                style={{
                  backgroundColor: examen.bgColor,
                  border: `1px solid ${examen.color}30`,
                  borderRadius: '20px',
                  padding: '1.5rem',
                  minHeight: '140px',
                  cursor: examen.disabled ? 'not-allowed' : 'pointer',
                  opacity: examen.disabled ? 0.5 : 1,
                  transition: 'all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94)',
                  backdropFilter: 'blur(20px) saturate(180%)',
                  WebkitBackdropFilter: 'blur(20px) saturate(180%)',
                }}
                onMouseEnter={(e) => {
                  if (!examen.disabled) {
                    e.currentTarget.style.transform = 'translateY(-4px)';
                    e.currentTarget.style.boxShadow = '0 10px 30px rgba(0, 0, 0, 0.1)';
                  }
                }}
                onMouseLeave={(e) => {
                  if (!examen.disabled) {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = 'none';
                  }
                }}
              >
                <Group align="center" gap="md">
                  <Box
                    style={{
                      width: '60px',
                      height: '60px',
                      borderRadius: '16px',
                      backgroundColor: `${examen.color}20`,
                      border: `1px solid ${examen.color}40`,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    <IconComponent size={28} style={{ color: examen.color }} />
                  </Box>

                  <Stack gap="xs">
                    <Title
                      order={4}
                      size="1rem"
                      fw={600}
                      style={{
                        color: colorScheme === 'dark' ? '#e2e8f0' : '#1e293b',
                        fontFamily: 'Space Grotesk, Inter, sans-serif',
                      }}
                    >
                      {examen.title}
                    </Title>
                    <Badge
                      size="xs"
                      radius="xl"
                      style={{
                        backgroundColor: colorScheme === 'dark' ? 'rgba(100, 116, 139, 0.8)' : 'rgba(148, 163, 184, 0.8)',
                        color: colorScheme === 'dark' ? '#e2e8f0' : '#1e293b',
                        fontFamily: 'Inter, sans-serif',
                        fontSize: '0.65rem',
                      }}
                    >
                      Próximamente
                    </Badge>
                  </Stack>
                </Group>
              </Paper>
            );
          })}
        </SimpleGrid>
      </Box>
    </Box>
  );
};

export default SimuladorPage;