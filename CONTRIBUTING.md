# Guía de Contribución

¡Gracias por tu interés en contribuir al proyecto HinoPE! 🎉

## 📋 Tabla de Contenidos

- [Código de Conducta](#código-de-conducta)
- [Cómo Contribuir](#cómo-contribuir)
- [Configuración del Entorno](#configuración-del-entorno)
- [Estándares de Código](#estándares-de-código)
- [Proceso de Pull Request](#proceso-de-pull-request)
- [Reportar Bugs](#reportar-bugs)
- [Sugerir Mejoras](#sugerir-mejoras)

## 📜 Código de Conducta

Este proyecto adhiere a un código de conducta. Al participar, se espera que mantengas este código.

### Nuestros Estándares

- ✅ Usar lenguaje acogedor e inclusivo
- ✅ Respetar diferentes puntos de vista
- ✅ Aceptar críticas constructivas
- ✅ Enfocarse en lo mejor para la comunidad

## 🤝 Cómo Contribuir

### Áreas de Contribución

1. **Código**
   - Nuevas funcionalidades
   - Corrección de bugs
   - Mejoras de rendimiento
   - Refactorización

2. **Documentación**
   - Mejorar README
   - Agregar ejemplos
   - Traducir documentación
   - Corregir typos

3. **Testing**
   - Escribir tests unitarios
   - Tests de integración
   - Tests end-to-end

4. **Diseño**
   - Mejorar UX/UI
   - Crear mockups
   - Diseñar iconos

## 🛠️ Configuración del Entorno

### Prerrequisitos

```bash
# Java 17
java -version

# Maven 3.8+
mvn -version

# PostgreSQL 14+
psql --version

# Git
git --version
```

### Instalación

1. **Fork el repositorio**
   ```bash
   # Haz fork desde GitHub
   ```

2. **Clona tu fork**
   ```bash
   git clone https://github.com/TU_USUARIO/AS232S5_APS_T01-be.git
   cd AS232S5_APS_T01-be
   ```

3. **Configura el upstream**
   ```bash
   git remote add upstream https://github.com/ORIGINAL/AS232S5_APS_T01-be.git
   ```

4. **Crea archivo .env**
   ```bash
   cp .env.example .env
   # Edita .env con tus credenciales
   ```

5. **Instala dependencias**
   ```bash
   mvn clean install
   ```

6. **Ejecuta la aplicación**
   ```bash
   mvn spring-boot:run
   ```

7. **Verifica que funcione**
   ```bash
   curl http://localhost:8080/swagger-ui.html
   ```

## 📝 Estándares de Código

### Convenciones de Java

```java
// ✅ BIEN - Nombres descriptivos
public Mono<User> getUserById(Long id) {
    return userRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario", id)));
}

// ❌ MAL - Nombres poco claros
public Mono<User> get(Long i) {
    return repo.find(i);
}
```

### Estructura de Clases

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    // 1. Constantes
    private static final int MAX_RETRIES = 3;
    
    // 2. Dependencias (final)
    private final UserRepository userRepository;
    
    // 3. Métodos públicos
    public Mono<User> getUserById(Long id) { }
    
    // 4. Métodos privados
    private Mono<User> validateUser(User user) { }
}
```

### Comentarios

```java
// ✅ BIEN - JavaDoc para métodos públicos
/**
 * Obtiene un usuario por su ID
 * 
 * @param id ID del usuario
 * @return Mono con el usuario encontrado
 * @throws ResourceNotFoundException si el usuario no existe
 */
public Mono<User> getUserById(Long id) { }

// ✅ BIEN - Comentarios para lógica compleja
// Verificar que el último admin no sea eliminado
if (isLastAdmin(user)) {
    return Mono.error(new IllegalArgumentException("No se puede eliminar el último admin"));
}

// ❌ MAL - Comentarios obvios
// Obtener usuario
User user = getUser();
```

### Manejo de Errores

```java
// ✅ BIEN - Errores específicos
return userRepository.findById(id)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario", id)))
        .onErrorResume(IllegalArgumentException.class, e -> 
            Mono.error(new BadRequestException(e.getMessage())));

// ❌ MAL - Errores genéricos
return userRepository.findById(id)
        .switchIfEmpty(Mono.error(new Exception("Error")));
```

### Logging

```java
// ✅ BIEN - Logs informativos
log.debug("Getting user by id: {}", id);
log.info("User authenticated successfully: {}", email);
log.warn("User with id {} is already deleted", id);
log.error("Error validating token", exception);

// ❌ MAL - Logs sin contexto
log.info("Success");
log.error("Error");
```

## 🔄 Proceso de Pull Request

### 1. Crea una rama

```bash
# Actualiza tu fork
git checkout main
git pull upstream main

# Crea rama descriptiva
git checkout -b feature/agregar-cambio-password
git checkout -b fix/corregir-login-error
git checkout -b docs/actualizar-readme
```

### 2. Realiza tus cambios

```bash
# Haz commits atómicos
git add .
git commit -m "feat: agregar endpoint para cambiar contraseña"
```

### 3. Convención de Commits

Usamos [Conventional Commits](https://www.conventionalcommits.org/):

```bash
# Tipos de commits
feat:     # Nueva funcionalidad
fix:      # Corrección de bug
docs:     # Cambios en documentación
style:    # Formato, punto y coma, etc (no afecta código)
refactor: # Refactorización de código
test:     # Agregar o modificar tests
chore:    # Cambios en build, dependencias, etc

# Ejemplos
git commit -m "feat: agregar soft delete para usuarios"
git commit -m "fix: corregir validación de email duplicado"
git commit -m "docs: actualizar guía de instalación"
git commit -m "refactor: optimizar queries de estadísticas"
git commit -m "test: agregar tests para UserService"
```

### 4. Ejecuta tests

```bash
# Ejecutar todos los tests
mvn test

# Verificar cobertura
mvn jacoco:report
```

### 5. Push y Pull Request

```bash
# Push a tu fork
git push origin feature/agregar-cambio-password

# Crea Pull Request en GitHub
# Incluye:
# - Descripción clara de los cambios
# - Issue relacionado (si existe)
# - Screenshots (si aplica)
# - Checklist de testing
```

### Template de Pull Request

```markdown
## Descripción
Breve descripción de los cambios realizados.

## Tipo de cambio
- [ ] Bug fix
- [ ] Nueva funcionalidad
- [ ] Breaking change
- [ ] Documentación

## ¿Cómo se ha probado?
- [ ] Tests unitarios
- [ ] Tests de integración
- [ ] Pruebas manuales

## Checklist
- [ ] Mi código sigue los estándares del proyecto
- [ ] He realizado self-review de mi código
- [ ] He comentado código complejo
- [ ] He actualizado la documentación
- [ ] Mis cambios no generan warnings
- [ ] He agregado tests
- [ ] Todos los tests pasan
- [ ] He actualizado el CHANGELOG.md

## Screenshots (si aplica)
```

## 🐛 Reportar Bugs

### Antes de Reportar

1. Verifica que no sea un issue duplicado
2. Asegúrate de usar la última versión
3. Recopila información del error

### Template de Bug Report

```markdown
**Descripción del bug**
Descripción clara y concisa del bug.

**Pasos para reproducir**
1. Ir a '...'
2. Hacer click en '...'
3. Ver error

**Comportamiento esperado**
Lo que esperabas que sucediera.

**Comportamiento actual**
Lo que realmente sucede.

**Screenshots**
Si aplica, agrega screenshots.

**Entorno**
- OS: [e.g. Windows 11]
- Java: [e.g. 17.0.8]
- Spring Boot: [e.g. 3.5.6]
- Base de datos: [e.g. PostgreSQL 14]

**Logs**
```
Pega aquí los logs relevantes
```

**Contexto adicional**
Cualquier otra información relevante.
```

## 💡 Sugerir Mejoras

### Template de Feature Request

```markdown
**¿Tu solicitud está relacionada con un problema?**
Descripción clara del problema. Ej: "Siempre me frustra cuando [...]"

**Describe la solución que te gustaría**
Descripción clara de lo que quieres que suceda.

**Describe alternativas que has considerado**
Otras soluciones o funcionalidades que has considerado.

**Contexto adicional**
Cualquier otra información, screenshots, mockups, etc.
```

## 📚 Recursos Útiles

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Project Reactor](https://projectreactor.io/docs)
- [R2DBC Documentation](https://r2dbc.io/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Git Flow](https://nvie.com/posts/a-successful-git-branching-model/)

## 🎯 Áreas que Necesitan Ayuda

- [ ] Tests unitarios para todos los servicios
- [ ] Tests de integración
- [ ] Documentación de API en español
- [ ] Ejemplos de uso con diferentes clientes
- [ ] Mejoras de rendimiento
- [ ] Internacionalización (i18n)

## 📞 Contacto

¿Preguntas? Contáctanos:
- Email: dev@hino.com.pe
- Slack: #hinope-dev
- GitHub Discussions: [Link]

---

¡Gracias por contribuir! 🙌
