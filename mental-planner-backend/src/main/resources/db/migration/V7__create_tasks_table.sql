CREATE TABLE IF NOT EXISTS task (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    scheduled_date DATE NOT NULL,
    start_time TIMESTAMPTZ(6),
    end_time TIMESTAMPTZ(6),
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    user_id VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_task_user_id_scheduled_date ON task(user_id, scheduled_date);

-- Optional link from a pomodoro session to the task it was focused on.
ALTER TABLE pomodoro_session ADD COLUMN task_id BIGINT REFERENCES task(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_pomodoro_session_task_id ON pomodoro_session(task_id);
