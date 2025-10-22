# Documentación del Módulo de Usuarios

## 📋 Descripción General

El módulo de usuarios gestiona la autenticación, autorización y administración de usuarios del sistema HinoPE.

## 🎯 Características

- ✅ Autenticación con JWT
- ✅ Gestión completa de usuarios (CRUD)
- ✅ Control de acceso basado en roles
- ✅ Eliminación lógica (soft delete)
- ✅ Restauración de usuarios eliminados
- ✅ Estadísticas de usuarios
- ✅ Paginación en listados
- ✅ Validación de datos
- ✅ Encriptación de contraseñas con BCrypt

## 👥 Roles Disponibles

| Rol | Descripción | Permisos |
|-----|-------------|----------|
| `admin` | Administrador del sistema | Acceso total |
| `asesor` | Asesor de ventas | Gestión de cotizaciones |
| `mecanico` | Mecánico | Gestión de mantenimiento |
| `supervisor` | Supervisor | Visualización de estadísticas |

## 🔐 Autenticación

### Login

**Endpoint:** `POST /api/auth/login`

**Request:**
```json
{
  "email": "usuario@hino.com.pe",
  "password": "MiPassword123"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Autenticación exitosa",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "nombre": "Juan Pérez",
      "email": "juan@hino.com.pe",
      "rol": "asesor",
      "estado": "activo"
    }
  }
}
```

Ver documentación completa en: [SOFT_DELETE_GUIDE.md](SOFT_DELETE_GUIDE.md)

## 📚 Endpoints Disponibles

### Autenticación

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| POST | `/api/auth/login` | Iniciar sesión | No |

### Gestión de Usuarios

| Método | Endpoint | Descripción | Rol Requerido |
|--------|----------|-------------|---------------|
| GET | `/api/users` | Listar usuarios activos (paginado) | Cualquiera |
| GET | `/api/users/{id}` | Obtener usuario por ID | Cualquiera |
| POST | `/api/users` | Crear nuevo usuario | ADMIN |
| PUT | `/api/users/{id}` | Actualizar usuario | ADMIN |
| DELETE | `/api/users/{id}` | Eliminar usuario (soft delete) | ADMIN |
| PUT | `/api/users/{id}/restore` | Restaurar usuario eliminado | ADMIN |
| GET | `/api/users/deleted` | Listar usuarios eliminados | ADMIN |
| DELETE | `/api/users/{id}/permanent` | Eliminar permanentemente | ADMIN |
| GET | `/api/users/stats` | Estadísticas de usuarios | ADMIN, SUPERVISOR |

## 🔧 Ejemplos de Uso

### Crear Usuario

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "María González",
    "email": "maria@hino.com.pe",
    "telefono": "+51 999 888 777",
    "rol": "asesor",
    "especialidad": "Camiones Pesados",
    "estado": "activo",
    "password": "Maria123"
  }'
```

### Listar Usuarios (Paginado)

```bash
curl http://localhost:8080/api/users?page=0&size=10 \
  -H "Authorization: Bearer {token}"
```

### Eliminar Usuario (Soft Delete)

```bash
curl -X DELETE http://localhost:8080/api/users/5 \
  -H "Authorization: Bearer {token}"
```

### Restaurar Usuario

```bash
curl -X PUT http://localhost:8080/api/users/5/restore \
  -H "Authorization: Bearer {token}"
```

## 📊 Modelo de Datos

### User Entity

```java
{
  "id": Long,
  "nombre": String,
  "email": String (unique),
  "telefono": String,
  "rol": String (admin|asesor|mecanico|supervisor),
  "especialidad": String,
  "estado": String (activo|inactivo),
  "ventas": Integer,
  "fechaIngreso": LocalDate,
  "avatarUrl": String,
  "createdAt": LocalDateTime,
  "updatedAt": LocalDateTime
}
```

## ⚠️ Validaciones

- **nombre**: Requerido, no vacío
- **email**: Requerido, formato válido, único
- **rol**: Requerido, valores: admin, asesor, mecanico, supervisor
- **estado**: Requerido, valores: activo, inactivo
- **password**: Requerido al crear, mínimo 6 caracteres (recomendado)

## 🔒 Seguridad

- Contraseñas encriptadas con BCrypt (10 rounds)
- Tokens JWT con expiración de 24 horas
- Usuarios inactivos no pueden iniciar sesión
- Endpoints protegidos por rol
- Validación de entrada en todos los endpoints

## 📖 Documentación Adicional

- [SOFT_DELETE_GUIDE.md](SOFT_DELETE_GUIDE.md) - Guía de eliminación lógica
- [SECURITY.md](SECURITY.md) - Políticas de seguridad
- [CHANGELOG.md](CHANGELOG.md) - Historial de cambios
- [CONTRIBUTING.md](CONTRIBUTING.md) - Guía de contribución
- Swagger UI: http://localhost:8080/swagger-ui.html
