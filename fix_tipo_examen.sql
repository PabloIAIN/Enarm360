-- Script para verificar y corregir duplicados en tipo_examen

-- 1. Ver todos los tipos de examen existentes
SELECT * FROM tipo_examen;

-- 2. Ver si hay duplicados por código (insensible a mayúsculas)
SELECT codigo, COUNT(*) as cantidad
FROM tipo_examen
GROUP BY UPPER(codigo)
HAVING COUNT(*) > 1;

-- 3. Verificar específicamente el código 'RAPIDO'
SELECT * FROM tipo_examen WHERE UPPER(codigo) = 'RAPIDO';

-- 4. OPCIONAL: Si quieres eliminar duplicados y mantener solo uno
-- CUIDADO: Solo ejecuta esto si estás seguro
-- DELETE FROM tipo_examen WHERE codigo = 'RAPIDO';

-- 5. OPCIONAL: Insertar el tipo de examen correctamente si no existe
-- INSERT INTO tipo_examen (codigo, nombre, descripcion, activo, creado_en, actualizado_en)
-- VALUES ('RAPIDO', 'Examen Rápido', 'Examen Rápido', true, NOW(), NOW())
-- ON CONFLICT (codigo) DO NOTHING;
