import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  Box,
  Title,
  Text,
  Paper,
  Stack,
  Group,
  Badge,
  Button,
  useMantineColorScheme,
  Progress,
  Divider,
  Center,
  RingProgress,
  Accordion,
} from '@mantine/core';
import { ResultadoExamen, DetallePreguntaResultado } from '../types/examen.types';

const ResultadosPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { colorScheme } = useMantineColorScheme();
  const resultado = location.state?.resultado as ResultadoExamen;

  const [filtro, setFiltro] = useState<'todas' | 'correctas' | 'incorrectas' | 'enblanco'>('todas');

  if (!resultado) {
    return (
      <Center style={{ minHeight: '100vh' }}>
        <Stack align="center">
          <Text size="xl">No se encontraron resultados</Text>
          <Button onClick={() => navigate('/estudiante/dashboard')}>Volver al inicio</Button>
        </Stack>
      </Center>
    );
  }

  const formatearTiempo = (segundos: number): string => {
    const horas = Math.floor(segundos / 3600);
    const mins = Math.floor((segundos % 3600) / 60);
    const secs = segundos % 60;

    if (horas > 0) {
      return `${horas}h ${mins}m ${secs}s`;
    }
    return `${mins}m ${secs}s`;
  };

  const obtenerPreguntasFiltradas = (): DetallePreguntaResultado[] => {
    switch (filtro) {
      case 'correctas':
        return resultado.preguntas.filter(p => p.esCorrecta);
      case 'incorrectas':
        return resultado.preguntas.filter(p => !p.esCorrecta && p.respuestaUsuario !== null);
      case 'enblanco':
        return resultado.preguntas.filter(p => p.respuestaUsuario === null);
      default:
        return resultado.preguntas;
    }
  };

  const preguntasFiltradas = obtenerPreguntasFiltradas();

  return (
    <Box
      style={{
        minHeight: '100vh',
        background:
          colorScheme === 'dark'
            ? 'linear-gradient(135deg, #0f172a 0%, #1e293b 100%)'
            : 'linear-gradient(135deg, #f7f3ee 0%, #f2ede6 100%)',
        padding: '24px',
      }}
    >
      <Paper
        shadow="md"
        radius="md"
        p="xl"
        withBorder
        style={{
          backgroundColor:
            colorScheme === 'dark' ? '#1e293b' : 'rgba(255,255,255,0.95)',
          maxWidth: '1200px',
          margin: '0 auto'
        }}
      >
        {/* Header */}
        <Title order={1} mb="md">Resultados del Examen</Title>
        <Text size="lg" c="dimmed" mb="xl">{resultado.tipoExamen}</Text>

        {/* Estadísticas principales */}
        <Group justify="center" mb="xl" grow>
          <Paper p="md" withBorder style={{ textAlign: 'center' }}>
            <Text size="xl" fw={700}>{resultado.totalPreguntas}</Text>
            <Text size="sm" c="dimmed">Total</Text>
          </Paper>

          <Paper p="md" withBorder style={{ textAlign: 'center', backgroundColor: '#E8F5E9' }}>
            <Text size="xl" fw={700} c="green">{resultado.correctas}</Text>
            <Text size="sm" c="dimmed">Correctas</Text>
          </Paper>

          <Paper p="md" withBorder style={{ textAlign: 'center', backgroundColor: '#FFEBEE' }}>
            <Text size="xl" fw={700} c="red">{resultado.incorrectas}</Text>
            <Text size="sm" c="dimmed">Incorrectas</Text>
          </Paper>

          <Paper p="md" withBorder style={{ textAlign: 'center', backgroundColor: '#FFF3E0' }}>
            <Text size="xl" fw={700} c="orange">{resultado.enBlanco}</Text>
            <Text size="sm" c="dimmed">En Blanco</Text>
          </Paper>
        </Group>

        {/* Gráfico circular y tiempo */}
        <Group justify="space-around" align="center" mb="xl">
          <div>
            <Center>
              <RingProgress
                size={180}
                thickness={20}
                sections={[
                  { value: resultado.porcentajeAciertos, color: 'teal' },
                ]}
                label={
                  <Center>
                    <div>
                      <Text size="xl" fw={700} ta="center">
                        {Math.round(resultado.porcentajeAciertos)}%
                      </Text>
                      <Text size="sm" c="dimmed" ta="center">
                        Aciertos
                      </Text>
                    </div>
                  </Center>
                }
              />
            </Center>
          </div>

          <Stack gap="xs">
            <Text><strong>Tiempo total:</strong> {formatearTiempo(resultado.duracionSeg)}</Text>
            <Text><strong>Inicio:</strong> {new Date(resultado.iniciadoEn).toLocaleString()}</Text>
            <Text><strong>Fin:</strong> {new Date(resultado.finalizadoEn).toLocaleString()}</Text>
          </Stack>
        </Group>

        <Divider my="xl" />

        {/* Filtros */}
        <Group mb="md">
          <Button
            variant={filtro === 'todas' ? 'filled' : 'outline'}
            onClick={() => setFiltro('todas')}
          >
            Todas ({resultado.totalPreguntas})
          </Button>
          <Button
            variant={filtro === 'correctas' ? 'filled' : 'outline'}
            color="green"
            onClick={() => setFiltro('correctas')}
          >
            Correctas ({resultado.correctas})
          </Button>
          <Button
            variant={filtro === 'incorrectas' ? 'filled' : 'outline'}
            color="red"
            onClick={() => setFiltro('incorrectas')}
          >
            Incorrectas ({resultado.incorrectas})
          </Button>
          <Button
            variant={filtro === 'enblanco' ? 'filled' : 'outline'}
            color="orange"
            onClick={() => setFiltro('enblanco')}
          >
            En Blanco ({resultado.enBlanco})
          </Button>
        </Group>

        {/* Preguntas con retroalimentación */}
        <Title order={3} mb="md">
          Revisión de Preguntas ({preguntasFiltradas.length})
        </Title>

        <Accordion variant="separated">
          {preguntasFiltradas.map((pregunta) => (
            <Accordion.Item key={pregunta.orden} value={`pregunta-${pregunta.orden}`}>
              <Accordion.Control>
                <Group justify="space-between">
                  <Text fw={500}>Pregunta {pregunta.orden}</Text>
                  <Group gap="xs">
                    <Badge color="violet" size="sm">{pregunta.tipo}</Badge>
                    <Badge
                      color={pregunta.respuestaUsuario === null ? 'gray' : pregunta.esCorrecta ? 'green' : 'red'}
                      size="sm"
                    >
                      {pregunta.respuestaUsuario === null ? 'Sin responder' : pregunta.esCorrecta ? '✓ Correcta' : '✗ Incorrecta'}
                    </Badge>
                  </Group>
                </Group>
              </Accordion.Control>

              <Accordion.Panel>
                {/* Caso clínico (si existe) */}
                {pregunta.casoEstudioContenido && (
                  <Paper
                    p="md"
                    mb="md"
                    withBorder
                    style={{
                      backgroundColor: colorScheme === 'dark' ? '#334155' : '#f8f9fa',
                      borderLeft: '4px solid #228be6',
                    }}
                  >
                    <Text fw={500} size="sm" mb="xs" c="blue">
                      📋 CASO CLÍNICO
                    </Text>
                    <Text style={{ whiteSpace: 'pre-wrap' }}>
                      {pregunta.casoEstudioContenido}
                    </Text>
                  </Paper>
                )}

                {/* Pregunta */}
                <Text fw={500} mb="md">{pregunta.pregunta}</Text>

                {/* Opciones */}
                <Stack gap="xs" mb="md">
                  {['a', 'b', 'c', 'd'].map((opcion) => {
                    const textoRespuesta =
                      opcion === 'a' ? pregunta.respuestaA :
                      opcion === 'b' ? pregunta.respuestaB :
                      opcion === 'c' ? pregunta.respuestaC :
                      pregunta.respuestaD;

                    const esRespuestaUsuario = pregunta.respuestaUsuario === opcion;
                    const esRespuestaCorrecta = pregunta.respuestaCorrecta === opcion;

                    let bgColor = colorScheme === 'dark' ? '#1e293b' : '#fff';
                    if (esRespuestaCorrecta) bgColor = colorScheme === 'dark' ? '#134e4a' : '#E8F5E9';
                    else if (esRespuestaUsuario) bgColor = colorScheme === 'dark' ? '#7f1d1d' : '#FFEBEE';

                    return (
                      <Paper
                        key={opcion}
                        p="sm"
                        withBorder
                        style={{
                          backgroundColor: bgColor,
                          borderWidth: esRespuestaCorrecta || esRespuestaUsuario ? 2 : 1,
                          borderColor: esRespuestaCorrecta ? 'green' : esRespuestaUsuario ? 'red' : undefined
                        }}
                      >
                        <Group justify="space-between">
                          <Text>
                            <strong>{opcion.toUpperCase()})</strong> {textoRespuesta}
                          </Text>
                          {esRespuestaCorrecta && <Badge color="green">✓ Correcta</Badge>}
                          {esRespuestaUsuario && !esRespuestaCorrecta && (
                            <Badge color="red">Tu respuesta</Badge>
                          )}
                        </Group>
                      </Paper>
                    );
                  })}
                </Stack>

                {/* Retroalimentación */}
                {pregunta.retroalimentacion && (
                  <Paper
                    p="md"
                    withBorder
                    style={{
                      backgroundColor: colorScheme === 'dark' ? '#1e3a5f' : '#E3F2FD',
                    }}
                  >
                    <Text fw={500} mb="xs">💡 Retroalimentación:</Text>
                    <Text>{pregunta.retroalimentacion}</Text>
                  </Paper>
                )}

                <Text size="xs" c="dimmed" mt="sm">
                  Tiempo: {pregunta.tiempoSeg}s
                </Text>
              </Accordion.Panel>
            </Accordion.Item>
          ))}
        </Accordion>

        {/* Botones de acción */}
        <Group justify="center" mt="xl">
          <Button variant="outline" onClick={() => navigate('/estudiante/dashboard')}>
            Volver al Inicio
          </Button>
          <Button onClick={() => window.print()}>
            Imprimir Resultados
          </Button>
        </Group>
      </Paper>
    </Box>
  );
};

export default ResultadosPage;
