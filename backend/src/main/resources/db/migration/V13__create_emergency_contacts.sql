CREATE TABLE emergency_contacts (
    id BIGSERIAL PRIMARY KEY,

    guardian_user_id BIGINT NOT NULL,

    full_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(30),
    email VARCHAR(255),

    relationship VARCHAR(40) NOT NULL,
    priority INTEGER NOT NULL,
    preferred_contact_method VARCHAR(30) NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_emergency_contacts_guardian
        FOREIGN KEY (guardian_user_id)
        REFERENCES guardian_users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_emergency_contacts_guardian_priority
        UNIQUE (guardian_user_id, priority),

    CONSTRAINT ck_emergency_contacts_priority
        CHECK (priority >= 1),

    CONSTRAINT ck_emergency_contacts_contact_details
        CHECK (
            phone_number IS NOT NULL
            OR email IS NOT NULL
        )
);

CREATE INDEX idx_emergency_contacts_guardian
    ON emergency_contacts (guardian_user_id);

CREATE INDEX idx_emergency_contacts_guardian_enabled_priority
    ON emergency_contacts (
        guardian_user_id,
        enabled,
        priority
    );