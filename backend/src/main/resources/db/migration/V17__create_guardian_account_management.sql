ALTER TABLE guardian_users
    ADD COLUMN last_login_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN password_changed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN account_version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE guardian_sessions (
    id BIGSERIAL PRIMARY KEY,

    session_id UUID NOT NULL UNIQUE,
    guardian_user_id BIGINT NOT NULL,

    refresh_token_hash VARCHAR(64) NOT NULL UNIQUE,

    device_name VARCHAR(150),
    platform VARCHAR(50),
    ip_address VARCHAR(64),
    user_agent VARCHAR(500),

    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_used_at TIMESTAMP WITH TIME ZONE,
    revoked_at TIMESTAMP WITH TIME ZONE,
    revocation_reason VARCHAR(255),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_guardian_session_user
        FOREIGN KEY (guardian_user_id)
        REFERENCES guardian_users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_guardian_sessions_user
    ON guardian_sessions (
        guardian_user_id,
        created_at DESC
    );

CREATE INDEX idx_guardian_sessions_active
    ON guardian_sessions (
        guardian_user_id,
        revoked_at,
        expires_at
    );

CREATE TABLE guardian_email_verification_tokens (
    id BIGSERIAL PRIMARY KEY,

    guardian_user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,

    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_email_verification_user
        FOREIGN KEY (guardian_user_id)
        REFERENCES guardian_users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_email_verification_user
    ON guardian_email_verification_tokens (
        guardian_user_id,
        created_at DESC
    );

CREATE INDEX idx_email_verification_expiration
    ON guardian_email_verification_tokens (
        expires_at,
        used_at
    );

CREATE TABLE guardian_password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,

    guardian_user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,

    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (guardian_user_id)
        REFERENCES guardian_users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_user
    ON guardian_password_reset_tokens (
        guardian_user_id,
        created_at DESC
    );

CREATE INDEX idx_password_reset_expiration
    ON guardian_password_reset_tokens (
        expires_at,
        used_at
    );