CREATE TABLE guardian_mobile_devices (
    id BIGSERIAL PRIMARY KEY,

    guardian_user_id BIGINT NOT NULL,

    push_token VARCHAR(512) NOT NULL,
    platform VARCHAR(20) NOT NULL,

    device_name VARCHAR(150),
    app_version VARCHAR(50),

    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_guardian_mobile_device_user
        FOREIGN KEY (guardian_user_id)
        REFERENCES guardian_users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_guardian_mobile_device_push_token
        UNIQUE (push_token)
);

CREATE INDEX idx_guardian_mobile_device_user
    ON guardian_mobile_devices (guardian_user_id);

CREATE INDEX idx_guardian_mobile_device_user_enabled
    ON guardian_mobile_devices (
        guardian_user_id,
        enabled
    );


CREATE TABLE push_notification_deliveries (
    id BIGSERIAL PRIMARY KEY,

    guardian_notification_id BIGINT NOT NULL,
    guardian_mobile_device_id BIGINT NOT NULL,

    status VARCHAR(30) NOT NULL,
    provider VARCHAR(50) NOT NULL,

    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP WITH TIME ZONE,

    provider_message_id VARCHAR(255),
    failure_reason VARCHAR(1000),

    sent_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_push_delivery_notification
        FOREIGN KEY (guardian_notification_id)
        REFERENCES guardian_notifications(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_push_delivery_mobile_device
        FOREIGN KEY (guardian_mobile_device_id)
        REFERENCES guardian_mobile_devices(id),

    CONSTRAINT uq_push_delivery_notification_device
        UNIQUE (
            guardian_notification_id,
            guardian_mobile_device_id
        ),

    CONSTRAINT ck_push_delivery_attempt_count
        CHECK (attempt_count >= 0)
);

CREATE INDEX idx_push_delivery_notification
    ON push_notification_deliveries (
        guardian_notification_id
    );

CREATE INDEX idx_push_delivery_mobile_device
    ON push_notification_deliveries (
        guardian_mobile_device_id
    );

CREATE INDEX idx_push_delivery_retry
    ON push_notification_deliveries (
        status,
        next_retry_at
    );