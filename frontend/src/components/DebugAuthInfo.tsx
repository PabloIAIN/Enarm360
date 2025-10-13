import React from 'react';
import { Alert, Stack, Text, Button, Code } from '@mantine/core';

const DebugAuthInfo: React.FC = () => {
  if (process.env.NODE_ENV !== 'development') {
    return null;
  }

  const tokenData = {
    accessToken: localStorage.getItem('accessToken'),
    refreshToken: localStorage.getItem('refreshToken'),
    user: localStorage.getItem('user')
  };

  const clearData = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    window.location.reload();
  };

  const hasAnyData = Object.values(tokenData).some(value => value !== null);

  if (!hasAnyData) {
    return (
      <Alert color="green" variant="light" style={{ marginBottom: '1rem' }}>
        <Text size="sm">✅ No hay datos de autenticación en localStorage</Text>
      </Alert>
    );
  }

  return (
    <Alert color="orange" variant="light" style={{ marginBottom: '1rem' }}>
      <Stack gap="sm">
        <Text size="sm" fw={600}>⚠️ Datos de autenticación encontrados:</Text>
        <Stack gap="xs">
          {tokenData.accessToken && (
            <Code block>accessToken: {tokenData.accessToken.substring(0, 20)}...</Code>
          )}
          {tokenData.refreshToken && (
            <Code block>refreshToken: {tokenData.refreshToken.substring(0, 20)}...</Code>
          )}
          {tokenData.user && (
            <Code block>user: {tokenData.user}</Code>
          )}
        </Stack>
        <Button size="xs" color="red" onClick={clearData}>
          Limpiar datos de autenticación
        </Button>
      </Stack>
    </Alert>
  );
};

export default DebugAuthInfo;