ALTER TABLE devices
    ADD COLUMN paired BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN paired_at TIMESTAMPTZ,
    ADD COLUMN pairing_code_hash VARCHAR(64);

CREATE INDEX idx_devices_pairing_status
    ON devices (device_uid, paired);

ALTER TABLE devices
    ADD CONSTRAINT chk_devices_pairing_state
        CHECK (
            (paired = FALSE AND paired_at IS NULL)
            OR
            (paired = TRUE AND paired_at IS NOT NULL)
        );