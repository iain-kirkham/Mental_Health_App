-- Add userId column to mood_entry table
ALTER TABLE mood_entry ADD COLUMN user_id VARCHAR(255);

-- Add userId column to pomodoro_session table
ALTER TABLE pomodoro_session ADD COLUMN user_id VARCHAR(255);

-- Create indexes for better query performance
CREATE INDEX idx_mood_entry_user_id ON mood_entry(user_id);
CREATE INDEX idx_pomodoro_session_user_id ON pomodoro_session(user_id);

