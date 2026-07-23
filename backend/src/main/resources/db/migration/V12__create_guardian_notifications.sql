CREATE TABLE guardian_notifications (
    id BIGSERIAL PRIMARY KEY,

    guardian_user_id BIGINT NOT NULL,
    guardian_alert_id BIGINT NOT NULL,

    read_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uq_guardian_notification_user_alert
        UNIQUE (guardian_user_id, guardian_alert_id),

    CONSTRAINT fk_guardian_notification_user
        FOREIGN KEY (guardian_user_id)
        REFERENCES guardian_users(id),

    CONSTRAINT fk_guardian_notification_alert
        FOREIGN KEY (guardian_alert_id)
        REFERENCES guardian_alerts(id)
);

CREATE INDEX idx_guardian_notification_user
    ON guardian_notifications (guardian_user_id);

CREATE INDEX idx_guardian_notification_alert
    ON guardian_notifications (guardian_alert_id);

CREATE INDEX idx_guardian_notification_user_read
    ON guardian_notifications (guardian_user_id, read_at);

CREATE INDEX idx_guardian_notification_created_at
    ON guardian_notifications (created_at);