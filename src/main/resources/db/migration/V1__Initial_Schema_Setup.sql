-- V1__Initial_Schema_Setup.sql

-- Crea una función reutilizable para actualizar automáticamente el campo 'updated_at'.
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Habilita extensiones si no existen.
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT "pgcrypto";