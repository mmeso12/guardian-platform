CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,

    device_uid VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(150),

    status VARCHAR(30) NOT NULL DEFAULT 'UNPAIRED',
    battery_level INTEGER,
    motion_state VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN',

    last_sequence_number BIGINT,
    firmware_version VARCHAR(50),
    last_seen_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_devices_battery_level
        CHECK (
            battery_level IS NULL
            OR battery_level BETWEEN 0 AND 100
        ),

    CONSTRAINT chk_devices_status
        CHECK (
            status IN (
                'UNPAIRED',
                'ONLINE',
                'OFFLINE',
                'EMERGENCY',
                'TAMPERED',
                'DEACTIVATED'
            )
        ),

    CONSTRAINT chk_devices_motion_state
        CHECK (
            motion_state IN (
                'UNKNOWN',
                'STATIONARY',
                'WALKING',
                'RUNNING',
                'VEHICLE'
            )
        )
);

CREATE INDEX idx_devices_device_uid
    ON devices (device_uid);

CREATE INDEX idx_devices_status
    ON devices (status);

CREATE INDEX idx_devices_last_seen_at
    ON devices (last_seen_at);