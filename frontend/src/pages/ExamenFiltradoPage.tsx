import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
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
  ActionIcon,
  Tooltip,
} from '@mantine/core';
import { IconPlayerPause, IconPlayerPlay, IconDeviceFloppy, IconCheck } from '@tabler/icons-react';
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

const ExamenFiltradoPage: React.FC = () => {
  console.log('🚀🚀🚀 [EXAMEN_FILTRADO] ====== COMPONENTE MONTADO ======');

  const navigate = useNavigate();
  const location = useLocation();
  const { colorScheme } = useMantineColorScheme();
  const currentUser = authService.getCurrentUserFromStorage();

  console.log('📍 [EXAMEN_FILTRADO] Location state al montar:', location.state);
  console.log('👤 [EXAMEN_FILTRADO] Usuario actual:', currentUser?.username);

  const [examenData, setExamenData] = useState<IniciarExamenResponse | null>(
    location.state?.examenData || null
  );
  const [preguntaActual, setPreguntaActual] = useState(0);
  const [respuestas, setRespuestas] = useState<Map<number, string | null>>(new Map());
  const [tiempoInicio, setTiempoInicio] = useState<Date | null>(null);
  const [tiempoTranscurrido, setTiempoTranscurrido] = useState(0);
  const [loading, setLoading] = useState(false);
  const [pausado, setPausado] = useState(false);

  // Estado para recuperación de examen
  const [examenEnProgreso, setExamenEnProgreso] = useState<ExamenEnProgreso | null>(null);
  const [mostrarModalRecuperar, setMostrarModalRecuperar] = useState(false);
  const [cargandoRecuperacion, setCargandoRecuperacion] = useState(false);
  const [verificandoProgreso, setVerificandoProgreso] = useState(true);
  const yaVerificado = useRef(false);
  const verificarExamenEnProgreso = useCallback(async () => {
    console.log('🔎 [PAGE_VERIFICAR] ====== INICIO ======');

    // Evitar múltiples verificaciones
    if (yaVerificado.current) {
      console.log('⚠️ [PAGE_VERIFICAR] Ya se verificó anteriormente, saltando...');
      return;
    }
    yaVerificado.current = true;

    console.log('🚀 [PAGE_VERIFICAR] Iniciando verificación de examen en progreso');
    console.log('📍 [PAGE_VERIFICAR] Usuario actual:', currentUser?.id, '-', currentUser?.username);
    console.log('📍 [PAGE_VERIFICAR] Location state:', {
      continuandoExamen: location.state?.continuandoExamen,
      tieneExamenData: !!location.state?.examenData,
      recuperando: location.state?.recuperando,
      tieneExamenEnProgreso: !!location.state?.examenEnProgreso
    });

    if (!currentUser) {
      console.error('❌ [PAGE_VERIFICAR] No hay usuario actual');
      alert('No se pudo obtener el usuario actual');
      navigate('/');
      return;
    }

    // PRIMERA PRIORIDAD: Si viene desde el simulador para continuar
    if (location.state?.continuandoExamen && location.state?.examenEnProgreso) {
      console.log('🔄 [PAGE_VERIFICAR] PRIORIDAD 1: Continuando examen desde simulador');
      console.log('📦 [PAGE_VERIFICAR] Datos del examen en progreso:');
      console.log('   - IntentoId:', location.state.examenEnProgreso.intentoId);
      console.log('   - Tipo:', location.state.examenEnProgreso.tipoExamen);
      console.log('🔧 [PAGE_VERIFICAR] Configurando estado...');
      setExamenEnProgreso(location.state.examenEnProgreso);
      setMostrarModalRecuperar(true);
      setVerificandoProgreso(false);
      console.log('✅ [PAGE_VERIFICAR] Modal configurado para mostrarse');
      console.log('🎉 [PAGE_VERIFICAR] ====== FIN (Desde simulador) ======');
      return; // IMPORTANTE: Salir aquí y NO continuar con las otras prioridades
    }

    // SEGUNDA PRIORIDAD: Si ya tenemos examenData del state
    if (location.state?.examenData) {
      console.log('📄 [PAGE_VERIFICAR] PRIORIDAD 2: Usando examenData del state');
      if (location.state?.recuperando) {
        console.log('🔧 [PAGE_VERIFICAR] Modo recuperación desde filtros');
        // Es una recuperación desde filtros, restaurar estado
        const progreso = location.state.progreso;
        console.log('📊 [PAGE_VERIFICAR] Progreso a restaurar:', progreso);
        setPreguntaActual(progreso.preguntaActual);
        setTiempoTranscurrido(progreso.tiempoTranscurrido);
        setPausado(progreso.pausado || false);

        // Restaurar respuestas
        const respuestasMap = new Map<number, string | null>();
        Object.entries(progreso.respuestas).forEach(([orden, respuesta]) => {
          respuestasMap.set(parseInt(orden), respuesta as string);
        });
        setRespuestas(respuestasMap);
        console.log('✅ [PAGE_VERIFICAR] Respuestas restauradas:', respuestasMap.size);

        // Calcular tiempo de inicio ajustado
        const tiempoAjustado = new Date(Date.now() - progreso.tiempoTranscurrido * 1000);
        setTiempoInicio(tiempoAjustado);

        console.log('✅ [PAGE_VERIFICAR] Estado recuperado desde filtros');
      } else {
        console.log('🆕 [PAGE_VERIFICAR] Modo examen nuevo desde filtros');
        // Es un examen nuevo desde filtros
        setTiempoInicio(new Date());
        console.log('✅ [PAGE_VERIFICAR] Examen nuevo iniciado');
      }
      setVerificandoProgreso(false);
      console.log('🎉 [PAGE_VERIFICAR] ====== FIN (Con examenData) ======');
      return;
    }

    // TERCERA PRIORIDAD: Solo si no hay nada en el state, verificar servidor
    console.log('🚑 [PAGE_VERIFICAR] PRIORIDAD 3: FALLBACK - Verificando servidor porque no hay datos en state');

    try {
      // Verificar autenticación antes de hacer la petición
      console.log('🔐 [PAGE_VERIFICAR] Verificando autenticación...');
      if (!authService.isAuthenticated()) {
        console.error('❌ [PAGE_VERIFICAR] Usuario no autenticado, redirigiendo al login');
        navigate('/login?expired=true');
        return;
      }
      console.log('✅ [PAGE_VERIFICAR] Usuario autenticado');

      console.log('🌐 [PAGE_VERIFICAR] Llamando al servidor...');
      const examen = await examenService.verificarExamenEnProgreso(currentUser.id);
      console.log('📡 [PAGE_VERIFICAR] Respuesta recibida del servidor:', examen);
      if (examen) {
        console.log('✅ [PAGE_VERIFICAR] Servidor devolvió examen:', examen.tipoExamen);
        // Verificar si el tipo de examen coincide con esta página
        if (examen.tipoExamen === 'FILTRADO') {
          console.log('✅ [PAGE_VERIFICAR] Tipo correcto (FILTRADO), mostrando modal');
          setExamenEnProgreso(examen);
          setMostrarModalRecuperar(true);
          setVerificandoProgreso(false);
          console.log('🎉 [PAGE_VERIFICAR] ====== FIN (Modal mostrado) ======');
        } else {
          console.log('⚠️ [PAGE_VERIFICAR] Tipo incorrecto:', examen.tipoExamen);
          // Hay un examen en progreso pero es de otro tipo
          const continuar = window.confirm(
            `Tienes un ${examen.nombreExamen} en progreso. ¿Deseas ir a ese examen?`
          );
          if (continuar) {
            console.log('🔀 [PAGE_VERIFICAR] Usuario acepta ir al otro examen');
            if (examen.tipoExamen === 'RAPIDO') {
              console.log('🔀 [PAGE_VERIFICAR] Navegando a examen-rapido');
              navigate('/estudiante/examen-rapido');
            } else if (examen.tipoExamen === 'ENARM') {
              console.log('🔀 [PAGE_VERIFICAR] Navegando a simulacro');
              navigate('/estudiante/simulacro');
            }
          } else {
            console.log('🔀 [PAGE_VERIFICAR] Usuario rechaza, navegando a filtros');
            // El usuario no quiere ir al otro examen, redirigir a filtros
            setVerificandoProgreso(false);
            navigate('/estudiante/simulador/filtros');
          }
        }
      } else {
        console.log('📭 [PAGE_VERIFICAR] Servidor no devolvió examen');
        // No hay examen en progreso y tampoco vino del state
        setVerificandoProgreso(false);
        console.log('🔀 [PAGE_VERIFICAR] Navegando a filtros (sin examen)');
        // Redirigir a filtros si no hay examen
        navigate('/estudiante/simulador/filtros');
      }
    } catch (error: any) {
      console.error('❌ [PAGE_VERIFICAR] ERROR en verificación:');
      console.error('   - Tipo:', error.constructor?.name);
      console.error('   - Mensaje:', error.message);
      console.error('   - Status:', error.response?.status);
      console.error('   - Data:', error.response?.data);
      setVerificandoProgreso(false);

      // Si es un error 401 (no autorizado), redirigir al login
      if (error.response?.status === 401) {
        console.log('🔐 [PAGE_VERIFICAR] Token expirado (401), redirigiendo al login');
        authService.clearTokens();
        navigate('/login?expired=true');
        return;
      }

      // Para otros errores, redirigir a filtros
      console.log('🔀 [PAGE_VERIFICAR] Error genérico, navegando a filtros');
      console.log('🚨 [PAGE_VERIFICAR] ====== ERROR ======');
      navigate('/estudiante/simulador/filtros');
    }
  }, [navigate, location, currentUser]);

  // Función para guardar progreso (se declara antes de los useEffect que la usan)
  const guardarProgresoActual = useCallback(async () => {
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
  }, [examenData, preguntaActual, tiempoTranscurrido, pausado, respuestas]);

  // Verificar examen en progreso al cargar - SOLO UNA VEZ, SIN DEPENDENCIAS
  useEffect(() => {
    console.log('🔎 useEffect inicial ejecutándose');
    verificarExamenEnProgreso();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // SIN DEPENDENCIAS para evitar loops

  useEffect(() => {
    if (examenData) {
      if (!tiempoInicio) {
        setTiempoInicio(new Date());
      }
    }
  }, [examenData]);

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
  }, [examenData, tiempoInicio, guardarProgresoActual]);

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

  const recuperarExamen = async () => {
    console.log('🔎 [PAGE_RECUPERAR] ====== INICIO ======');
    console.log('📥 [PAGE_RECUPERAR] Iniciando recuperación de examen');

    if (!examenEnProgreso) {
      console.error('❌ [PAGE_RECUPERAR] No hay examen en progreso para recuperar');
      return;
    }

    console.log('📦 [PAGE_RECUPERAR] Datos del examen a recuperar:');
    console.log('   - IntentoId:', examenEnProgreso.intentoId);
    console.log('   - Tipo:', examenEnProgreso.tipoExamen);
    console.log('   - Nombre:', examenEnProgreso.nombreExamen);

    setCargandoRecuperacion(true);
    try {
      // Verificar que el usuario siga autenticado antes de continuar
      console.log('🔐 [PAGE_RECUPERAR] Verificando autenticación...');
      if (!authService.isAuthenticated()) {
        console.error('❌ [PAGE_RECUPERAR] Sesión expirada');
        alert('Tu sesión ha expirado. Serás redirigido al login.');
        authService.clearTokens();
        navigate('/login?expired=true');
        return;
      }
      console.log('✅ [PAGE_RECUPERAR] Usuario autenticado');

      console.log('🌐 [PAGE_RECUPERAR] Llamando recuperarProgreso...');
      const progreso = await examenService.recuperarProgreso(examenEnProgreso.intentoId);

      console.log('✅ [PAGE_RECUPERAR] Progreso recibido, restaurando estado...');
      console.log('📊 [PAGE_RECUPERAR] Estado a restaurar:');
      console.log('   - Pregunta actual:', progreso.preguntaActual);
      console.log('   - Tiempo transcurrido:', progreso.tiempoTranscurrido, 's');
      console.log('   - Pausado:', progreso.pausado);
      console.log('   - Respuestas:', Object.keys(progreso.respuestas).length);

      // Restaurar estado completo
      console.log('🔧 [PAGE_RECUPERAR] Restaurando examenData...');
      setExamenData(progreso.examenData);
      console.log('🔧 [PAGE_RECUPERAR] Restaurando pregunta actual:', progreso.preguntaActual);
      setPreguntaActual(progreso.preguntaActual);
      console.log('🔧 [PAGE_RECUPERAR] Restaurando tiempo transcurrido:', progreso.tiempoTranscurrido);
      setTiempoTranscurrido(progreso.tiempoTranscurrido);
      console.log('🔧 [PAGE_RECUPERAR] Restaurando pausado:', progreso.pausado || false);
      setPausado(progreso.pausado || false);

      // Restaurar respuestas
      console.log('🔧 [PAGE_RECUPERAR] Restaurando respuestas...');
      const respuestasMap = new Map<number, string | null>();
      Object.entries(progreso.respuestas).forEach(([orden, respuesta]) => {
        respuestasMap.set(parseInt(orden), respuesta);
      });
      setRespuestas(respuestasMap);
      console.log('✅ [PAGE_RECUPERAR] Respuestas restauradas:', respuestasMap.size);

      // Calcular tiempo de inicio ajustado
      const tiempoAjustado = new Date(Date.now() - progreso.tiempoTranscurrido * 1000);
      console.log('🔧 [PAGE_RECUPERAR] Tiempo de inicio ajustado:', tiempoAjustado.toISOString());
      setTiempoInicio(tiempoAjustado);

      console.log('🚪 [PAGE_RECUPERAR] Cerrando modal...');
      setMostrarModalRecuperar(false);

      console.log('🎉 [PAGE_RECUPERAR] ====== ÉXITO ======');
      console.log('✅ [PAGE_RECUPERAR] Examen recuperado exitosamente. Usuario puede continuar.');
    } catch (error: any) {
      console.error('❌ [PAGE_RECUPERAR] ERROR CAPTURADO:');
      console.error('   - Tipo:', error.constructor.name);
      console.error('   - Mensaje:', error.message);
      console.error('   - Response status:', error.response?.status);
      console.error('   - Response data:', error.response?.data);
      console.log('🚨 [PAGE_RECUPERAR] ====== ERROR ======');
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
      // Redirigir a filtros para crear nuevo
      navigate('/estudiante/simulador/filtros');
    } catch (error) {
      console.error('Error abandonando examen:', error);
      alert('Error al abandonar el examen anterior');
    } finally {
      setCargandoRecuperacion(false);
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

  const pausarYGuardar = async () => {
    if (!examenData) return;

    setLoading(true);
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
        pausado: true, // Marcar como pausado
        respuestas: respuestasObj
      };

      // Guardar en backend
      await examenService.guardarProgreso(request);

      // Guardar en localStorage como backup
      examenStorage.guardarProgreso(request);

      // Actualizar estado local
      setPausado(true);

      console.log('💾 Progreso guardado y examen pausado');

      // Mostrar mensaje
      alert('✅ Examen pausado y guardado. Puedes continuar desde donde lo dejaste.');

      // Volver al simulador
      navigate('/estudiante/simulador');
    } catch (error) {
      console.error('Error al pausar y guardar:', error);
      alert('Error al guardar el progreso del examen.');
    } finally {
      setLoading(false);
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

  console.log('🖼️ [PAGE_RENDER] Estado actual:', {
    verificandoProgreso,
    tieneExamenData: !!examenData,
    tieneExamenEnProgreso: !!examenEnProgreso,
    mostrarModal: mostrarModalRecuperar
  });

  if (verificandoProgreso) {
    console.log('🖼️ [PAGE_RENDER] Mostrando loader de verificación');
    return (
      <Center style={{ minHeight: '100vh' }}>
        <Stack align="center">
          <Loader size="lg" />
          <Text>Verificando progreso...</Text>
        </Stack>
      </Center>
    );
  }

  // IMPORTANTE: Si hay examen en progreso y se debe mostrar el modal, NO mostrar el loader
  // Permitir que el modal se renderice aunque no haya examenData todavía
  if (!examenData && !mostrarModalRecuperar) {
    console.log('🖼️ [PAGE_RENDER] No hay examenData ni modal, mostrando loader');
    return (
      <Center style={{ minHeight: '100vh' }}>
        <Stack align="center">
          <Loader size="lg" />
          <Text>Cargando examen...</Text>
        </Stack>
      </Center>
    );
  }

  // Si debe mostrar modal pero no hay examenData, mostrar un contenedor vacío con el modal
  if (!examenData && mostrarModalRecuperar) {
    console.log('🖼️ [PAGE_RENDER] Mostrando solo modal (sin examenData aún)');
    return (
      <>
        <RecuperarExamenModal
          examen={examenEnProgreso}
          opened={mostrarModalRecuperar}
          onContinuar={recuperarExamen}
          onNuevo={comenzarNuevoExamen}
          loading={cargandoRecuperacion}
        />
        <Center style={{ minHeight: '100vh' }}>
          <Stack align="center">
            <Loader size="lg" />
            <Text>Preparando examen...</Text>
          </Stack>
        </Center>
      </>
    );
  }

  // Si no hay examenData en este punto, no deberíamos estar aquí (seguridad)
  if (!examenData) {
    console.error('❌ [PAGE_RENDER] Estado inválido: no hay examenData');
    return (
      <Center style={{ minHeight: '100vh' }}>
        <Stack align="center">
          <Text c="red">Error: No hay datos del examen</Text>
          <Button onClick={() => navigate('/estudiante/simulador')}>
            Volver al Simulador
          </Button>
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
              <Title order={2}>Examen Personalizado</Title>
              <Text size="sm" c="dimmed">{examenData.totalPreguntas} preguntas</Text>
            </div>
            <Group gap="xs">
              <Badge color={pausado ? "orange" : "blue"} size="xl">
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
              <Button onClick={siguientePregunta}>
                Siguiente →
              </Button>
            )}
          </Group>

          {/* Botones de acción del examen */}
          <Group justify="center" mt="xl" gap="md">
            <Button
              variant="light"
              color="blue"
              leftSection={<IconDeviceFloppy size={20} />}
              onClick={pausarYGuardar}
              disabled={loading}
            >
              Pausar y Guardar
            </Button>

            <Button
              color="teal"
              leftSection={<IconCheck size={20} />}
              onClick={finalizarExamen}
              disabled={loading}
            >
              {loading ? 'Finalizando...' : 'Finalizar Examen'}
            </Button>
          </Group>
        </Paper>
      </Box>
    </>
  );
};

export default ExamenFiltradoPage;
