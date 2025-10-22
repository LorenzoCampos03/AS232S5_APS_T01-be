 Guía de Eliminación Lógica (Soft Delete)

## 📋 Resumen

El sistema implementa **eliminación lógica** para usuarios usando el campo `estado`. Cuando eliminas un usuario, no se borra físicamente de la base de datos, sino que su estado cambia a `"inactivo"`.

## 🎯 Beneficios

1. **Recuperación de datos** - Puedes restaurar usuarios eliminados por error
2. **Auditoría completa** - Mantienes historial de usuarios inactivos
3. **Integridad referencial** - No rompes relaciones con cotizaciones, notificaciones, etc.
4. **Cumplimiento legal** - Algunos países requieren mantener registros históricos

## 🗄️ Campo Utilizado

### Campo `estado` (ya existente):
- **`activo`** = Usuario activo (puede iniciar sesión)
- **`inactivo`** = Usuario eliminado lógicamente (no puede iniciar sesión)

**No se requiere migración** - El campo `estado` ya existe en la tabla `users`.

## 🔌 Nuevos Endpoints

### 1. Eliminar usuario (Soft Delete)
```http
DELETE /api/users/{id}
Authorization: Bearer {token}
```

**Comportamiento:**
- Cambia `estado` a "inactivo"
- Actualiza `updated_at` con timestamp actual
- El usuario desaparece de listados normales
- El usuario NO puede iniciar sesión
- **NO se elimina físicamente**

**Respuesta exitosa (200):**
```json
{
  "success": true,
  "message": "Usuario eliminado exitosamente",
  "data": null,
  "timestamp": "2025-10-21T15:30:00"
}
```

**Error si ya está eliminado (409):**
```json
{
  "success": false,
  "message": "El usuario ya está eliminado",
  "data": null,
  "timestamp": "2025-10-21T15:30:00"
}
```

---

### 2. Restaurar usuario
```http
PUT /api/users/{id}/restore
Authorization: Bearer {token}
```

**Comportamiento:**
- Cambia `estado` a "activo"
- Actualiza `updated_at` con timestamp actual
- El usuario vuelve a aparecer en listados
- El usuario puede iniciar sesión nuevamente

**Respuesta exitosa (200):**
```json
{
  "success": true,
  "message": "Usuario restaurado exitosamente",
  "data": {
    "id": 5,
    "nombre": "Juan Pérez",
    "email": "juan@example.com",
    "rol": "asesor",
    "estado": "activo",
    "deletedAt": null
  },
  "timestamp": "2025-10-21T15:35:00"
}
```

**Error si no está eliminado (409):**
```json
{
  "success": false,
  "message": "El usuario no está eliminado",
  "data": null,
  "timestamp": "2025-10-21T15:35:00"
}
```

---

### 3. Ver usuarios eliminados
```http
GET /api/users/deleted?page=0&size=10
Authorization: Bearer {token}
```

**Respuesta (200):**
```json
{
  "success": true,
  "message": "Usuarios eliminados obtenidos exitosamente",
  "data": {
    "content": [
      {
        "id": 5,
        "nombre": "Juan Pérez",
        "email": "juan@example.com",
        "rol": "asesor",
        "estado": "inactivo"
      }
    ],
    "currentPage": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1
  },
  "timestamp": "2025-10-21T15:40:00"
}
```

---

### 4. Eliminar permanentemente (Physical Delete)
```http
DELETE /api/users/{id}/permanent
Authorization: Bearer {token}
```

⚠️ **ADVERTENCIA**: Esta acción **NO puede deshacerse**. El usuario se elimina físicamente de la base de datos.

**Cuándo usar:**
- Cumplimiento con GDPR (derecho al olvido)
- Limpieza de datos de prueba
- Eliminación definitiva solicitada por el usuario

**Respuesta exitosa (200):**
```json
{
  "success": true,
  "message": "Usuario eliminado permanentemente",
  "data": null,
  "timestamp": "2025-10-21T15:45:00"
}
```

## 📊 Comportamiento de Listados

### GET /api/users
- **Antes**: Mostraba todos los usuarios
- **Ahora**: Solo muestra usuarios con `estado = 'activo'`

### GET /api/users/stats
- **Antes**: Contaba todos los usuarios
- **Ahora**: Solo cuenta usuarios con `estado = 'activo'`

### GET /api/users/{id}
- Funciona igual, puede obtener usuarios inactivos por ID
- Útil para auditoría

### POST /api/auth/login
- Solo permite login a usuarios con `estado = 'activo'`
- Usuarios inactivos no pueden iniciar sesión

## 🔐 Permisos

Todos los endpoints de eliminación/restauración requieren:
- **Autenticación**: Token JWT válido
- **Autorización**: Rol `ADMIN`

## 💡 Ejemplos de Uso

### Flujo típico de eliminación y restauración

```bash
# 1. Eliminar usuario (soft delete)
curl -X DELETE http://localhost:8080/api/users/5 \
  -H "Authorization: Bearer {token}"

# 2. Verificar que no aparece en listado normal
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer {token}"
# Usuario 5 no aparece

# 3. Ver usuarios eliminados
curl http://localhost:8080/api/users/deleted \
  -H "Authorization: Bearer {token}"
# Usuario 5 aparece aquí

# 4. Restaurar usuario
curl -X PUT http://localhost:8080/api/users/5/restore \
  -H "Authorization: Bearer {token}"

# 5. Verificar que vuelve a aparecer
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer {token}"
# Usuario 5 aparece de nuevo
```

### Eliminación permanente (usar con precaución)

```bash
# Eliminar permanentemente
curl -X DELETE http://localhost:8080/api/users/5/permanent \
  -H "Authorization: Bearer {token}"

# Intentar restaurar (fallará)
curl -X PUT http://localhost:8080/api/users/5/restore \
  -H "Authorization: Bearer {token}"
# Error 404: Usuario no encontrado
```

## 🔍 Queries en Base de Datos

### Ver todos los usuarios (incluyendo inactivos)
```sql
SELECT * FROM users;
```

### Ver solo usuarios activos
```sql
SELECT * FROM users WHERE estado::text = 'activo';
```

### Ver solo usuarios inactivos (eliminados)
```sql
SELECT * FROM users WHERE estado::text = 'inactivo';
```

### Restaurar manualmente un usuario
```sql
UPDATE users 
SET estado = 'activo', updated_at = NOW() 
WHERE id = 5;
```

### Eliminar permanentemente usuarios inactivos
```sql
DELETE FROM users 
WHERE estado::text = 'inactivo';
```

## 📝 Notas Importantes

1. **Campo estado**: Se usa el campo existente `estado` con valores "activo" / "inactivo"
2. **Login bloqueado**: Usuarios con `estado = 'inactivo'` NO pueden iniciar sesión
3. **Timestamps**: El campo `updated_at` se actualiza en cada operación
4. **Estadísticas**: Las estadísticas (`/api/users/stats`) solo cuentan usuarios activos
5. **Paginación**: Los listados paginados excluyen usuarios inactivos automáticamente
6. **Sin migración**: No se requiere ninguna migración de base de datos

## 🚀 Próximos Pasos Sugeridos

1. **Tarea programada**: Crear un job que elimine permanentemente usuarios eliminados hace más de X meses
2. **Notificaciones**: Enviar notificación cuando un usuario es eliminado/restaurado
3. **Auditoría**: Registrar quién eliminó/restauró cada usuario
4. **Bulk operations**: Endpoints para eliminar/restaurar múltiples usuarios a la vez
5. **Papelera**: UI para mostrar usuarios eliminados con opción de restaurar

## ⚠️ Datos Existentes

Si ya tienes usuarios en tu base de datos, todos deberían tener `estado = 'activo'` por defecto. Los usuarios con `estado = 'inactivo'` se consideran eliminados lógicamente.
