CREATE TABLE emergency_escalations (
    id BIGSERIAL PRIMARY KEY,

    guardian_alert_id BIGINT NOT NULL,
    guardian_user_id BIGINT NOT NULL,

    status VARCHAR(30) NOT NULL,

    current_priority INTEGER,
    current_attempt_number INTEGER NOT NULL DEFAULT 0,

    next_action_at TIMESTAMP WITH TIME ZONE,

    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    resolved_at TIMESTAMP WITH TIME ZONE,

    acknowledgement_note VARCHAR(1000),
    resolution_note VARCHAR(1000),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_emergency_escalation_alert
        FOREIGN KEY (guardian_alert_id)
        REFERENCES guardian_alerts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_emergency_escalation_guardian
        FOREIGN KEY (guardian_user_id)
        REFERENCES guardian_users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_emergency_escalation_alert_guardian
        UNIQUE (guardian_alert_id, guardian_user_id),

    CONSTRAINT ck_emergency_escalation_attempt_number
        CHECK (current_attempt_number >= 0),

    CONSTRAINT ck_emergency_escalation_priority
        CHECK (
            current_priority IS NULL
            OR current_priority >= 1
        )
);

CREATE INDEX idx_emergency_escalation_guardian
    ON emergency_escalations (guardian_user_id);

CREATE INDEX idx_emergency_escalation_alert
    ON emergency_escalations (guardian_alert_id);

CREATE INDEX idx_emergency_escalation_status_next_action
    ON emergency_escalations (status, next_action_at);


CREATE TABLE emergency_contact_attempts (
    id BIGSERIAL PRIMARY KEY,

    emergency_escalation_id BIGINT NOT NULL,
    emergency_contact_id BIGINT NOT NULL,

    attempt_number INTEGER NOT NULL,

    contact_method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,

    contact_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(30),
    email VARCHAR(255),

    attempted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,

    failure_reason VARCHAR(1000),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_contact_attempt_escalation
        FOREIGN KEY (emergency_escalation_id)
        REFERENCES emergency_escalations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_contact_attempt_contact
        FOREIGN KEY (emergency_contact_id)
        REFERENCES emergency_contacts(id),

    CONSTRAINT uq_contact_attempt_number
        UNIQUE (
            emergency_escalation_id,
            attempt_number
        ),

    CONSTRAINT ck_contact_attempt_number
        CHECK (attempt_number >= 1)
);

CREATE INDEX idx_contact_attempt_escalation
    ON emergency_contact_attempts (
        emergency_escalation_id,
        attempt_number
    );

CREATE INDEX idx_contact_attempt_status
    ON emergency_contact_attempts (status);