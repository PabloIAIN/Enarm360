import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Container,
  Title,
  Text,
  Button,
  Stack,
  Group,
  Paper,
  Alert,
  LoadingOverlay,
  useMantineColorScheme,
  Box,
  ActionIcon,
  Center,
  Image
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import {
  IconCheck,
  IconAlertCircle,
  IconArrowLeft,
  IconSun,
  IconMoon
} from '@tabler/icons-react';
import PageTransition from '../components/animations/PageTransition';
import RegistrationWizard from '../components/registration/RegistrationWizard';
import DebugAuthInfo from '../components/DebugAuthInfo';
import { authService } from '../services/authService';
import enarmLogo from '../assets/enarm_logo.png';

const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { colorScheme, toggleColorScheme } = useMantineColorScheme();
  const [redirecting, setRedirecting] = useState(false);

  // Verificar si el usuario ya está autenticado y mostrar alerta
  const [existingSession, setExistingSession] = useState<boolean>(false);
  const [currentUser, setCurrentUser] = useState<string>('');
  const [showWizard, setShowWizard] = useState(false);
  
  useEffect(() => {
    if (authService.isAuthenticated()) {
      setExistingSession(true);
      const user = authService.getCurrentUserFromStorage();
      setCurrentUser(user?.username || user?.email || 'Usuario desconocido');
    }
  }, []);
  const handleLogoutExistingSession = async () => {
    try {
      await authService.logout();
      setExistingSession(false);
      setCurrentUser('');
      notifications.show({
        title: 'Sesión cerrada',
        message: 'La sesión anterior ha sido cerrada exitosamente',
        color: 'green',
        icon: <IconCheck size={16} />
      });
    } catch (error) {
      console.error('Error al cerrar sesión:', error);
      // Forzar limpieza local incluso si falla la petición al servidor
      authService.clearTokens();
      setExistingSession(false);
      setCurrentUser('');
      notifications.show({
        title: 'Sesión cerrada',
        message: 'La sesión anterior ha sido eliminada localmente',
        color: 'yellow',
        icon: <IconAlertCircle size={16} />
      });
    }
  };

  const handleStartRegistration = () => {
    if (process.env.NODE_ENV === 'development') {
      console.log('🚀 Starting registration wizard...');
    }
    setShowWizard(true);
  };


  // Si está redirigiendo, mostrar una página de carga
  if (redirecting) {
    return (
      <PageTransition type="medical" duration={800}>
        <Box
          style={{
            minHeight: '100vh',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: colorScheme === 'dark'
              ? 'linear-gradient(135deg, #1a1b23 0%, #2d3142 100%)'
              : 'linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)'
          }}
        >
          <Stack align="center" gap="md">
            <LoadingOverlay visible={true} />
            <Text size="lg" c="dimmed">
              Redirigiendo al panel...
            </Text>
          </Stack>
        </Box>
      </PageTransition>
    );
  }

  // Si se eligió usar el wizard, mostrar el wizard
  if (showWizard) {
    return <RegistrationWizard />;
  }

  return (
    <PageTransition type="medical" duration={800}>
      <Box
        style={{
          minHeight: '100vh',
          width: '100vw',
          background: colorScheme === 'dark'
            ? 'linear-gradient(135deg, #0f172a 0%, #1e293b 100%)'
            : 'linear-gradient(135deg, #f7f3ee 0%, #f2ede6 100%)',
          position: 'relative',
          overflow: 'hidden'
        }}
      >
        {/* Keyframes para la animación */}
        <style>
          {`
            @keyframes fadeInUp {
              from {
                opacity: 0;
                transform: translateY(20px);
              }
              to {
                opacity: 1;
                transform: translateY(0);
              }
            }
          `}
        </style>
        {/* Background Pattern */}
        <div style={{
          position: 'absolute',
          inset: 0,
          backgroundImage: colorScheme === 'dark'
            ? `url("data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><defs><pattern id='grid' width='20' height='20' patternUnits='userSpaceOnUse'><path d='M 20 0 L 0 0 0 20' fill='none' stroke='%23374151' stroke-width='0.5' opacity='0.2'/></pattern></defs><rect width='100' height='100' fill='url(%23grid)'/></svg>")`
            : `url("data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><defs><pattern id='grid' width='20' height='20' patternUnits='userSpaceOnUse'><path d='M 20 0 L 0 0 0 20' fill='none' stroke='%23e2e8f0' stroke-width='0.5' opacity='0.3'/></pattern></defs><rect width='100' height='100' fill='url(%23grid)'/></svg>")`,
          opacity: 0.3,
          pointerEvents: 'none'
        }} />

        {/* Dark Mode Toggle */}
        <ActionIcon
          onClick={toggleColorScheme}
          variant="light"
          size="lg"
          radius="xl"
          style={{
            position: 'absolute',
            top: '2rem',
            right: '2rem',
            zIndex: 10,
            backgroundColor: colorScheme === 'dark'
              ? 'rgba(30, 41, 59, 0.8)'
              : 'rgba(255, 255, 255, 0.8)',
            backdropFilter: 'blur(20px) saturate(180%)',
            WebkitBackdropFilter: 'blur(20px) saturate(180%)',
            border: `1px solid ${colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(255, 255, 255, 0.5)'}`,
            boxShadow: colorScheme === 'dark'
              ? '0 4px 16px rgba(0, 0, 0, 0.3)'
              : '0 4px 16px rgba(0, 0, 0, 0.1)',
          }}
        >
          {colorScheme === 'dark' ? <IconSun size={20} /> : <IconMoon size={20} />}
        </ActionIcon>

        {/* Back to Landing Button */}
        <ActionIcon
          onClick={() => navigate('/')}
          variant="light"
          size="lg"
          radius="xl"
          style={{
            position: 'absolute',
            top: '2rem',
            left: '2rem',
            zIndex: 10,
            backgroundColor: colorScheme === 'dark'
              ? 'rgba(30, 41, 59, 0.8)'
              : 'rgba(255, 255, 255, 0.8)',
            backdropFilter: 'blur(20px) saturate(180%)',
            WebkitBackdropFilter: 'blur(20px) saturate(180%)',
            border: `1px solid ${colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(255, 255, 255, 0.5)'}`,
            boxShadow: colorScheme === 'dark'
              ? '0 4px 16px rgba(0, 0, 0, 0.3)'
              : '0 4px 16px rgba(0, 0, 0, 0.1)',
            transition: 'all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94)',
          }}
        >
          <IconArrowLeft size={20} />
        </ActionIcon>

        <div style={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1,
          padding: '2rem 1rem'
        }}>
          <Container size={580} style={{ width: '100%', maxWidth: '580px' }}>
            <Paper
              radius="xl"
              p="xl"
              withBorder
              style={{
                width: '100%',
                backgroundColor: colorScheme === 'dark'
                  ? 'rgba(30, 41, 59, 0.7)'
                  : 'rgba(247, 243, 238, 0.6)',
                backdropFilter: 'blur(40px) saturate(200%)',
                WebkitBackdropFilter: 'blur(40px) saturate(200%)',
                border: `1px solid ${colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(221, 216, 209, 0.8)'}`,
                boxShadow: colorScheme === 'dark'
                  ? 'inset 0 1px 0 rgba(255, 255, 255, 0.1), 0 4px 20px rgba(0, 0, 0, 0.3)'
                  : '0 4px 16px rgba(0, 0, 0, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.6)',
              }}
            >
              <DebugAuthInfo />
              <Stack gap="lg">
                <Center>
                  <Stack align="center" gap="sm">
                    <Image
                      src={enarmLogo}
                      alt="ENARM360 Logo"
                      height={60}
                      fit="contain"
                      style={{
                        borderRadius: '12px',
                        maxWidth: '200px',
                        cursor: 'pointer',
                        transition: 'transform 0.3s ease'
                      }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.transform = 'scale(1.05) rotate(1deg)';
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.transform = 'scale(1) rotate(0deg)';
                      }}
                      onClick={() => navigate('/')}
                    />
                    <Title order={1} size="h2" ta="center" mb="xs">
                      ¡Únete a ENARM360!
                    </Title>
                    <Text c="dimmed" ta="center" size="md" mb="md">
                      La plataforma más completa para tu preparación al ENARM
                    </Text>

                    {/* Alerta de sesión existente */}
                    {existingSession && (
                      <Alert
                        color="yellow"
                        variant="light"
                        radius="md"
                        mb="lg"
                        style={{
                          border: '1px solid rgba(245, 158, 11, 0.3)',
                          background: colorScheme === 'dark'
                            ? 'rgba(245, 158, 11, 0.1)'
                            : 'rgba(245, 158, 11, 0.05)'
                        }}
                      >
                        <Stack gap="sm">
                          <Text size="sm" fw={500} ta="center">
                             Ya tienes una sesión activa como: <strong>{currentUser}</strong>
                          </Text>
                          <Group justify="center" gap="sm">
                            <Button
                              size="xs"
                              variant="outline"
                              color="orange"
                              onClick={handleLogoutExistingSession}
                            >
                              Cerrar sesión anterior
                            </Button>
                            <Button
                              size="xs"
                              variant="filled"
                              color="blue"
                              onClick={() => {
                                const userRole = authService.isAdmin() ? 'admin' : 'estudiante';
                                navigate(`/${userRole}/dashboard`);
                              }}
                            >
                              Ir al dashboard
                            </Button>
                          </Group>
                        </Stack>
                      </Alert>
                    )}

                  </Stack>
                </Center>

                {/* CTA Principal */}
                <Stack gap="lg" align="center">
                  <Text ta="center" size="lg">
                    🎯 <strong>¿Listo para dominar el ENARM?</strong>
                  </Text>
                  
                  <Stack gap="sm" style={{ width: '100%', maxWidth: 400 }}>
                    <Text size="sm" c="dimmed" ta="center">
                      ✅ Más de 10,000 preguntas actualizadas<br/>
                      ✅ Simulacros idénticos al examen real<br/>
                      ✅ Estadísticas detalladas de tu progreso<br/>
                      ✅ Comunidad de estudiantes activa
                    </Text>
                  </Stack>

                  <Button
                    size="xl"
                    radius="xl"
                    onClick={handleStartRegistration}
                    style={{
                      backgroundColor: colorScheme === 'dark' 
                        ? 'var(--mantine-color-blue-6)' 
                        : 'var(--mantine-color-blue-6)',
                      fontSize: '1.1rem',
                      fontWeight: 700,
                      padding: '16px 40px',
                      width: '100%',
                      maxWidth: 400
                    }}
                  >
                    🚀 Comenzar mi preparación
                  </Button>
                  
                  <Text size="xs" c="dimmed" ta="center">
                    Proceso guiado paso a paso • Solo toma 2 minutos
                  </Text>
                </Stack>

                <Center>
                  <Text size="sm" c="dimmed">
                    ¿Ya tienes una cuenta?{' '}
                    <Text
                      component="span"
                      size="sm"
                      c="blue"
                      style={{ cursor: 'pointer', fontWeight: 500 }}
                      onClick={() => navigate('/login')}
                    >
                      Inicia sesión aquí
                    </Text>
                  </Text>
                </Center>
              </Stack>
            </Paper>
          </Container>
        </div>
      </Box>
    </PageTransition>
  );
};

export default RegisterPage;