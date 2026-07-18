ALTER TABLE devices
    ADD COLUMN device_key_hash VARCHAR(64);

CREATE INDEX idx_devices_active_authentication
    ON devices (device_uid, status);