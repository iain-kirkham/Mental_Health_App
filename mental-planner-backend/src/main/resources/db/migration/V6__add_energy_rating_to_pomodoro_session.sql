-- Add optional energy rating (ENERGIZING / DRAINING) to completed pomodoro sessions.
ALTER TABLE pomodoro_session ADD COLUMN energy_rating VARCHAR(20);
