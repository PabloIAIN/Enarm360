import React from 'react';
import { Modal, Stack, Text, Button, Group, Badge, Paper } from '@mantine/core';
import { ExamenEnProgreso } from '../types/examen.types';

interface RecuperarExamenModalProps {
  examen: ExamenEnProgreso | null;
  opened: boolean;
  onContinuar: () => void;
  onNuevo: () => void;
  loading?: boolean;
}

const RecuperarExamenModal: React.FC<RecuperarExamenModalProps> = ({
  examen,
  opened,
  onContinuar,
  onNuevo,
  loading = false
}) => {
  console.log('🔎 [MODAL] Renderizando RecuperarExamenModal');
  console.log('📦 [MODAL] Props:', {
    examen: examen ? {
      intentoId: examen.intentoId,
      tipo: examen.tipoExamen,
      nombre: examen.nombreExamen
    } : null,
    opened,
    loading
  });

  if (!examen) {
    console.log('⚠️ [MODAL] No hay examen para mostrar, retornando null');
    return null;
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

  const formatearFecha = (fecha: string): string => {
    const date = new Date(fecha);
    const ahora = new Date();
    const diff = ahora.getTime() - date.getTime();
    const horas = Math.floor(diff / (1000 * 60 * 60));

    if (horas < 1) {
      const minutos = Math.floor(diff / (1000 * 60));
      return `Hace ${minutos} minuto${minutos !== 1 ? 's' : ''}`;
    }
    if (horas < 24) {
      return `Hace ${horas} hora${horas !== 1 ? 's' : ''}`;
    }
    const dias = Math.floor(horas / 24);
    return `Hace ${dias} día${dias !== 1 ? 's' : ''}`;
  };

  return (
    <Modal
      opened={opened}
      onClose={() => {}} // No permitir cerrar sin elegir
      title="Examen en Progreso"
      centered
      size="md"
      closeOnClickOutside={false}
      closeOnEscape={false}
      withCloseButton={false}
    >
      <Stack gap="md">
        <Paper p="md" withBorder style={{ backgroundColor: '#f8f9fa' }}>
          <Stack gap="xs">
            <Group justify="space-between">
              <Text fw={600} size="lg">{examen.nombreExamen}</Text>
              <Badge color="blue" size="lg">{examen.tipoExamen}</Badge>
            </Group>

            <Text size="sm" c="dimmed">
              {formatearFecha(examen.iniciadoEn)}
            </Text>

            <Group mt="xs" gap="xl">
              <div>
                <Text size="xs" c="dimmed">Progreso</Text>
                <Text fw={600}>
                  {examen.preguntaActual + 1} / {examen.totalPreguntas}
                </Text>
              </div>

              <div>
                <Text size="xs" c="dimmed">Tiempo transcurrido</Text>
                <Text fw={600}>{formatearTiempo(examen.tiempoTranscurrido)}</Text>
              </div>

              {examen.pausado && (
                <Badge color="orange">Pausado</Badge>
              )}
            </Group>
          </Stack>
        </Paper>

        <Text size="sm">
          Tienes un examen sin finalizar. ¿Deseas continuar donde lo dejaste o comenzar uno nuevo?
        </Text>

        <Group justify="flex-end" gap="sm" mt="md">
          <Button
            variant="outline"
            onClick={() => {
              console.log('🔘 [MODAL] Usuario hizo clic en "Comenzar Nuevo"');
              console.log('📦 [MODAL] Llamando onNuevo()');
              onNuevo();
            }}
            disabled={loading}
          >
            Comenzar Nuevo
          </Button>
          <Button
            onClick={() => {
              console.log('🔘 [MODAL] Usuario hizo clic en "Continuar Examen"');
              console.log('📦 [MODAL] Llamando onContinuar()');
              onContinuar();
            }}
            loading={loading}
            color="teal"
          >
            Continuar Examen
          </Button>
        </Group>
      </Stack>
    </Modal>
  );
};

export default RecuperarExamenModal;
