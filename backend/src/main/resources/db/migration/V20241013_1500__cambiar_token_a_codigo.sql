-- Migración para cambiar token por código en verificacion_email
-- Fecha: 2025-10-13
-- Descripción: Cambiar de token UUID a código numérico de 6 dígitos para consistencia con SMS

-- 1. Primero eliminar todas las verificaciones pendientes (no verificadas)
DELETE FROM verificacion_email WHERE verificado = false;

-- 2. Agregar la nueva columna codigo
ALTER TABLE verificacion_email ADD COLUMN codigo VARCHAR(6);

-- 3. Para las verificaciones ya verificadas (si las hay), generar códigos aleatorios para mantener integridad
UPDATE verificacion_email SET codigo = LPAD(FLOOR(RANDOM() * 1000000)::TEXT, 6, '0') WHERE codigo IS NULL;

-- 4. Hacer la columna codigo NOT NULL
ALTER TABLE verificacion_email ALTER COLUMN codigo SET NOT NULL;

-- 5. Eliminar la columna token antigua
ALTER TABLE verificacion_email DROP COLUMN token;

-- 6. Agregar índices para el nuevo campo codigo
CREATE INDEX idx_verificacion_email_codigo ON verificacion_email(codigo);
CREATE INDEX idx_verificacion_email_codigo_email ON verificacion_email(codigo, email);