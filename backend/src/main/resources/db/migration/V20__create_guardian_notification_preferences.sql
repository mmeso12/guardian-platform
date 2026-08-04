CREATE TABLE guardian_notification_preferences (
    id BIGSERIAL PRIMARY KEY,

    guardian_user_id BIGINT NOT NULL UNIQUE,

    in_app_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,

    informational_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    warning_enabled BOOLEAN NOT NULL DEFAULT TRUE,

    sos_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    tamper_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    low_battery_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    device_online_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    device_offline_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    geofence_entry_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    geofence_exit_enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_notification_preferences_guardian
        FOREIGN KEY (guardian_user_id)
        REFERENCES guardian_users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_notification_preferences_guardian
    ON guardian_notification_preferences(guardian_user_id);


ALTER TABLE guardian_notifications
    ADD COLUMN visible_in_app BOOLEAN NOT NULL DEFAULT TRUE;