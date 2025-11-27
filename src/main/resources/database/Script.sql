-- Migration: create noticias and noticia_imagenes
CREATE TYPE IF NOT EXISTS noticia_tipo AS ENUM ('promocion', 'descuentos', 'lanzamientos', 'testimonios');
CREATE TYPE IF NOT EXISTS noticia_estado AS ENUM ('ACTIVE', 'INACTIVE');

CREATE TABLE IF NOT EXISTS noticias (
  id BIGSERIAL PRIMARY KEY,
  titulo VARCHAR(255) NOT NULL,
  slug VARCHAR(255) UNIQUE,
  resumen VARCHAR(512),
  contenido TEXT,
  tipo noticia_tipo NOT NULL,
  estado noticia_estado NOT NULL DEFAULT 'ACTIVE',
  publish_at TIMESTAMPTZ NULL,
  orden_prioridad INTEGER DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_noticias_tipo ON noticias(tipo);
CREATE INDEX IF NOT EXISTS idx_noticias_estado ON noticias(estado);
CREATE INDEX IF NOT EXISTS idx_noticias_publish_at ON noticias(publish_at);

CREATE TABLE IF NOT EXISTS noticia_imagen (
  id BIGSERIAL PRIMARY KEY,
  noticia_id BIGINT NOT NULL REFERENCES noticias(id) ON DELETE CASCADE,
  file_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(100),
  data BYTEA NOT NULL,
  file_size BIGINT,
  orden INTEGER NOT NULL DEFAULT 0,
  is_cover BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_noticia_imagen_noticia ON noticia_imagen(noticia_id, orden);
-- Hino Connect Database Schema
-- This schema defines all tables needed for the vehicle fleet management system

-- Create enum types
-- CREATE TYPE vehicle_type AS ENUM ('camion', 'bus');
-- CREATE TYPE vehicle_status AS ENUM ('disponible', 'reservado', 'vendido');
-- CREATE TYPE quote_status AS ENUM ('pendiente', 'en-proceso', 'enviada', 'cerrada');
-- CREATE TYPE quote_priority AS ENUM ('alta', 'media', 'baja');
-- CREATE TYPE user_role AS ENUM ('asesor', 'admin', 'mecanico', 'supervisor');
-- CREATE TYPE user_status AS ENUM ('activo', 'inactivo');
-- CREATE TYPE notification_type AS ENUM ('alert', 'maintenance', 'fuel', 'system', 'quote', 'user', 'vehicle', 'sale');
-- CREATE TYPE notification_priority AS ENUM ('alta', 'media', 'baja');

-- Table: vehicles (Flota de vehículos)
CREATE TABLE vehicles (
    id SERIAL PRIMARY KEY,
    modelo VARCHAR(255) NOT NULL,
    tipo vehicle_type NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    precio DECIMAL(12, 2) NOT NULL,
    capacidad VARCHAR(100) NOT NULL,
    motor VARCHAR(100) NOT NULL,
    anio INTEGER NOT NULL,
    estado vehicle_status NOT NULL DEFAULT 'disponible',
    stock INTEGER NOT NULL DEFAULT 1,
    imagen_url VARCHAR(500),
    descripcion TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table: users (Usuarios)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    rol user_role NOT NULL,
    especialidad VARCHAR(100),
    estado user_status NOT NULL DEFAULT 'activo',
    ventas INTEGER DEFAULT 0,
    fecha_ingreso DATE DEFAULT CURRENT_DATE,
    avatar_url VARCHAR(500),
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table: quotes (Cotizaciones)
CREATE TABLE quotes (
    id SERIAL PRIMARY KEY,
    cliente_nombre VARCHAR(255) NOT NULL,
    cliente_email VARCHAR(255) NOT NULL,
    cliente_telefono VARCHAR(20),
    empresa VARCHAR(255),
    tipo_vehiculo VARCHAR(100) NOT NULL,
    mensaje TEXT,
    estado quote_status NOT NULL DEFAULT 'pendiente',
    prioridad quote_priority NOT NULL DEFAULT 'media',
    asesor_asignado_id INTEGER REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table: notifications (Notificaciones)
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    tipo notification_type NOT NULL,
    prioridad notification_priority NOT NULL DEFAULT 'media',
    titulo VARCHAR(255) NOT NULL,
    mensaje TEXT NOT NULL,
    vehiculo_id INTEGER REFERENCES vehicles(id),
    quote_id INTEGER REFERENCES quotes(id),
    user_id INTEGER REFERENCES users(id),
    leido BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table: vehicle_images (Imágenes adicionales de vehículos)
CREATE TABLE vehicle_images (
    id SERIAL PRIMARY KEY,
    vehicle_id INTEGER NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table: quote_attachments (Archivos adjuntos a cotizaciones)
CREATE TABLE quote_attachments (
    id SERIAL PRIMARY KEY,
    quote_id INTEGER NOT NULL REFERENCES quotes(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table: contact_messages (Mensajes de contacto)
CREATE TABLE contact_messages (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better performance
CREATE INDEX idx_vehicles_tipo ON vehicles(tipo);
CREATE INDEX idx_vehicles_estado ON vehicles(estado);
CREATE INDEX idx_vehicles_categoria ON vehicles(categoria);
CREATE INDEX idx_users_rol ON users(rol);
CREATE INDEX idx_users_estado ON users(estado);
CREATE INDEX idx_quotes_estado ON quotes(estado);
CREATE INDEX idx_quotes_prioridad ON quotes(prioridad);
CREATE INDEX idx_quotes_asesor ON quotes(asesor_asignado_id);
CREATE INDEX idx_notifications_leido ON notifications(leido);
CREATE INDEX idx_notifications_tipo ON notifications(tipo);
CREATE INDEX idx_notifications_prioridad ON notifications(prioridad);
CREATE INDEX idx_contact_messages_status ON contact_messages(status);

-- Sample data for vehicles
INSERT INTO vehicles (modelo, tipo, categoria, precio, capacidad, motor, año, estado, stock, imagen_url) VALUES
('HINO Serie 300', 'camion', 'Ligero', 45000.00, '3.5 - 5 toneladas', '4.0L Diesel', 2025, 'disponible', 8, '/hino-300-series-white-truck.jpg'),
('HINO Serie 500', 'camion', 'Mediano', 75000.00, '8 - 12 toneladas', '7.7L Diesel', 2025, 'disponible', 12, '/hino-500-series-red-truck.jpg'),
('HINO Serie 700', 'camion', 'Pesado', 120000.00, '15 - 25 toneladas', '13.0L Diesel', 2025, 'disponible', 5, '/hino-700-series-heavy-duty-truck.jpg'),
('HINO AK Bus Urbano', 'bus', 'Urbano', 95000.00, '40 - 50 pasajeros', '7.7L Diesel', 2025, 'disponible', 6, '/hino-urban-bus-white.jpg'),
('HINO FC Bus Interurbano', 'bus', 'Interurbano', 135000.00, '45 - 55 pasajeros', '8.9L Diesel', 2025, 'reservado', 3, '/hino-intercity-bus-red.jpg');

-- Habilitar la extensión (una sola vez por base de datos)
--  CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Sample data for users
INSERT INTO users (nombre, email, telefono, rol, especialidad, estado, ventas, fecha_ingreso, password_hash) VALUES
('Carlos Mendoza', 'carlos.mendoza@hino.com.pe', '+51 999 888 777', 'asesor', 'Camiones Pesados', 'activo', 150, '2020-03-15', crypt('Carlos123', gen_salt('bf'))),
('María González', 'maria.gonzalez@hino.com.pe', '+51 999 777 666', 'asesor', 'Buses Urbanos', 'activo', 120, '2021-06-20', crypt('Maria456', gen_salt('bf'))),
('Roberto Silva', 'roberto.silva@hino.com.pe', '+51 999 666 555', 'asesor', 'Camiones Ligeros', 'activo', 95, '2022-01-10', crypt('Roberto789', gen_salt('bf'))),
('Ana Torres', 'ana.torres@hino.com.pe', '+51 999 555 444', 'admin', 'Administración', 'activo', 0, '2019-08-05', crypt('AnaAdmin', gen_salt('bf'))),
('Luis Valle', 'luis.valle@hino.com.pe', '+51 999 444 333', 'admin', 'Gerencia de Ventas', 'activo', 0, '2018-05-12', crypt('LuisAdmin', gen_salt('bf')));

-- Sample data for quotes
INSERT INTO quotes (cliente_nombre, cliente_email, cliente_telefono, empresa, tipo_vehiculo, mensaje, estado, prioridad) VALUES
('Juan Pérez', 'juan.perez@transportes.com', '+51 999 111 222', 'Transportes Lima SAC', 'Camión Pesado', 'Necesito cotización para 3 camiones pesados para transporte de carga', 'pendiente', 'alta'),
('María González', 'maria@logistica.com', '+51 999 222 333', 'Logística del Sur', 'Bus Urbano', 'Interesada en buses urbanos para transporte público', 'en-proceso', 'media'),
('Roberto Silva', 'roberto@cargo.com', '+51 999 333 444', 'Cargo Express', 'Camión Mediano', 'Requiero información sobre camiones medianos', 'enviada', 'baja'),
('Ana Martínez', 'ana@transporte.com', '+51 999 444 555', 'Transporte Nacional', 'Bus Interurbano', 'Necesito cotización urgente para buses interurbanos', 'pendiente', 'alta');

-- Sample data for notifications
INSERT INTO notifications (tipo, prioridad, titulo, mensaje, vehiculo_id, leido) VALUES
('alert', 'alta', 'Temperatura del motor elevada', 'El vehículo HINO-089 presenta temperatura del motor por encima del rango normal', 1, false),
('maintenance', 'media', 'Mantenimiento programado próximo', 'El vehículo HINO-001 tiene mantenimiento programado para mañana', 2, false),
('fuel', 'media', 'Nivel de combustible bajo', 'El vehículo HINO-045 tiene menos del 25% de combustible', 3, true),
('user', 'media', 'Nuevo usuario registrado', 'Se ha registrado un nuevo usuario: Juan Pérez (asesor)', NULL, false),
('vehicle', 'media', 'Nuevo vehículo agregado', 'Se ha agregado un nuevo vehículo: HINO Serie 800', NULL, false),
('sale', 'alta', 'Venta completada', 'Se ha completado una venta de vehículo HINO Serie 500', 2, false);


-- Script to update the schema - remove CASCADE constraints from notifications

-- Drop existing foreign key constraints
--  ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_vehiculo_id_fkey;
--  ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_quote_id_fkey;
--  ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_user_id_fkey;

-- Recreate foreign key constraints without CASCADE
ALTER TABLE notifications 
ADD CONSTRAINT notifications_vehiculo_id_fkey 
FOREIGN KEY (vehiculo_id) REFERENCES vehicles(id);

ALTER TABLE notifications 
ADD CONSTRAINT notifications_quote_id_fkey 
FOREIGN KEY (quote_id) REFERENCES quotes(id);

ALTER TABLE notifications 
ADD CONSTRAINT notifications_user_id_fkey 
FOREIGN KEY (user_id) REFERENCES users(id);