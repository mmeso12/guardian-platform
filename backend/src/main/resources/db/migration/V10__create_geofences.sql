CREATE TABLE geofences (
    id BIGSERIAL PRIMARY KEY,

    guardian_user_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,

    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),

    center_latitude DOUBLE PRECISION NOT NULL,
    center_longitude DOUBLE PRECISION NOT NULL,
    radius_meters DOUBLE PRECISION NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_geofences_guardian_user
        FOREIGN KEY (guardian_user_id)
        REFERENCES guardian_users(id),

    CONSTRAINT fk_geofences_device
        FOREIGN KEY (device_id)
        REFERENCES devices(id),

    CONSTRAINT ck_geofences_latitude
        CHECK (
            center_latitude >= -90
            AND center_latitude <= 90
        ),

    CONSTRAINT ck_geofences_longitude
        CHECK (
            center_longitude >= -180
            AND center_longitude <= 180
        ),

    CONSTRAINT ck_geofences_radius
        CHECK (
            radius_meters >= 10
            AND radius_meters <= 100000
        )
);

CREATE INDEX idx_geofences_guardian_user
    ON geofences (guardian_user_id);

CREATE INDEX idx_geofences_device
    ON geofences (device_id);

CREATE INDEX idx_geofences_device_enabled
    ON geofences (device_id, enabled);

CREATE INDEX idx_geofences_created_at
    ON geofences (created_at);