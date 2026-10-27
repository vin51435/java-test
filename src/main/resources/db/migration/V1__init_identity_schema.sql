-- =============================================================================
-- Migration: V1__init_identity_schema.sql
-- Description: Create identity bounded context tables for users and refresh tokens
-- Bounded Context: com.manasvia.backend.modules.identity
-- =============================================================================

-- Ensure UUID generation is available (gen_random_uuid is standard in Postgres 13+)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Identity Users Table
CREATE TABLE IF NOT EXISTS identity_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_fingerprint VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    google_id VARCHAR(255),
    role VARCHAR(32) NOT NULL DEFAULT 'GUEST',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_identity_users_device_fingerprint UNIQUE (device_fingerprint),
    CONSTRAINT uq_identity_users_email UNIQUE (email),
    CONSTRAINT uq_identity_users_google_id UNIQUE (google_id)
);

CREATE INDEX IF NOT EXISTS idx_identity_users_role ON identity_users (role);

-- 2. Identity Refresh Tokens Table
CREATE TABLE IF NOT EXISTS identity_refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(512) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_identity_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES identity_users (id) ON DELETE CASCADE,
    CONSTRAINT uq_identity_refresh_tokens_token UNIQUE (token)
);

CREATE INDEX IF NOT EXISTS idx_identity_refresh_tokens_user_id ON identity_refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_identity_refresh_tokens_token ON identity_refresh_tokens (token);
