# Changelog

Todos los cambios notables en el módulo de Usuarios serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [1.3.0] - 2025-10-22

### Agregado
- Guía de implementación frontend para autenticación (`AUTH_FRONTEND.md`)
- Queries personalizadas con CAST para tipos ENUM de PostgreSQL
- Método `insertUser()` en repositorio para creación con CAST
- Método `updateUser()` en repositorio para actualización con CAST
- Soporte completo para tipos ENUM `user_role` y `user_status`

### Cambiado
- `PUT /api/users/{id}/restore` cambiado a `PATCH /api/users/{id}/restore`
- `createdAt` ahora se establece automáticamente al crear usuario
- `updatedAt` es `null` al crear y solo se actualiza en operaciones PATCH
- Eliminado campo de usuario creador en el registro
- Método `updateEstado()` ahora recibe `updatedAt` como parámetro
- Pool de conexiones R2DBC optimizado para mejor estabilidad

### Removido
- Endpoint `GET /api/users/deleted` eliminado
- Método `getDeletedUsers()` del servicio eliminado
- Uso de `save()` directo reemplazado por queries con CAST

### Corregido
- Error de tipo ENUM en PostgreSQL al crear/actualizar usuarios
- Error "column rol is of type user_role but expression is of type character varying"
- Configuración del pool de conexiones para evitar timeouts
- Sintaxis duplicada en `UserDto.toEntity()`

### Mejorado
- Manejo de timestamps más consistente (creación vs actualización)
- Compatibilidad con tipos ENUM personalizados de PostgreSQL
- Estabilidad de conexiones con la base de datos
- Documentación de integración frontend

### Seguridad
- Timestamps de auditoría más precisos para operaciones PATCH
- Mejor trazabilidad de cambios con `updatedAt`

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
