CREATE TABLE device_child_assignments (
    id BIGSERIAL PRIMARY KEY,

    assignment_id UUID NOT NULL UNIQUE,

    device_id BIGINT NOT NULL,
    child_profile_id BIGINT NOT NULL,

    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL,
    unassigned_at TIMESTAMP WITH TIME ZONE,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_device_child_assignment_device
        FOREIGN KEY (device_id)
        REFERENCES devices(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_device_child_assignment_child
        FOREIGN KEY (child_profile_id)
        REFERENCES child_profiles(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_device_child_assignment_dates
        CHECK (
            (active = TRUE AND unassigned_at IS NULL)
            OR
            (active = FALSE AND unassigned_at IS NOT NULL)
        )
);

CREATE INDEX idx_device_child_assignments_device
    ON device_child_assignments(device_id);

CREATE INDEX idx_device_child_assignments_child
    ON device_child_assignments(child_profile_id);

CREATE INDEX idx_device_child_assignments_active
    ON device_child_assignments(active);

CREATE UNIQUE INDEX uq_device_active_child_assignment
    ON device_child_assignments(device_id)
    WHERE active = TRUE;