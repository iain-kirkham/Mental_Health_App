CREATE TABLE IF NOT EXISTS task_time_entry (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL,
    started_at TIMESTAMPTZ(6),
    ended_at TIMESTAMPTZ(6),
    minutes INTEGER NOT NULL,
    entry_date DATE NOT NULL,
    source VARCHAR(10) NOT NULL,
    note VARCHAR(500),
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_task_time_entry_task_id ON task_time_entry(task_id);
CREATE INDEX IF NOT EXISTS idx_task_time_entry_user_id_entry_date ON task_time_entry(user_id, entry_date);
