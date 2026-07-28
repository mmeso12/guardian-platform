CREATE TABLE device_commands (
    id BIGSERIAL PRIMARY KEY,

    device_id BIGINT NOT NULL,
    created_by_guardian_id BIGINT NOT NULL,

    command_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,

    payload_json TEXT,
    result_json TEXT,
    failure_reason VARCHAR(1000),

    delivered_at TIMESTAMP WITH TIME ZONE,
    received_at TIMESTAMP WITH TIME ZONE,
    execution_started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_device_command_device
        FOREIGN KEY (device_id)
        REFERENCES devices(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_device_command_guardian
        FOREIGN KEY (created_by_guardian_id)
        REFERENCES guardian_users(id),

    CONSTRAINT ck_device_command_terminal_time
        CHECK (
            completed_at IS NULL
            OR failed_at IS NULL
        )
);

CREATE INDEX idx_device_commands_device_created
    ON device_commands (
        device_id,
        created_at DESC
    );

CREATE INDEX idx_device_commands_pending
    ON device_commands (
        device_id,
        status,
        expires_at
    );

CREATE INDEX idx_device_commands_expiration
    ON device_commands (
        status,
        expires_at
    );

CREATE INDEX idx_device_commands_guardian
    ON device_commands (
        created_by_guardian_id,
        created_at DESC
    );