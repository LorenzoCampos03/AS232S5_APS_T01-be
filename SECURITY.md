# Política de Seguridad

## 🔒 Versiones Soportadas

| Versión | Soportada          |
| ------- | ------------------ |
| 1.3.x   | :white_check_mark: |
| 1.2.x   | :white_check_mark: |
| 1.1.x   | :x:                |
| < 1.0   | :x:                |

## 🛡️ Características de Seguridad

### Autenticación
- JWT (JSON Web Tokens) para autenticación stateless
- Tokens con expiración configurable (default: 24 horas)
- Algoritmo de firma: HS256
- Secret key configurable vía variables de entorno

### Contraseñas
- BCrypt para hashing de contraseñas
- Factor de trabajo: 10 rounds
- Nunca se almacenan contraseñas en texto plano
- Nunca se retornan contraseñas en respuestas API
- Contraseñas hasheadas antes de insertar en base de datos

### Base de Datos
- Tipos ENUM personalizados para roles y estados
- Queries parametrizadas con CAST para prevenir SQL injection
- Pool de conexiones configurado con validación automática
- Timestamps de auditoría (`createdAt`, `updatedAt`) para trazabilidad

### Control de Acceso
- Autorización basada en roles (RBAC)
- Roles: admin, asesor, mecanico, supervisor
- Endpoints protegidos con @PreAuthorize
- Usuarios inactivos no pueden iniciar sesión

## 🚨 Reportar una Vulnerabilidad

Si descubres una vulnerabilidad de seguridad, **NO** la reportes públicamente.

**Envía un email a:** henry.lunazco@vallegrande.edu.pe

**Incluye:**
- Descripción detallada de la vulnerabilidad
- Pasos para reproducir el problema
- Impacto potencial
- Versión afectada

**Tiempo de respuesta:**
- Confirmación inicial: 48 horas
- Evaluación completa: 7 días
- Parche de seguridad: 30 días

## 🔐 Mejores Prácticas

### Variables de Entorno

⚠️ NUNCA commitees el archivo .env con credenciales reales.

```bash
# ❌ MAL
JWT_SECRET=mySecretKey
DB_PASSWORD=123456

# ✅ BIEN
JWT_SECRET=8f3b2c1a9e7d6f5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1
DB_PASSWORD=Xk9$mP2#vL8@qR5&nT7!wY3
```

Ver documentación completa en el archivo.

### Generación de Secrets

```bash
# Generar JWT secret seguro (256 bits)
openssl rand -hex 32

# Generar password seguro
openssl rand -base64 32
```

## 🔍 Auditoría y Monitoreo

### Logs de Seguridad

El sistema registra:
- Intentos de login (exitosos y fallidos)
- Creación/modificación de usuarios
- Cambios de roles
- Eliminación de usuarios
- Errores de autenticación

### Eventos a Monitorear

```log
WARN - Authentication failed for user: usuario@example.com
INFO - Soft deleting user with id: 5
INFO - Restoring user with id: 3
ERROR - Error validating token
ERROR - Connection Error: Connection reset
DEBUG - Creating user: usuario@example.com
DEBUG - Updating user with id: 2
```

## 🛠️ Checklist de Seguridad

### Antes de Producción

- [ ] Cambiar JWT_SECRET por uno fuerte (256+ bits)
- [ ] Cambiar credenciales de base de datos
- [ ] Configurar HTTPS/TLS
- [ ] Reducir JWT_EXPIRATION (1-2 horas)
- [ ] Cambiar LOG_LEVEL a INFO o WARN
- [ ] Configurar rate limiting
- [ ] Habilitar CORS solo para dominios específicos
- [ ] Verificar que .env esté en .gitignore
- [ ] Configurar backups automáticos de BD
- [ ] Implementar rotación de secrets
- [ ] Configurar alertas de seguridad
- [ ] Realizar pruebas de penetración
- [ ] Ajustar pool de conexiones según carga esperada
- [ ] Verificar tipos ENUM en PostgreSQL
- [ ] Configurar timeouts de conexión apropiados

### Mantenimiento Regular

- [ ] Actualizar dependencias mensualmente
- [ ] Revisar logs de seguridad semanalmente
- [ ] Auditar usuarios inactivos trimestralmente
- [ ] Rotar JWT secret cada 6 meses
- [ ] Revisar permisos de usuarios mensualmente

## 📚 Recursos Adicionales

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [BCrypt Explained](https://en.wikipedia.org/wiki/Bcrypt)

## 📞 Contacto

Para consultas de seguridad:
- Email: henry.lunazco@vallegrande.edu.pe
- Equipo: Seguridad de la Información - Hino Perú

## 🔧 Configuración de Seguridad

### Pool de Conexiones R2DBC

```yaml
spring:
  r2dbc:
    pool:
      initial-size: 5          # Conexiones iniciales
      max-size: 10             # Máximo de conexiones
      max-idle-time: 10m       # Tiempo máximo inactivo
      max-life-time: 30m       # Tiempo de vida máximo
      validation-query: SELECT 1  # Validar conexiones
```

### Tipos ENUM PostgreSQL

El sistema usa tipos ENUM personalizados:
- `user_role`: admin, asesor, mecanico, supervisor
- `user_status`: activo, inactivo

Las queries usan CAST explícito para compatibilidad:
```sql
CAST($1 AS user_role)
CAST($2 AS user_status)
```

---

**Última actualización**: 22 de Octubre, 2025
