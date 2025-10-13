import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Text,
  Title,
  Stack,
  Box,
  Button,
  NumberInput,
  Group,
  useMantineColorScheme,
  Paper,
  Badge,
  SimpleGrid,
  Divider,
  Card,
  Accordion,
  Chip,
  Avatar,
  ActionIcon,
  Grid,
  Loader,
  Center,
} from '@mantine/core';
import {
  IconStethoscope,
  IconHeartbeat,
  IconCut,
  IconBabyCarriage,
  IconShieldCheck,
  IconVaccine,
  IconChevronDown,
} from '@tabler/icons-react';
import { authService } from '../services/authService';
import { examenService } from '../services/examenService';
import { especialidadService, Especialidad } from '../services/especialidadService';
import { temaService, Tema } from '../services/temaService';
import { ExamenEnProgreso } from '../types/examen.types';
import RecuperarExamenModal from '../components/RecuperarExamenModal';
import { examenStorage } from '../utils/examenStorage';

const ExamenFiltrosPage: React.FC = () => {
  const navigate = useNavigate();
  const { colorScheme } = useMantineColorScheme();
  const [user] = useState(authService.getCurrentUserFromStorage());

  // Estado de especialidades, temas y configuración
  const [especialidades, setEspecialidades] = useState<Especialidad[]>([]);
  const [temas, setTemas] = useState<Tema[]>([]);
  const [cargandoTemas, setCargandoTemas] = useState(true);
  const [selectedTemas, setSelectedTemas] = useState<number[]>([]); // IDs de los temas seleccionados
  const [numReactivos, setNumReactivos] = useState(10);
  const [selectedDificultad, setSelectedDificultad] = useState<number | null>(null); // 1=FÁCIL, 2=INTERMEDIO, 3=DIFÍCIL
  const [accordionValue, setAccordionValue] = useState<string[]>([]);

  // Estado para recuperación de examen
  const [examenEnProgreso, setExamenEnProgreso] = useState<ExamenEnProgreso | null>(null);
  const [mostrarModalRecuperar, setMostrarModalRecuperar] = useState(false);
  const [cargandoRecuperacion, setCargandoRecuperacion] = useState(false);

  // Cargar especialidades y temas desde el backend
  useEffect(() => {
    const cargarDatos = async () => {
      try {
        setCargandoTemas(true);
        const [especialidadesData, temasData] = await Promise.all([
          especialidadService.listar(),
          temaService.listar()
        ]);
        setEspecialidades(especialidadesData);
        setTemas(temasData);
        console.log('✅ Especialidades cargadas:', especialidadesData);
        console.log('✅ Temas cargados:', temasData);
      } catch (error) {
        console.error('Error cargando datos:', error);
        alert('Error al cargar especialidades y temas');
      } finally {
        setCargandoTemas(false);
      }
    };

    cargarDatos();
  }, []);

  // Verificar examen en progreso al cargar
  useEffect(() => {
    verificarExamenEnProgreso();
  }, []);

  const verificarExamenEnProgreso = async () => {
    if (!user?.id) {
      return;
    }

    try {
      const examen = await examenService.verificarExamenEnProgreso(user.id);
      if (examen) {
        // Verificar si el tipo de examen coincide con esta página (filtros)
        if (examen.tipoExamen === 'FILTRADO') {
          setExamenEnProgreso(examen);
          setMostrarModalRecuperar(true);
        } else {
          // Hay un examen en progreso pero es de otro tipo
          const continuar = window.confirm(
            `Tienes un ${examen.nombreExamen} en progreso. ¿Deseas ir a ese examen?`
          );
          if (continuar) {
            if (examen.tipoExamen === 'RAPIDO') {
              navigate('/estudiante/examen-rapido');
            } else if (examen.tipoExamen === 'ENARM') {
              navigate('/estudiante/simulacro');
            }
          }
          // Si dice que no, se queda en la página de filtros
        }
      }
    } catch (error) {
      console.error('Error verificando examen en progreso:', error);
    }
  };

  const recuperarExamen = async () => {
    if (!examenEnProgreso) return;

    setCargandoRecuperacion(true);
    try {
      const progreso = await examenService.recuperarProgreso(examenEnProgreso.intentoId);

      // Navegar a la página de examen filtrado con los datos recuperados
      navigate(`/estudiante/examen-filtrado/${examenEnProgreso.intentoId}`, {
        state: { 
          examenData: progreso.examenData,
          recuperando: true,
          progreso: {
            preguntaActual: progreso.preguntaActual,
            tiempoTranscurrido: progreso.tiempoTranscurrido,
            pausado: progreso.pausado,
            respuestas: progreso.respuestas
          }
        }
      });

      console.log('✅ Examen recuperado exitosamente');
    } catch (error) {
      console.error('Error recuperando examen:', error);
      alert('Error al recuperar el examen');
    } finally {
      setCargandoRecuperacion(false);
    }
  };

  const comenzarNuevoExamen = async () => {
    if (!examenEnProgreso) return;

    setCargandoRecuperacion(true);
    try {
      // Abandonar el examen anterior
      await examenService.abandonarExamen(examenEnProgreso.intentoId);
      examenStorage.limpiarProgreso();

      setMostrarModalRecuperar(false);
      setExamenEnProgreso(null);
      
      console.log('✅ Examen anterior abandonado');
    } catch (error) {
      console.error('Error abandonando examen:', error);
      alert('Error al abandonar el examen anterior');
    } finally {
      setCargandoRecuperacion(false);
    }
  };

  // Mapeo de especialidadId a clave de categoría UI
  const mapeoEspecialidadACategoria: Record<number, string> = {
    1: 'pediatria',
    2: 'medicina',
    3: 'cirugia',
    4: 'gineco',
    5: 'urgencias'
  };

  // Función para obtener temas por especialidad
  const getTemasPorEspecialidad = (categoriaKey: string): Tema[] => {
    // Encontrar el ID de la especialidad basándose en la categoría
    const especialidadId = Object.entries(mapeoEspecialidadACategoria)
      .find(([, cat]) => cat === categoriaKey)?.[0];

    if (!especialidadId) return [];

    return temas.filter(tema => tema.especialidadId === parseInt(especialidadId));
  };

  // Dificultades hardcoded
  const dificultades = {
    1: { id: 1, nombre: 'FACIL' },
    2: { id: 2, nombre: 'INTERMEDIO' },
    3: { id: 3, nombre: 'DIFICIL' }
  };

  // Configuración de categorías con iconos y metadatos (solo las 5 categorías reales)
  const categorias = {
    pediatria: {
      nombre: "Pediatría y Desarrollo",
      icon: IconBabyCarriage,
      color: "#ffb3ba",
      descripcion: "Cuidado integral infantil"
    },
    medicina: {
      nombre: "Medicina Interna",
      icon: IconStethoscope,
      color: "#bae1ff",
      descripcion: "Especialidades médicas generales"
    },
    cirugia: {
      nombre: "Especialidades Quirúrgicas",
      icon: IconCut,
      color: "#ffdfba",
      descripcion: "Procedimientos y cirugías"
    },
    gineco: {
      nombre: "Ginecología y Obstetricia",
      icon: IconHeartbeat,
      color: "#ffffba",
      descripcion: "Salud femenina y reproductiva"
    },
    urgencias: {
      nombre: "Medicina de Urgencias",
      icon: IconShieldCheck,
      color: "#ffb3ff",
      descripcion: "Atención médica inmediata"
    }
  };

  const handleGenerarExamen = async () => {
    try {
      if (!user?.id) {
        alert("Usuario no autenticado");
        return;
      }
      if (selectedTemas.length === 0) {
        alert("Selecciona al menos un tema");
        return;
      }

      console.log('🚀 Creando examen filtrado...');

      // Construir objeto de dificultad si está seleccionada
      const dificultadObj = selectedDificultad ? dificultades[selectedDificultad as keyof typeof dificultades] : undefined;

      console.log('📊 Configuración:', {
        temas: selectedTemas,
        numReactivos,
        dificultad: dificultadObj?.nombre || 'Todas'
      });

      // Usar nueva API con examenService.crearExamenFiltrado
      // NOTA: El backend espera especialidadIds, pero ahora enviamos temaIds (IDs de subespecialidades)
      const examenData = await examenService.crearExamenFiltrado(
        user.id,
        selectedTemas,  // Enviamos IDs de temas en lugar de especialidades
        numReactivos,
        undefined, // cantidadCasos (opcional)
        dificultadObj  // dificultad como objeto {id, nombre}
      );

      if (!examenData || !examenData.intentoId) {
        alert("No se pudo generar el examen. Verifica que existan reactivos en las especialidades seleccionadas.");
        return;
      }

      console.log('✅ Examen creado:', examenData);

      // Navegar a la página de examen filtrado pasando los datos
      navigate(`/estudiante/examen-filtrado/${examenData.intentoId}`, {
        state: { examenData }
      });
    } catch (error) {
      console.error("❌ Error generando examen:", error);
      alert("Ocurrió un error al generar el examen");
    }
  };

  return (
    <>
      <RecuperarExamenModal
        examen={examenEnProgreso}
        opened={mostrarModalRecuperar}
        onContinuar={recuperarExamen}
        onNuevo={comenzarNuevoExamen}
        loading={cargandoRecuperacion}
      />
      
      <Box
        style={{
          display: 'flex',
          flexDirection: 'column',
          minHeight: '100vh',
        }}
      >
      <Box
        style={{
          padding: '20px 32px',
          maxWidth: '1200px',
          margin: '0 auto',
          width: '100%',
          flex: 1,
        }}
      >
      {/* Page Header */}
      <Group justify="space-between" align="flex-start" mb="md">
        <Box>
          <Title
            order={3}
            size="h4"
            fw={600}
            style={{
              color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26',
              fontFamily: 'Space Grotesk, Inter, sans-serif',
              marginBottom: '4px',
              textAlign: 'left',
            }}
          >
            Arma tu Examen
          </Title>
          <Text
            size="xs"
            style={{
              color: colorScheme === 'dark' ? '#64748b' : '#6b7280',
              fontFamily: 'Inter, sans-serif',
              opacity: 0.7,
              textAlign: 'left',
            }}
          >
            Configura tu examen personalizado seleccionando especialidades y número de preguntas
          </Text>
        </Box>

        {/* Back Button */}
        <Button
          variant="subtle"
          onClick={() => navigate('/estudiante/simulador')}
          size="sm"
          style={{
            color: colorScheme === 'dark' ? '#94a3b8' : '#5a5550',
            backgroundColor: 'transparent',
            fontSize: '16px',
            fontWeight: 400,
            padding: '8px',
            minWidth: 'auto',
          }}
        >
          ←
        </Button>
      </Group>

      {/* Main Content Grid */}
      <Grid>
        {/* Left Column - Especialidades (smaller) */}
        <Grid.Col span={6}>
          <Stack gap="md">
            {/* Título de Especialidades */}
            <Group justify="space-between" align="center">
              <Text
                size="md"
                fw={600}
                style={{
                  color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26',
                  fontFamily: 'Space Grotesk, Inter, sans-serif',
                }}
              >
                Selecciona Especialidades
              </Text>
              <Group gap="xs">
                <Button
                  variant="subtle"
                  size="xs"
                  onClick={() => setAccordionValue(Object.keys(categorias))}
                  style={{
                    fontSize: '11px',
                    color: colorScheme === 'dark' ? '#94a3b8' : '#5a5550',
                  }}
                >
                  Expandir Todo
                </Button>
                <Button
                  variant="subtle"
                  size="xs"
                  onClick={() => setAccordionValue([])}
                  style={{
                    fontSize: '11px',
                    color: colorScheme === 'dark' ? '#94a3b8' : '#5a5550',
                  }}
                >
                  Colapsar Todo
                </Button>
              </Group>
            </Group>

            {/* Accordion de Especialidades */}
            {cargandoTemas ? (
              <Center p="xl">
                <Stack align="center" gap="sm">
                  <Loader size="lg" />
                  <Text size="sm" c="dimmed">Cargando temas...</Text>
                </Stack>
              </Center>
            ) : (
            <Accordion
              multiple
              value={accordionValue}
              onChange={setAccordionValue}
              chevron={<IconChevronDown size={16} />}
              style={{
                backgroundColor: colorScheme === 'dark'
                  ? 'rgba(30, 41, 59, 0.7)'
                  : 'rgba(247, 243, 238, 0.9)',
                border: `1px solid ${colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(221, 216, 209, 0.8)'}`,
                borderRadius: '12px',
              }}
              styles={{
                chevron: {
                  color: colorScheme === 'dark' ? '#94a3b8' : '#5a5550',
                  transition: 'transform 200ms ease',
                },
                control: {
                  backgroundColor: 'transparent',
                  border: 'none',
                  padding: '12px 16px',
                  '&:hover': {
                    backgroundColor: colorScheme === 'dark'
                      ? 'rgba(255, 255, 255, 0.05)'
                      : 'rgba(160, 142, 115, 0.1)',
                  },
                },
                panel: {
                  padding: '0 16px 16px 16px',
                },
                item: {
                  border: 'none',
                  borderBottom: `1px solid ${colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(221, 216, 209, 0.5)'}`,
                  '&:last-child': {
                    borderBottom: 'none',
                  },
                },
              }}
            >
              {Object.entries(categorias).map(([categoriaKey, categoria]) => {
                const temasCat = getTemasPorEspecialidad(categoriaKey);
                if (temasCat.length === 0) return null;

                const Icon = categoria.icon;
                const selectedCount = temasCat.filter(tema =>
                  selectedTemas.includes(tema.id)
                ).length;

                return (
                  <Accordion.Item key={categoriaKey} value={categoriaKey}>
                    <Accordion.Control>
                      <Group justify="space-between" wrap="nowrap" style={{ width: '100%' }}>
                        <Group gap="sm" wrap="nowrap">
                          <Avatar
                            size="sm"
                            style={{
                              backgroundColor: colorScheme === 'dark'
                                ? 'rgba(255, 255, 255, 0.1)'
                                : categoria.color + '40',
                              color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26',
                            }}
                          >
                            <Icon size={16} />
                          </Avatar>
                          <Box style={{ flex: 1 }}>
                            <Text
                              size="sm"
                              fw={600}
                              style={{
                                color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26',
                                fontFamily: 'Inter, sans-serif',
                                letterSpacing: '0.5px',
                              }}
                            >
                              {categoria.nombre}
                            </Text>
                            <Text
                              size="xs"
                              style={{
                                color: colorScheme === 'dark' ? '#94a3b8' : '#5a5550',
                                fontFamily: 'Inter, sans-serif',
                              }}
                            >
                              {categoria.descripcion}
                            </Text>
                          </Box>
                        </Group>
                        <Badge
                          size="sm"
                          variant={selectedCount > 0 ? "filled" : "outline"}
                          style={{
                            backgroundColor: selectedCount > 0
                              ? colorScheme === 'dark' ? '#0ea5e9' : '#2563eb'
                              : 'transparent',
                            borderColor: colorScheme === 'dark' ? '#0ea5e9' : '#2563eb',
                            color: selectedCount > 0
                              ? '#ffffff'
                              : colorScheme === 'dark' ? '#94a3b8' : '#5a5550',
                          }}
                        >
                          {selectedCount}/{temasCat.length}
                        </Badge>
                      </Group>
                    </Accordion.Control>

                    <Accordion.Panel>
                      <Group gap="xs">
                        {temasCat.map((tema) => {
                          const isSelected = selectedTemas.includes(tema.id);
                          return (
                            <Button
                              key={tema.id}
                              onClick={() => {
                                if (isSelected) {
                                  setSelectedTemas(prev => prev.filter(id => id !== tema.id));
                                } else {
                                  setSelectedTemas(prev => [...prev, tema.id]);
                                }
                              }}
                              size="xs"
                              variant={isSelected ? "filled" : "outline"}
                              style={{
                                backgroundColor: isSelected
                                  ? colorScheme === 'dark' ? '#0ea5e9' : '#2563eb'
                                  : 'transparent',
                                borderColor: isSelected
                                  ? colorScheme === 'dark' ? '#0ea5e9' : '#2563eb'
                                  : colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.2)' : 'rgba(148, 163, 184, 0.4)',
                                color: isSelected
                                  ? '#ffffff'
                                  : colorScheme === 'dark' ? '#94a3b8' : '#64748b',
                                fontSize: '11px',
                                fontWeight: isSelected ? 600 : 500,
                                fontFamily: 'Inter, sans-serif',
                                borderRadius: '16px',
                                padding: '6px 12px',
                                minHeight: 'auto',
                                height: 'auto',
                              }}
                            >
                              {tema.nombre}
                            </Button>
                          );
                        })}
                      </Group>
                    </Accordion.Panel>
                  </Accordion.Item>
                );
              })}
            </Accordion>
            )}
          </Stack>
        </Grid.Col>

        {/* Right Column - Configuration & Summary */}
        <Grid.Col span={6}>
          <Stack gap="md" style={{ marginTop: '46px' }}>
            {/* Configuration Card */}
            <Card
              padding="lg"
              style={{
                backgroundColor: colorScheme === 'dark'
                  ? 'rgba(30, 41, 59, 0.7)'
                  : 'rgba(247, 243, 238, 0.9)',
                border: `1px solid ${colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(221, 216, 209, 0.8)'}`,
                borderRadius: '12px',
              }}
            >
              <Text
                size="md"
                fw={600}
                mb="md"
                style={{
                  color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26',
                  fontFamily: 'Space Grotesk, Inter, sans-serif',
                }}
              >
                Configuración
              </Text>

              <Stack gap="md">
                <NumberInput
                  label="Número de Reactivos"
                  description="Entre 5 y 50 preguntas"
                  value={numReactivos}
                  onChange={(val) => setNumReactivos(Number(val) || 10)}
                  min={5}
                  max={50}
                  size="md"
                  styles={{
                    root: {
                      textAlign: 'left',
                    },
                    label: {
                      color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26',
                      fontFamily: 'Inter, sans-serif',
                      fontWeight: 500,
                      fontSize: '14px',
                      textAlign: 'left',
                    },
                    description: {
                      color: colorScheme === 'dark' ? '#94a3b8' : '#5a5550',
                      fontFamily: 'Inter, sans-serif',
                      fontSize: '12px',
                      textAlign: 'left',
                    },
                    input: {
                      backgroundColor: colorScheme === 'dark'
                        ? 'rgba(255, 255, 255, 0.05)'
                        : 'rgba(242, 237, 230, 0.5)',
                      border: `1px solid ${colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(221, 216, 209, 0.5)'}`,
                      borderRadius: '8px',
                      fontSize: '14px',
                      color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26',
                      textAlign: 'left',
                    },
                  }}
                />

                {/* Selector de Dificultad */}
                <Box>
                  <Text
                    size="sm"
                    fw={500}
                    mb="xs"
                    style={{
                      color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26',
                      fontFamily: 'Inter, sans-serif',
                      textAlign: 'left',
                    }}
                  >
                    Dificultad
                  </Text>
                  <Text
                    size="xs"
                    mb="sm"
                    style={{
                      color: colorScheme === 'dark' ? '#94a3b8' : '#5a5550',
                      fontFamily: 'Inter, sans-serif',
                      textAlign: 'left',
                    }}
                  >
                    Opcional - Filtra por nivel de dificultad
                  </Text>
                  <Group gap="xs">
                    <Button
                      variant={selectedDificultad === 1 ? "filled" : "outline"}
                      size="sm"
                      onClick={() => setSelectedDificultad(selectedDificultad === 1 ? null : 1)}
                      style={{
                        backgroundColor: selectedDificultad === 1
                          ? '#22c55e'
                          : 'transparent',
                        borderColor: selectedDificultad === 1
                          ? '#22c55e'
                          : colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.2)' : 'rgba(148, 163, 184, 0.4)',
                        color: selectedDificultad === 1
                          ? '#ffffff'
                          : colorScheme === 'dark' ? '#94a3b8' : '#64748b',
                        fontFamily: 'Inter, sans-serif',
                        fontSize: '13px',
                        fontWeight: 500,
                      }}
                    >
                      Fácil
                    </Button>
                    <Button
                      variant={selectedDificultad === 2 ? "filled" : "outline"}
                      size="sm"
                      onClick={() => setSelectedDificultad(selectedDificultad === 2 ? null : 2)}
                      style={{
                        backgroundColor: selectedDificultad === 2
                          ? '#f59e0b'
                          : 'transparent',
                        borderColor: selectedDificultad === 2
                          ? '#f59e0b'
                          : colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.2)' : 'rgba(148, 163, 184, 0.4)',
                        color: selectedDificultad === 2
                          ? '#ffffff'
                          : colorScheme === 'dark' ? '#94a3b8' : '#64748b',
                        fontFamily: 'Inter, sans-serif',
                        fontSize: '13px',
                        fontWeight: 500,
                      }}
                    >
                      Intermedio
                    </Button>
                    <Button
                      variant={selectedDificultad === 3 ? "filled" : "outline"}
                      size="sm"
                      onClick={() => setSelectedDificultad(selectedDificultad === 3 ? null : 3)}
                      style={{
                        backgroundColor: selectedDificultad === 3
                          ? '#ef4444'
                          : 'transparent',
                        borderColor: selectedDificultad === 3
                          ? '#ef4444'
                          : colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.2)' : 'rgba(148, 163, 184, 0.4)',
                        color: selectedDificultad === 3
                          ? '#ffffff'
                          : colorScheme === 'dark' ? '#94a3b8' : '#64748b',
                        fontFamily: 'Inter, sans-serif',
                        fontSize: '13px',
                        fontWeight: 500,
                      }}
                    >
                      Difícil
                    </Button>
                  </Group>
                </Box>
              </Stack>
            </Card>

            {/* Summary Card */}
            <Card
              padding="lg"
              style={{
                backgroundColor: colorScheme === 'dark'
                  ? 'rgba(14, 165, 233, 0.08)'
                  : 'rgba(160, 142, 115, 0.1)',
                border: `1px solid ${colorScheme === 'dark' ? 'rgba(14, 165, 233, 0.2)' : 'rgba(160, 142, 115, 0.3)'}`,
                borderRadius: '12px',
              }}
            >
              <Text
                size="md"
                fw={600}
                mb="md"
                style={{
                  color: colorScheme === 'dark' ? '#0ea5e9' : '#8b7355',
                  fontFamily: 'Space Grotesk, Inter, sans-serif',
                }}
              >
                Resumen
              </Text>

              <Stack gap="sm" style={{ textAlign: 'left' }}>
                <Group justify="space-between">
                  <Text size="sm" style={{ color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26', fontFamily: 'Inter, sans-serif', textAlign: 'left' }}>
                    Temas:
                  </Text>
                  <Text size="sm" fw={600} style={{ color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26', fontFamily: 'Inter, sans-serif', textAlign: 'right' }}>
                    {selectedTemas.length}
                  </Text>
                </Group>

                <Group justify="space-between">
                  <Text size="sm" style={{ color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26', fontFamily: 'Inter, sans-serif', textAlign: 'left' }}>
                    Preguntas:
                  </Text>
                  <Text size="sm" fw={600} style={{ color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26', fontFamily: 'Inter, sans-serif', textAlign: 'right' }}>
                    {numReactivos}
                  </Text>
                </Group>

                <Group justify="space-between">
                  <Text size="sm" style={{ color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26', fontFamily: 'Inter, sans-serif', textAlign: 'left' }}>
                    Dificultad:
                  </Text>
                  <Text size="sm" fw={600} style={{ color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26', fontFamily: 'Inter, sans-serif', textAlign: 'right' }}>
                    {selectedDificultad ? (selectedDificultad === 1 ? 'Fácil' : selectedDificultad === 2 ? 'Intermedio' : 'Difícil') : 'Todas'}
                  </Text>
                </Group>

                <Group justify="space-between">
                  <Text size="sm" style={{ color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26', fontFamily: 'Inter, sans-serif', textAlign: 'left' }}>
                    Tiempo estimado:
                  </Text>
                  <Text size="sm" fw={600} style={{ color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26', fontFamily: 'Inter, sans-serif', textAlign: 'right' }}>
                    {Math.ceil(numReactivos * 1.5)} min
                  </Text>
                </Group>
              </Stack>
            </Card>

            {/* Generate Button */}
            <Button
              fullWidth
              size="lg"
              onClick={handleGenerarExamen}
              disabled={selectedTemas.length === 0}
              style={{
                backgroundColor: selectedTemas.length > 0
                  ? colorScheme === 'dark' ? '#0ea5e9' : '#8b7355'
                  : '#6b7280',
                fontSize: '16px',
                fontWeight: 600,
                fontFamily: 'Inter, sans-serif',
                borderRadius: '8px',
                padding: '16px 24px',
              }}
            >
              Generar Examen Personalizado
            </Button>
          </Stack>
        </Grid.Col>
      </Grid>
      </Box>

      {/* Sticky Footer */}
      <Box
        style={{
          borderTop: `1px solid ${colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(221, 216, 209, 0.5)'}`,
          textAlign: 'center',
          padding: '1rem',
          backgroundColor: colorScheme === 'dark'
            ? 'rgba(15, 23, 42, 0.8)'
            : 'rgba(247, 243, 238, 0.8)',
          marginTop: 'auto',
        }}
      >
        <Text
          size="xs"
          style={{
            color: colorScheme === 'dark' ? '#94a3b8' : '#5a5550',
            fontFamily: 'Inter, sans-serif',
          }}
        >
          © 2024 ENARM360. Todos los derechos reservados.
        </Text>
      </Box>
      </Box>
    </>
  );
};

export default ExamenFiltrosPage;
