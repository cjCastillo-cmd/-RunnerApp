# RunnerApp — Backend API

Backend Laravel para la app RunnerApp (Android). Gestiona autenticacion, carreras, amigos, estadisticas y notificaciones push.

## Requisitos

- PHP >= 8.2
- Composer
- XAMPP (MySQL + Apache) o equivalente
- MySQL 8.0+

## Instalacion

1. **Clonar el repositorio** y entrar a la carpeta del backend:

```bash
cd backend
```

2. **Instalar dependencias**:

```bash
composer install
```

3. **Configurar el entorno** — copiar el archivo de ejemplo y generar la key:

```bash
cp .env.example .env
php artisan key:generate
```

4. **Crear la base de datos** `runner_app` en phpMyAdmin (`http://localhost/phpmyadmin`) o por consola:

```sql
CREATE DATABASE runner_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

5. **Ejecutar migraciones**:

```bash
php artisan migrate
```

6. **Iniciar el servidor** (si no usas Apache de XAMPP):

```bash
php artisan serve
```

## Configuracion de la app Android

La app se conecta al backend. El servidor esta en:

```
http://34.172.180.141/runner_backend/public/api/
```

## Estructura de la API

### Rutas publicas (`/api/auth/...`)
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `auth/register` | Registro de usuario |
| POST | `auth/login` | Inicio de sesion |
| POST | `auth/verify-email` | Verificar email |
| POST | `auth/resend-code` | Reenviar codigo |
| POST | `auth/forgot-password` | Recuperar password |
| POST | `auth/reset-password` | Resetear password |

### Rutas protegidas (Bearer Token)
| Modulo | Endpoints |
|--------|-----------|
| Auth | `POST auth/logout` |
| Usuario | `GET user/profile`, `POST user/update`, `POST user/upload-photo`, `GET users/by-country`, `DELETE user/account` |
| Carreras | `POST runs`, `GET runs`, `GET runs/{id}`, `DELETE runs/{id}`, `POST runs/{id}/photo` |
| Amigos | `POST friends/request`, `POST friends/respond`, `GET friends`, `GET friends/pending` |
| Stats | `GET stats/monthly`, `GET stats/weekly-compare`, `GET stats/monthly-compare`, `GET stats/leaderboard` |
| Charts | `GET charts/weekly`, `GET charts/monthly` |
| FCM | `POST user/fcm-token` |

## Testing

```bash
php artisan test
```