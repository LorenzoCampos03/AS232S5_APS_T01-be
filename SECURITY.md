# Política de Seguridad

## 🔒 Versiones Soportadas

| Versión | Soportada          |
| ------- | ------------------ |
| 1.2.x   | :white_check_mark: |
| 1.1.x   | :white_check_mark: |
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
ERROR - Error validating token
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

---

**Última actualización**: 21 de Octubre, 2025
