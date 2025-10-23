# Guía de Implementación - Autenticación Frontend

## Endpoints Disponibles

### 1. Iniciar Sesión (Login)
```
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "usuario@ejemplo.com",
  "password": "contraseña123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Autenticación exitosa",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "nombre": "Juan Pérez",
      "email": "usuario@ejemplo.com",
      "rol": "admin",
      "estado": "activo"
    }
  }
}
```

**Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Credenciales incorrectas",
  "data": null
}
```

### 2. Cerrar Sesión (Logout)
```
POST /api/auth/logout
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Sesión cerrada exitosamente",
  "data": null
}
```

---

## Implementación en React/JavaScript

### Configuración Inicial

```javascript
// config/api.js
const API_BASE_URL = 'http://localhost:8080/api';

export const API_ENDPOINTS = {
  LOGIN: `${API_BASE_URL}/auth/login`,
  LOGOUT: `${API_BASE_URL}/auth/logout`,
};
```

### Servicio de Autenticación

```javascript
// services/authService.js

// Iniciar sesión
export const login = async (email, password) => {
  try {
    const response = await fetch(API_ENDPOINTS.LOGIN, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password }),
    });

    const data = await response.json();

    if (response.ok && data.success) {
      // Guardar token en localStorage
      localStorage.setItem('token', data.data.token);
      localStorage.setItem('user', JSON.stringify(data.data.user));
      return { success: true, data: data.data };
    } else {
      return { success: false, message: data.message };
    }
  } catch (error) {
    return { success: false, message: 'Error de conexión' };
  }
};

// Cerrar sesión
export const logout = async () => {
  try {
    const token = localStorage.getItem('token');
    
    if (token) {
      await fetch(API_ENDPOINTS.LOGOUT, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
    }

    // Limpiar localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    
    return { success: true };
  } catch (error) {
    // Limpiar localStorage incluso si falla la petición
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    return { success: true };
  }
};

// Obtener token
export const getToken = () => {
  return localStorage.getItem('token');
};

// Obtener usuario actual
export const getCurrentUser = () => {
  const userStr = localStorage.getItem('user');
  return userStr ? JSON.parse(userStr) : null;
};

// Verificar si está autenticado
export const isAuthenticated = () => {
  return !!getToken();
};
```

### Componente de Login

```jsx
// components/Login.jsx
import { useState } from 'react';
import { login } from '../services/authService';
import { useNavigate } from 'react-router-dom';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const result = await login(email, password);

    if (result.success) {
      // Redirigir al dashboard
      navigate('/dashboard');
    } else {
      setError(result.message || 'Error al iniciar sesión');
    }

    setLoading(false);
  };

  return (
    <div className="login-container">
      <form onSubmit={handleSubmit}>
        <h2>Iniciar Sesión</h2>
        
        {error && <div className="error-message">{error}</div>}
        
        <div className="form-group">
          <label>Email:</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        
        <div className="form-group">
          <label>Contraseña:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        
        <button type="submit" disabled={loading}>
          {loading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
        </button>
      </form>
    </div>
  );
}

export default Login;
```

### Componente de Logout

```jsx
// components/Header.jsx
import { logout, getCurrentUser } from '../services/authService';
import { useNavigate } from 'react-router-dom';

function Header() {
  const navigate = useNavigate();
  const user = getCurrentUser();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <header>
      <div className="user-info">
        <span>Bienvenido, {user?.nombre}</span>
        <button onClick={handleLogout}>Cerrar Sesión</button>
      </div>
    </header>
  );
}

export default Header;
```

### Protección de Rutas

```jsx
// components/ProtectedRoute.jsx
import { Navigate } from 'react-router-dom';
import { isAuthenticated } from '../services/authService';

function ProtectedRoute({ children }) {
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default ProtectedRoute;
```

### Configuración de Rutas

```jsx
// App.jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

### Interceptor para Peticiones Autenticadas

```javascript
// utils/apiClient.js
import { getToken } from '../services/authService';

export const apiClient = async (url, options = {}) => {
  const token = getToken();
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  // Si el token expiró (401), redirigir al login
  if (response.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
    throw new Error('Sesión expirada');
  }

  return response;
};
```

---

## Notas Importantes

1. **Seguridad del Token:**
   - El token se guarda en `localStorage`
   - Incluye el token en el header `Authorization: Bearer {token}` para todas las peticiones protegidas

2. **Expiración del Token:**
   - El token expira según la configuración del backend (por defecto 24 horas)
   - Maneja el error 401 para redirigir al login cuando expire

3. **CORS:**
   - Asegúrate de que el backend tenga configurado CORS para tu dominio frontend
   - Por defecto está configurado para `http://localhost:3000`

4. **Roles de Usuario:**
   - Los roles disponibles son: `admin`, `asesor`, `mecanico`, `supervisor`
   - Usa el rol del usuario para mostrar/ocultar funcionalidades en el frontend

---

## Ejemplo de Uso Completo

```javascript
// Ejemplo de flujo completo
import { login, logout, getCurrentUser, isAuthenticated } from './services/authService';

// 1. Login
const handleLogin = async () => {
  const result = await login('admin@hino.com', 'password123');
  
  if (result.success) {
    console.log('Usuario:', result.data.user);
    console.log('Token:', result.data.token);
  }
};

// 2. Verificar autenticación
if (isAuthenticated()) {
  const user = getCurrentUser();
  console.log('Usuario actual:', user);
}

// 3. Logout
const handleLogout = async () => {
  await logout();
  // Redirigir al login
};
```
