import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Paper,
  Stepper,
  Stack,
  Group,
  Button,
  Title,
  Text,
  ActionIcon,
  Box,
  useMantineColorScheme,
  LoadingOverlay,
  Progress
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import {
  IconArrowLeft,
  IconSun,
  IconMoon,
  IconCheck
} from '@tabler/icons-react';

import PageTransition from '../animations/PageTransition';
import Step1PersonalData from './Step1PersonalData';
import Step2Contact from './Step2Contact';
import Step3EmailVerification from './Step3EmailVerification';
import Step4PhoneVerification from './Step4PhoneVerification';
import Step5PlanSelection from './Step5PlanSelection';

import {
  RegistroWizardData,
  WIZARD_STEPS,
  Paso1DatosPersonales,
  Paso2Contacto
} from '../../types/registro';

const RegistrationWizard: React.FC = () => {
  const navigate = useNavigate();
  const { colorScheme, toggleColorScheme } = useMantineColorScheme();
  
  const [loading, setLoading] = useState(false);
  const [wizardData, setWizardData] = useState<RegistroWizardData>({
    currentStep: 0,
    totalSteps: WIZARD_STEPS.length,
    steps: WIZARD_STEPS.map((step, index) => ({
      ...step,
      isCompleted: false,
      isActive: index === 0,
      isAccessible: index === 0
    }))
  });

  // ==========================================================
  // NAVEGACIÓN ENTRE PASOS
  // ==========================================================

  const nextStep = () => {
    if (wizardData.currentStep < wizardData.totalSteps - 1) {
      const newCurrentStep = wizardData.currentStep + 1;
      
      setWizardData(prev => ({
        ...prev,
        currentStep: newCurrentStep,
        steps: prev.steps.map((step, index) => ({
          ...step,
          isCompleted: index < newCurrentStep,
          isActive: index === newCurrentStep,
          isAccessible: index <= newCurrentStep
        }))
      }));
    }
  };

  const prevStep = () => {
    if (wizardData.currentStep > 0) {
      const newCurrentStep = wizardData.currentStep - 1;
      
      setWizardData(prev => ({
        ...prev,
        currentStep: newCurrentStep,
        steps: prev.steps.map((step, index) => ({
          ...step,
          isActive: index === newCurrentStep,
          isCompleted: index < newCurrentStep
        }))
      }));
    }
  };

  const goToStep = (stepIndex: number) => {
    if (wizardData.steps[stepIndex]?.isAccessible) {
      setWizardData(prev => ({
        ...prev,
        currentStep: stepIndex,
        steps: prev.steps.map((step, index) => ({
          ...step,
          isActive: index === stepIndex,
          isCompleted: index < stepIndex
        }))
      }));
    }
  };

  // ==========================================================
  // MANEJO DE DATOS DE PASOS
  // ==========================================================

  const handlePaso1Complete = (data: Paso1DatosPersonales) => {
    setWizardData(prev => ({
      ...prev,
      paso1: data
    }));
    nextStep();
  };

  const handlePaso2Complete = (data: Paso2Contacto, usuarioId: number) => {
    setWizardData(prev => ({
      ...prev,
      paso2: data,
      usuarioCreado: {
        id: usuarioId,
        email: data.email,
        telefono: data.telefono
      }
    }));
    nextStep();
  };

  const handleEmailVerified = () => {
    notifications.show({
      title: 'Email verificado',
      message: 'Tu correo electrónico ha sido verificado exitosamente',
      color: 'green',
      icon: <IconCheck size={16} />
    });
    nextStep();
  };

  const handlePhoneVerified = () => {
    notifications.show({
      title: 'Teléfono verificado',
      message: 'Tu número telefónico ha sido verificado exitosamente',
      color: 'green',
      icon: <IconCheck size={16} />
    });
    nextStep();
  };

  const handleRegistrationComplete = () => {
    notifications.show({
      title: 'Registro completado',
      message: '¡Bienvenido a ENARM360! Tu cuenta ha sido creada exitosamente',
      color: 'green',
      icon: <IconCheck size={16} />
    });
    
    // Redirigir al dashboard o login
    setTimeout(() => {
      navigate('/login');
    }, 2000);
  };

  // ==========================================================
  // RENDERIZADO DE PASOS
  // ==========================================================

  const renderCurrentStep = () => {
    switch (wizardData.currentStep) {
      case 0:
        return (
          <Step1PersonalData
            onComplete={handlePaso1Complete}
            loading={loading}
            setLoading={setLoading}
          />
        );
      case 1:
        return (
          <Step2Contact
            paso1Data={wizardData.paso1!}
            onComplete={handlePaso2Complete}
            onBack={prevStep}
            loading={loading}
            setLoading={setLoading}
          />
        );
      case 2:
        return (
          <Step3EmailVerification
            email={wizardData.usuarioCreado?.email || ''}
            onVerified={handleEmailVerified}
            onBack={prevStep}
            loading={loading}
            setLoading={setLoading}
          />
        );
      case 3:
        return (
          <Step4PhoneVerification
            telefono={wizardData.usuarioCreado?.telefono || ''}
            onVerified={handlePhoneVerified}
            onBack={prevStep}
            loading={loading}
            setLoading={setLoading}
          />
        );
      case 4:
        return (
          <Step5PlanSelection
            onComplete={handleRegistrationComplete}
            onBack={prevStep}
            usuarioId={wizardData.usuarioCreado?.id || 0}
            loading={loading}
            setLoading={setLoading}
          />
        );
      default:
        return null;
    }
  };

  const progressPercentage = ((wizardData.currentStep + 1) / wizardData.totalSteps) * 100;

  return (
    <PageTransition type="medical" duration={800}>
      <Box
        style={{
          minHeight: '100vh',
          background: colorScheme === 'dark'
            ? 'linear-gradient(135deg, #0f172a 0%, #1e293b 100%)'
            : 'linear-gradient(135deg, #f7f3ee 0%, #f2ede6 100%)',
          position: 'relative',
          overflow: 'hidden'
        }}
      >
        {/* Header con controles */}
        <Box p="md">
          <Group justify="space-between">
            {/* Botón volver */}
            <ActionIcon
              onClick={() => wizardData.currentStep === 0 ? navigate('/') : prevStep()}
              variant="light"
              size="lg"
              radius="xl"
              style={{
                backgroundColor: colorScheme === 'dark'
                  ? 'rgba(30, 41, 59, 0.8)'
                  : 'rgba(255, 255, 255, 0.8)',
                backdropFilter: 'blur(20px) saturate(180%)'
              }}
            >
              <IconArrowLeft size={20} />
            </ActionIcon>

            {/* Progreso */}
            <Box style={{ flex: 1, maxWidth: 400, margin: '0 auto' }}>
              <Progress 
                value={progressPercentage} 
                size="lg" 
                radius="xl"
                color="blue"
                style={{
                  backgroundColor: 'rgba(255, 255, 255, 0.2)',
                }}
              />
              <Text size="sm" ta="center" mt="xs" c="dimmed">
                Paso {wizardData.currentStep + 1} de {wizardData.totalSteps}
              </Text>
            </Box>

            {/* Dark mode toggle */}
            <ActionIcon
              onClick={toggleColorScheme}
              variant="light"
              size="lg"
              radius="xl"
              style={{
                backgroundColor: colorScheme === 'dark'
                  ? 'rgba(30, 41, 59, 0.8)'
                  : 'rgba(255, 255, 255, 0.8)',
                backdropFilter: 'blur(20px) saturate(180%)'
              }}
            >
              {colorScheme === 'dark' ? <IconSun size={20} /> : <IconMoon size={20} />}
            </ActionIcon>
          </Group>
        </Box>

        {/* Contenido principal */}
        <Container size="md" px="md" pb="xl">
          <Paper
            radius="xl"
            p="xl"
            withBorder
            style={{
              backgroundColor: colorScheme === 'dark'
                ? 'rgba(30, 41, 59, 0.7)'
                : 'rgba(247, 243, 238, 0.6)',
              backdropFilter: 'blur(40px) saturate(200%)',
              border: `1px solid ${colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(221, 216, 209, 0.8)'}`,
              position: 'relative'
            }}
          >
            <LoadingOverlay visible={loading} />

            {/* Header del paso actual */}
            <Stack gap="lg" mb="xl">
              <Box ta="center">
                <Title order={2} mb="xs">
                  {WIZARD_STEPS[wizardData.currentStep]?.title}
                </Title>
                <Text c="dimmed">
                  {WIZARD_STEPS[wizardData.currentStep]?.description}
                </Text>
              </Box>

              {/* Stepper visual */}
              <Stepper 
                active={wizardData.currentStep} 
                size="sm"
                allowNextStepsSelect={false}
                styles={{
                  step: {
                    cursor: 'default'
                  }
                }}
              >
                {WIZARD_STEPS.map((step, index) => (
                  <Stepper.Step
                    key={step.id}
                    label={step.title}
                    description={step.description}
                    allowStepClick={wizardData.steps[index]?.isAccessible}
                    onClick={() => goToStep(index)}
                  />
                ))}
              </Stepper>
            </Stack>

            {/* Contenido del paso actual */}
            <Box>
              {renderCurrentStep()}
            </Box>
          </Paper>
        </Container>
      </Box>
    </PageTransition>
  );
};

export default RegistrationWizard;