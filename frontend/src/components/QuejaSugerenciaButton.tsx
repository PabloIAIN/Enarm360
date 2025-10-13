import React, { useState, useEffect } from 'react';
import {
  ActionIcon,
  Modal,
  Stack,
  Text,
  Textarea,
  Button,
  Group,
  Rating,
  useMantineColorScheme,
  Tooltip,
} from '@mantine/core';
import { IconFlag } from '@tabler/icons-react';
import { quejaSugerenciaService, QuejaSugerenciaDTO } from '../services/quejaSugerenciaService';
import { authService } from '../services/authService';
import { notifications } from '@mantine/notifications';

interface QuejaSugerenciaButtonProps {
  reactivoId?: number;
  preguntaCasoId?: number;
  tipoObjetivo: 'REACTIVO' | 'PREGUNTA_CASO';
}

const QuejaSugerenciaButton: React.FC<QuejaSugerenciaButtonProps> = ({
  reactivoId,
  preguntaCasoId,
  tipoObjetivo,
}) => {
  const { colorScheme } = useMantineColorScheme();
  const [opened, setOpened] = useState(false);
  const [rating, setRating] = useState(0);
  const [comentario, setComentario] = useState('');
  const [loading, setLoading] = useState(false);
  const [existingReview, setExistingReview] = useState<QuejaSugerenciaDTO | null>(null);

  const usuario = authService.getCurrentUserFromStorage();
  const referenciaId = tipoObjetivo === 'REACTIVO' ? reactivoId : preguntaCasoId;

  useEffect(() => {
    if (opened && usuario && referenciaId) {
      // Cargar review existente si existe
      loadExistingReview();
    }
  }, [opened, usuario, referenciaId]);

  const loadExistingReview = async () => {
    if (!usuario || !referenciaId) return;

    try {
      const review = await quejaSugerenciaService.obtenerReviewUsuario(
        usuario.id,
        tipoObjetivo,
        referenciaId
      );

      if (review) {
        setExistingReview(review);
        setRating(review.rating);
        setComentario(review.comentario || '');
      }
    } catch (error) {
      console.error('Error cargando review existente:', error);
    }
  };

  const handleSubmit = async () => {
    if (!usuario || rating === 0 || !referenciaId) {
      notifications.show({
        title: 'Error',
        message: 'Por favor selecciona una calificación',
        color: 'red',
      });
      return;
    }

    setLoading(true);

    try {
      const data: QuejaSugerenciaDTO = {
        usuarioId: usuario.id,
        tipoObjetivo,
        referenciaId: referenciaId,
        rating,
        comentario: comentario.trim() || undefined,
      };

      if (existingReview) {
        // Actualizar review existente
        await quejaSugerenciaService.actualizar(existingReview.id!, data);
        notifications.show({
          title: 'Actualizado',
          message: 'Tu opinión ha sido actualizada exitosamente',
          color: 'green',
        });
      } else {
        // Crear nueva review
        await quejaSugerenciaService.crear(data);
        notifications.show({
          title: 'Enviado',
          message: 'Gracias por tu opinión',
          color: 'green',
        });
      }

      setOpened(false);
      setRating(0);
      setComentario('');
      setExistingReview(null);
    } catch (error) {
      console.error('Error enviando queja/sugerencia:', error);
      notifications.show({
        title: 'Error',
        message: 'Hubo un problema al enviar tu opinión',
        color: 'red',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Tooltip label="Reportar problema o sugerencia" position="top">
        <ActionIcon
          variant="subtle"
          size="md"
          onClick={() => setOpened(true)}
          style={{
            color: colorScheme === 'dark' ? '#94a3b8' : '#64748b',
          }}
        >
          <IconFlag size={18} />
        </ActionIcon>
      </Tooltip>

      <Modal
        opened={opened}
        onClose={() => setOpened(false)}
        title={
          <Text
            fw={600}
            style={{
              color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26',
              fontFamily: 'Space Grotesk, Inter, sans-serif',
              fontSize: '18px',
            }}
          >
            {existingReview ? 'Editar opinión' : 'Reportar problema o sugerencia'}
          </Text>
        }
        centered
        styles={{
          content: {
            backgroundColor: colorScheme === 'dark'
              ? 'rgba(30, 41, 59, 0.95)'
              : 'rgba(247, 243, 238, 0.95)',
            border: `1px solid ${
              colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(221, 216, 209, 0.8)'
            }`,
          },
          header: {
            backgroundColor: 'transparent',
          },
        }}
      >
        <Stack gap="md">
          <div>
            <Text
              size="sm"
              mb="xs"
              style={{
                color: colorScheme === 'dark' ? '#94a3b8' : '#5a5550',
                fontFamily: 'Inter, sans-serif',
                fontWeight: 600,
              }}
            >
              Calificación
            </Text>
            <Rating
              value={rating}
              onChange={setRating}
              size="lg"
              color={colorScheme === 'dark' ? 'yellow' : 'orange'}
            />
          </div>

          <Textarea
            label="Comentario (opcional)"
            placeholder="Describe el problema o sugerencia..."
            value={comentario}
            onChange={(e) => setComentario(e.target.value)}
            minRows={4}
            maxRows={6}
            styles={{
              label: {
                color: colorScheme === 'dark' ? '#94a3b8' : '#5a5550',
                fontFamily: 'Inter, sans-serif',
                fontWeight: 600,
                fontSize: '14px',
                marginBottom: '8px',
              },
              input: {
                backgroundColor: colorScheme === 'dark'
                  ? 'rgba(15, 23, 42, 0.5)'
                  : 'rgba(255, 255, 255, 0.8)',
                border: `1px solid ${
                  colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(221, 216, 209, 0.5)'
                }`,
                color: colorScheme === 'dark' ? '#e2e8f0' : '#2d2a26',
                fontFamily: 'Inter, sans-serif',
                fontSize: '14px',
              },
            }}
          />

          <Group justify="flex-end" gap="sm">
            <Button
              variant="subtle"
              onClick={() => setOpened(false)}
              style={{
                color: colorScheme === 'dark' ? '#94a3b8' : '#5a5550',
                fontFamily: 'Inter, sans-serif',
              }}
            >
              Cancelar
            </Button>
            <Button
              onClick={handleSubmit}
              loading={loading}
              disabled={rating === 0}
              style={{
                backgroundColor: colorScheme === 'dark' ? '#3b82f6' : '#2563eb',
                color: '#ffffff',
                fontFamily: 'Inter, sans-serif',
                fontWeight: 600,
              }}
            >
              {existingReview ? 'Actualizar' : 'Enviar'}
            </Button>
          </Group>
        </Stack>
      </Modal>
    </>
  );
};

export default QuejaSugerenciaButton;
