import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Title,
  Text,
  Radio,
  Button,
  Stack,
  Group,
  Paper,
  Badge,
  useMantineColorScheme,
  Center,
  Loader,
  Modal,
  Progress,
  ActionIcon,
  Tooltip,
} from '@mantine/core';
import { IconPlayerPause, IconPlayerPlay } from '@tabler/icons-react';
import examenService from '../services/examenService';
import {
  IniciarExamenResponse,
  RespuestaUsuario,
  ExamenEnProgreso
} from '../types/examen.types';
import QuejaSugerenciaButton from '../components/QuejaSugerenciaButton';
import RecuperarExamenModal from '../components/RecuperarExamenModal';
import { authService } from '../services/authService';
import { examenStorage } from '../utils/examenStorage';

const ExamenSimulacroPage: React.FC = () => {
  const navigate = useNavigate();
  const { colorScheme } = useMantineColorScheme();
  const currentUser = authService.getCurrentUserFromStorage();

  const [examenData, setExamenData] = useState<IniciarExamenResponse | null>(null);
  const [preguntaActual, setPreguntaActual] = useState(0);
  const [respuestas, setRespuestas] = useState<Map<number, string | null>>(new Map());
  const [tiempoInicio, setTiempoInicio] = useState<Date | null>(null);
  const [tiempoTranscurrido, setTiempoTranscurrido] = useState(0);
  const [loading, setLoading] = useState(false);
  const [iniciandoExamen, setIniciandoExamen] = useState(false);
  const [mostrarAdvertencia, setMostrarAdvertencia] = useState(true);
  const [mostrarMapa, setMostrarMapa] = useState(false);
  const [pausado, setPausado] = useState(false);

  // Estado para recuperación de examen
  const [examenEnProgreso, setExamenEnProgreso] = useState<ExamenEnProgreso | null>(null);
  const [mostrarModalRecuperar, setMostrarModalRecuperar] = useState(false);
  const [cargandoRecuperacion, setCargandoRecuperacion] = useState(false);

  // Verificar examen en progreso cuando se cierra la advertencia
  const verificarExamenEnProgreso = async () => {
    if (!currentUser) {
      alert('No se pudo obtener el usuario actual');
      navigate('/');
      return;
    }

    try {
      const examen = await examenService.verificarExamenEnProgreso(currentUser.id);
      if (examen) {
        // Verificar si el tipo de examen coincide con esta página
        if (examen.tipoExamen === 'ENARM') {
          setExamenEnProgreso(examen);
          setMostrarModalRecuperar(true);
          setIniciandoExamen(false);
        } else {
          // Hay un examen en progreso pero es de otro tipo
          const continuar = window.confirm(
            `Tienes un ${examen.nombreExamen} en progreso. ¿Deseas ir a ese examen?`
          );
          if (continuar) {
            if (examen.tipoExamen === 'RAPIDO') {
              navigate('/estudiante/examen-rapido');
            } else if (examen.tipoExamen === 'FILTRADO') {
              navigate(`/estudiante/examen-filtrado/${examen.intentoId}`);
            }
          } else {
            // El usuario no quiere ir al otro examen, iniciar nuevo simulacro
            await iniciarExamen();
          }
        }
      } else {
        // No hay examen en progreso, iniciar nuevo
        await iniciarExamen();
      }
    } catch (error) {
      console.error('Error verificando examen en progreso:', error);
      await iniciarExamen();
    }
  };

  useEffect(() => {
    if (!tiempoInicio || pausado) return;

    const interval = setInterval(() => {
      const ahora = new Date();
      const diferencia = Math.floor((ahora.getTime() - tiempoInicio.getTime()) / 1000);
      setTiempoTranscurrido(diferencia);

      // Alerta si quedan 30 minutos
      if (examenData && examenData.tiempoLimiteMin) {
        const tiempoLimite = examenData.tiempoLimiteMin * 60;
        const tiempoRestante = tiempoLimite - diferencia;
        if (tiempoRestante === 1800) {
          alert('⚠️ Quedan 30 minutos para finalizar el examen');
        }
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [tiempoInicio, examenData, pausado]);

  // Auto-guardar progreso cada 30 segundos
  useEffect(() => {
    if (!examenData || !tiempoInicio) return;

    const interval = setInterval(() => {
      guardarProgresoActual();
    }, 30000); // 30 segundos

    return () => clearInterval(interval);
  }, [examenData, preguntaActual, tiempoTranscurrido, pausado, respuestas]);

  const togglePausa = () => {
    if (pausado) {
      const tiempoPausado = tiempoTranscurrido;
      setTiempoInicio(new Date(Date.now() - tiempoPausado * 1000));
    } else {
      setTiempoTranscurrido(tiempoTranscurrido);
      guardarProgresoActual(); // Guardar inmediatamente al pausar
    }
    setPausado(!pausado);
  };

  const iniciarExamen = async () => {
    if (!currentUser) {
      alert('No se pudo obtener el usuario actual');
      navigate('/');
      return;
    }

    setIniciandoExamen(true);
    setMostrarAdvertencia(false);
    try {
      console.log('Iniciando Simulacro ENARM...');
      const data = await examenService.crearSimulacroENARM(currentUser.id);
      setExamenData(data);
      setTiempoInicio(new Date());
      console.log('✅ Simulacro ENARM iniciado:', data);
    } catch (error) {
      console.error('❌ Error al iniciar simulacro:', error);
      alert('Error al iniciar el simulacro ENARM.');
      navigate('/');
    } finally {
      setIniciandoExamen(false);
    }
  };

  const recuperarExamen = async () => {
    if (!examenEnProgreso) return;

    setCargandoRecuperacion(true);
    try {
      const progreso = await examenService.recuperarProgreso(examenEnProgreso.intentoId);

      // Restaurar estado completo
      setExamenData(progreso.examenData);
      setPreguntaActual(progreso.preguntaActual);
      setTiempoTranscurrido(progreso.tiempoTranscurrido);
      setPausado(progreso.pausado || false);

      // Restaurar respuestas
      const respuestasMap = new Map<number, string | null>();
      Object.entries(progreso.respuestas).forEach(([orden, respuesta]) => {
        respuestasMap.set(parseInt(orden), respuesta);
      });
      setRespuestas(respuestasMap);

      // Calcular tiempo de inicio ajustado
      const tiempoAjustado = new Date(Date.now() - progreso.tiempoTranscurrido * 1000);
      setTiempoInicio(tiempoAjustado);

      setMostrarModalRecuperar(false);
      setMostrarAdvertencia(false);

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
      // Iniciar nuevo examen
      await iniciarExamen();
    } catch (error) {
      console.error('Error abandonando examen:', error);
      alert('Error al abandonar el examen anterior');
    } finally {
      setCargandoRecuperacion(false);
    }
  };

  const guardarProgresoActual = async () => {
    if (!examenData) return;

    try {
      // Convertir Map a objeto
      const respuestasObj: Record<number, string> = {};
      respuestas.forEach((valor, clave) => {
        if (valor) respuestasObj[clave] = valor;
      });

      const request = {
        intentoId: examenData.intentoId,
        preguntaActual,
        tiempoTranscurrido,
        pausado,
        respuestas: respuestasObj
      };

      // Guardar en backend
      await examenService.guardarProgreso(request);

      // Guardar en localStorage como backup
      examenStorage.guardarProgreso(request);

      console.log('💾 Progreso guardado');
    } catch (error) {
      console.error('Error guardando progreso:', error);
    }
  };

  const seleccionarRespuesta = (respuesta: string) => {
    if (!examenData) return;
    const pregunta = examenData.preguntas[preguntaActual];
    const nuevasRespuestas = new Map(respuestas);
    nuevasRespuestas.set(pregunta.orden, respuesta);
    setRespuestas(nuevasRespuestas);
  };

  const siguientePregunta = () => {
    if (!examenData) return;
    if (preguntaActual < examenData.preguntas.length - 1) {
      setPreguntaActual(preguntaActual + 1);
      guardarProgresoActual();
    }
  };

  const preguntaAnterior = () => {
    if (preguntaActual > 0) {
      setPreguntaActual(preguntaActual - 1);
      guardarProgresoActual();
    }
  };

  const irAPregunta = (indice: number) => {
    setPreguntaActual(indice);
    setMostrarMapa(false);
    guardarProgresoActual();
  };

  const finalizarExamen = async () => {
    if (!examenData || !tiempoInicio) return;

    const respondidas = respuestas.size;
    const sinResponder = examenData.totalPreguntas - respondidas;

    if (sinResponder > 0) {
      if (!window.confirm(
        `Tienes ${sinResponder} preguntas sin responder. ¿Estás seguro de finalizar el examen?`
      )) {
        return;
      }
    }

    setLoading(true);
    try {
      const respuestasArray: RespuestaUsuario[] = examenData.preguntas.map((pregunta) => ({
        orden: pregunta.orden,
        respuesta: respuestas.get(pregunta.orden) || null,
        tiempoSeg: Math.floor(tiempoTranscurrido / examenData.totalPreguntas)
      }));

      const resultado = await examenService.finalizarExamen({
        intentoId: examenData.intentoId,
        duracionSeg: tiempoTranscurrido,
        respuestas: respuestasArray
      });

      // Limpiar progreso guardado
      examenStorage.limpiarProgreso();

      navigate(`/estudiante/resultados/${examenData.intentoId}`, { state: { resultado } });
    } catch (error) {
      console.error('Error al finalizar:', error);
      alert('Error al finalizar el examen.');
    } finally {
      setLoading(false);
    }
  };

  const formatearTiempo = (segundos: number): string => {
    const horas = Math.floor(segundos / 3600);
    const mins = Math.floor((segundos % 3600) / 60);
    const secs = segundos % 60;
    return `${horas.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const manejarInicioExamen = () => {
    setMostrarAdvertencia(false);
    verificarExamenEnProgreso();
  };

  // Pantalla de advertencia
  if (mostrarAdvertencia) {
    return (
      <Center style={{ minHeight: '100vh', padding: '24px' }}>
        <Paper
          shadow="md"
          radius="md"
          p="xl"
          withBorder
          style={{
            backgroundColor:
              colorScheme === 'dark' ? '#1e293b' : 'rgba(255,255,255,0.95)',
            maxWidth: '600px'
          }}
        >
          <Title order={2} mb="md">Simulacro ENARM</Title>
          <Title order={3} mb="lg" c="orange">⚠️ Información Importante</Title>

          <Stack gap="md" mb="xl">
            <Text>• Este simulacro contiene <strong>280 preguntas</strong></Text>
            <Text>• Tiempo límite: <strong>4 horas (240 minutos)</strong></Text>
            <Text>• Incluye reactivos y casos clínicos</Text>
            <Text>• Puedes pausar el examen y retomarlo después</Text>
            <Text>• Asegúrate de estar en un lugar tranquilo</Text>
          </Stack>

          <Group justify="space-between">
            <Button variant="outline" onClick={() => navigate('/estudiante/simulador')}>
              Cancelar
            </Button>
            <Button color="teal" onClick={manejarInicioExamen}>
              Iniciar Simulacro
            </Button>
          </Group>
        </Paper>
      </Center>
    );
  }

  if (iniciandoExamen) {
    return (
      <Center style={{ minHeight: '100vh' }}>
        <Stack align="center">
          <Loader size="xl" />
          <Title order={3}>Generando Simulacro ENARM...</Title>
          <Text c="dimmed">Esto puede tomar unos segundos...</Text>
        </Stack>
      </Center>
    );
  }

  if (!examenData) {
    return <Center style={{ minHeight: '100vh' }}><Text>Error al cargar el simulacro</Text></Center>;
  }

  const pregunta = examenData.preguntas[preguntaActual];
  const respuestaSeleccionada = respuestas.get(pregunta.orden);
  const respondidas = respuestas.size;
  const progreso = ((preguntaActual + 1) / examenData.totalPreguntas) * 100;

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
            maxWidth: '1000px',
            margin: '0 auto'
          }}
        >
          {/* Header */}
          <Group justify="space-between" mb="lg">
            <div>
              <Title order={2}>Simulacro ENARM</Title>
              <Text size="sm" c="dimmed">
                Respondidas: {respondidas}/{examenData.totalPreguntas}
              </Text>
            </div>
            <Group gap="xs">
              <Badge color={pausado ? "orange" : "red"} size="xl">
                ⏱ {formatearTiempo(tiempoTranscurrido)}
              </Badge>
              <Tooltip label={pausado ? "Reanudar" : "Pausar"}>
                <ActionIcon
                  size="lg"
                  variant="light"
                  color={pausado ? "green" : "red"}
                  onClick={togglePausa}
                >
                  {pausado ? <IconPlayerPlay size={20} /> : <IconPlayerPause size={20} />}
                </ActionIcon>
              </Tooltip>
            </Group>
          </Group>

          {/* Barra de progreso */}
          <Box mb="md">
            <Progress value={progreso} size="lg" radius="xl" />
          </Box>

          <Group justify="space-between" mb="md">
            <Badge color="green" size="lg">
              Pregunta {preguntaActual + 1} / {examenData.totalPreguntas}
            </Badge>
            <Group gap="xs">
              <Badge color="violet" size="lg">{pregunta.tipo}</Badge>
              <Button size="xs" variant="outline" onClick={() => setMostrarMapa(true)}>
                Mapa
              </Button>
            </Group>
          </Group>

          {/* Caso clínico */}
          {pregunta.tipo === 'CASO' && pregunta.casoEstudioContenido && (
            <Paper
              p="md"
              mb="lg"
              withBorder
              style={{
                backgroundColor: colorScheme === 'dark' ? '#334155' : '#f8f9fa',
                borderLeft: `4px solid ${colorScheme === 'dark' ? '#60a5fa' : '#228be6'}`,
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
          <Group justify="space-between" align="flex-start" mb="sm">
            <Title order={4} style={{ flex: 1 }}>
              {preguntaActual + 1}. {pregunta.pregunta}
            </Title>

            {currentUser && (
              <QuejaSugerenciaButton
                reactivoId={pregunta.tipo === 'REACTIVO' ? pregunta.reactivoId : undefined}
                preguntaCasoId={pregunta.tipo === 'CASO' ? pregunta.preguntaCasoId : undefined}
                tipoObjetivo={pregunta.tipo === 'CASO' ? 'PREGUNTA_CASO' : 'REACTIVO'}
              />
            )}
          </Group>

          {/* Mensaje de pausa */}
          {pausado && (
            <Paper
              p="md"
              mb="md"
              style={{
                backgroundColor: colorScheme === 'dark' ? 'rgba(251, 146, 60, 0.1)' : 'rgba(251, 146, 60, 0.1)',
                border: '2px solid rgba(251, 146, 60, 0.5)',
                borderRadius: '8px',
              }}
            >
              <Text size="sm" fw={500} c="orange" ta="center">
                ⏸️ El examen está pausado. Reanuda el cronómetro para continuar respondiendo.
              </Text>
            </Paper>
          )}

          {/* Opciones */}
          <Stack gap="sm" mb="xl" style={{ opacity: pausado ? 0.5 : 1, pointerEvents: pausado ? 'none' : 'auto' }}>
            <Radio.Group
              value={respuestaSeleccionada || ''}
              onChange={(value) => seleccionarRespuesta(value)}
            >
              <Stack gap="sm">
                <Radio value="a" label={pregunta.respuestaA} disabled={pausado} />
                <Radio value="b" label={pregunta.respuestaB} disabled={pausado} />
                <Radio value="c" label={pregunta.respuestaC} disabled={pausado} />
                <Radio value="d" label={pregunta.respuestaD} disabled={pausado} />
              </Stack>
            </Radio.Group>
          </Stack>

          {/* Navegación */}
          <Group justify="space-between" mt="xl">
            <Button
              variant="outline"
              onClick={preguntaAnterior}
              disabled={preguntaActual === 0}
            >
              ← Anterior
            </Button>

            {preguntaActual < examenData.totalPreguntas - 1 ? (
              <Button onClick={siguientePregunta}>
                Siguiente →
              </Button>
            ) : (
              <Button color="teal" onClick={finalizarExamen} disabled={loading} size="lg">
                {loading ? 'Finalizando...' : 'Finalizar Simulacro'}
              </Button>
            )}
          </Group>
        </Paper>

        {/* Modal del mapa de navegación */}
        <Modal
          opened={mostrarMapa}
          onClose={() => setMostrarMapa(false)}
          title="Navegación Rápida"
          size="lg"
        >
          <Box
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(50px, 1fr))',
              gap: '8px'
            }}
          >
            {examenData.preguntas.map((p, index) => (
              <Button
                key={p.orden}
                variant={index === preguntaActual ? 'filled' : respuestas.has(p.orden) ? 'light' : 'outline'}
                color={index === preguntaActual ? 'blue' : respuestas.has(p.orden) ? 'green' : 'gray'}
                onClick={() => irAPregunta(index)}
                size="sm"
              >
                {index + 1}
              </Button>
            ))}
          </Box>
        </Modal>
      </Box>
    </>
  );
};

export default ExamenSimulacroPage;
