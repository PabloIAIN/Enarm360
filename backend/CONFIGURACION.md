# Configuración del Backend - ENARM360

## Variables de Entorno

Este proyecto utiliza variables de entorno para mantener las credenciales seguras.

### Configuración Local

1. Copia el archivo `.env.example` a `.env`:
```bash
cp .env.example .env
```

2. Edita el archivo `.env` y completa con tus credenciales reales.

### Configuración en el VPS

Cuando despliegues en el VPS, crea el archivo `.env` en el directorio del backend:

```bash
cd /opt/enarm360/backend
nano .env
```

Configuración recomendada para el VPS:

```properties
# Base de datos (localhost porque PostgreSQL está en el mismo servidor)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=enarm360
DB_USERNAME=enarmdev
DB_PASSWORD=tu_password_seguro

# JWT
JWT_SECRET=tu_jwt_secret_generado
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# CORS (tus dominios de producción)
CORS_ALLOWED_ORIGINS=https://enarm360.com,https://www.enarm360.com

# Email (Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=tu_app_password

# SMS (Twilio)
SMS_PROVIDER=twilio
TWILIO_ACCOUNT_SID=tu_account_sid
TWILIO_AUTH_TOKEN=tu_auth_token
TWILIO_FROM_NUMBER=tu_numero_twilio

# Aplicación
APP_NAME=ENARM360
APP_URL=https://enarm360.com
APP_MAIL_FROM=no-reply@enarm360.com

# Servidor
SERVER_PORT=8080

# Uploads
UPLOADS_DIR=/opt/enarm360/uploads
UPLOADS_PUBLIC_BASE=/files/
```

## Cargar Variables de Entorno

Spring Boot automáticamente carga las variables de entorno. Asegúrate de:

1. El archivo `.env` está en el directorio `backend/`
2. Tienes permisos de lectura: `chmod 600 .env`
3. El archivo NO está en el repositorio Git (está en `.gitignore`)

## Ejecución

```bash
# Desarrollo
./mvnw spring-boot:run

# Producción
java -jar target/enarm360-0.0.1-SNAPSHOT.jar
```

## Seguridad

⚠️ **IMPORTANTE**:
- NUNCA subas el archivo `.env` al repositorio Git
- Cambia las credenciales de Twilio si fueron expuestas
- Usa contraseñas seguras en producción
- Genera un nuevo JWT_SECRET para producción
