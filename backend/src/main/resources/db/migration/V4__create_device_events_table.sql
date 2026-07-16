CREATE TABLE device_events (
    id BIGSERIAL PRIMARY KEY,

    device_id BIGINT NOT NULL,
    sequence_number BIGINT NOT NULL,

    event_type VARCHAR(40) NOT NULL,
    severity VARCHAR(30) NOT NULL,

    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    battery_level INTEGER,

    recorded_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ,

    metadata TEXT,

    CONSTRAINT fk_device_events_device
        FOREIGN KEY (device_id)
        REFERENCES devices(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_device_events_device_sequence
        UNIQUE (device_id, sequence_number),

    CONSTRAINT chk_device_events_type
        CHECK (
            event_type IN (
                'SOS',
                'TAMPER',
                'LOW_BATTERY',
                'DEVICE_ONLINE',
                'DEVICE_OFFLINE',
                'GEOFENCE_ENTRY',
                'GEOFENCE_EXIT'
            )
        ),

    CONSTRAINT chk_device_events_severity
        CHECK (
            severity IN (
                'INFORMATIONAL',
                'WARNING',
                'EMERGENCY'
            )
        ),

    CONSTRAINT chk_device_events_latitude
        CHECK (
            latitude IS NULL
            OR latitude BETWEEN -90 AND 90
        ),

    CONSTRAINT chk_device_events_longitude
        CHECK (
            longitude IS NULL
            OR longitude BETWEEN -180 AND 180
        ),

    CONSTRAINT chk_device_events_battery
        CHECK (
            battery_level IS NULL
            OR battery_level BETWEEN 0 AND 100
        )
);

CREATE INDEX idx_device_events_device_recorded_at
    ON device_events (device_id, recorded_at DESC);

CREATE INDEX idx_device_events_event_type
    ON device_events (event_type);

CREATE INDEX idx_device_events_unresolved_emergencies
    ON device_events (severity, recorded_at DESC)
    WHERE resolved_at IS NULL;