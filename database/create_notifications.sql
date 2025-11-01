-- Script de creación para la tabla notifications (Postgres / Neon)
-- Ejecutar en la base de datos del proyecto (p. ej. Neon DB)

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    entity TEXT NOT NULL,
    entity_id BIGINT,
    action TEXT NOT NULL,
    description TEXT,
    actor_id BIGINT,
    actor_email TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Índices para consultas comunes
CREATE INDEX IF NOT EXISTS idx_notifications_entity_created_at ON notifications(entity, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_actor_id ON notifications(actor_id);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);

-- Opcional: política de retención (mantén última N días)
-- Por ejemplo borrar notificaciones más antiguas de 365 días
-- CREATE FUNCTION purge_old_notifications() RETURNS void AS $$
-- BEGIN
--   DELETE FROM notifications WHERE created_at < now() - interval '365 days';
-- END;
-- $$ LANGUAGE plpgsql;

-- Agregar un rol/permiso ejemplo si necesitas dar acceso a un usuario de aplicación
-- GRANT SELECT, INSERT, UPDATE, DELETE ON notifications TO your_app_role;

-- Fin del script
