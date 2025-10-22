# HinoPE - Sistema de Gestión de Flota

Backend del sistema de gestión de flota de vehículos Hino Perú.

## 📋 Descripción

Sistema reactivo construido con Spring Boot WebFlux para la gestión integral de vehículos, usuarios, cotizaciones y notificaciones de Hino Perú.

## 🚀 Tecnologías

- **Java 17**
- **Spring Boot 3.5.6** (WebFlux)
- **R2DBC** (PostgreSQL reactivo)
- **Spring Security** + JWT
- **BCrypt** para encriptación
- **Swagger/OpenAPI** para documentación
- **Lombok** para reducir boilerplate
- **Maven** para gestión de dependencias

## 📚 Documentación

- [USER.md](USER.md) - Documentación del módulo de usuarios
- [SOFT_DELETE_GUIDE.md](SOFT_DELETE_GUIDE.md) - Guía de eliminación lógica
- [SECURITY.md](SECURITY.md) - Políticas de seguridad
- [CHANGELOG.md](CHANGELOG.md) - Historial de cambios
- [CONTRIBUTING.md](CONTRIBUTING.md) - Guía de contribución
- [Swagger UI](http://localhost:8080/swagger-ui.html) - Documentación interactiva de API

## 🏗️ Arquitectura

```
com.aps.hino/
├── config/          # Configuración (Security, CORS, OpenAPI)
├── dto/             # Data Transfer Objects
├── exception/       # Excepciones personalizadas
├── model/           # Entidades del dominio
├── repository/      # Repositorios R2DBC
├── rest/            # Controladores REST
├── security/        # JWT y filtros de autenticación
├── service/         # Lógica de negocio
└── util/            # Utilidades
```

## ⚙️ Instalación

### Prerrequisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### Pasos

1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/AS232S5_APS_T01-be.git
cd AS232S5_APS_T01-be
```

2. **Configurar variables de entorno**
```bash
cp .env.example .env
# Editar .env con tus credenciales
```

3. **Instalar dependencias**
```bash
mvn clean install
```

4. **Ejecutar la aplicación**
```bash
mvn spring-boot:run
```

5. **Acceder a Swagger**
```
http://localhost:8080/swagger-ui.html
```

## 🔐 Configuración

### Variables de Entorno (.env)

```env
# Base de datos
DB_URL=r2dbc:postgresql://host:5432/database
DB_USERNAME=usuario
DB_PASSWORD=contraseña

# JWT
JWT_SECRET=tu-secret-key-seguro-256-bits
JWT_EXPIRATION=86400000

# Servidor
SERVER_PORT=8080
```

## 📦 Módulos

### ✅ Usuarios (Completado)
- Autenticación con JWT
- CRUD completo
- Soft delete con restauración
- Control de acceso por roles
- Estadísticas

### 🚧 Vehículos (En desarrollo)
- Gestión de flota
- Imágenes de vehículos
- Estados y disponibilidad

### 🚧 Cotizaciones (En desarrollo)
- Gestión de cotizaciones
- Asignación a asesores
- Archivos adjuntos

### 🚧 Notificaciones (En desarrollo)
- Sistema de notificaciones
- Alertas y recordatorios

## 🧪 Testing

```bash
# Ejecutar tests
mvn test

# Cobertura de código
mvn jacoco:report
```

## 🤝 Contribuir

Lee [CONTRIBUTING.md](CONTRIBUTING.md) para detalles sobre nuestro código de conducta y el proceso para enviar pull requests.

## 📄 Licencia

Este proyecto es privado y pertenece a Hino Perú.

## 👥 Equipo

- **Módulo Usuarios**: Henry Lunazco
- **Módulo Vehículos**: [Compañero 1]
- **Módulo Cotizaciones**: [Compañero 2]
- **Módulo Notificaciones**: [Compañero 3]

## 📞 Contacto

- Email: dev@hino.com.pe
- Proyecto: AS232S5_APS_T01