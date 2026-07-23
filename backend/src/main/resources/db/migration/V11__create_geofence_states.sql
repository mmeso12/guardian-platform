CREATE TABLE geofence_states (
    id BIGSERIAL PRIMARY KEY,

    geofence_id BIGINT NOT NULL,

    inside BOOLEAN NOT NULL,

    last_distance_meters DOUBLE PRECISION NOT NULL,

    last_evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    entered_at TIMESTAMP WITH TIME ZONE,

    exited_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_geofence_states_geofence
        FOREIGN KEY (geofence_id)
        REFERENCES geofences(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_geofence_states_geofence
        UNIQUE (geofence_id),

    CONSTRAINT ck_geofence_states_distance
        CHECK (last_distance_meters >= 0)
);

CREATE INDEX idx_geofence_states_last_evaluated_at
    ON geofence_states (last_evaluated_at);