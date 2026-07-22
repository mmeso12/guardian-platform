CREATE TABLE guardian_alerts (
    id BIGSERIAL PRIMARY KEY,

    device_event_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,

    event_type VARCHAR(50) NOT NULL,
    severity VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,

    title VARCHAR(150) NOT NULL,
    message VARCHAR(1000) NOT NULL,

    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,

    opened_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_guardian_alert_device_event
        UNIQUE (device_event_id),

    CONSTRAINT fk_guardian_alert_device_event
        FOREIGN KEY (device_event_id)
        REFERENCES device_events(id),

    CONSTRAINT fk_guardian_alert_device
        FOREIGN KEY (device_id)
        REFERENCES devices(id)
);

CREATE INDEX idx_guardian_alert_device
    ON guardian_alerts (device_id);

CREATE INDEX idx_guardian_alert_status
    ON guardian_alerts (status);

CREATE INDEX idx_guardian_alert_created_at
    ON guardian_alerts (created_at);