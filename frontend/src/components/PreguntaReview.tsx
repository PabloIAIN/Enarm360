import React, { useState, useEffect } from 'react';
import { IconStar, IconStarFilled } from '@tabler/icons-react';
import { quejaService, QuejaSugerenciaDTO } from '../services/quejaService';
import { Paper, Text, Title, Textarea, Button, Group, Alert, Box } from '@mantine/core';

interface PreguntaReviewProps {
  usuarioId: number;
  tipoObjetivo: 'REACTIVO' | 'PREGUNTA_CASO';
  referenciaId: number;
  mostrarPromedio?: boolean;
}

export const PreguntaReview: React.FC<PreguntaReviewProps> = ({
  usuarioId,
  tipoObjetivo,
  referenciaId,
  mostrarPromedio = true
}) => {
  const [rating, setRating] = useState<number>(0);
  const [hoverRating, setHoverRating] = useState<number>(0);
  const [comentario, setComentario] = useState<string>('');
  const [reviewExistente, setReviewExistente] = useState<QuejaSugerenciaDTO | null>(null);
  const [promedioRating, setPromedioRating] = useState<number>(0);
  const [guardando, setGuardando] = useState(false);
  const [mensaje, setMensaje] = useState<string>('');

  useEffect(() => {
    cargarReview();
    if (mostrarPromedio) {
      cargarPromedio();
    }
  }, [usuarioId, tipoObjetivo, referenciaId]);

  const cargarReview = async () => {
    try {
      const review = await quejaService.obtenerReviewUsuario(usuarioId, tipoObjetivo, referenciaId);
      if (review) {
        setReviewExistente(review);
        setRating(review.rating);
        setComentario(review.comentario || '');
      }
    } catch (error) {
      console.error('Error al cargar review:', error);
    }
  };

  const cargarPromedio = async () => {
    try {
      const promedio = await quejaService.obtenerPromedioRating(tipoObjetivo, referenciaId);
      setPromedioRating(promedio);
    } catch (error) {
      console.error('Error al cargar promedio:', error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (rating === 0) {
      setMensaje('Por favor selecciona una calificación');
      return;
    }

    setGuardando(true);
    setMensaje('');

    try {
      const comentarioFinal = comentario.trim();
      const data: QuejaSugerenciaDTO = {
        usuarioId,
        tipoObjetivo,
        referenciaId,
        rating,
        ...(comentarioFinal && { comentario: comentarioFinal })
      };

      console.log('🔍 DEBUG - Datos del review:', {
        usuarioId,
        tipoObjetivo,
        referenciaId,
        rating,
        comentario: comentarioFinal,
        dataCompleta: data
      });

      if (reviewExistente) {
        console.log('🔄 Actualizando review existente ID:', reviewExistente.id);
        await quejaService.actualizar(reviewExistente.id!, data);
        setMensaje('✅ Review actualizado correctamente');
      } else {
        console.log('➕ Creando nuevo review con data:', JSON.stringify(data));
        const nuevoReview = await quejaService.crear(data);
        console.log('✅ Review creado:', nuevoReview);
        setReviewExistente(nuevoReview);
        setMensaje('✅ Review guardado correctamente');
      }

      if (mostrarPromedio) {
        cargarPromedio();
      }
    } catch (error: any) {
      console.error('❌ ERROR COMPLETO al guardar review:', {
        error,
        message: error?.message,
        response: error?.response,
        responseData: error?.response?.data,
        status: error?.response?.status
      });

      const errorMsg = error?.response?.data || error?.message || 'Error desconocido';
      setMensaje(`❌ Error: ${errorMsg}`);
    } finally {
      setGuardando(false);
      setTimeout(() => setMensaje(''), 5000);
    }
  };

  return (
    <Paper shadow="sm" p="md" withBorder>
      <Title order={4} mb="md">Califica esta pregunta</Title>

      {mostrarPromedio && promedioRating > 0 && (
        <Text size="sm" c="dimmed" mb="md">
          Calificación promedio: {promedioRating.toFixed(1)} ⭐
        </Text>
      )}

      <form onSubmit={handleSubmit}>
        {/* Rating con estrellas */}
        <Box mb="md">
          <Text size="sm" fw={500} mb="xs">
            Tu calificación:
          </Text>
          <Group gap="xs">
            {[1, 2, 3, 4, 5].map((star) => (
              <Box
                key={star}
                component="button"
                type="button"
                onClick={() => setRating(star)}
                onMouseEnter={() => setHoverRating(star)}
                onMouseLeave={() => setHoverRating(0)}
                style={{
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  padding: 0,
                  transition: 'transform 0.2s'
                }}
                onMouseOver={(e) => e.currentTarget.style.transform = 'scale(1.1)'}
                onMouseOut={(e) => e.currentTarget.style.transform = 'scale(1)'}
              >
                {star <= (hoverRating || rating) ? (
                  <IconStarFilled size={32} color="#ffd43b" />
                ) : (
                  <IconStar size={32} color="#adb5bd" />
                )}
              </Box>
            ))}
          </Group>
        </Box>

        {/* Comentario */}
        <Textarea
          label="Comentario (opcional):"
          placeholder="Comparte tu opinión sobre esta pregunta..."
          value={comentario}
          onChange={(e) => setComentario(e.currentTarget.value)}
          maxLength={1000}
          rows={4}
          mb="md"
          description={`${comentario.length}/1000 caracteres`}
        />

        {/* Botón de envío */}
        <Button
          type="submit"
          fullWidth
          disabled={guardando || rating === 0}
          loading={guardando}
        >
          {reviewExistente ? 'Actualizar Review' : 'Enviar Review'}
        </Button>

        {/* Mensaje de feedback */}
        {mensaje && (
          <Alert
            color={mensaje.includes('Error') ? 'red' : 'green'}
            mt="md"
          >
            {mensaje}
          </Alert>
        )}
      </form>
    </Paper>
  );
};
