# Changelog

Todos los cambios notables en el módulo de Usuarios serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [1.2.0] - 2025-10-21

### Agregado
- Eliminación lógica (soft delete) usando campo `estado`
- Endpoint `PUT /api/users/{id}/restore` para restaurar usuarios eliminados
- Endpoint `GET /api/users/deleted` para listar usuarios inactivos
- Endpoint `DELETE /api/users/{id}/permanent` para eliminación física
- Paginación en `GET /api/users` con parámetros `page` y `size`
- Endpoint `GET /api/users/stats` para estadísticas de usuarios
- Documentación completa con Swagger/OpenAPI en todos los endpoints
- Control de acceso basado en roles con `@PreAuthorize`
- Queries optimizadas en base de datos para estadísticas

### Cambiado
- `DELETE /api/users/{id}` ahora realiza soft delete (cambia estado a "inactivo")
- `GET /api/users` ahora solo muestra usuarios activos
- `POST /api/auth/login` solo permite login a usuarios con estado "activo"
- Método `getUserStats()` optimizado con queries agregadas en BD
- Todas las respuestas ahora usan formato `ApiResponse` consistente

### Mejorado
- Manejo de errores global con `GlobalExceptionHandler`
- Validación de roles corregida: admin, asesor, mecanico, supervisor
- `PasswordUtil` refactorizado como clase utility pura
- Documentación Swagger completa con ejemplos y códigos de respuesta

### Seguridad
- Usuarios inactivos no pueden iniciar sesión
- Endpoints sensibles protegidos con rol ADMIN
- Estadísticas solo accesibles para ADMIN y SUPERVISOR

## [1.1.0] - 2025-10-20

### Agregado
- Manejo de errores con excepciones personalizadas
- `ResourceNotFoundException` para recursos no encontrados
- Validaciones mejoradas en DTOs

### Cambiado
- Respuestas de error ahora retornan códigos HTTP apropiados
- 404 para recursos no encontrados
- 409 para conflictos (email duplicado)
- 400 para errores de validación

## [1.0.0] - 2025-10-19

### Agregado
- Módulo de usuarios inicial
- CRUD completo de usuarios
- Autenticación con JWT
- Encriptación de contraseñas con BCrypt
- Endpoints básicos:
  - `POST /api/auth/login` - Iniciar sesión
  - `GET /api/users` - Listar usuarios
  - `GET /api/users/{id}` - Obtener usuario por ID
  - `POST /api/users` - Crear usuario
  - `PUT /api/users/{id}` - Actualizar usuario
  - `DELETE /api/users/{id}` - Eliminar usuario

### Seguridad
- Autenticación JWT implementada
- Contraseñas hasheadas con BCrypt (10 rounds)
- Validación de tokens en cada request

---

## Tipos de cambios

- **Agregado** - Para nuevas funcionalidades
- **Cambiado** - Para cambios en funcionalidades existentes
- **Deprecado** - Para funcionalidades que serán removidas
- **Removido** - Para funcionalidades removidas
- **Corregido** - Para corrección de bugs
- **Seguridad** - Para cambios relacionados con seguridad
- **Mejorado** - Para mejoras de rendimiento o código
