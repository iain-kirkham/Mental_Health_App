
-- Create table for mood entries
CREATE TABLE mood_entry (
    id BIGSERIAL PRIMARY KEY,
    mood_score INTEGER NOT NULL,
    date_time TIMESTAMPTZ(6) NOT NULL,
    factors JSONB,
    notes TEXT
);

-- Create table for pomodoro sessions
CREATE TABLE pomodoro_session (
    id BIGSERIAL PRIMARY KEY,
    start_time TIMESTAMPTZ(6),
    end_time TIMESTAMPTZ(6),
    duration INTEGER,
    score SMALLINT,
    notes VARCHAR(1000)
);
