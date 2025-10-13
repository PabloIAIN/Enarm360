import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { MantineProvider, createTheme, ColorSchemeScript, MantineColorSchemeManager, localStorageColorSchemeManager } from '@mantine/core';
import { Notifications } from '@mantine/notifications';
// import { GoogleOAuthProvider } from '@react-oauth/google'; // REMOVIDO TEMPORALMENTE
import SessionTimeoutProvider from './components/SessionTimeoutProvider';
import { getSessionConfig } from './config/sessionConfig';
import LandingPage from './pages/LandingPage';
import Login from './components/Login';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import ExamLayout from './components/ExamLayout';
import AdminDashboard from './pages/AdminDashboard';
import MejorarPlanPage from './pages/MejorarPlanPage';
import CheckoutPage from './pages/CheckoutPage';
import EstudianteDashboard from './pages/EstudianteDashboard';
import ProfilePage from './pages/ProfilePage';
import SettingsPage from './pages/SettingsPage';
import RegisterPage from './pages/RegisterPage';
// Páginas del módulo estudiantil
import SimuladorPage from './pages/SimuladorPage';
import ExamenRapidoPage from './pages/ExamenRapidoPage';
import ExamenFiltrosPage from './pages/ExamenFiltrosPage';
import ExamenFiltradoPage from './pages/ExamenFiltradoPage';
import ExamenSimulacroPage from './pages/ExamenSimulacroPage';
import SimulacionCompletaPage from './pages/SimulacionCompletaPage';
import RepasoInteligentePage from './pages/RepasoInteligentePage';
import EstadisticasPage from './pages/EstadisticasPage';
import RankingsPage from './pages/RankingsPage';
import CrearPreguntasPage from './pages/CrearPreguntasPage';
import ForoPage from './pages/ForoPage';
import NotificacionesPage from './pages/NotificacionesPage';
// Páginas del módulo de administración
import ClinicalCasesPage from './pages/admin/ClinicalCasesPage';
import QuestionReviewsPage from './pages/admin/QuestionReviewsPage';
import UserStatisticsPage from './pages/admin/UserStatisticsPage';
import SubscriptionsPage from './pages/admin/SubscriptionsPage';
import QuestionDatabasePage from './pages/admin/QuestionDatabasePage';
import AdminNotificationsPage from './pages/admin/AdminNotificationsPage';
import PermissionsAdminPage from './pages/admin/PermissionsAdminPage';
import ResultadosPage from './pages/ResultadosPage';

//Páginas de examen
// import ExamenPage from './pages/ExamenPage';

import './App.css';
import '@mantine/core/styles.css';
import '@mantine/notifications/styles.css';
import './styles/mantine-custom.css';

// Importar servicios para configurar interceptors globales
import './services';

const theme = createTheme({
  primaryColor: 'blue',
  colors: {
    'medical-blue': [
      '#f0f9ff',
      '#e0f2fe',
      '#bae6fd',
      '#7dd3fc',
      '#38bdf8',
      '#0ea5e9',
      '#0284c7',
      '#0369a1',
      '#075985',
      '#0c4a6e'
    ],
    'medical-green': [
      '#f0fdf4',
      '#dcfce7',
      '#bbf7d0',
      '#86efac',
      '#4ade80',
      '#22c55e',
      '#16a34a',
      '#15803d',
      '#166534',
      '#14532d'
    ]
  },
  fontFamily: 'Inter, sans-serif',
  headings: { fontFamily: 'Inter, sans-serif' },
  radius: {
    md: '12px',
    lg: '16px',
    xl: '24px'
  },
  other: {
    lightBg: '#f7f3ee',
    lightCard: '#ebe8e3',
    lightBorder: '#ddd8d1',
    lightText: '#2d2a26',
    lightTextSecondary: '#5a5550'
  }
});

const colorSchemeManager = localStorageColorSchemeManager({
  key: 'enarm360-color-scheme',
});

function App() {
  // Obtener configuración de sesión según el entorno
  const sessionConfig = getSessionConfig();

  return (
    <>
      <ColorSchemeScript defaultColorScheme="light" />
      <MantineProvider theme={theme} colorSchemeManager={colorSchemeManager} defaultColorScheme="light">
        <Notifications position="top-right" />
        <Router>
          <SessionTimeoutProvider 
            timeout={sessionConfig.timeout}
            warningTime={sessionConfig.warningTime}
            enabled={sessionConfig.enabled}
          >
          <div className="App">
            <Routes>
              {/* Rutas públicas */}
              <Route path="/" element={<LandingPage />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<RegisterPage />} />

              {/* Rutas protegidas con Layout */}
              <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
                {/* Rutas de administrador */}
                <Route path="/admin/dashboard" element={<ProtectedRoute requiredRole="ADMIN"><AdminDashboard /></ProtectedRoute>} />
                <Route path="/admin/clinical-cases" element={<ProtectedRoute requiredRole="ADMIN"><ClinicalCasesPage /></ProtectedRoute>} />
                <Route path="/admin/question-reviews" element={<ProtectedRoute requiredRole="ADMIN"><QuestionReviewsPage /></ProtectedRoute>} />
                <Route path="/admin/statistics" element={<ProtectedRoute requiredRole="ADMIN"><UserStatisticsPage /></ProtectedRoute>} />
                <Route path="/admin/subscriptions" element={<ProtectedRoute requiredRole="ADMIN"><SubscriptionsPage /></ProtectedRoute>} />
                <Route path="/admin/question-database" element={<ProtectedRoute requiredRole="ADMIN"><QuestionDatabasePage /></ProtectedRoute>} />
                <Route path="/admin/permissions" element={<ProtectedRoute requiredRole="ADMIN"><PermissionsAdminPage /></ProtectedRoute>} />
                <Route path="/admin/notificaciones" element={<ProtectedRoute requiredRole="ADMIN"><AdminNotificationsPage /></ProtectedRoute>} />

                {/* Rutas de estudiante */}
                <Route path="/estudiante/dashboard" element={<ProtectedRoute requiredRole="ESTUDIANTE"><EstudianteDashboard /></ProtectedRoute>} />
                <Route path="/estudiante/simulador" element={<ProtectedRoute requiredRole="ESTUDIANTE"><SimuladorPage /></ProtectedRoute>} />
                <Route path="/estudiante/simulador/rapido" element={<ProtectedRoute requiredRole="ESTUDIANTE"><ExamenRapidoPage /></ProtectedRoute>} />
                <Route path="/estudiante/simulador/filtros" element={<ProtectedRoute requiredRole="ESTUDIANTE"><ExamenFiltrosPage /></ProtectedRoute>} />
                <Route path="/estudiante/examen-filtrado/:intentoId" element={<ProtectedRoute requiredRole="ESTUDIANTE"><ExamenFiltradoPage /></ProtectedRoute>} />
                <Route path="/estudiante/simulador/completo" element={<ProtectedRoute requiredRole="ESTUDIANTE"><ExamenSimulacroPage /></ProtectedRoute>} />
                <Route path="/estudiante/simulador/inteligente" element={<ProtectedRoute requiredRole="ESTUDIANTE"><RepasoInteligentePage /></ProtectedRoute>} />
                <Route path="/estudiante/estadisticas" element={<ProtectedRoute requiredRole="ESTUDIANTE"><EstadisticasPage /></ProtectedRoute>} />
                <Route path="/estudiante/rankings" element={<ProtectedRoute requiredRole="ESTUDIANTE"><RankingsPage /></ProtectedRoute>} />
                <Route path="/estudiante/preguntas" element={<ProtectedRoute requiredRole="ESTUDIANTE"><CrearPreguntasPage /></ProtectedRoute>} />
                <Route path="/estudiante/forum" element={<ProtectedRoute requiredRole="ESTUDIANTE"><ForoPage /></ProtectedRoute>} />
                <Route path="/estudiante/notificaciones" element={<ProtectedRoute requiredRole="ESTUDIANTE"><NotificacionesPage /></ProtectedRoute>} />

                {/* Configuración */}
                <Route path="/settings" element={<ProtectedRoute><SettingsPage /></ProtectedRoute>} />
                <Route path="/settings/:section" element={<ProtectedRoute><SettingsPage /></ProtectedRoute>} />


                {/* Redirección */}
                <Route path="/profile" element={<Navigate to="/settings" replace />} />
              </Route>

              {/* Rutas de examen con layout sin distracciones */}
              <Route element={<ProtectedRoute><ExamLayout /></ProtectedRoute>}>
                {/* <Route path="/examen/:id" element={<ProtectedRoute requiredRole="ESTUDIANTE"><ExamenPage /></ProtectedRoute>} /> */}
                <Route path="/examenes/:intentoId/resultado" element={<ProtectedRoute requiredRole="ESTUDIANTE"><ResultadosPage /></ProtectedRoute>} />
                <Route path="/estudiante/resultados/:intentoId" element={<ProtectedRoute requiredRole="ESTUDIANTE"><ResultadosPage /></ProtectedRoute>} />
              </Route>

              {/* Mejorar plan y checkout (sin sidebar / header) */}
              <Route path="/mejorarplan" element={<ProtectedRoute requiredRole="ESTUDIANTE"><MejorarPlanPage /></ProtectedRoute>} />
              <Route path="/checkout/:planId" element={<ProtectedRoute requiredRole="ESTUDIANTE"><CheckoutPage /></ProtectedRoute>} />

              {/* Ruta por defecto - redirige a home */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </div>
          </SessionTimeoutProvider>
        </Router>
      </MantineProvider>
    </>
  );
}

export default App;
