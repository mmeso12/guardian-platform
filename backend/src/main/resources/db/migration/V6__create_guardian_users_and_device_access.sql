CREATE TABLE guardian_users (
    id BIGSERIAL PRIMARY KEY,

    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(30),

    role VARCHAR(30) NOT NULL DEFAULT 'PARENT',

    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_guardian_users_email
        UNIQUE (email),

    CONSTRAINT chk_guardian_users_role
        CHECK (
            role IN (
                'PARENT',
                'SCHOOL_ADMIN',
                'PLATFORM_ADMIN'
            )
        )
);

CREATE INDEX idx_guardian_users_email
    ON guardian_users (email);


CREATE TABLE guardian_device_access (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,

    access_role VARCHAR(30) NOT NULL,

    can_view_location BOOLEAN NOT NULL DEFAULT TRUE,
    can_manage_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    can_manage_device BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_guardian_device_access_user
        FOREIGN KEY (user_id)
        REFERENCES guardian_users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_guardian_device_access_device
        FOREIGN KEY (device_id)
        REFERENCES devices(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_guardian_device_access_user_device
        UNIQUE (user_id, device_id),

    CONSTRAINT chk_guardian_device_access_role
        CHECK (
            access_role IN (
                'OWNER',
                'GUARDIAN',
                'SCHOOL'
            )
        )
);

CREATE INDEX idx_guardian_device_access_user
    ON guardian_device_access (user_id);

CREATE INDEX idx_guardian_device_access_device
    ON guardian_device_access (device_id);