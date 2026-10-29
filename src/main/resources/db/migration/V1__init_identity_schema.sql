-- =============================================================================
-- Migration: V1__init_identity_schema.sql
-- Description: Create identity bounded context tables for users and refresh tokens
-- Bounded Context: com.manasvia.backend.modules.identity
-- =============================================================================

-- Ensure UUID generation is available (gen_random_uuid is standard in Postgres 13+)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Identity Users Table: Accommodates Guest, Local Password, and OAuth credentials
CREATE TABLE IF NOT EXISTS identity_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_fingerprint VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    password_hash VARCHAR(255),
    google_id VARCHAR(255),
    display_name VARCHAR(128),
    email_verified_at TIMESTAMP WITH TIME ZONE,
    role VARCHAR(32) NOT NULL DEFAULT 'GUEST',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_identity_users_device_fingerprint UNIQUE (device_fingerprint),
    CONSTRAINT uq_identity_users_email UNIQUE (email),
    CONSTRAINT uq_identity_users_google_id UNIQUE (google_id)
);

CREATE INDEX IF NOT EXISTS idx_identity_users_role ON identity_users (role);
CREATE INDEX IF NOT EXISTS idx_identity_users_email ON identity_users (email);

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

-- 3. Ephemeral / Audit OTP Verification Table (Backed by Redis for active TTL in production)
CREATE TABLE IF NOT EXISTS identity_otp_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    purpose VARCHAR(32) NOT NULL, -- 'EMAIL_VERIFICATION', 'PASSWORD_RESET'
    code_hash VARCHAR(256) NOT NULL,
    attempts_count INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_identity_otp_requests_email ON identity_otp_requests (email);

-- 4. Spring Modulith Event Publication Tables (Transactional Outbox & Completed Archive)
CREATE TABLE IF NOT EXISTS event_publication (
    id UUID PRIMARY KEY,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    listener_id VARCHAR(512) NOT NULL,
    serialized_event TEXT NOT NULL,
    event_type VARCHAR(512) NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_event_publication_event_type ON event_publication (event_type);

CREATE TABLE IF NOT EXISTS event_publication_archive (
    id UUID PRIMARY KEY,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    listener_id VARCHAR(512) NOT NULL,
    serialized_event TEXT NOT NULL,
    event_type VARCHAR(512) NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_event_publication_archive_event_type ON event_publication_archive (event_type);
