ALTER TABLE guardian_alerts
    ADD COLUMN acknowledged_by_user_id BIGINT,
    ADD COLUMN acknowledged_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN acknowledgement_note VARCHAR(1000),
    ADD COLUMN resolved_by_user_id BIGINT,
    ADD COLUMN resolved_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN resolution_note VARCHAR(1000);

ALTER TABLE guardian_alerts
    ADD CONSTRAINT fk_guardian_alert_acknowledged_user
        FOREIGN KEY (acknowledged_by_user_id)
        REFERENCES guardian_users(id);

ALTER TABLE guardian_alerts
    ADD CONSTRAINT fk_guardian_alert_resolved_user
        FOREIGN KEY (resolved_by_user_id)
        REFERENCES guardian_users(id);