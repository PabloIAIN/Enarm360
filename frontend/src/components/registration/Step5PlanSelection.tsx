import React, { useState } from 'react';
import {
  Stack,
  Button,
  Text,
  Alert,
  Group,
  Card,
  Grid,
  Badge,
  List,
  Center,
  Title,
  Divider,
  Box
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import {
  IconCheck,
  IconX,
  IconStar,
  IconArrowLeft,
  IconCrown,
  IconGift,
  IconBolt,
  IconShield,
  IconUsers,
  IconBookmark,
  IconTrophy,
  IconRocket
} from '@tabler/icons-react';

interface Step5PlanSelectionProps {
  onComplete: () => void;
  onBack: () => void;
  usuarioId: number;
  loading: boolean;
  setLoading: (loading: boolean) => void;
}

type PlanType = 'gratuito' | 'basico' | 'premium';

interface Plan {
  id: PlanType;
  name: string;
  price: string;
  originalPrice?: string;
  description: string;
  icon: React.ReactNode;
  color: string;
  badge?: string;
  badgeColor?: string;
  features: string[];
  limitations?: string[];
  recommended?: boolean;
}

const plans: Plan[] = [
  {
    id: 'gratuito',
    name: 'Plan Gratuito',
    price: 'Gratis',
    description: 'Ideal para comenzar tu preparación',
    icon: <IconGift size={24} />,
    color: 'blue',
    features: [
      '50 preguntas por día',
      'Estadísticas básicas',
      'Acceso a temas generales',
      'Foros de estudiantes'
    ],
    limitations: [
      'Sin simulacros completos',
      'Sin análisis detallado',
      'Acceso limitado a especialidades'
    ]
  },
  {
    id: 'basico',
    name: 'Plan Básico',
    price: '$299 MXN/mes',
    originalPrice: '$399 MXN/mes',
    description: 'La opción más popular para estudiantes serios',
    icon: <IconBolt size={24} />,
    color: 'green',
    badge: 'Más Popular',
    badgeColor: 'green',
    recommended: true,
    features: [
      'Preguntas ilimitadas',
      'Simulacros completos tipo ENARM',
      'Estadísticas avanzadas y progreso',
      'Acceso a todas las especialidades',
      'Análisis detallado de respuestas',
      'Banco de preguntas actualizado',
      'Soporte por email'
    ]
  },
  {
    id: 'premium',
    name: 'Plan Premium',
    price: '$499 MXN/mes',
    originalPrice: '$699 MXN/mes',
    description: 'Para quienes buscan la excelencia',
    icon: <IconCrown size={24} />,
    color: 'yellow',
    badge: 'Mejor Valor',
    badgeColor: 'yellow',
    features: [
      'Todo lo del Plan Básico',
      'Simulacros personalizados',
      'Tutorías 1:1 con médicos especialistas',
      'Casos clínicos interactivos',
      'Acceso prioritario a nuevas funciones',
      'Grupo exclusivo de estudio',
      'Soporte 24/7',
      'Certificado de preparación',
      'Garantía de mejora o reembolso'
    ]
  }
];

const Step5PlanSelection: React.FC<Step5PlanSelectionProps> = ({
  onComplete,
  onBack,
  usuarioId,
  loading,
  setLoading
}) => {
  const [selectedPlan, setSelectedPlan] = useState<PlanType>('basico');

  const handlePlanSelection = async () => {
    setLoading(true);

    try {
      // Simular llamada API para asignar plan
      await new Promise(resolve => setTimeout(resolve, 1000));

      const planNames = {
        gratuito: 'Gratuito',
        basico: 'Básico',
        premium: 'Premium'
      };

      notifications.show({
        title: '¡Plan seleccionado!',
        message: `Has elegido el Plan ${planNames[selectedPlan]}. ¡Bienvenido a ENARM360!`,
        color: 'green',
        icon: <IconCheck size={16} />,
        autoClose: 5000
      });

      // Llamar al callback de completado
      onComplete();

    } catch (error: any) {
      console.error('Error selecting plan:', error);
      
      notifications.show({
        title: 'Error',
        message: 'Hubo un problema al seleccionar tu plan. Inténtalo de nuevo.',
        color: 'red',
        icon: <IconX size={16} />
      });
    } finally {
      setLoading(false);
    }
  };

  const renderPlanCard = (plan: Plan) => {
    const isSelected = selectedPlan === plan.id;
    
    return (
      <Card
        key={plan.id}
        shadow={isSelected ? "lg" : "sm"}
        padding="lg"
        radius="md"
        withBorder
        style={{
          cursor: 'pointer',
          transform: isSelected ? 'scale(1.02)' : 'scale(1)',
          transition: 'all 0.2s ease',
          borderColor: isSelected ? `var(--mantine-color-${plan.color}-5)` : undefined,
          borderWidth: isSelected ? '2px' : '1px',
          position: 'relative'
        }}
        onClick={() => setSelectedPlan(plan.id)}
      >
        {/* Badge */}
        {plan.badge && (
          <Badge
            color={plan.badgeColor}
            variant="filled"
            style={{
              position: 'absolute',
              top: -8,
              right: 16,
              zIndex: 1
            }}
          >
            {plan.badge}
          </Badge>
        )}

        {/* Header */}
        <Group justify="space-between" align="flex-start" mb="md">
          <Group>
            <Box color={plan.color}>
              {plan.icon}
            </Box>
            <Stack gap="xs">
              <Text size="lg" fw={700}>
                {plan.name}
              </Text>
              <Group gap="xs">
                <Text size="xl" fw={900} c={plan.color}>
                  {plan.price}
                </Text>
                {plan.originalPrice && (
                  <Text size="sm" td="line-through" c="dimmed">
                    {plan.originalPrice}
                  </Text>
                )}
              </Group>
            </Stack>
          </Group>
          
          {isSelected && (
            <IconCheck size={20} color="var(--mantine-color-green-6)" />
          )}
        </Group>

        <Text size="sm" c="dimmed" mb="md">
          {plan.description}
        </Text>

        <Divider mb="md" />

        {/* Features */}
        <Stack gap="sm">
          <Text size="sm" fw={600}>
            ✅ Incluye:
          </Text>
          <List size="sm" spacing="xs">
            {plan.features.map((feature, index) => (
              <List.Item key={index}>
                <Text size="sm">{feature}</Text>
              </List.Item>
            ))}
          </List>
        </Stack>

        {/* Limitations */}
        {plan.limitations && (
          <Stack gap="sm" mt="md">
            <Text size="sm" fw={600} c="dimmed">
              ⚠️ Limitaciones:
            </Text>
            <List size="sm" spacing="xs">
              {plan.limitations.map((limitation, index) => (
                <List.Item key={index}>
                  <Text size="sm" c="dimmed">{limitation}</Text>
                </List.Item>
              ))}
            </List>
          </Stack>
        )}
      </Card>
    );
  };

  return (
    <Stack gap="lg">
      
      {/* Header */}
      <Center>
        <Stack align="center" gap="sm">
          <IconRocket size={32} color="var(--mantine-color-blue-6)" />
          <Title order={3}>¡Ya casi terminamos!</Title>
          <Text size="sm" c="dimmed" ta="center">
            Elige el plan que mejor se adapte a tus necesidades de preparación
          </Text>
        </Stack>
      </Center>

      {/* Promoción */}
      <Alert color="green" variant="light">
        <Group>
          <IconGift size={20} />
          <Stack gap="xs" style={{ flex: 1 }}>
            <Text size="sm" fw={600}>🎉 Oferta de lanzamiento</Text>
            <Text size="sm">
              ¡25% de descuento en tu primer mes! Esta promoción es válida solo durante el registro.
            </Text>
          </Stack>
        </Group>
      </Alert>

      {/* Plans Grid */}
      <Grid>
        {plans.map((plan) => (
          <Grid.Col key={plan.id} span={{ base: 12, sm: 6, md: 4 }}>
            {renderPlanCard(plan)}
          </Grid.Col>
        ))}
      </Grid>

      {/* Selected plan info */}
      {selectedPlan && (
        <Alert color="blue" variant="light">
          <Stack gap="xs">
            <Text size="sm" fw={600}>
              📋 Plan seleccionado: {plans.find(p => p.id === selectedPlan)?.name}
            </Text>
            <Text size="sm">
              {selectedPlan === 'gratuito' 
                ? 'Podrás actualizar tu plan en cualquier momento desde tu dashboard.'
                : 'Podrás cambiar o cancelar tu suscripción en cualquier momento desde tu perfil.'
              }
            </Text>
          </Stack>
        </Alert>
      )}

      {/* Garantía */}
      <Alert color="teal" variant="light">
        <Group>
          <IconShield size={20} />
          <Stack gap="xs" style={{ flex: 1 }}>
            <Text size="sm" fw={600}>🛡️ Garantía de satisfacción</Text>
            <Text size="sm">
              Si no estás satisfecho con tu plan de pago, tienes 7 días para solicitar un reembolso completo.
            </Text>
          </Stack>
        </Group>
      </Alert>

      {/* Navigation buttons */}
      <Group justify="space-between">
        <Button
          variant="light"
          leftSection={<IconArrowLeft size={16} />}
          onClick={onBack}
          disabled={loading}
        >
          Volver
        </Button>
        
        <Button
          size="lg"
          loading={loading}
          onClick={handlePlanSelection}
          leftSection={<IconTrophy size={16} />}
          style={{ flex: 1, marginLeft: '1rem' }}
        >
          {loading 
            ? 'Configurando tu cuenta...' 
            : `Continuar con Plan ${plans.find(p => p.id === selectedPlan)?.name}`
          }
        </Button>
      </Group>

      {/* Terms notice */}
      <Text size="xs" c="dimmed" ta="center">
        Al continuar, aceptas nuestros términos de servicio y política de privacidad.
        {selectedPlan !== 'gratuito' && ' La suscripción se renovará automáticamente cada mes.'}
      </Text>
    </Stack>
  );
};

export default Step5PlanSelection;