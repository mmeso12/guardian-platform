CREATE TABLE child_profiles (
    id BIGSERIAL PRIMARY KEY,

    child_id UUID NOT NULL UNIQUE,

    guardian_user_id BIGINT NOT NULL,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),

    date_of_birth DATE,

    gender VARCHAR(30),

    profile_image_url VARCHAR(500),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_child_profiles_guardian
        FOREIGN KEY (guardian_user_id)
        REFERENCES guardian_users(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_child_profiles_guardian_user_id
    ON child_profiles(guardian_user_id);

CREATE INDEX idx_child_profiles_guardian_active
    ON child_profiles(guardian_user_id, active);

CREATE INDEX idx_child_profiles_created_at
    ON child_profiles(created_at);