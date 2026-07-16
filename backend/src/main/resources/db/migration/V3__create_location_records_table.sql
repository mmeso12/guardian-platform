CREATE TABLE location_records (
    id BIGSERIAL PRIMARY KEY,

    device_id BIGINT NOT NULL,
    sequence_number BIGINT NOT NULL,

    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    accuracy_meters DOUBLE PRECISION,
    speed_meters_per_second DOUBLE PRECISION,
    heading_degrees DOUBLE PRECISION,

    battery_level INTEGER,
    motion_state VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN',

    recorded_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_location_records_device
        FOREIGN KEY (device_id)
        REFERENCES devices(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_location_records_device_sequence
        UNIQUE (device_id, sequence_number),

    CONSTRAINT chk_location_records_latitude
        CHECK (latitude BETWEEN -90 AND 90),

    CONSTRAINT chk_location_records_longitude
        CHECK (longitude BETWEEN -180 AND 180),

    CONSTRAINT chk_location_records_accuracy
        CHECK (
            accuracy_meters IS NULL
            OR accuracy_meters >= 0
        ),

    CONSTRAINT chk_location_records_speed
        CHECK (
            speed_meters_per_second IS NULL
            OR speed_meters_per_second >= 0
        ),

    CONSTRAINT chk_location_records_heading
        CHECK (
            heading_degrees IS NULL
            OR heading_degrees BETWEEN 0 AND 360
        ),

    CONSTRAINT chk_location_records_battery
        CHECK (
            battery_level IS NULL
            OR battery_level BETWEEN 0 AND 100
        ),

    CONSTRAINT chk_location_records_motion_state
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

CREATE INDEX idx_location_records_device_recorded_at
    ON location_records (device_id, recorded_at DESC);

CREATE INDEX idx_location_records_received_at
    ON location_records (received_at);