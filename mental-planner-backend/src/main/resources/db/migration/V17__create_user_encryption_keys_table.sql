CREATE TABLE IF NOT EXISTS user_encryption_keys (
    user_id VARCHAR(255) PRIMARY KEY,
    wrapped_dek TEXT NOT NULL,
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT now()
);
