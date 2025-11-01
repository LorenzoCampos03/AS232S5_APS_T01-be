# Integración Frontend — Sistema de Notificaciones

Este documento describe todo lo necesario para implementar la interfaz de notificaciones en tu frontend y consumir las APIs que implementamos en el backend.

Contenido:
- Endpoints y contrato de API
- Autenticación y headers
- Formatos de datos (DTOs)
- Estrategias para consumir (polling, SSE/WebSocket)
- Paginación y filtros recomendados
- Comportamiento UI y componentes sugeridos
- Manejo de errores y seguridad
- Ejemplos de flujo (login → obtener notificaciones → marcar como leídas)

## 1. Endpoints disponibles
Base URL: `http://localhost:8080`

Endpoints relevantes:
- GET `/api/notifications`
  - Descripción: Retorna todas las notificaciones ordenadas por `created_at` desc.
  - Requiere: Autenticación Bearer (JWT).
  - Respuesta: `200` con array de objetos `NotificationDto`.

- GET `/api/notifications/count-unread`
  - Descripción: Retorna el conteo de notificaciones no leídas.
  - Requiere: Autenticación Bearer (JWT).
  - Respuesta: `200` con número entero (long) en el body.

- POST `/api/notifications/{id}/read`
  - Descripción: Marca una notificación como leída (set `is_read = true`).
  - Requiere: Autenticación Bearer (JWT).
  - Respuesta: `204 No Content` si fue exitoso.

> Nota: Si necesitas endpoints adicionales (marcar varias como leídas, paginar, filtrar por `entity` o `onlyUnread`), puedo añadirlos al backend y documentarlos aquí.

## 2. Autenticación y headers
- El backend usa JWT (Bearer token). Debes obtener el token en el frontend a través de:
  - POST `/api/auth/login` con body: `{ "email": "..", "password": ".." }`
  - Respuesta incluye `token` (en `data.token` dentro del `ApiResponse` del backend).

- Para todas las llamadas protegidas, agregar header:
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json` (si envías JSON)

- CORS: el backend ya permite orígenes locales comunes (ej. `http://localhost:5173` y `http://localhost:3000`). Si tu origin es distinto, actualiza `CorsConfigurationSource` en backend o añade origen al front.

## 3. Formatos de datos (DTOs)
NotificationDto (ejemplo de shape devuelto por `GET /api/notifications`):
{
  id: Long,
  entity: String,       // p.ej. "users", "vehicles", "maintenance", "cotizaciones"
  entityId: Long|null,  // id del recurso afectado o null
  action: String,       // p.ej. "CREATE", "UPDATE", "DELETE", "ADD_ITEM", "UPDATE_ITEM",
  description: String,  // texto descriptivo legible por humanos
  actorId: Long|null,   // id del usuario que realizó la acción (si disponible)
  actorEmail: String|null,
  read: Boolean,
  createdAt: String (ISO 8601 timestamp with timezone)
}

## 4. Estrategias para consumir notificaciones
Tienes 3 opciones principales para mantener la UI actualizada:

1) Polling (fácil, universal)
- Estructura: cada X segundos (p.ej. 10s) realizar `GET /api/notifications` o `GET /api/notifications/count-unread`.
- Pros: simple, funciona con cualquier hosting.
- Contras: latencia entre eventos y consumo extra en servidor si hay muchos usuarios.
- Recomendación: usar `count-unread` para badge en tiempo real y `GET /api/notifications?page=...` para listado.

2) Server-Sent Events (SSE) / EventSource (recomendado si sólo necesitas push unidireccional)
- Beneficio: conexión ligera, el servidor puede empujar nuevas notificaciones en tiempo real.
- Backend: actualmente no implementado SSE; puedo añadir endpoint `/api/notifications/stream` que emite nuevos eventos cuando se crean.
- Frontend (ejemplo):
  - const es = new EventSource('/api/notifications/stream');
  - es.onmessage = (e) => { const notif = JSON.parse(e.data); /* actualizar UI */ }
- Autenticación con Bearer en SSE: requiere pasar token en query param o usar cookie; si eliges query param, asegurarse de usar HTTPS y token con TTL corto.

3) WebSocket (bidireccional)
- Útil si quieres que el frontend confirme recepción o enviar acciones en tiempo real.
- Requiere agregar endpoint WebSocket en backend y manejar reconexión.

Recomendación práctica: empezar con polling para validar UX, luego migrar a SSE para reducir carga y latencia.

## 5. Paginación y filtros (recomendados en frontend)
- Backend actualmente devuelve todas las notificaciones; si el volumen crece, implementar paginación es crítico.
- Propuesta de API (backend):
  - `GET /api/notifications?page=0&size=20&onlyUnread=true&entity=maintenance`
  - Respuesta: `{ content: [..], currentPage:0, pageSize:20, totalElements: 123, totalPages: 7 }`
- En frontend: usar infinito scroll o paginado por páginas dependiendo del diseño.

## 6. Componentes UI sugeridos
- Componente `NotificationsButton` (icono de campana):
  - Muestra badge con `count-unread`.
  - Al hacer click abre `NotificationsDropdown`.

- `NotificationsDropdown`:
  - Lista compacta (últimas 6-10 notificaciones): muestra `description`, `createdAt` (relativo: "hace 5 min"), y estado read/unread.
  - Acciones: click en item → navegar al recurso (usar `entity` y `entityId` para construir ruta), y marcar como leído (llamar `POST /api/notifications/{id}/read`).
  - Botón "Ver todas" → abre `NotificationsPage`.

- `NotificationsPage` (lista completa):
  - Filtros: solo no leídas, por entidad, búsqueda por texto.
  - Paginación/infinite scroll.
  - Bulk actions: "Marcar todas como leídas" (requiere endpoint backend que puedo agregar).

UX adicional:
- Marcar automáticamente como leído cuando el usuario abre el item o realiza la acción.
- Mostrar agrupación por día/hora.
- Mostrar actor si disponible: "Carlos (carlos@x) creó la cotización #123".

## 7. Manejo de errores y estados
- 401 Unauthorized: solicitar al usuario re-login; refrescar token o redirigir a login.
- 403 Forbidden: mostrar mensaje "No tienes permisos".
- 5xx: mostrar mensaje genérico "Error al cargar notificaciones" y reintentar con backoff.
- Retry: para polling, implementar backoff exponencial si hay errores continuos.

## 8. Seguridad y tokens
- Nunca exponer token en URL pública en producción. Para SSE con token en query string considera usar cookies o establecer la conexión desde backend por proxy si necesitas seguridad fuerte.
- Usar HTTPS en producción siempre.
- Considera usar refresh tokens si los JWT son de corta duración.

## 9. Ejemplos de flujo (sin datos mock, pasos concretos)
1) Login (frontend): POST `/api/auth/login` → extraer `token`.
2) Guardar token seguro (en memoria/redux/Context; no poner en localStorage si riesgo XSS alto; si lo guardas, usar HttpOnly cookie con backend en producción).
3) Al iniciar la app, realizar `GET /api/notifications/count-unread` para llenado del badge.
4) Mostrar badge en componente global.
5) Abrir dropdown: `GET /api/notifications` → mostrar lista.
6) Usuario hace click en notificación: llamar `POST /api/notifications/{id}/read` y navegar al recurso correspondiente.

## 10. Recomendaciones de implementación técnica (por stack front típico: React + fetch/axios + Vite)
- HTTP client:
  - Usa Axios o fetch wrapper que automáticamente incluya `Authorization` header desde tu store (contexto de auth).
  - Interceptor para manejar 401 (redirigir a login una sola vez, refrescar token si aplicable).

- Ejemplo de helper (pseudocódigo):
  - Request:
    - headers = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }

- Polling: usar `setInterval` o hook custom `useInterval` que limpie en unmount.
- SSE: usar `EventSource` en un hook `useNotificationsStream` y limpiar con `es.close()` en unmount.

## 11. Consideraciones finales y próximos pasos que puedo hacer por ti
- Si quieres que el backend exponga SSE o WebSocket, lo implemento y documentaré el uso en este documento.
- Puedo añadir endpoints de paginación y bulk-mark-read si lo deseas.
- Puedo añadir ejemplos concretos de componentes React (sin datos mock) si lo pides.

---

Si quieres, ahora:
- A) Agrego un endpoint SSE (`/api/notifications/stream`) y lo documento en `front.md` con ejemplos concretos de `EventSource`.
- B) Agrego paginación y bulk-mark-read en backend y actualizo `front.md`.
- C) Genero componentes de ejemplo en React (sin datos mock) para tu frontend.

Dime cuál prefieres y lo hago en la siguiente iteración.
