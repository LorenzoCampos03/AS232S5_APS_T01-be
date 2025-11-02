# Frontend v2 - Sistema de Notificaciones en Tiempo Real

## 🎯 Visión General

Este documento describe la implementación completa del frontend para el sistema de notificaciones en tiempo real de Hino, incluyendo la integración con el backend, manejo de estados, y componentes de UI modernos.

## 📋 Tabla de Contenidos

1. [Arquitectura del Frontend](#arquitectura-del-frontend)
2. [Configuración del Proyecto](#configuración-del-proyecto)
3. [Sistema de Notificaciones](#sistema-de-notificaciones)
4. [Componentes Principales](#componentes-principales)
5. [Servicios y API](#servicios-y-api)
6. [Estados y Context](#estados-y-context)
7. [Routing y Navegación](#routing-y-navegación)
8. [Estilos y Temas](#estilos-y-temas)
9. [Testing](#testing)
10. [Deployment](#deployment)

## 🏗️ Arquitectura del Frontend



### Estructura del Proyecto
```
src/
├── components/           # Componentes reutilizables
│   ├── ui/              # Componentes base (Button, Input, etc.)
│   ├── layout/          # Layout components (Header, Sidebar, etc.)
│   ├── notifications/   # Componentes específicos de notificaciones
│   └── forms/           # Componentes de formularios
├── pages/               # Páginas de la aplicación
├── services/            # Servicios de API y WebSocket
├── stores/              # Estados globales con Zustand
├── hooks/               # Custom hooks
├── types/               # Definiciones de TypeScript
├── utils/               # Utilidades y helpers
├── constants/           # Constantes de la aplicación
└── styles/              # Estilos globales
```

## 🔔 Sistema de Notificaciones

### Tipos de Notificaciones
```typescript
// types/notifications.ts
export interface Notification {
  id: number;
  userId: number;
  title: string;
  message: string;
  eventType: EventType;
  entityType: EntityType;
  entityId: string;
  entityName: string;
  priority: NotificationPriority;
  isRead: boolean;
  createdAt: string;
  additionalData?: Record<string, any>;
}

export enum EventType {
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  LOGIN = 'LOGIN',
  LOGOUT = 'LOGOUT',
  STATUS_CHANGE = 'STATUS_CHANGE',
  RESTORE = 'RESTORE'
}

export enum EntityType {
  USER = 'USER',
  VEHICLE = 'VEHICLE',
  COTIZACION = 'COTIZACION',
  MAINTENANCE = 'MAINTENANCE',
  SYSTEM = 'SYSTEM'
}

export enum NotificationPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}
```

### Store de Notificaciones
```typescript
// stores/notificationStore.ts
import { create } from 'zustand';
import { Notification } from '@/types/notifications';
import { notificationService } from '@/services/notificationService';

interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  isLoading: boolean;
  error: string | null;
  
  // Actions
  fetchNotifications: () => Promise<void>;
  markAsRead: (id: number) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  addNotification: (notification: Notification) => void;
  removeNotification: (id: number) => void;
  clearError: () => void;
}

export const useNotificationStore = create<NotificationState>((set, get) => ({
  notifications: [],
  unreadCount: 0,
  isLoading: false,
  error: null,

  fetchNotifications: async () => {
    set({ isLoading: true, error: null });
    try {
      const notifications = await notificationService.getAll();
      const unreadCount = notifications.filter(n => !n.isRead).length;
      set({ notifications, unreadCount, isLoading: false });
    } catch (error) {
      set({ error: 'Error al cargar notificaciones', isLoading: false });
    }
  },

  markAsRead: async (id: number) => {
    try {
      await notificationService.markAsRead(id);
      set(state => ({
        notifications: state.notifications.map(n => 
          n.id === id ? { ...n, isRead: true } : n
        ),
        unreadCount: Math.max(0, state.unreadCount - 1)
      }));
    } catch (error) {
      set({ error: 'Error al marcar como leída' });
    }
  },

  markAllAsRead: async () => {
    try {
      await notificationService.markAllAsRead();
      set(state => ({
        notifications: state.notifications.map(n => ({ ...n, isRead: true })),
        unreadCount: 0
      }));
    } catch (error) {
      set({ error: 'Error al marcar todas como leídas' });
    }
  },

  addNotification: (notification: Notification) => {
    set(state => ({
      notifications: [notification, ...state.notifications],
      unreadCount: state.unreadCount + 1
    }));
  },

  removeNotification: (id: number) => {
    set(state => ({
      notifications: state.notifications.filter(n => n.id !== id),
      unreadCount: state.notifications.find(n => n.id === id && !n.isRead) 
        ? state.unreadCount - 1 
        : state.unreadCount
    }));
  },

  clearError: () => set({ error: null })
}));
```

### Servicio de Notificaciones
```typescript
// services/notificationService.ts
import axios from 'axios';
import { Notification } from '@/types/notifications';

const API_BASE = '/api/notifications';

export const notificationService = {
  // Obtener todas las notificaciones
  async getAll(): Promise<Notification[]> {
    const response = await axios.get(`${API_BASE}/public/all`);
    return response.data.data;
  },

  // Obtener notificaciones recientes
  async getRecent(limit = 10): Promise<Notification[]> {
    const response = await axios.get(`${API_BASE}/public/recent?limit=${limit}`);
    return response.data.data;
  },

  // Obtener estadísticas
  async getStats() {
    const response = await axios.get(`${API_BASE}/public/stats`);
    return response.data.data;
  },

  // Marcar como leída (requiere autenticación)
  async markAsRead(id: number): Promise<void> {
    await axios.put(`${API_BASE}/${id}/read`);
  },

  // Marcar todas como leídas (requiere autenticación)
  async markAllAsRead(): Promise<void> {
    await axios.put(`${API_BASE}/mark-all-read`);
  },

  // Health check
  async healthCheck() {
    const response = await axios.get('/api/diagnostic/notifications/health');
    return response.data;
  }
};
```

### WebSocket para Tiempo Real
```typescript
// services/websocketService.ts
import { io, Socket } from 'socket.io-client';
import { Notification } from '@/types/notifications';
import { useNotificationStore } from '@/stores/notificationStore';
import toast from 'react-hot-toast';

class WebSocketService {
  private socket: Socket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;

  connect() {
    if (this.socket?.connected) return;

    this.socket = io('ws://localhost:8080', {
      transports: ['websocket'],
      autoConnect: true,
    });

    this.socket.on('connect', () => {
      console.log('🔌 WebSocket conectado');
      this.reconnectAttempts = 0;
      toast.success('Conectado al sistema de notificaciones');
    });

    this.socket.on('disconnect', () => {
      console.log('🔌 WebSocket desconectado');
      this.handleReconnect();
    });

    this.socket.on('notification', (notification: Notification) => {
      console.log('🔔 Nueva notificación:', notification);
      
      // Agregar al store
      useNotificationStore.getState().addNotification(notification);
      
      // Mostrar toast
      this.showNotificationToast(notification);
    });

    this.socket.on('connect_error', (error) => {
      console.error('❌ Error de conexión WebSocket:', error);
      this.handleReconnect();
    });
  }

  private handleReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      const delay = Math.pow(2, this.reconnectAttempts) * 1000; // Exponential backoff
      
      setTimeout(() => {
        console.log(`🔄 Reintentando conexión (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
        this.connect();
      }, delay);
    } else {
      toast.error('No se pudo conectar al sistema de notificaciones');
    }
  }

  private showNotificationToast(notification: Notification) {
    const toastOptions = {
      duration: 5000,
      position: 'top-right' as const,
    };

    switch (notification.priority) {
      case 'CRITICAL':
        toast.error(`🚨 ${notification.title}: ${notification.message}`, toastOptions);
        break;
      case 'HIGH':
        toast.error(`⚠️ ${notification.title}: ${notification.message}`, toastOptions);
        break;
      case 'MEDIUM':
        toast(`📢 ${notification.title}: ${notification.message}`, toastOptions);
        break;
      case 'LOW':
        toast.success(`ℹ️ ${notification.title}: ${notification.message}`, toastOptions);
        break;
      default:
        toast(`🔔 ${notification.title}: ${notification.message}`, toastOptions);
    }
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
  }

  isConnected(): boolean {
    return this.socket?.connected ?? false;
  }
}

export const websocketService = new WebSocketService();
```

## 🧩 Componentes Principales

### Componente de Notificaciones
```typescript
// components/notifications/NotificationCenter.tsx
import React, { useEffect, useState } from 'react';
import { BellIcon } from '@heroicons/react/24/outline';
import { BellIcon as BellSolidIcon } from '@heroicons/react/24/solid';
import { useNotificationStore } from '@/stores/notificationStore';
import { NotificationList } from './NotificationList';
import { NotificationStats } from './NotificationStats';

export const NotificationCenter: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const { notifications, unreadCount, fetchNotifications, isLoading } = useNotificationStore();

  useEffect(() => {
    fetchNotifications();
  }, [fetchNotifications]);

  return (
    <div className="relative">
      {/* Notification Bell */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2 text-gray-600 hover:text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-500 rounded-lg"
      >
        {unreadCount > 0 ? (
          <BellSolidIcon className="h-6 w-6 text-primary-600" />
        ) : (
          <BellIcon className="h-6 w-6" />
        )}
        
        {/* Badge */}
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown Panel */}
      {isOpen && (
        <div className="absolute right-0 mt-2 w-96 bg-white rounded-lg shadow-lg border border-gray-200 z-50">
          <div className="p-4 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900">
                Notificaciones
              </h3>
              <button
                onClick={() => setIsOpen(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                ✕
              </button>
            </div>
            <NotificationStats />
          </div>

          <div className="max-h-96 overflow-y-auto">
            {isLoading ? (
              <div className="p-4 text-center text-gray-500">
                Cargando notificaciones...
              </div>
            ) : (
              <NotificationList 
                notifications={notifications.slice(0, 10)} 
                onClose={() => setIsOpen(false)}
              />
            )}
          </div>

          <div className="p-4 border-t border-gray-200">
            <button
              onClick={() => {
                setIsOpen(false);
                // Navigate to full notifications page
              }}
              className="w-full text-center text-primary-600 hover:text-primary-700 font-medium"
            >
              Ver todas las notificaciones
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
```

### Lista de Notificaciones
```typescript
// components/notifications/NotificationList.tsx
import React from 'react';
import { formatDistanceToNow } from 'date-fns';
import { es } from 'date-fns/locale';
import { Notification } from '@/types/notifications';
import { useNotificationStore } from '@/stores/notificationStore';
import { NotificationIcon } from './NotificationIcon';

interface NotificationListProps {
  notifications: Notification[];
  onClose?: () => void;
}

export const NotificationList: React.FC<NotificationListProps> = ({ 
  notifications, 
  onClose 
}) => {
  const { markAsRead } = useNotificationStore();

  const handleNotificationClick = async (notification: Notification) => {
    if (!notification.isRead) {
      await markAsRead(notification.id);
    }
    onClose?.();
  };

  if (notifications.length === 0) {
    return (
      <div className="p-8 text-center text-gray-500">
        <BellIcon className="h-12 w-12 mx-auto mb-4 text-gray-300" />
        <p>No hay notificaciones</p>
      </div>
    );
  }

  return (
    <div className="divide-y divide-gray-100">
      {notifications.map((notification) => (
        <div
          key={notification.id}
          onClick={() => handleNotificationClick(notification)}
          className={`p-4 hover:bg-gray-50 cursor-pointer transition-colors ${
            !notification.isRead ? 'bg-blue-50' : ''
          }`}
        >
          <div className="flex items-start space-x-3">
            <NotificationIcon 
              eventType={notification.eventType}
              priority={notification.priority}
            />
            
            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between">
                <p className={`text-sm font-medium ${
                  !notification.isRead ? 'text-gray-900' : 'text-gray-700'
                }`}>
                  {notification.title}
                </p>
                {!notification.isRead && (
                  <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                )}
              </div>
              
              <p className="text-sm text-gray-600 mt-1">
                {notification.message}
              </p>
              
              <div className="flex items-center justify-between mt-2">
                <span className="text-xs text-gray-500">
                  {formatDistanceToNow(new Date(notification.createdAt), {
                    addSuffix: true,
                    locale: es
                  })}
                </span>
                
                <span className={`text-xs px-2 py-1 rounded-full ${
                  notification.priority === 'CRITICAL' ? 'bg-red-100 text-red-800' :
                  notification.priority === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                  notification.priority === 'MEDIUM' ? 'bg-blue-100 text-blue-800' :
                  'bg-gray-100 text-gray-800'
                }`}>
                  {notification.priority}
                </span>
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};
```

### Iconos de Notificaciones
```typescript
// components/notifications/NotificationIcon.tsx
import React from 'react';
import {
  UserPlusIcon,
  PencilSquareIcon,
  TrashIcon,
  ArrowRightOnRectangleIcon,
  ArrowLeftOnRectangleIcon,
  TruckIcon,
  DocumentTextIcon,
  WrenchScrewdriverIcon,
  ComputerDesktopIcon,
  ExclamationTriangleIcon,
} from '@heroicons/react/24/outline';
import { EventType, EntityType, NotificationPriority } from '@/types/notifications';

interface NotificationIconProps {
  eventType: EventType;
  entityType?: EntityType;
  priority?: NotificationPriority;
}

export const Notificatiot.FC<NotificationIconProps> = ({
  eventType,
  entityType,
  priority
}) => {
  const getIcon = () => {
    // Priority-based icons for critical notifications
    if (priority === 'CRITICAL') {
      return <ExclamationTriangleIcon className="h-5 w-5 text-red-500" />;
    }

    // Event-based icons
    switch (eventType) {
      case EventType.CREATE:
        return <UserPlusIcon className="h-5 w-5 text-green-500" />;
      case EventType.UPDATE:
        return <PencilSquareIcon className="h-5 w-5 text-blue-500" />;
      case EventType.DELETE:
        return <TrashIcon className="h-5 w-5 text-red-500" />;
      case EventType.LOGIN:
        return <ArrowRightOnRectangleIcon className="h-5 w-5 text-green-500" />;
      case EventType.LOGOUT:
        return <ArrowLeftOnRectangleIcon className="h-5 w-5 text-gray-500" />;
      default:
        // Entity-based icons as fallback
        switch (entityType) {
          case EntityType.USER:
            return <UserPlusIcon className="h-5 w-5 text-blue-500" />;
          case EntityType.VEHICLE:
            return <TruckIcon className="h-5 w-5 text-purple-500" />;
          case EntityType.COTIZACION:
            return <DocumentTextIcon className="h-5 w-5 text-orange-500" />;
          case EntityType.MAINTENANCE:
            return <WrenchScrewdriverIcon className="h-5 w-5 text-yellow-500" />;
          case EntityType.SYSTEM:
            return <ComputerDesktopIcon className="h-5 w-5 text-gray-500" />;
          default:
            return <DocumentTextIcon className="h-5 w-5 text-gray-500" />;
        }
    }
  };

  return (
    <div className="flex-shrink-0">
      {getIcon()}
    </div>
  );
};
```

### Estadísticas de Notificaciones
```typescript
// components/notifications/NotificationStats.tsx
import React, { useEffect, useState } from 'react';
import { notificationService } from '@/services/notificationService';

interface Stats {
  total: number;
  unread: number;
  byPriority: Record<string, number>;
  byType: Record<string, number>;
}

export const NotificationStats: React.FC = () => {
  const [stats, setStats] = useState<Stats | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const data = await notificationService.getStats();
        setStats(data);
      } catch (error) {
        console.error('Error fetching stats:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchStats();
  }, []);

  if (isLoading) {
    return <div className="text-sm text-gray-500">Cargando estadísticas...</div>;
  }

  if (!stats) {
    return null;
  }

  return (
    <div className="mt-2 text-sm text-gray-600">
      <div className="flex items-center justify-between">
        <span>Total: {stats.total}</span>
        <span>Sin leer: {stats.unread}</span>
      </div>
      
      {stats.byPriority.CRITICAL > 0 && (
        <div className="mt-1 text-red-600 font-medium">
          ⚠️ {stats.byPriority.CRITICAL} críticas
        </div>
      )}
    </div>
  );
};
```

## 🔧 Hooks Personalizados

### Hook para Notificaciones en Tiempo Real
```typescript
// hooks/useRealTimeNotifications.ts
import { useEffect } from 'react';
import { websocketService } from '@/services/websocketService';
import { useNotificationStore } from '@/stores/notificationStore';

export const useRealTimeNotifications = () => {
  const { fetchNotifications } = useNotificationStore();

  useEffect(() => {
    // Conectar WebSocket
    websocketService.connect();

    // Fetch inicial de notificaciones
    fetchNotifications();

    // Cleanup al desmontar
    return () => {
      websocketService.disconnect();
    };
  }, [fetchNotifications]);

  return {
    isConnected: websocketService.isConnected(),
  };
};
```

### Hook para Polling de Notificaciones
```typescript
// hooks/useNotificationPolling.ts
import { useEffect, useRef } from 'react';
import { useNotificationStore } from '@/stores/notificationStore';

export const useNotificationPolling = (intervalMs = 30000) => {
  const { fetchNotifications } = useNotificationStore();
  const intervalRef = useRef<NodeJS.Timeout>();

  useEffect(() => {
    // Fetch inicial
    fetchNotifications();

    // Configurar polling
    intervalRef.current = setInterval(() => {
      fetchNotifications();
    }, intervalMs);

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [fetchNotifications, intervalMs]);

  const stopPolling = () => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
    }
  };

  const startPolling = () => {
    stopPolling();
    intervalRef.current = setInterval(() => {
      fetchNotifications();
    }, intervalMs);
  };

  return { stopPolling, startPolling };
};
```

## 📱 Páginas Principales

### Dashboard Principal
```typescript
// pages/Dashboard.tsx
import React from 'react';
import { useRealTimeNotifications } from '@/hooks/useRealTimeNotifications';
import { NotificationCenter } from '@/components/notifications/NotificationCenter';
import { DashboardStats } from '@/components/dashboard/DashboardStats';
import { RecentActivity } from '@/components/dashboard/RecentActivity';

export const Dashboard: React.FC = () => {
  const { isConnected } = useRealTimeNotifications();

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <h1 className="text-2xl font-bold text-gray-900">
                Dashboard Hino
              </h1>
              {!isConnected && (
                <span className="ml-4 px-2 py-1 bg-red-100 text-red-800 text-xs rounded-full">
                  Desconectado
                </span>
              )}
            </div>
            
            <div className="flex items-center space-x-4">
              <NotificationCenter />
              {/* User menu, etc. */}
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Stats */}
          <div className="lg:col-span-2">
            <DashboardStats />
          </div>
          
          {/* Recent Activity */}
          <div>
            <RecentActivity />
          </div>
        </div>
      </main>
    </div>
  );
};
```

### Página de Notificaciones Completa
```typescript
// pages/Notifications.tsx
import React, { useState, useEffect } from 'react';
import { useNotificationStore } from '@/stores/notificationStore';
import { NotificationList } from '@/components/notifications/NotificationList';
import { NotificationFilters } from '@/components/notifications/NotificationFilters';

export const NotificationsPage: React.FC = () => {
  const { 
    notifications, 
    isLoading, 
    fetchNotifications, 
    markAllAsRead 
  } = useNotificationStore();
  
  const [filters, setFilters] = useState({
    priority: '',
    entityType: '',
    isRead: '',
  });

  useEffect(() => {
    fetchNotifications();
  }, [fetchNotifications]);

  const filteredNotifications = notifications.filter(notification => {
    if (filters.priority && notification.priority !== filters.priority) return false;
    if (filters.entityType && notification.entityType !== filters.entityType) return false;
    if (filters.isRead === 'read' && !notification.isRead) return false;
    if (filters.isRead === 'unread' && notification.isRead) return false;
    return true;
  });

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <h1 className="text-3xl font-bold text-gray-900">
              Notificaciones
            </h1>
            <button
              onClick={markAllAsRead}
              className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
            >
              Marcar todas como leídas
            </button>
          </div>
          
          <p className="mt-2 text-gray-600">
            {filteredNotifications.length} notificaciones
          </p>
        </div>

        {/* Filters */}
        <div className="mb-6">
          <NotificationFilters 
            filters={filters}
            onFiltersChange={setFilters}
          />
        </div>

        {/* Notifications */}
        <div className="bg-white rounded-lg shadow">
          {isLoading ? (
            <div className="p-8 text-center text-gray-500">
              Cargando notificaciones...
            </div>
          ) : (
            <NotificationList notifications={filteredNotifications} />
          )}
        </div>
      </div>
    </div>
  );
};
```

## 🎨 Componentes de UI Base

### Button Component
```typescript
// components/ui/Button.tsx
import React from 'react';
import { clsx } from 'clsx';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  children: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  isLoading = false,
  className,
  children,
  disabled,
  ...props
}) => {
  const baseClasses = 'inline-flex items-center justify-center font-medium rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2';
  
  const variantClasses = {
    primary: 'bg-primary-600 text-white hover:bg-primary-700 focus:ring-primary-500',
    secondary: 'bg-gray-200 text-gray-900 hover:bg-gray-300 focus:ring-gray-500',
    danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
    ghost: 'text-gray-700 hover:bg-gray-100 focus:ring-gray-500',
  };
  
  const sizeClasses = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-3 text-base',
  };

  return (
    <button
      className={clsx(
        baseClasses,
        variantClasses[variant],
        sizeClasses[size],
        (disabled || isLoading) && 'opacity-50 cursor-not-allowed',
        className
      )}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading && (
        <svg className="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
        </svg>
      )}
      {children}
    </button>
  );
};
```

## 🚀 Configuración de Desarrollo

### Scripts de Package.json
```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "lint": "eslint src --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
    "lint:fix": "eslint src --ext ts,tsx --fix",
    "type-check": "tsc --noEmit"
  }
}
```

### Variables de Entorno
```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080
VITE_APP_NAME=Hino Notifications

# .env.production
VITE_API_BASE_URL=https://api.hino.com
VITE_WS_URL=wss://api.hino.com
VITE_APP_NAME=Hino Notifications
```

## 📋 Checklist de Implementación

### Fase 1: Configuración Base
- [ ] Configurar proyecto Vite con TypeScript
- [ ] Instalar y configurar Tailwind CSS
- [ ] Configurar Zustand para estado global
- [ ] Configurar React Router
- [ ] Configurar Axios para API calls

### Fase 2: Sistema de Notificaciones
- [ ] Implementar tipos TypeScript
- [ ] Crear store de notificaciones
- [ ] Implementar servicio de API
- [ ] Configurar WebSocket service
- [ ] Crear componentes de notificaciones

### Fase 3: UI y UX
- [ ] Implementar NotificationCenter
- [ ] Crear componentes de lista y filtros
- [ ] Implementar iconos y estados visuales
- [ ] Configurar toasts para notificaciones en tiempo real
- [ ] Implementar página completa de notificaciones

### Fase 4: Integración y Testing
- [ ] Integrar con backend
- [ ] Implementar manejo de errores
- [ ] Configurar polling como fallback
- [ ] Escribir tests unitarios
- [ ] Realizar testing de integración

### Fase 5: Optimización y Deploy
- [ ] Optimizar rendimiento
- [ ] Implementar lazy loading
- [ ] Configurar PWA (opcional)
- [ ] Configurar CI/CD
- [ ] Deploy a producción

## 🔍 Testing

### Configuración de Vitest
```typescript
// vitest.config.ts
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import from 'path';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
});
```

### Test de Componente
```typescript
// components/notifications/__tests__/NotificationCenter.test.tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { NotificationCenter } from '../NotificationCenter';

// Mock del store
vi.mock('@/stores/notificationStore', () => ({
  useNotificationStore: () => ({
    notifications: [],
    unreadCount: 5,
    fetchNotifications: vi.fn(),
    isLoading: false,
  }),
}));

describe('NotificationCenter', () => {
  it('should render notification bell with badge', () => {
    render(<NotificationCenter />);
    
    const bell = screen.getByRole('button');
    expect(bell).toBeInTheDocument();
    
    const badge = screen.getByText('5');
    expect(badge).toBeInTheDocument();
  });

  it('should open dropdown when bell is clicked', () => {
    render(<NotificationCenter />);
    
    const bell = screen.getByRole('button');
    fireEvent.click(bell);
    
    expect(screen.getByText('Notificaciones')).toBeInTheDocument();
  });
});
```

## 🚀 Deployment

### Build para Producción
```bash
npm run build
```

### Configuración de Nginx
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/hino-frontend/dist;
    index index.html;

    # Handle client-side routing
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API proxy
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # WebSocket proxy
    location /socket.io/ {
        proxy_pass http://backend:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

### Docker Configuration
```dockerfile
# Dockerfile
FROM node:18-alpine as builder

WORKDIR /app
COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 📚 Recursos Adicionales

### Documentación de APIs
- [Endpoints de Notificaciones](./ENDPOINTS_PUBLICOS_POSTMAN.md)
- [Guía de Testing](./GUIA_COMPLETA_TESTING_NOTIFICACIONES.md)

### Herramientas de Desarrollo
- [React DevTools](https://chrome.google.com/webstore/detail/react-developer-tools/)
- [Zustand DevTools](https://github.com/pmndrs/zustand#devtools)
- [Tailwind CSS IntelliSense](https://marketplace.visualstudio.com/items?itemName=bradlc.vscode-tailwindcss)

### Mejores Prácticas
1. **Manejo de Estado**: Usar Zustand para estado global, useState para estado local
2. **Performance**: Implementar React.memo para componentes pesados
3. **Accesibilidad**: Seguir pautas WCAG 2.1
4. **SEO**: Implementar meta tags apropiados
5. **Seguridad**: Validar datos del cliente, sanitizar inputs

---

Este documento proporciona una guía completa para implementar el frontend del sistema de notificaciones en tiempo real. La arquitectura propuesta es escalable, mantenible y sigue las mejores prácticas de React y TypeScript.