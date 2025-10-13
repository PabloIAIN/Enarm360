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
  Collapse,
  ActionIcon,
  Tooltip,
} from '@mantine/core';
import { IconPlayerPause, IconPlayerPlay } from '@tabler/icons-react';
import examenService from '../services/examenService';
import {
  IniciarExamenResponse,
  RespuestaUsuario,
  ExamenEnProgreso,
  RecuperarProgresoResponse
} from '../types/examen.types';
import QuejaSugerenciaButton from '../components/QuejaSugerenciaButton';
import RecuperarExamenModal from '../components/RecuperarExamenModal';
import { authService } from '../services/authService';
import { examenStorage } from '../utils/examenStorage';

const ExamenRapidoPage: React.FC = () => {
  const navigate = useNavigate();
  const { colorScheme } = useMantineColorScheme();
  const currentUser = authService.getCurrentUserFromStorage();

  // Estado del examen
  const [examenData, setExamenData] = useState<IniciarExamenResponse | null>(null);
  const [preguntaActual, setPreguntaActual] = useState(0);
  const [respuestas, setRespuestas] = useState<Map<number, string | null>>(new Map());
  const [tiempoInicio, setTiempoInicio] = useState<Date | null>(null);
  const [tiempoTranscurrido, setTiempoTranscurrido] = useState(0);
  const [loading, setLoading] = useState(false);
  const [iniciandoExamen, setIniciandoExamen] = useState(true);
  const [pausado, setPausado] = useState(false);

  // Estado para recuperación de examen
  const [examenEnProgreso, setExamenEnProgreso] = useState<ExamenEnProgreso | null>(null);
  const [mostrarModalRecuperar, setMostrarModalRecuperar] = useState(false);
  const [cargandoRecuperacion, setCargandoRecuperacion] = useState(false);

  // Verificar examen en progreso al cargar
  useEffect(() => {
    verificarExamenEnProgreso();
  }, []);

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
        if (examen.tipoExamen === 'RAPIDO') {
          setExamenEnProgreso(examen);
          setMostrarModalRecuperar(true);
          setIniciandoExamen(false);
        } else {
          // Hay un examen en progreso pero es de otro tipo
          // Informar al usuario
          const continuar = window.confirm(
            `Tienes un ${examen.nombreExamen} en progreso. ¿Deseas ir a ese examen?`
          );
          if (continuar) {
            // Redirigir según el tipo
            if (examen.tipoExamen === 'ENARM') {
              navigate('/estudiante/simulacro');
            } else if (examen.tipoExamen === 'FILTRADO') {
              navigate(`/estudiante/examen-filtrado/${examen.intentoId}`);
            }
          } else {
            // Usuario quiere iniciar uno nuevo de todos modos
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

  // Timer para el examen
  useEffect(() => {
    if (!tiempoInicio || pausado) return;

    const interval = setInterval(() => {
      const ahora = new Date();
      const diferencia = Math.floor((ahora.getTime() - tiempoInicio.getTime()) / 1000);
      setTiempoTranscurrido(diferencia);
    }, 1000);

    return () => clearInterval(interval);
  }, [tiempoInicio, pausado]);

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
      // Reanudar: ajustar tiempoInicio para compensar el tiempo pausado
      const tiempoPausado = tiempoTranscurrido;
      setTiempoInicio(new Date(Date.now() - tiempoPausado * 1000));
    } else {
      // Pausar: guardar el tiempo actual
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
    try {
      console.log('🚀 Iniciando examen rápido...');
      const data = await examenService.crearExamenRapido(currentUser.id);
      setExamenData(data);
      setTiempoInicio(new Date());
      console.log('✅ Examen iniciado:', data);
    } catch (error) {
      console.error('❌ Error al iniciar examen:', error);
      alert('Error al iniciar el examen. Intenta de nuevo.');
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
      guardarProgresoActual(); // Guardar inmediatamente al cambiar de pregunta
    }
  };

  const preguntaAnterior = () => {
    if (preguntaActual > 0) {
      setPreguntaActual(preguntaActual - 1);
      guardarProgresoActual(); // Guardar inmediatamente al cambiar de pregunta
    }
  };

  const finalizarExamen = async () => {
    if (!examenData || !tiempoInicio) return;

    const confirmacion = window.confirm(
      '¿Estás seguro de que quieres finalizar el examen?'
    );

    if (!confirmacion) return;

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
      console.error('❌ Error al finalizar examen:', error);
      alert('Error al finalizar el examen.');
    } finally {
      setLoading(false);
    }
  };

  const formatearTiempo = (segundos: number): string => {
    const mins = Math.floor(segundos / 60);
    const secs = segundos % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  if (iniciandoExamen) {
    return (
      <Center style={{ minHeight: '100vh' }}>
        <Stack align="center">
          <Loader size="lg" />
          <Text ml="md">Generando examen rápido...</Text>
        </Stack>
      </Center>
    );
  }

  if (!examenData) {
    return (
      <Center style={{ minHeight: '100vh' }}>
        <Stack align="center">
          <Text size="xl">Error al cargar el examen</Text>
          <Button onClick={() => navigate('/')}>Volver al inicio</Button>
        </Stack>
      </Center>
    );
  }

  const pregunta = examenData.preguntas[preguntaActual];
  const respuestaSeleccionada = respuestas.get(pregunta.orden);

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
            maxWidth: '900px',
            margin: '0 auto'
          }}
        >
          {/* Header */}
          <Group justify="space-between" mb="lg">
            <div>
              <Title order={2}>Examen Rápido</Title>
              <Text size="sm" c="dimmed">{examenData.totalPreguntas} preguntas</Text>
            </div>
            <Group gap="xs">
              <Badge color={pausado ? "red" : "blue"} size="xl">
                ⏱ {formatearTiempo(tiempoTranscurrido)}
              </Badge>
              <Tooltip label={pausado ? "Reanudar" : "Pausar"}>
                <ActionIcon
                  size="lg"
                  variant="light"
                  color={pausado ? "green" : "blue"}
                  onClick={togglePausa}
                >
                  {pausado ? <IconPlayerPlay size={20} /> : <IconPlayerPause size={20} />}
                </ActionIcon>
              </Tooltip>
            </Group>
          </Group>

          {/* Progreso */}
          <Group justify="space-between" mb="md">
            <Badge color="green" size="lg">
              Pregunta {preguntaActual + 1} / {examenData.totalPreguntas}
            </Badge>
            <Badge color="violet" size="lg">
              {pregunta.tipo}
            </Badge>
          </Group>

          {/* Caso de estudio (si aplica) */}
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
              <Button
                color="teal"
                onClick={finalizarExamen}
                disabled={loading}
              >
                {loading ? 'Finalizando...' : 'Finalizar Examen'}
              </Button>
            )}
          </Group>
        </Paper>
      </Box>
    </>
  );
};

export default ExamenRapidoPage;
